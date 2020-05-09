package Messages;

/**
 * Checkout
 */
public class Checkout implements Message {
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

    /**
     * Get "checkout" message type
     * @return Message type
     */
    public String getType() {
        return "checkout";
    }
}
