package Messages;

import java.util.HashSet;

/**
 * DB Message
 */
public class DBContent implements Message {
    private final byte[] backup;
    private final boolean isFullBackup;
    private final HashSet<String> latestDBServers;

    /**
     * Parameterized constructor
     * @param backup Backup
     * @param isFullBackup isFullBackup
     * @param latestDBServers latestDBServers
     */
    public DBContent(byte[] backup, boolean isFullBackup, HashSet<String> latestDBServers) {
        this.backup = backup;
        this.isFullBackup = isFullBackup;
        this.latestDBServers = latestDBServers;
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
     * Get latestDBServers
     * @return latestDBServers
     */
    public HashSet<String> getLatestDBServers() {
        return this.latestDBServers;
    }

    /**
     * Get DB's message type
     * @return DB's message type
     */
    public String getType() {
        return "db";
    }
}
