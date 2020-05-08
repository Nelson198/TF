/**
 * DB Message
 */
public class DBMessage implements Message {
    // ..... contents of the database

    /**
     * Get DB's message type
     * @return DB's message type
     */
    public String getType() {
        return "db";
    }
}
