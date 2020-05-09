package Messages;

/**
 * Messages.Message Interface
 */
public interface Message {
    String getType();

    class AddProduct implements Message {
        public String getType() {
            return "newCart";
        }
    }
}
