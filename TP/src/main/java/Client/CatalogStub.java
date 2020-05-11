package Client;

import Helpers.Serializers;
import Middleware.ClientConnection;

import io.atomix.utils.serializer.Serializer;

/**
 * Catalog Stub
 */
public class CatalogStub {
    private ClientConnection connection;
    private Serializer serializer = Serializers.clientSerializer;

    /**
     * Parameterized constructor
     * @param connection Connection to the cluster
     */
    public CatalogStub(ClientConnection connection) {
        this.connection = connection;

        this.connection.registerHandler("catalogRes"); // TODO - add the others
    }

    /**
     * Get catalog information
     * @return Catalog information
     */
    public String getCatalog() {
        byte[] res = this.connection.sendAndReceive("getCatalog", null);
        String catalog = this.serializer.decode(res);
        return catalog;
    }

    /**
     * Get product's price
     * @param idProduct Product's identifier
     * @return Product's price
     */
    public float getPrice(String idProduct) {
        byte[] res = this.connection.sendAndReceive("getPrice", this.serializer.encode(idProduct));
        float price = this.serializer.decode(res);
        return price;
    }

    /**
     * Get product's availability
     * @param idProduct Product's identifier
     * @return Product's availability
     */
    public int getAvailability(String idProduct) {
        byte[] res = this.connection.sendAndReceive("getAvailability", this.serializer.encode(idProduct));
        int availability = this.serializer.decode(res);
        return availability;
    }
}
