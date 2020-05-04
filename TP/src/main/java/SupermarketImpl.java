import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;
import spread.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Supermarket Implementation
 */
public class SupermarketImpl implements Supermarket {
    public CartStub newCart() {
        // TODO
        return new CartStub();
    }

    public static <ManagedMessagingService> void main(String[] args) throws InterruptedException, SpreadException, ExecutionException, UnknownHostException {
        if (args.length != 1) {
            System.out.println("Indique a porta do servidor");
            System.exit(1);
        }

        // Serializer for all the messages
        Serializer serializer = new SerializerBuilder().build();

        // Initialize the spread connection
        SpreadConnection c = new SpreadConnection();
        c.connect(InetAddress.getByName("localhost"), 4803, args[0], false, true);

        c.add(new AdvancedMessageListener() {
            @Override
            public void regularMessageReceived(SpreadMessage spreadMessage) {
                // TODO
            }

            @Override
            public void membershipMessageReceived(SpreadMessage spreadMessage) {
                MembershipInfo info = spreadMessage.getMembershipInfo();
                // TODO
            }
        });

        SpreadGroup g = new SpreadGroup();
        g.join(c, "bank");


        // Initialize the messaging service and register messaging service handlers
        ManagedMessagingService ms = new NettyMessagingService("bank", Address.from(Integer.parseInt(args[0])), new MessagingConfig());
        ExecutorService executor = Executors.newFixedThreadPool(1);

        ms.registerHandler("handler1", (address, bytes) -> {
            // TODO
        }, executor);

        ms.registerHandler("handler2", (address, bytes) -> {
            // TODO
        }, executor);

        ms.start().get();

        System.out.println("Server is running...");

        new CompletableFuture<>().get();
    }
}
