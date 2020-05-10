package Server;

import Messages.DBUpdate;
import Serializer.ServerSerializer;

import io.atomix.utils.serializer.Serializer;

/**
 * Timer Thread
 */
public class TimerThread extends Thread {
    private int cartID;
    private Supermarket connection; // TODO - use ServerConnection here
    private ServerSerializer serializer;

    // Time frame during which the cart needs to be checked out
    private final int TMAX = 30; // TODO - 30s for the moment

    /**
     * Parameterized constructor
     * @param cartID Cart's identifier
     * @param connection Supermarket's connection
     */
    public TimerThread(int cartID, Supermarket connection) {
        this.cartID = cartID;
        this.connection = connection;
        this.serializer = new ServerSerializer();
    }

    /**
     * Run thread
     */
    public void run() {
        try {
            Thread.sleep(this.TMAX * 1000);

            Serializer s = this.serializer.getSerializer();
            DBUpdate dbu = new DBUpdate("DELETE FROM cart WHERE id=" + this.cartID, null, null, "deleteCart"); // TODO - query
            this.connection.sendCluster(s.encode(dbu));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
