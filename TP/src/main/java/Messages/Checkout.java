package Messages;

public class Checkout implements Message {
    private final String id;

    public Checkout(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return "checkout";
    }
}
