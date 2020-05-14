package Messages;

/**
 * DB Message
 */
public class DBContent implements Message {
    private final byte[] backup;

    public DBContent(byte[] backup) {
        this.backup = backup;
    }

    public byte[] getBackup() {
        return backup;
    }

    /**
     * Get DB's message type
     * @return DB's message type
     */
    public String getType() {
        return "db";
    }
}
