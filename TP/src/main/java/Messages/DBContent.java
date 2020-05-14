package Messages;

import java.util.HashMap;

/**
 * DB Message
 */
public class DBContent implements Message {
    private final byte[] backup;
    private final boolean isFullBackup;

    private final HashMap<String, Integer> lastDBVersion;

    public DBContent(byte[] backup, boolean isFullBackup, HashMap<String, Integer> lastDBVersion) {
        this.backup = backup;
        this.isFullBackup = isFullBackup;
        this.lastDBVersion = lastDBVersion;
    }

    public HashMap<String, Integer> getLastDBVersion() {
        return lastDBVersion;
    }

    public boolean getIsFullBackup() {
        return isFullBackup;
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
