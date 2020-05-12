package Client;

import Helpers.CartUpdate;
import Helpers.Product;
import Helpers.Serializers;
import Middleware.ClientConnection;

import io.atomix.utils.serializer.Serializer;

import java.util.List;

/**
 * Cart Stub
 */
public class CartStub {
    private final String idCart;
    private final ClientConnection connection;
    private final Serializer serializer = Serializers.clientSerializer;

    /**
     * Parameterized constructor
     * @param connection Connection to the cluster
     */
    public CartStub(ClientConnection connection) {
        this.connection = connection;
        byte[] res = this.connection.sendAndReceive("newCart", this.serializer.encode(null));
        this.idCart = this.serializer.decode(res);
    }

    /**
     * Add product
     * @param idProduct Product's identifier
     * @param amount Product's amount
     */
    public void addProduct(String idProduct, int amount) {
        CartUpdate toSend = new CartUpdate(this.idCart, idProduct, amount);
        this.connection.sendAndReceive("updateProduct", this.serializer.encode(toSend));
    }

    /**
     * Remove product
     * @param idProduct Product's identifier
     * @param amount Product's amount
     */
    public void removeProduct(String idProduct, int amount) {
        CartUpdate toSend = new CartUpdate(this.idCart, idProduct, amount);
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
