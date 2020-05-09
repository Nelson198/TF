package Server;

import Messages.CartUpdate;
import Messages.Checkout;
import Messages.DBContent;
import Messages.DBUpdate;
import Messages.Message;
import Messages.ProductGet;
import Types.Product;

import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

import spread.AdvancedMessageListener;
import spread.MembershipInfo;
import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Supermarket Implementation
 */
public class Supermarket {
    // Server port
    String port;

    // Variable to store processes after which this one will be primary
    ArrayList<SpreadGroup> primaryAfter = new ArrayList<>();

    // Serializer for the messages sent between servers
    Serializer serializer = new SerializerBuilder().build();

    // Initialize the spread connection
    SpreadConnection connection = new SpreadConnection();

    // Database connection
    Connection dbConnection;

    // Atomix connection
    ManagedMessagingService ms;

    // Skeleton for the catalog
    CatalogSkeleton catalog;

    // Skeletons for the carts
    HashMap<String, CartSkeleton> carts = new HashMap<>();

    // Queries that arrived between this server's connection and the reception of the DB
    ArrayList<String> pendingQueries = new ArrayList<>();

    /**
     * Parameterized constructor
     * @param port Port
     */
    public Supermarket(String port) {
        this.port = port;
    }

    /**
     * Initialize Spread connection
     * @throws SpreadException SpreadException
     * @throws UnknownHostException UnknownHostException
     */
    public void initializeSpread() throws SpreadException, UnknownHostException {
        // Initialize the spread connection
        this.connection = new SpreadConnection();
        this.connection.connect(InetAddress.getByName("localhost"), 4803, this.port, false, true);

        Supermarket aux = this;

        this.connection.add(new AdvancedMessageListener() {
            public void regularMessageReceived(SpreadMessage spreadMessage) {
                Message ms = aux.serializer.decode(spreadMessage.getData());
                if (ms.getType().equals("db")) {
                    DBContent dbMsg = (DBContent) ms;
                    // ........ update the db file
                    try {
                        aux.dbConnection = DriverManager.getConnection("jdbc:hsqldb:file:supermarket" + port + "?allowMultiQueries=true", "SA", "");
                        aux.dbConnection.setAutoCommit(true); // default

                        for (String pq : aux.pendingQueries) {
                            Statement s = aux.dbConnection.createStatement();
                            s.executeUpdate(pq);
                        }
                        aux.pendingQueries = null;

                        aux.catalog = new CatalogSkeleton(aux.dbConnection);
                        initializeAtomix();
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                } else if (ms.getType().equals("dbUpdate")) {
                    DBUpdate dbUpdate = (DBUpdate) ms;
                    String query = dbUpdate.getQuery();

                    if (aux.dbConnection == null) {
                        aux.pendingQueries.add(query);
                    } else {
                        try {
                            Statement s = aux.dbConnection.createStatement();
                            int res = s.executeUpdate(query);

                            switch (dbUpdate.getSecondaryType()) {
                                case "newCart":
                                    aux.carts.put(Integer.toString(res), new CartSkeleton(Integer.toString(res), aux.dbConnection));
                                    if (dbUpdate.getServer().equals(aux.port)) {
                                        Thread timer = new TimerThread(res, aux);
                                        timer.start();
                                    }
                                    break;
                                case "deleteCart":
                                    aux.carts.remove(res);
                                    break;
                                case "checkout":
                                    aux.carts.remove(res);
                                    break;
                            }

                            if (dbUpdate.getServer().equals(aux.port)) {
                                aux.ms.sendAsync(Address.from(dbUpdate.getClient()), "res", serializer.encode(res)); // TODO - figure out what to send to the client
                            }
                        } catch (SQLException exception) {
                            exception.printStackTrace();
                        }
                    }
                }
            }

            public void membershipMessageReceived(SpreadMessage spreadMessage) {
                MembershipInfo info = spreadMessage.getMembershipInfo();
                if (info.isCausedByJoin()) {
                    if (info.getJoined().equals(aux.connection.getPrivateGroup())) {
                        if (info.getMembers().length == 1) {
                            try {
                                aux.dbConnection = DriverManager.getConnection("jdbc:hsqldb:file:supermarket" + port + "?allowMultiQueries=true", "SA", "");
                                aux.dbConnection.setAutoCommit(true);

                                aux.catalog = new CatalogSkeleton(aux.dbConnection);

                                initializeAtomix();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            for (SpreadGroup member : info.getMembers()) {
                                if (!member.equals(aux.connection.getPrivateGroup()))
                                    primaryAfter.add(member);
                            }
                        }
                    } else if (primaryAfter.size() == 0) {
                        // TODO - send database (or updates) to new member
                    }
                } else if (info.isCausedByDisconnect()) {
                    primaryAfter.remove(info.getDisconnected());
                } else if (info.isCausedByLeave()) {
                    primaryAfter.remove(info.getLeft());
                } else if (info.isCausedByNetwork()) {
                    // TODO - deal with network partitions
                    //        see info.getMyVirtualSynchronySet(), info.getStayed(), info.getVirtualSynchronySets()
                }
            }
        });

        SpreadGroup g = new SpreadGroup();
        g.join(this.connection, "supermarket");
    }

    /**
     * Initialize Atomix connection
     * @throws ExecutionException ExecutionException
     * @throws InterruptedException InterruptedException
     */
    public void initializeAtomix() throws ExecutionException, InterruptedException {
        // Initialize the messaging service and register messaging service handlers
        this.ms = new NettyMessagingService("supermarket", Address.from(Integer.parseInt(this.port)), new MessagingConfig());
        ExecutorService executor = Executors.newFixedThreadPool(1);

        Supermarket aux = this;

        ms.registerHandler("newCart", (address, bytes) -> {
            DBUpdate dbu = new DBUpdate("INSERT INTO cart VALUES ()", aux.connection.getPrivateGroup().toString(), address.toString(), "newCart"); // TODO - query
            aux.sendCluster(aux.serializer.encode(dbu));
        }, executor);

        ms.registerHandler("updateCart", (address, bytes) -> {
            CartUpdate cu = aux.serializer.decode(bytes);

            DBUpdate dbu = new DBUpdate("UPDATE cart SET ... WHERE id=...", aux.connection.getPrivateGroup().toString(), address.toString(), "addProduct"); // TODO - query
            aux.sendCluster(aux.serializer.encode(dbu));
        }, executor);

        ms.registerHandler("checkout", (address, bytes) -> {
            Checkout co = aux.serializer.decode(bytes);

            DBUpdate dbu = new DBUpdate("UPDATE cart SET ... WHERE id=...", aux.connection.getPrivateGroup().toString(), address.toString(), "checkout"); // TODO - query
            aux.sendCluster(aux.serializer.encode(dbu));
        }, executor);

        ms.registerHandler("getCatalog", (address, bytes) -> {
            ArrayList<Product> res = catalog.getCatalog();
            ms.sendAsync(address, "res", serializer.encode(res));
        }, executor);

        ms.registerHandler("getProduct", (address, bytes) -> {
            ProductGet pg = aux.serializer.decode(bytes);
            Product res = catalog.getProduct(pg.getId());
            ms.sendAsync(address, "res", serializer.encode(res));
        }, executor);

        ms.start().get();
    }

    /**
     * Multicast a message to the cluster
     * @param message Message
     *
     * TODO - move to ServerConnection
     */
    public void sendCluster(byte[] message) {
        SpreadMessage m = new SpreadMessage();
        m.addGroup("supermarket");
        m.setData(serializer.encode(message));
        m.setSafe();
        try {
            this.connection.multicast(m);
        } catch (SpreadException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException, SpreadException, ExecutionException, UnknownHostException {
        if (args.length != 1) {
            System.out.println("Indique a porta do servidor");
            System.exit(1);
        }

        Supermarket s = new Supermarket(args[0]);
        s.initializeSpread();

        System.out.println("Server is running on port " + args[0] + " ...");

        new CompletableFuture<>().get();
    }
}
