import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

/**
 * Cart Stub
 */
public class CartStub {
    AtomixConnection connection;

    String idCart; // ?

    // TODO - make a class for the serializers
    Serializer serializer = new SerializerBuilder().addType(CartUpdate.class).addType(String.class).build();

    /**
     * Parameterized constructor
     * @param connection Connection to the cluster
     */
    public CartStub(AtomixConnection connection) {
        this.connection = connection;

        this.connection.registerHandler("res");
        this.connection.registerHandler("newCart");  // TODO - add the others
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
}
