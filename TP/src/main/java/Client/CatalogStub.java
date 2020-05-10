package Client;

import Middleware.ClientConnection;

import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

/**
 * Catalog Stub
 */
public class CatalogStub {
    private ClientConnection connection;

    // TODO - make a class for the serializers
    private Serializer serializer = new SerializerBuilder().build();

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
        // TODO
        this.connection.sendAndReceive("getCatalog", null);
        return null;
    }

    /**
     * Get product's price
     * @param idProduct Product's identifier
     * @return Product's price
     */
    public float getPrice(String idProduct) {
        // TODO
        this.connection.sendAndReceive("getPrice", this.serializer.encode(idProduct));
        return 0;
    }

    /**
     * Get product's availability
     * @param idProduct
     * @return Product's availability
     */
    public int getAvailability(String idProduct) {
        // TODO
        this.connection.sendAndReceive("getAvailability", this.serializer.encode(idProduct));
        return 0;
    }
}
