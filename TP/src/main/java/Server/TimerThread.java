package Server;

import Messages.DBUpdate;

import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

/**
 * Timer Thread
 */
public class TimerThread extends Thread {
    private int cartID;
    private Supermarket connection; // TODO - use ServerConnection here

    // TODO - make a class for the serializers
    private Serializer serializer = new SerializerBuilder().build();

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
    }

    /**
     * Run thread
     */
    public void run() {
        try {
            Thread.sleep(TMAX * 1000);

            DBUpdate dbu = new DBUpdate("DELETE FROM cart WHERE id=" + this.cartID, null, null, "deleteCart"); // TODO - query
            this.connection.sendCluster(this.serializer.encode(dbu));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
