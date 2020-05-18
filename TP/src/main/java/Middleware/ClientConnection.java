package Middleware;

import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;

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
    private final ManagedMessagingService ms;
    private final List<Address> servers;
    private int currentServer;
    private CompletableFuture<byte[]> res;

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
        this.currentServer = rand.nextInt(servers.size());

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

        int initialServer = this.currentServer;
        while (true) {
            CompletableFuture<Void> x = this.ms.sendAsync(this.servers.get(this.currentServer), type, message);
            try {
                x.get();
                return this.res.get();
            } catch (Exception e) {
                System.out.println("Switching server ...");
                this.currentServer = (this.currentServer + 1) % this.servers.size();
                if (this.currentServer == initialServer) {
                    System.out.println("No servers available. Try again later...");
                    System.exit(1);
                }
            }
        }
    }
}
