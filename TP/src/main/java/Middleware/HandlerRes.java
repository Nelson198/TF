package Middleware;

/**
 * Handler Response
 */
public class HandlerRes {
    private final byte[] info;
    private final boolean sendToCluster;
    private final boolean sendToClient;

    /**
     * Parameterized constructor
     * @param info Info
     * @param sendToCluster sendToCluster
     * @param sendToClient sendToClient
     */
    public HandlerRes(byte[] info, boolean sendToCluster, boolean sendToClient) {
        this.info = info;
        this.sendToCluster = sendToCluster;
        this.sendToClient = sendToClient;
    }

    /**
     * Get info
     * @return Info
     */
    public byte[] getInfo() {
        return this.info;
    }

    /**
     * Get sendToCluster
     * @return sendToCluster
     */
    public boolean getSendToCluster() {
        return this.sendToCluster;
    }

    /**
     * Get sendToClient
     * @return sendToClient
     */
    public boolean getSendToClient() {
        return this.sendToClient;
    }
}
