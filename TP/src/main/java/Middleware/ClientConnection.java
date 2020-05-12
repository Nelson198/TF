package Middleware;

import Helpers.Serializers;
import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

/**
 * ClientConnection - Middleware that handles the network connection of a client to the cluster.
 *                    It handles the cases of when a server goes down.
 *                    However, it does not contain logic related to the business logic.
 *                    Given that the client is single-threaded, this middleware can only handle one operation at a time.
 */
public class ClientConnection {
    private ManagedMessagingService ms;
    private List<Address> servers;
    private Address currentServer;
    private CompletableFuture<byte[]> res;
    private final Serializer serializer = Serializers.clientSerializer;

    /**
     * Parameterized constructor
     * @param address Address
     * @param servers Cluster's servers
     * @throws ExecutionException ExecutionException
     * @throws InterruptedException InterruptedException
     */
    public ClientConnection(Address address, List<Address> servers) throws ExecutionException, InterruptedException {
        this.servers = servers;

        Random rand = new Random();
        int server = rand.nextInt(servers.size());
        this.currentServer = servers.get(server);

        this.ms = new NettyMessagingService("cluster", address, new MessagingConfig());

        this.ms.registerHandler("res", (address1, bytes) -> {
            this.res.complete(bytes);
        }, Executors.newFixedThreadPool(1));

        this.ms.start().get();
    }

    /**
     * Send and receive a message
     * @param type Message's type
     * @param message Message's content
     * @return Reply message
     */
    public byte[] sendAndReceive(String type, byte[] message) {
        this.res = new CompletableFuture<>();

        this.ms.sendAsync(this.currentServer, type, message);

        try {
            return this.res.get(); // TODO - handle these exceptions better (switch servers)
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
