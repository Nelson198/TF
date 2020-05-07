public class DBUpdateMessage implements Message {
    private String query;
    private int id;
    private String serverToAnswer;

    public DBUpdateMessage(String query, int id, String serverToAnswer) {
        this.query = query;
        this.id = id;
        this.serverToAnswer = serverToAnswer;
    }

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

    public int getId() {
        return id;
    }

    public String getServerToAnswer() {
        return serverToAnswer;
    }
}
