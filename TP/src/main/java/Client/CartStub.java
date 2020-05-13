package Client;

import Helpers.Product;
import Helpers.Serializers;
import Helpers.CartUpdate;
import Middleware.ClientConnection;

import io.atomix.utils.serializer.Serializer;

import java.util.List;

/**
 * Cart Stub
 */
public class CartStub {
    private final ClientConnection connection;
    private final Serializer serializer = Serializers.clientSerializer;
    private final String idCart;

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
        return this.serializer.decode(res);
    }
}
