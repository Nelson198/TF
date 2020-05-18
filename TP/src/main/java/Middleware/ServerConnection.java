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

import java.io.File;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * ServerConnection - Middleware that handles the network connection of a server in the cluster.
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

    // Last version of DB each server has
    Map<String, Integer> lastDBVersion = new HashMap<>();

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
            System.exit(1);
        }
    }

    /**
     * Unicast a message to a server
     * @param message Message
     */
    public void sendServer(byte[] message, SpreadGroup server) {
        SpreadMessage m = new SpreadMessage();
        m.addGroup(server);
        m.setData(message);
        m.setSafe();
        try {
            this.spreadConnection.multicast(m);
        } catch (SpreadException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Start a timer, after which a message is sent to the cluster
     * @param message Message
     */
    public void startTimer(Object message, String type, int time) {
        Thread timer = new TimerThread(message, type, time, this);
        timer.start();
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
                    try {
                        DBContent dbMsg = (DBContent) ms;
                        aux.lastDBVersion = dbMsg.getLastDBVersion();
                        if (dbMsg.getIsFullBackup()) {
                            System.out.println("Received full backup");
                            byte[] backup = dbMsg.getBackup();

                            File directory = new File("clusterDB" + port + "/");
                            directory.mkdir();

                            FileOutputStream os = new FileOutputStream("clusterDB" + port + "/backup.tar.gz");
                            os.write(backup);
                            os.close();

                            Runtime rt = Runtime.getRuntime();
                            Process untar = rt.exec(new String[]{"tar", "-xvzf", "clusterDB" + port + "/backup.tar.gz", "-C", "clusterDB" + port + "/"});
                            untar.waitFor();

                            File file = new File("clusterDB" + port + "/backup.tar.gz");
                            file.delete();
                        } else {
                            System.out.println("Received incremental backup");
                            byte[] log = dbMsg.getBackup();

                            FileOutputStream os = new FileOutputStream("clusterDB" + port + "/db.log");
                            os.write(log);
                            os.close();
                        }

                        aux.dbConnection = DriverManager.getConnection("jdbc:hsqldb:file:clusterDB" + port + "/db", "sa", "");

                        for (DBUpdate dbUpdate : aux.pendingQueries)
                            processDBUpdate.apply(dbUpdate, aux.dbConnection);
                        aux.pendingQueries = null;

                        afterDBStart.accept(aux.dbConnection);

                        // Initialize the atomix connection
                        initializeAtomix(handlers);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        System.exit(1);
                    }
                } else if (ms.getType().equals("dbUpdate")) {
                    DBUpdate dbUpdate = (DBUpdate) ms;

                    if (aux.dbConnection == null) {
                        aux.pendingQueries.add(dbUpdate);
                    } else {
                        Object res = processDBUpdate.apply(dbUpdate, aux.dbConnection);
                        if (dbUpdate.getServer() != null && dbUpdate.getServer().equals(aux.spreadConnection.getPrivateGroup().toString()))
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
                                aux.dbConnection = DriverManager.getConnection("jdbc:hsqldb:file:clusterDB" + port + "/db", "SA", "");

                                // Create the tables
                                Statement stm = dbConnection.createStatement();
                                for (String table : tablesToCreate)
                                    stm.executeUpdate(table);

                                afterDBStart.accept(aux.dbConnection);

                                lastDBVersion.put(aux.spreadConnection.getPrivateGroup().toString(), 0);

                                // Initialize the atomix connection
                                initializeAtomix(handlers);
                            } catch (Exception e) {
                                e.printStackTrace();
                                System.exit(1);
                            }
                        } else {
                            for (SpreadGroup member : info.getMembers()) {
                                if (!member.equals(aux.spreadConnection.getPrivateGroup()))
                                    primaryAfter.add(member);
                            }
                        }
                    } else {
                        try {
                            Integer joinedLastVersion = aux.lastDBVersion.get(info.getJoined().toString());
                            Integer thisLastVersion = aux.lastDBVersion.get(spreadConnection.getPrivateGroup().toString());
                            if (joinedLastVersion == null || !joinedLastVersion.equals(thisLastVersion)) {
                                // Send full database
                                Statement stm = aux.dbConnection.createStatement();
                                stm.executeUpdate("CHECKPOINT"); // Make a checkpoint in every member of the cluster to synchronize the DB's

                                HashMap<String, Integer> aux = new HashMap<>();
                                lastDBVersion.forEach((k, v) -> aux.put(k, v+1));
                                lastDBVersion = aux;

                                lastDBVersion.put(info.getJoined().toString(), thisLastVersion+1);

                                if (primaryAfter.size() == 0) {
                                    stm.executeUpdate("BACKUP DATABASE TO 'clusterDB" + port + "/backup.tar.gz' NOT BLOCKING");

                                    byte[] backup = Files.readAllBytes(Paths.get("clusterDB" + port + "/backup.tar.gz"));
                                    sendServer(serializer.encode(new DBContent(backup, true, lastDBVersion)), info.getJoined());

                                    File file = new File("clusterDB" + port + "/backup.tar.gz");
                                    file.delete();
                                }
                            } else {
                                lastDBVersion.put(info.getJoined().toString(), thisLastVersion+1);

                                if (primaryAfter.size() == 0) {
                                    lastDBVersion.put(info.getJoined().toString(), thisLastVersion);
                                    byte[] backup = Files.readAllBytes(Paths.get("clusterDB" + port + "/db.log"));
                                    sendServer(serializer.encode(new DBContent(backup, false, lastDBVersion)), info.getJoined());
                                }
                            }
                        } catch (Exception exception) {
                            exception.printStackTrace();
                            System.exit(1);
                        }
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
