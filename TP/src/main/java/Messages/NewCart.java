package Messages;

public class NewCart implements Message {
    public String getType() {
        return "newCart";
    }
}
