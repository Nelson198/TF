package Messages;

public class Checkout implements Message {
    public String getType() {
        return "checkout";
    }
}
