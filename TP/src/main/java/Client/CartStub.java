package Client;

import Messages.CartUpdate;
import Middleware.ClientConnection;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Cart Stub
 */
public class CartStub {
    ClientConnection connection;

    String idCart; // ?

    // TODO - make a class for the serializers
    Serializer serializer = new SerializerBuilder().addType(CartUpdate.class).addType(String.class).build();

    /**
     * Parameterized constructor
     * @param connection Connection to the cluster
     */
    public CartStub(ClientConnection connection) {
        this.connection = connection;

        this.connection.registerHandler("res");
        this.connection.registerHandler("newCart");  // TODO - add the others

        // TODO - get ID from server
    }

    /**
     * Add product
     * @param idProduct Product's identifier
     * @param qtd Product's quantity
     */
    public void addProduct(String idProduct, int qtd) {
        CartUpdate toSend = new CartUpdate(idCart, idProduct, qtd);
        byte[] res = this.connection.sendAndReceive("addProduct", this.serializer.encode(toSend));
        // TODO - ....
    }

    /**
     * Remove product
     * @param idProduct Product's identifier
     * @param qtd Product's quantity
     */
    public void removeProduct(String idProduct, int qtd) {
        CartUpdate toSend = new CartUpdate(idCart, idProduct, qtd);
        byte[] res = this.connection.sendAndReceive("removeProduct", this.serializer.encode(toSend));
        // TODO - ....
    }

    /**
     * Checkout
     */
    public void checkout() {
        byte[] res = this.connection.sendAndReceive("checkout", this.serializer.encode(idCart));
        // TODO - ....
    }

    /**
     * Get cart's products
     * @return Cart's products
     */
    public Map<String, Integer> getProducts() {
        // TODO - ....
        return new HashMap<>();
    }
}
