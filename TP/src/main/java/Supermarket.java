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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Supermarket Implementation
 */
public class Supermarket {
    // Variable to store processes after which this one will be primary
    ArrayList<SpreadGroup> primaryAfter = new ArrayList<>();

    // Serializer for the messages sent between servers
    Serializer serializer = new SerializerBuilder().build();

    // Initialize the spread connection
    SpreadConnection c = new SpreadConnection();

    // Database connection
    Connection dbConnection;

    // Skeleton for the catalog
    Catalog catalog = new Catalog();

    // Skeletons for the carts
    Map<Integer, CartSkeleton> carts = new HashMap<>();

    /**
     * Parameterized constructor
     * @param port Port
     * @throws SpreadException SpreadException
     * @throws UnknownHostException UnknownHostException
     * @throws ExecutionException ExecutionException
     * @throws InterruptedException InterruptedException
     */
    public Supermarket(String port) throws SpreadException, UnknownHostException, ExecutionException, InterruptedException {
        initializeSpread(port);
        initializeAtomix(Integer.parseInt(port));
    }

    /**
     * initializeSpread
     * @param port Port
     * @throws SpreadException SpreadException
     * @throws UnknownHostException UnknownHostException
     */
    public void initializeSpread(String port) throws SpreadException, UnknownHostException {
        // Initialize the spread connection
        SpreadConnection c = new SpreadConnection();
        c.connect(InetAddress.getByName("localhost"), 4803, port, false, true);

        Supermarket aux = this;

        c.add(new AdvancedMessageListener() {
            public void regularMessageReceived(SpreadMessage spreadMessage) {
                Message ms = aux.serializer.decode(spreadMessage.getData());
                switch (ms.getType()) {
                    case "database":
                        DatabaseMessage dbMsg = (DatabaseMessage) ms;
                        // ........ update the db file
                        try {
                            aux.dbConnection = DriverManager.getConnection("jdbc:hsqldb:file:supermarket" + port, "SA", "");
                        } catch (SQLException exception) {
                            exception.printStackTrace();
                        }
                        break;
                }
            }

            public void membershipMessageReceived(SpreadMessage spreadMessage) {
                MembershipInfo info = spreadMessage.getMembershipInfo();
                if (info.isCausedByJoin()) {
                    if (info.getGroup().equals(c.getPrivateGroup())) {
                        for (SpreadGroup member : info.getMembers()) {
                            if (!member.equals(c.getPrivateGroup()))
                                primaryAfter.add(member);
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
        g.join(c, "supermarket");
    }

    /**
     * initializeAtomix
     * @param port Port
     * @throws ExecutionException ExecutionException
     * @throws InterruptedException InterruptedException
     */
    public void initializeAtomix(int port) throws ExecutionException, InterruptedException {
        // Initialize the messaging service and register messaging service handlers
        ManagedMessagingService ms = new NettyMessagingService("supermarket", Address.from(port), new MessagingConfig());
        ExecutorService executor = Executors.newFixedThreadPool(1);

        Supermarket aux = this;

        ms.registerHandler("newCart", (address, bytes) -> {
            // TODO
        }, executor);

        ms.registerHandler("addProduct", (address, bytes) -> {
            // TODO
        }, executor);

        ms.registerHandler("checkout", (address, bytes) -> {
            // TODO
        }, executor);

        ms.registerHandler("removeProduct", (address, bytes) -> {
            // TODO
        }, executor);

        ms.registerHandler("getCatalog", (address, bytes) -> {
            // TODO
        }, executor);

        ms.registerHandler("getProduct", (address, bytes) -> {
            // TODO
        }, executor);

        ms.start().get();
    }

    public static void main(String[] args) throws InterruptedException, SpreadException, ExecutionException, UnknownHostException {
        if (args.length != 1) {
            System.out.println("Indique a porta do servidor");
            System.exit(1);
        }

        Supermarket s = new Supermarket(args[0]);

        System.out.println("Server is running on port " + args[0] + " ...");

        new CompletableFuture<>().get();
    }
}
