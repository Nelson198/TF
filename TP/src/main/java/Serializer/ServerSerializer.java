package Serializer;

import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

/**
 * Client Serializer
 */
public class ServerSerializer {
    private Serializer serializer;

    /**
     * Constructor
     */
    public ServerSerializer() {
        this.serializer = new SerializerBuilder().build();
    }

    /**
     * Get server's serializer
     * @return Serializer
     */
    public Serializer getSerializer() {
        return this.serializer;
    }
}
