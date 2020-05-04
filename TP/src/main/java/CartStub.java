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
    String idCart;

    Serializer serializer = new SerializerBuilder().addType(String.class).build();

    ManagedMessagingService ms;
    Address serverAddress;

    ExecutorService executor = Executors.newFixedThreadPool(1);
    CompletableFuture<Boolean> res;

    public CartStub(ManagedMessagingService ms, Address serverAddress) throws ExecutionException, InterruptedException {
        // Initialize the messaging service
        this.ms = ms;
        this.serverAddress = serverAddress;

        this.ms.registerHandler("res", (a, b) -> {
            boolean res = serializer.decode(b);
            this.res.complete(res);
        }, this.executor);

        this.ms.registerHandler("newCart", (a, b) -> {
            idCart = serializer.decode(b);
        }, this.executor);

        ms.start().get();
    }

    public void addProduct(String idProduct, int qtd) {
        CartUpdate toSend = new CartUpdate(idCart, idProduct, qtd);
        this.ms.sendAsync(serverAddress, "addProduct", this.serializer.encode(toSend));
    }

    public void removeProduct(String idProduct, int qtd) {
        CartUpdate toSend = new CartUpdate(idCart, idProduct, qtd);
        this.ms.sendAsync(serverAddress, "removeProduct", this.serializer.encode(toSend));
    }

    public void checkout() {
        this.res = new CompletableFuture<>();
        this.ms.sendAsync(serverAddress, "checkout", this.serializer.encode(idCart));
    }
}