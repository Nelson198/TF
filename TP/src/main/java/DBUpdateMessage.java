public class DBUpdateMessage implements Message {
    public String query;

    public String getType() {
        return "dbUpdate";
    }
}
