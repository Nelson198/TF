package Server;

/**
 * Handler Response
 */
public class HandlerRes {
    private boolean sendToCluster;
    private boolean sendToClient;
    private Object info;

    /**
     * Parameterized constructor
     * @param info Info
     * @param sendToCluster sendToCluster
     * @param sendToClient sendToClient
     */
    public HandlerRes(Object info, boolean sendToCluster, boolean sendToClient) {
        this.info = info;
        this.sendToCluster = sendToCluster;
        this.sendToClient = sendToClient;
    }

    /**
     * Get info
     * @return Info
     */
    public Object getInfo() {
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
