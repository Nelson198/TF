import java.util.HashMap;
import java.util.Map;

/**
 * Cart Implementation
 */
public class CartImpl implements Cart {
    private final String id;
    private Map<String, Integer> products;

    /**
     * Parameterized constructor
     * @param id Cart's identifier
     */
    public CartImpl(String id) {
        this.id = id;
        this.products = new HashMap<>();
    }

    /**
     * Parameterized constructor
     * @param id Cart's identifier
     * @param products Cart's products
     */
    public CartImpl(String id, Map<String, Integer> products) {
        this.id = id;
        this.products = new HashMap<>(products);
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
     * Add a product to the cart
     * @param idProduct Product identifier
     */
    public void addProduct(String idProduct) {
        if (!this.products.containsKey(idProduct)) {
            this.products.put(idProduct, 1);
        } else {
            int quantity = this.products.get(idProduct);
            this.products.put(idProduct, quantity + 1);
        }
    }
}
