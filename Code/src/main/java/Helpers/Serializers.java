package Helpers;

import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

/**
 * Serializers
 */
public class Serializers {
    public static Serializer clientSerializer = new SerializerBuilder().addType(CartUpdate.class)
                                                                       .addType(Float.class)
                                                                       .addType(Integer.class)
                                                                       .addType(String.class)
                                                                       .addType(Product.class)
                                                                       .build();
}
