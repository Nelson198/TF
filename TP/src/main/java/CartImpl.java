import java.util.HashMap;
import java.util.Map;

/**
 * Cart Implementation
 */
public class CartImpl implements Cart {
    private String id;
    private Map<String, Integer> products;

    public CartImpl(String id) {
        this.id = id;
        this.products = new HashMap<>();
    }

    public CartImpl(String id, Map<String, Integer> products) {
        this.id = id;
        this.products = new HashMap<>(products);
    }

    public String getId() {
        return this.id;
    }

    public Map<String, Integer> getProducts() {
        return new HashMap<>(this.products);
    }

    public void addProduct(String idProduct) {
        if (!this.products.containsKey(idProduct)) {
            this.products.put(idProduct, 1);
        } else {
            int quantity = this.products.get(idProduct);
            this.products.put(idProduct, quantity + 1);
        }
    }
}
