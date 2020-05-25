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
    private final int idCart;
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
     * Update product
     * @param idProduct Product's identifier
     * @param amount Product's amount
     */
    public void updateProduct(int idProduct, int amount) {
        CartUpdate toSend = new CartUpdate(this.idCart, idProduct, amount);
        this.connection.sendAndReceive("updateCart", this.serializer.encode(toSend));
    }

    /**
     * Checkout
     */
    public boolean checkout() {
        byte[] res = this.connection.sendAndReceive("checkout", this.serializer.encode(this.idCart));
        return this.serializer.decode(res);
    }

    /**
     * Get cart's products
     * @return Cart's products
     */
    public List<Product> getProducts() {
        byte[] res = this.connection.sendAndReceive("getProducts", this.serializer.encode(this.idCart));
        return this.serializer.decode(res);
    }
}
