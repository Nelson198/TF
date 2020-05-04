import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;
import io.atomix.cluster.messaging.ManagedMessagingService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Cart Stub
 */
public class CartStub {
    Serializer serializer = new SerializerBuilder().addType(String.class).build();
    ManagedMessagingService ms;
    Address serverAddress;
    ExecutorService executor = Executors.newFixedThreadPool(1);
    CompletableFuture<CartImpl> res;

    public CartStub(Address myAddress, Address serverAddress) throws ExecutionException, InterruptedException {
        this.serverAddress = serverAddress;

        // Initialize the messaging service
        this.ms = new NettyMessagingService("cart", myAddress, new MessagingConfig());

        this.ms.registerHandler("res", (a, b) -> {
            CartImpl res = serializer.decode(b);
            this.res.complete(res);
        }, this.executor);

        ms.start().get();
    }

    public void addProduct(String idProduct) throws ExecutionException, InterruptedException {
        this.res = new CompletableFuture<>();
        this.ms.sendAsync(serverAddress, "addProduct", this.serializer.encode(idProduct));
    }
}