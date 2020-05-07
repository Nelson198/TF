public class DBUpdateMessage implements Message {
    private String query;

    /**
     * Get DB update message type
     * @return Message type
     */
    public String getType() {
        return "dbUpdate";
    }

    /**
     * Get query
     * @return Query
     */
    public String getQuery() {
        return this.query;
    }
}
