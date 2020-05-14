package Messages;

/**
 * DB Update Message
 */
public class DBUpdate implements Message {
    private final Object updateInfo;

    private final String client; // TODO - should be Address, but the serializer complains
    private final String server; // TODO - should be SpreadGroup, but the serializer complains

    private final String secondaryType;

    /**
     * Parameterized constructor
     * @param updateInfo Information about the update
     * @param server Server to answer from
     * @param client Client to answer to
     */
    public DBUpdate(Object updateInfo, String client, String server, String secondaryType) {
        this.updateInfo = updateInfo;
        this.client = client;
        this.server = server;
        this.secondaryType = secondaryType;
    }

    /**
     * Get updateInfo
     * @return UpdateInfo
     */
    public Object getUpdateInfo() {
        return this.updateInfo;
    }

    /**
     * Get the client to which to answer
     * @return Client to answer to
     */
    public String getClient() {
        return this.client;
    }

    /**
     * Get the server to answer
     * @return Server to answer
     */
    public String getServer() {
        return this.server;
    }

    /**
     * Get DB update message secondary type
     * @return Message secondary type
     */
    public String getSecondaryType() {
        return this.secondaryType;
    }

    /**
     * Get DB update message type
     * @return Message type
     */
    public String getType() {
        return "dbUpdate";
    }
}
