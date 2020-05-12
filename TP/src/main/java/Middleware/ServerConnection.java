package Middleware;

import Helpers.Serializers;
import Messages.DBContent;
import Messages.DBUpdate;
import Messages.Message;
import Server.HandlerRes;

import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;

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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * ServerConnection - Middleware that handles the network connection of a server in the cluster.
 *
 * TODO - bring more stuff to this class
 */
public class ServerConnection {
    // Server port
    private final String port;

    // Variable to store processes after which this one will be primary
    List<SpreadGroup> primaryAfter = new ArrayList<>();

    // Initialize the spread connection
    private SpreadConnection spreadConnection = new SpreadConnection();

    // Database connection
    private Connection dbConnection;

    // Atomix connection
    ManagedMessagingService ms;

    // Serializer for the messages
    private final Serializer serializer = Serializers.serverSerializer;

    // DBUpdate's that arrived between this server's connection and the reception of the DB
    List<DBUpdate> pendingQueries = new ArrayList<>();

    /**
     * Parameterized constructor
     * @param port Port
     */
    public ServerConnection(String port) {
        this.port = port;
    }

    /**
     * isPrimary
     * @return Status
     */
    public boolean isPrimary() {
        return this.primaryAfter.size() == 0;
    }

    /**
     * Multicast a message to the cluster
     * @param message Message
     */
    public void sendCluster(byte[] message) {
        SpreadMessage m = new SpreadMessage();
        m.addGroup("cluster");
        m.setData(message);
        m.setSafe();
        try {
            this.spreadConnection.multicast(m);
        } catch (SpreadException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize Spread connection
     * @param processDBUpdate Function executed when there is a DBUpdate that arrives to the server
     * @param afterDBStart Function executed after the DB is initialized
     * @param tablesToCreate ArrayList of SQL statements to create the tables when the DB is ready
     * @param handlers Handlers to be called when the client sends requests
     * @throws SpreadException SpreadException
     * @throws UnknownHostException UnknownHostException
     */
    public void initialize(BiFunction<DBUpdate, Connection, Object> processDBUpdate, Consumer<Connection> afterDBStart, List<String> tablesToCreate, Map<String, BiFunction<Address, byte[], HandlerRes>> handlers) throws SpreadException, UnknownHostException {
        // Initialize the spread connection
        this.spreadConnection = new SpreadConnection();
        this.spreadConnection.connect(InetAddress.getByName("localhost"), 4803, this.port, false, true);

        ServerConnection aux = this;

        this.spreadConnection.add(new AdvancedMessageListener() {
            public void regularMessageReceived(SpreadMessage spreadMessage) {
                Message ms = aux.serializer.decode(spreadMessage.getData());
                if (ms.getType().equals("db")) {
                    DBContent dbMsg = (DBContent) ms;
                    // TODO - update the db file
                    try {
                        aux.dbConnection = DriverManager.getConnection("jdbc:hsqldb:file:supermarket" + port + "/", "sa", "");

                        for (DBUpdate dbUpdate : aux.pendingQueries)
                            processDBUpdate.apply(dbUpdate, aux.dbConnection);
                        aux.pendingQueries = null;

                        afterDBStart.accept(aux.dbConnection);

                        // Initialize the atomix connection
                        initializeAtomix(handlers);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                } else if (ms.getType().equals("dbUpdate")) {
                    DBUpdate dbUpdate = (DBUpdate) ms;

                    if (aux.dbConnection == null) {
                        aux.pendingQueries.add(dbUpdate);
                    } else {
                        Object res = processDBUpdate.apply(dbUpdate, aux.dbConnection);
                        if (dbUpdate.getServer().equals(aux.spreadConnection.getPrivateGroup().toString()))
                            aux.ms.sendAsync(Address.from(dbUpdate.getClient()), "res", aux.serializer.encode(res));
                    }
                }
            }

            public void membershipMessageReceived(SpreadMessage spreadMessage) {
                MembershipInfo info = spreadMessage.getMembershipInfo();
                if (info.isCausedByJoin()) {
                    if (info.getJoined().equals(aux.spreadConnection.getPrivateGroup())) {
                        if (info.getMembers().length == 1) {
                            try {
                                aux.dbConnection = DriverManager.getConnection("jdbc:hsqldb:file:supermarket" + port + "/", "SA", "");

                                // Create the tables
                                Statement stm = dbConnection.createStatement();
                                for (String table : tablesToCreate)
                                    stm.executeUpdate(table);

                                afterDBStart.accept(aux.dbConnection);

                                // Initialize the atomix connection
                                initializeAtomix(handlers);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            for (SpreadGroup member : info.getMembers()) {
                                if (!member.equals(aux.spreadConnection.getPrivateGroup()))
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
        g.join(this.spreadConnection, "cluster");
    }

    /**
     * Initialize Atomix connection
     * @throws ExecutionException ExecutionException
     * @throws InterruptedException InterruptedException
     */
    public void initializeAtomix(Map<String, BiFunction<Address, byte[], HandlerRes>> handlers) throws ExecutionException, InterruptedException {
        // Initialize the messaging service and register messaging service handlers
        this.ms = new NettyMessagingService("cluster", Address.from(Integer.parseInt(port)), new MessagingConfig());

        ExecutorService executor = Executors.newFixedThreadPool(1);
        handlers.forEach((k, v) -> {
            this.ms.registerHandler(k, (address, bytes) -> {
                HandlerRes res = v.apply(address, bytes);
                if (res.getSendToCluster())
                    sendCluster(serializer.encode(new DBUpdate(res.getInfo(), address.toString(), spreadConnection.getPrivateGroup().toString(), k)));
                else if (res.getSendToClient())
                    ms.sendAsync(address, "res", (byte[]) res.getInfo());
            }, executor);
        });

        ms.start().get();
    }
}
