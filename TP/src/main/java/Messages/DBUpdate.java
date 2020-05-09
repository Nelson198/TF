package Messages;

/**
 * DB Update Messages.Message
 */
public class DBUpdate implements Message {
    private final String query;

    private final String server; // TODO - should be SpreadGroup, but the serializer complains
    private final String client; // TODO - should be Address, but the serializer complains

    /**
     * Parameterized constructor
     * @param query Query
     * @param server Server to answer from
     * @param client Client.Client to answer to
     */
    public DBUpdate(String query, String client, String server) {
        this.query = query;
        this.client = client;
        this.server = server;
    }

    /**
     * Get DB update message type
     * @return Messages.Message type
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

    /**
     * Get the server to answer
     * @return Server to answer
     */
    public String getServer() {
        return this.server;
    }

    /**
     * Get the client to which to answer
     * @return Client.Client to answer to
     */
    public String getClient() {
        return this.client;
    }
}
