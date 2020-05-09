package Middleware;

import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
    Middleware.ClientConnection - Middleware that handles the network connection of a client to the cluster.
                       It handles the cases of when a server goes down.
                       However, it does not contain logic related to the business logic.
                       Given that the client is single-threaded, this middleware can only handle one operation at a time.
 */
public class ClientConnection {
    private ExecutorService executor;
    private ManagedMessagingService ms;
    private ArrayList<Address> servers;
    private Address currentServer;
    private CompletableFuture<byte[]> res;

    /**
     * Parameterized constructor
     * @param address Address
     * @param servers Cluster's servers
     * @throws ExecutionException ExecutionException
     * @throws InterruptedException InterruptedException
     */
    public ClientConnection(Address address, ArrayList<Address> servers) throws ExecutionException, InterruptedException {
        this.executor = Executors.newFixedThreadPool(1);
        this.servers = servers;

        Random rand = new Random();
        int server = rand.nextInt(servers.size());
        this.currentServer = servers.get(server);

        this.ms = new NettyMessagingService("client", address, new MessagingConfig());
        this.ms.start().get();
    }

    /**
     * Register handler of a message with certain type
     * @param type Message's type
     */
    public void registerHandler(String type) {
        this.ms.registerHandler(type, (address, bytes) -> {
            res.complete(bytes);
        }, this.executor);
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
            return this.res.get(); // TODO - handle these error better
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
