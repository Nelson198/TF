import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;
import spread.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
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

    // Skeleton for the catalog
    Catalog catalog = new Catalog();

    // Skeletons for the carts
    HashMap<Integer, CartSkeleton> carts = new HashMap<Integer, CartSkeleton>();

    public Supermarket(int port) throws SpreadException, UnknownHostException, ExecutionException, InterruptedException {
        initializeSpread(port);
        initializeAtomix(port);
    }

    public void initializeSpread(int port) throws SpreadException, UnknownHostException {
        // Initialize the spread connection
        SpreadConnection c = new SpreadConnection();
        c.connect(InetAddress.getByName("localhost"), 4803, port, false, true);

        Supermarket aux = this;

        c.add(new AdvancedMessageListener() {
            @Override
            public void regularMessageReceived(SpreadMessage spreadMessage) {
                // ... = aux.serializer.decode(spreadMessage.getData());
            }

            @Override
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

    public void initializeAtomix(int port) throws ExecutionException, InterruptedException {
        // Initialize the messaging service and register messaging service handlers
        ManagedMessagingService ms = new NettyMessagingService("bank", Address.from(port), new MessagingConfig());
        ExecutorService executor = Executors.newFixedThreadPool(1);

        ms.registerHandler("newCart", (address, bytes) -> {
            // TODO
        }, executor);

        ms.registerHandler("addProduct", (address, bytes) -> {
            // TODO
        }, executor);

        ms.registerHandler("confirmOrder", (address, bytes) -> {
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

        Supermarket si = new Supermarket(Integer.parseInt(args[0]));

        System.out.println("Server is running...");

        new CompletableFuture<>().get();
    }
}
