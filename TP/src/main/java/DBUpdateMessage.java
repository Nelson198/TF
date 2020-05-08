/**
 * DB Update Message
 */
public class DBUpdateMessage implements Message {
    private final int id;
    private String query;
    private String serverToAnswer;

    /**
     * Parameterized constructor
     * @param query Query
     * @param id Identifier
     * @param serverToAnswer Server to answer
     */
    public DBUpdateMessage(int id, String query, String serverToAnswer) {
        this.id = id;
        this.query = query;
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
     * Get DB update message identifier
     * @return Message identifier
     */
    public int getId() {
        return this.id;
    }

    /**
     * Get query
     * @return Query
     */
    public String getQuery() {
        return this.query;
    }

    /**
     * Get the server to answer
     * @return Server to answer
     */
    public String getServerToAnswer() {
        return this.serverToAnswer;
    }
}
