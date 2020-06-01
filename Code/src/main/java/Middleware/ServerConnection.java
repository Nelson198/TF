package Middleware;

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

import java.io.File;
import java.io.FileOutputStream;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final Serializer serializer = new SerializerBuilder().addType(DBUpdate.class)
                                                                 .addType(DBContent.class)
                                                                 .build();

    // DBUpdate's that arrived between this server's connection and the reception of the DB
    List<DBUpdate> pendingQueries = new ArrayList<>();

    // Set of servers that are in the latest version of the database
    Set<String> latestDBServers = new HashSet<>();

    // Set of all servers in the cluster
    Set<SpreadGroup> allServers = new HashSet<>();

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
    public void startTimer(byte[] message, String type, int time, boolean loop) {
        Thread timer = new TimerThread(this.serializer, message, type, time, this, loop);
        timer.start();
    }

    public void checkpoint() {
        try {
            Statement stm = this.dbConnection.createStatement();
            stm.executeUpdate("CHECKPOINT"); // Make a checkpoint in every member of the cluster to synchronize the DB's
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }

        HashSet<String> aux = new HashSet<>();
        latestDBServers.forEach((k) -> {
            for (SpreadGroup member : this.allServers) {
                if (k.equals(member.toString())) {
                    aux.add(k);
                    break;
                }
            }
        });
        latestDBServers = aux;
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
    public void initialize(BiFunction<DBUpdate, Connection, byte[]> processDBUpdate, Consumer<Connection> afterDBStart, List<String> tablesToCreate, Map<String, BiFunction<Address, byte[], HandlerRes>> handlers) throws SpreadException, UnknownHostException {
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
                        aux.latestDBServers = dbMsg.getLatestDBServers();
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

                            // Save the log in memory and delete from the filesystem
                            byte[] logFileContent = null;
                            try {
                                logFileContent = Files.readAllBytes(Paths.get("clusterDB" + port + "/db.log"));
                            } catch (Exception ignored) {}

                            File logFile = new File("clusterDB" + port + "/db.log");
                            logFile.delete();

                            aux.dbConnection = DriverManager.getConnection("jdbc:hsqldb:file:clusterDB" + port + "/db", "sa", "");

                            startTimer(aux.serializer.encode(null), "checkpoint", 60*60, true); // Make a CHECKPOINT every hour

                            // Write the log after starting the server, so that it isn't cleared
                            if (logFileContent != null) {
                                FileOutputStream logStream = new FileOutputStream("clusterDB" + port + "/db.log");
                                logStream.write(logFileContent);
                                logStream.close();
                            }
                        } else {
                            System.out.println("Received incremental backup");

                            // Delete previous log from filesystem
                            File logFile = new File("clusterDB" + port + "/db.log");
                            logFile.delete();

                            aux.dbConnection = DriverManager.getConnection("jdbc:hsqldb:file:clusterDB" + port + "/db", "sa", "");

                            startTimer(aux.serializer.encode(null), "checkpoint", 60*60, true); // Make a CHECKPOINT every hour

                            // Write the log after starting the server, so that it isn't cleared
                            FileOutputStream os = new FileOutputStream("clusterDB" + port + "/db.log");
                            os.write(dbMsg.getBackup());
                            os.close();
                        }

                        for (DBUpdate dbUpdate : aux.pendingQueries) {
                            if (dbUpdate.getSecondaryType().equals("checkpoint"))
                                checkpoint();
                            else
                                processDBUpdate.apply(dbUpdate, aux.dbConnection);
                        }
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
                        if (dbUpdate.getSecondaryType().equals("checkpoint")) {
                            checkpoint();
                        } else {
                            byte[] res = processDBUpdate.apply(dbUpdate, aux.dbConnection);
                            if (dbUpdate.getServer() != null && dbUpdate.getServer().equals(aux.spreadConnection.getPrivateGroup().toString()))
                                aux.ms.sendAsync(Address.from(dbUpdate.getClient()), "res", res);
                        }
                    }
                }
            }

            public void membershipMessageReceived(SpreadMessage spreadMessage) {
                MembershipInfo info = spreadMessage.getMembershipInfo();
                if (info.isCausedByJoin()) {
                    aux.allServers.add(info.getJoined());

                    if (info.getJoined().equals(aux.spreadConnection.getPrivateGroup())) {
                        if (info.getMembers().length == 1) {
                            try {
                                aux.dbConnection = DriverManager.getConnection("jdbc:hsqldb:file:clusterDB" + port + "/db", "SA", "");

                                startTimer(aux.serializer.encode(null), "checkpoint", 60*60, true); // Make a CHECKPOINT every hour

                                // Create the tables
                                Statement stm = dbConnection.createStatement();
                                for (String table : tablesToCreate)
                                    stm.executeUpdate(table);

                                afterDBStart.accept(aux.dbConnection);

                                latestDBServers.add(aux.spreadConnection.getPrivateGroup().toString());

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
                            if (!latestDBServers.contains(info.getJoined().toString())) {
                                // Send full database backup
                                checkpoint();

                                latestDBServers.add(info.getJoined().toString());

                                if (aux.isPrimary()) {
                                    Statement stm = aux.dbConnection.createStatement();
                                    stm.executeUpdate("BACKUP DATABASE TO 'clusterDB" + port + "/backup.tar.gz' NOT BLOCKING");

                                    byte[] backup = Files.readAllBytes(Paths.get("clusterDB" + port + "/backup.tar.gz"));
                                    sendServer(serializer.encode(new DBContent(backup, true, latestDBServers)), info.getJoined());

                                    File file = new File("clusterDB" + port + "/backup.tar.gz");
                                    file.delete();
                                }
                            } else {
                                // Send incremental database backup
                                if (primaryAfter.size() == 0) {
                                    byte[] backup = Files.readAllBytes(Paths.get("clusterDB" + port + "/db.log"));
                                    sendServer(serializer.encode(new DBContent(backup, false, latestDBServers)), info.getJoined());
                                }
                            }
                        } catch (Exception exception) {
                            exception.printStackTrace();
                            System.exit(1);
                        }
                    }
                } else if (info.isCausedByDisconnect()) {
                    aux.allServers.remove(info.getJoined());
                    primaryAfter.remove(info.getDisconnected());
                } else if (info.isCausedByLeave()) {
                    aux.allServers.remove(info.getJoined());
                    primaryAfter.remove(info.getLeft());
                } else if (info.isCausedByNetwork()) {
                    HashSet<SpreadGroup> newAllServers = new HashSet<>();
                    boolean inGroup = false;
                    for (SpreadGroup member : info.getMyVirtualSynchronySet().getMembers()) {
                        newAllServers.add(member);
                        if (member.equals(spreadConnection.getPrivateGroup())) {
                            inGroup = true;
                            break;
                        }
                    }
                    if (!inGroup) {
                        System.out.println("Server is going to be disconnected due to a network partition");
                        System.exit(1);
                    }

                    aux.allServers = newAllServers;
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
                    ms.sendAsync(address, "res", res.getInfo());
            }, executor);
        });

        ms.start().get();
    }
}
