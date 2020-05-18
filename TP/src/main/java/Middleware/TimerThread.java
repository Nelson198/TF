package Middleware;

import Helpers.Serializers;
import Messages.DBUpdate;

import io.atomix.utils.serializer.Serializer;

/**
 * Timer Thread
 */
public class TimerThread extends Thread {
    private final byte[] message;
    private final String type;
    private final int time;

    private final ServerConnection connection;
    private final Serializer serializer = Serializers.clientSerializer;

    /**
     * Parameterized constructor
     * @param connection Supermarket's connection
     */
    public TimerThread(byte[] message, String type, int time, ServerConnection connection) {
        this.message = message;
        this.type = type;
        this.time = time;
        this.connection = connection;
    }

    /**
     * Run thread
     */
    public void run() {
        try {
            Thread.sleep(this.time * 1000);

            if (this.connection.isPrimary()) {
                DBUpdate dbu = new DBUpdate(this.message, null, null, this.type);
                this.connection.sendCluster(this.serializer.encode(dbu));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
