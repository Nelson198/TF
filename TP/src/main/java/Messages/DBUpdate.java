package Messages;

/**
 * DB Update Message
 */
public class DBUpdate implements Message {
    private final String query; // TODO - see if this needs to be an array of queries (for checkout, for example - remove cart and update catalog)

    private final String server; // TODO - should be SpreadGroup, but the serializer complains
    private final String client; // TODO - should be Address, but the serializer complains

    private final String secondaryType;

    /**
     * Parameterized constructor
     * @param query Query
     * @param server Server to answer from
     * @param client Client to answer to
     */
    public DBUpdate(String query, String client, String server, String secondaryType) {
        this.query = query;
        this.client = client;
        this.server = server;
        this.secondaryType = secondaryType;
    }

    /**
     * Get DB update message type
     * @return Message type
     */
    public String getType() {
        return "dbUpdate";
    }

    /**
     * Get DB update message secondary type
     * @return Message secondary type
     */
    public String getSecondaryType() {
        return this.secondaryType;
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
     * @return Client to answer to
     */
    public String getClient() {
        return this.client;
    }
}
