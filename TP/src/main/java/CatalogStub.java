import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

/**
 * Catalog Stub
 */
public class CatalogStub {
    AtomixConnection connection;

    // TODO - make a class for the serializers
    Serializer serializer = new SerializerBuilder().build();

    /**
     * Parameterized constructor
     * @param connection Connection to the cluster
     */
    public CatalogStub(AtomixConnection connection) {
        this.connection = connection;

        this.connection.registerHandler("catalogRes"); // TODO - add the others
    }

    // TODO - add the methods needed
}
