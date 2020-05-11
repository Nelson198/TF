package Messages;

/**
 * Checkout
 */
public class Checkout {
    private final String id;

    /**
     * Parameterized constructor
     * @param id Identifier
     */
    public Checkout(String id) {
        this.id = id;
    }

    /**
     * Get identifier
     * @return Identifier
     */
    public String getId() {
        return this.id;
    }
}
