package Server;

public class HandlerRes {
    boolean sendToCluster;
    boolean sendToClient;
    Object info;

    public HandlerRes(Object info, boolean sendToCluster, boolean sendToClient) {
        this.info = info;
        this.sendToCluster = sendToCluster;
        this.sendToClient = sendToClient;
    }

    public Object getInfo() {
        return info;
    }

    public boolean getSendToCluster() {
        return sendToCluster;
    }

    public boolean getSendToClient() {
        return sendToClient;
    }
}
