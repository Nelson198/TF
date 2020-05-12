package Helpers;

import Messages.DBUpdate;

import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

/**
 * Serializers
 */
public class Serializers {
    public static Serializer clientSerializer = new SerializerBuilder().addType(CartUpdate.class)
                                                                       .addType(Float.class)
                                                                       .addType(Integer.class)
                                                                       .addType(String.class).build();

    public static Serializer serverSerializer = new SerializerBuilder().addType(DBUpdate.class).build();

    // TODO - add a serializer just for the supermarket
}
