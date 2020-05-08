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

    public String getCatalog() {
        // TODO
        return null;
    }

    public double getPrice(String idProduct) {
        // TODO
        return 0.0;
    }

    public int getAvailability(String idProduct) {
        // TODO
        return 0;
    }
}
