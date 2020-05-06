import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * Cart Skeleton
 */
public class CartSkeleton {
    private final String id;
    private final Map<String, Integer> products;
    private final Connection connection;

    /**
     * Parameterized constructor
     * @param id Cart's identifier
     */
    public CartSkeleton(String id, Connection connection) {
        this.id = id;
        this.products = new HashMap<>();
        this.connection = connection;
    }

    /**
     * Parameterized constructor
     * @param id Cart's identifier
     * @param products Cart's products
     */
    public CartSkeleton(String id, Map<String, Integer> products, Connection connection) {
        this.id = id;
        this.products = new HashMap<>(products);
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
    public Map<String, Integer> getProducts() {
        return new HashMap<>(this.products);
    }

    /**
     * Get the connection to DB
     * @return DB's connection
     */
    public Connection getConnection() {
        return this.connection;
    }

    /**
     * Add a product to the cart
     * @param idProduct Product identifier
     */
    public void addProduct(String idProduct) {
        if (!this.products.containsKey(idProduct)) {
            this.products.put(idProduct, 1);
        } else {
            int amount = this.products.get(idProduct);
            this.products.put(idProduct, amount + 1);
        }
    }
}
