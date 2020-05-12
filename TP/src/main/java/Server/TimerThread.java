package Server;

import Helpers.Serializers;
import Messages.DBUpdate;
import Middleware.ServerConnection;

import io.atomix.utils.serializer.Serializer;

/**
 * Timer Thread
 */
public class TimerThread extends Thread {
    private final String cartID;
    private final ServerConnection connection; // TODO - use ServerConnection here
    private final Serializer serializer = Serializers.serverSerializer;

    // Time frame during which the cart needs to be checked out
    private final int TMAX = 30; // TODO - 30s for the moment

    /**
     * Parameterized constructor
     * @param cartID Cart's identifier
     * @param connection Supermarket's connection
     */
    public TimerThread(String cartID, ServerConnection connection) {
        this.cartID = cartID;
        this.connection = connection;
    }

    /**
     * Run thread
     */
    public void run() {
        try {
            Thread.sleep(this.TMAX * 1000);

            if (this.connection.isPrimary()) {
                DBUpdate dbu = new DBUpdate(this.cartID, null, null, "deleteCart"); // TODO - query
                this.connection.sendCluster(this.serializer.encode(dbu));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
