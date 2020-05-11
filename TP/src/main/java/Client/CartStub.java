package Client;

import Helpers.Serializers;
import Messages.CartUpdate;
import Middleware.ClientConnection;

import io.atomix.utils.serializer.Serializer;

import java.util.HashMap;

/**
 * Cart Stub
 */
public class CartStub {
    private ClientConnection connection;
    private Serializer serializer = Serializers.clientSerializer;
    private final String idCart;

    /**
     * Parameterized constructor
     * @param connection Connection to the cluster
     */
    public CartStub(ClientConnection connection) {
        this.connection = connection;

        this.connection.registerHandler("res");

        byte[] res = this.connection.sendAndReceive("newCart", null);
        this.idCart = this.serializer.decode(res);
    }

    /**
     * Add product
     * @param idProduct Product's identifier
     * @param qtd Product's quantity
     */
    public void addProduct(String idProduct, int qtd) {
        CartUpdate toSend = new CartUpdate(this.idCart, idProduct, qtd);
        this.connection.sendAndReceive("updateProduct", this.serializer.encode(toSend));
    }

    /**
     * Remove product
     * @param idProduct Product's identifier
     * @param qtd Product's quantity
     */
    public void removeProduct(String idProduct, int qtd) {
        CartUpdate toSend = new CartUpdate(this.idCart, idProduct, qtd);
        this.connection.sendAndReceive("updateProduct", this.serializer.encode(toSend));
    }

    /**
     * Checkout
     */
    public void checkout() {
        this.connection.sendAndReceive("checkout", this.serializer.encode(this.idCart));
    }

    /**
     * Get cart's products
     * @return Cart's products
     */
    public List<Product> getProducts() {
        byte[] res = this.connection.sendAndReceive("getProducts", this.serializer.encode(this.idCart));
        List<Product> productList = this.serializer.decode(res);
        return productList;
    }
}
