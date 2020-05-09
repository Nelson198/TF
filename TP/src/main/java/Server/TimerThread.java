package Server;

import Messages.DBUpdate;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

public class TimerThread extends Thread {
    int cartID;
    Supermarket connection; // TODO - use ServerConnection here

    // TODO - make a class for the serializers
    Serializer serializer = new SerializerBuilder().build();

    // Time frame during which the cart needs to be checked out
    final int TMAX = 30; // TODO - 30s for the moment

    public TimerThread(int cartID, Supermarket connection) {
        this.cartID = cartID;
        this.connection = connection;
    }

    public void run() {
        try {
            Thread.sleep(TMAX * 1000);

            DBUpdate dbu = new DBUpdate("DELETE FROM cart WHERE id=" +  cartID, null, null, "deleteCart"); // TODO - query
            this.connection.sendCluster(serializer.encode(dbu));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
