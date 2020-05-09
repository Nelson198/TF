import Messages.DBContent;
import Messages.DBUpdate;
import Messages.Message;
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
    HashMap<Integer, CartSkeleton> carts = new HashMap<>();

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
     * initializeSpread
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
                switch (ms.getType()) {
                    case "db":
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
                        break;
                    case "dbUpdate":
                        DBUpdate dbUpdate = (DBUpdate) ms;
                        String query = dbUpdate.getQuery();

                        if (aux.dbConnection == null) {
                            aux.pendingQueries.add(query);
                        } else {
                            try {
                                Statement s = aux.dbConnection.createStatement();
                                int res = s.executeUpdate(query);

                                if (dbUpdate.getServer().equals(aux.port)) {
                                    aux.ms.sendAsync(Address.from(dbUpdate.getClient()), "res", serializer.encode(res)); // TODO - figure out what to send to the client
                                }
                            } catch (SQLException exception) {
                                exception.printStackTrace();
                            }
                        }
                        break;
                }
            }

            public void membershipMessageReceived(SpreadMessage spreadMessage) {
                MembershipInfo info = spreadMessage.getMembershipInfo();
                if (info.isCausedByJoin()) {
                    if (info.getGroup().equals(aux.connection.getPrivateGroup())) {
                        if (info.getMembers().length == 1) {
                            try {
                                aux.dbConnection = DriverManager.getConnection("jdbc:hsqldb:file:supermarket" + port + "?allowMultiQueries=true", "SA", "");
                                aux.dbConnection.setAutoCommit(true);

                                aux.catalog = new CatalogSkeleton(aux.dbConnection);
                            } catch (SQLException exception) {
                                exception.printStackTrace();
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
                }
            }
        });

        SpreadGroup g = new SpreadGroup();
        g.join(this.connection, "supermarket");
    }

    /**
     * initializeAtomix
     * @throws ExecutionException ExecutionException
     * @throws InterruptedException InterruptedException
     */
    public void initializeAtomix() throws ExecutionException, InterruptedException {
        // Initialize the messaging service and register messaging service handlers
        this.ms = new NettyMessagingService("supermarket", Address.from(Integer.parseInt(this.port)), new MessagingConfig());
        ExecutorService executor = Executors.newFixedThreadPool(1);

        Supermarket aux = this;

        ms.registerHandler("newCart", (address, bytes) -> {
            DBUpdate dbu = new DBUpdate("INSERT INTO carts VALUES ()", aux.connection.getPrivateGroup().toString(), address.toString()); // TODO - query
            aux.sendCluster(aux.serializer.encode(dbu));
        }, executor);

        ms.registerHandler("addProduct", (address, bytes) -> {
            DBUpdate dbu = new DBUpdate("UPDATE carts SET ... WHERE id=...", aux.connection.getPrivateGroup().toString(), address.toString()); // TODO - query
            aux.sendCluster(aux.serializer.encode(dbu));
        }, executor);

        ms.registerHandler("removeProduct", (address, bytes) -> {
            DBUpdate dbu = new DBUpdate("UPDATE carts SET ... WHERE id=...", aux.connection.getPrivateGroup().toString(), address.toString()); // TODO - query
            aux.sendCluster(aux.serializer.encode(dbu));
        }, executor);

        ms.registerHandler("checkout", (address, bytes) -> {
            DBUpdate dbu = new DBUpdate("UPDATE carts SET ... WHERE id=...", aux.connection.getPrivateGroup().toString(), address.toString()); // TODO - query
            aux.sendCluster(aux.serializer.encode(dbu));
        }, executor);

        ms.registerHandler("getCatalog", (address, bytes) -> {
            Connection c = aux.serializer.decode(bytes);
            String res = catalog.getCatalog();
            ms.sendAsync(address, "res", serializer.encode(res));
        }, executor);

        ms.start().get();
    }

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
