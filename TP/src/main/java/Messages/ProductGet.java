package Messages;

/**
 * Product Get
 */
public class ProductGet {
    private final String id;

    /**
     * Parameterized constructor
     * @param id Product's identifier
     */
    public ProductGet(String id) {
        this.id = id;
    }

    /**
     * Get product's identifier
     * @return Product's identifier
     */
    public String getId() {
        return this.id;
    }
}
