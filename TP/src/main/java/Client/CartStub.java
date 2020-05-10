package Client;

import Messages.CartUpdate;
import Middleware.ClientConnection;
import Serializer.ClientSerializer;

import io.atomix.utils.serializer.Serializer;

import java.util.HashMap;

/**
 * Cart Stub
 */
public class CartStub {
    private ClientConnection connection;
    private ClientSerializer serializer;
    private final String idCart;

    /**
     * Parameterized constructor
     * @param connection Connection to the cluster
     */
    public CartStub(ClientConnection connection) {
        this.connection = connection;
        this.serializer = new ClientSerializer();

        this.connection.registerHandler("res");

        Serializer s = this.serializer.getSerializer();
        byte[] res = this.connection.sendAndReceive("newCart", null);
        this.idCart = s.decode(res);
    }

    /**
     * Add product
     * @param idProduct Product's identifier
     * @param qtd Product's quantity
     */
    public void addProduct(String idProduct, int qtd) {
        Serializer s = this.serializer.getSerializer();
        CartUpdate toSend = new CartUpdate(this.idCart, idProduct, qtd);
        byte[] res = this.connection.sendAndReceive("updateProduct", s.encode(toSend));
        // TODO - ....
    }

    /**
     * Remove product
     * @param idProduct Product's identifier
     * @param qtd Product's quantity
     */
    public void removeProduct(String idProduct, int qtd) {
        Serializer s = this.serializer.getSerializer();
        CartUpdate toSend = new CartUpdate(this.idCart, idProduct, qtd);
        byte[] res = this.connection.sendAndReceive("updateProduct", s.encode(toSend));
        // TODO - ....
    }

    /**
     * Checkout
     */
    public void checkout() {
        Serializer s = this.serializer.getSerializer();
        byte[] res = this.connection.sendAndReceive("checkout", s.encode(this.idCart));
        // TODO - ....
    }

    /**
     * Get cart's products
     * @return Cart's products
     */
    public HashMap<String, Integer> getProducts() {
        Serializer s = this.serializer.getSerializer();
        // TODO - ....
        return new HashMap<>();
    }
}
