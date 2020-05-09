package Messages;

public class ProductGet {
    private final String id;

    public ProductGet(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
