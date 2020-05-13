package Middleware;

import Helpers.Serializers;
import Messages.DBUpdate;

import io.atomix.utils.serializer.Serializer;

/**
 * Timer Thread
 */
public class TimerThread extends Thread {
    private final Object message;
    private final String type;
    private final int time;

    private final ServerConnection connection;
    private final Serializer serializer = Serializers.serverSerializer;

    /**
     * Parameterized constructor
     * @param connection Supermarket's connection
     */
    public TimerThread(Object message, String type, int time, ServerConnection connection) {
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
        }
    }
}
