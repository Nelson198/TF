package Server;

import java.sql.Connection;
import java.util.HashMap;

/**
 * Cart Skeleton
 */
public class CartSkeleton {
    private final String id;
    private final Connection connection;

    /**
     * Parameterized constructor
     * @param id Cart's identifier
     * @param connection Connection to the DB
     */
    public CartSkeleton(String id, Connection connection) {
        this.id = id;
        this.connection = connection;
    }

    /**
     * Get the cart's identifier
     * @return Cart's identifier
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get the cart's products
     * @return Cart's products
     */
    public HashMap<String, Integer> getProducts() {
        // TODO - call the DB
        return new HashMap<>();
    }

    /**
     * Add a product to the cart
     * @param idProduct Product identifier
     */
    public void addProduct(String idProduct, int quantity) {
        // TODO - call the DB
    }

    /**
     * Remove a product from the cart
     * @param idProduct Product identifier
     */
    public void removeProduct(String idProduct) {
        // TODO - call the DB
    }
}
