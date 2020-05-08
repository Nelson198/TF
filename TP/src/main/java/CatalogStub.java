import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Catalog Stub
 */
public class CatalogStub {
    Serializer serializer = new SerializerBuilder().addType(CartUpdate.class).addType(String.class).build();
    ManagedMessagingService ms;
    Address serverAddress; // TODO - this can change if the server goes down
    ExecutorService executor = Executors.newFixedThreadPool(1);  // TODO - reuse across the client
    CompletableFuture<Boolean> res;

    /**
     * Parameterized constructor
     * @param ms Stub address
     * @param serverAddress Server address
     * @throws ExecutionException ExecutionException
     * @throws InterruptedException InterruptedException
     */
    public CatalogStub(ManagedMessagingService ms, Address serverAddress) throws ExecutionException, InterruptedException {
        // Initialize the messaging service
        this.ms = ms;
        this.serverAddress = serverAddress;

        // TODO - this can't be done here, because we can have more than one cart
        this.ms.registerHandler("res", (a, b) -> {
            boolean res = serializer.decode(b);
            this.res.complete(res);
        }, this.executor);

        ms.start().get();
    }

    // TODO - add the methods needed
}
