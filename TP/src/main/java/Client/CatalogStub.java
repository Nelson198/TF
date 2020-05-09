package Client;

import Middleware.ClientConnection;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

/**
 * Catalog Stub
 */
public class CatalogStub {
    ClientConnection connection;

    // TODO - make a class for the serializers
    Serializer serializer = new SerializerBuilder().build();

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
        return null;
    }

    /**
     * Get product's price
     * @param idProduct Product's identifier
     * @return Product's price
     */
    public float getPrice(String idProduct) {
        // TODO
        return 0;
    }

    /**
     * Get product's availability
     * @param idProduct
     * @return Product's availability
     */
    public int getAvailability(String idProduct) {
        // TODO
        return 0;
    }
}
