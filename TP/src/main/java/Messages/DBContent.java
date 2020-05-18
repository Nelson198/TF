package Messages;

import java.util.Map;

/**
 * DB Message
 */
public class DBContent implements Message {
    private final byte[] backup;
    private final boolean isFullBackup;
    private final Map<String, Integer> lastDBVersion;

    /**
     * Parameterized constructor
     * @param backup Backup
     * @param isFullBackup isFullBackup
     * @param lastDBVersion lastDBVersion
     */
    public DBContent(byte[] backup, boolean isFullBackup, Map<String, Integer> lastDBVersion) {
        this.backup = backup;
        this.isFullBackup = isFullBackup;
        this.lastDBVersion = lastDBVersion;
    }

    /**
     * Get backup
     * @return backup
     */
    public byte[] getBackup() {
        return this.backup;
    }

    /**
     * Get isFullBackup
     * @return isFullBackup
     */
    public boolean getIsFullBackup() {
        return this.isFullBackup;
    }

    /**
     * Get lastDBVersion
     * @return lastDBVersion
     */
    public Map<String, Integer> getLastDBVersion() {
        return this.lastDBVersion;
    }

    /**
     * Get DB's message type
     * @return DB's message type
     */
    public String getType() {
        return "db";
    }
}
