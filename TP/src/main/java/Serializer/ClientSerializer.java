package Serializer;

import Messages.CartUpdate;

import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

/**
 * Client Serializer
 */
public class ClientSerializer {
    private Serializer serializer;

    /**
     * Constructor
     */
    public ClientSerializer() {
        this.serializer = new SerializerBuilder().addType(CartUpdate.class)
                                                 .addType(Float.class)
                                                 .addType(Integer.class)
                                                 .addType(String.class).build();
    }

    /**
     * Get client's serializer
     * @return Serializer
     */
    public Serializer getSerializer() {
        return this.serializer;
    }
}
