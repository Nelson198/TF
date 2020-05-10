package Client;

import Middleware.ClientConnection;
import Serializer.ClientSerializer;

import io.atomix.utils.serializer.Serializer;

/**
 * Catalog Stub
 */
public class CatalogStub {
    private ClientConnection connection;
    private ClientSerializer serializer;

    /**
     * Parameterized constructor
     * @param connection Connection to the cluster
     */
    public CatalogStub(ClientConnection connection) {
        this.connection = connection;
        this.serializer = new ClientSerializer();

        this.connection.registerHandler("catalogRes"); // TODO - add the others
    }

    /**
     * Get catalog information
     * @return Catalog information
     */
    public String getCatalog() {
        Serializer s = this.serializer.getSerializer();
        byte[] res = this.connection.sendAndReceive("getCatalog", null);
        String catalog = s.decode(res);
        return catalog;
    }

    /**
     * Get product's price
     * @param idProduct Product's identifier
     * @return Product's price
     */
    public float getPrice(String idProduct) {
        Serializer s = this.serializer.getSerializer();
        byte[] res = this.connection.sendAndReceive("getPrice", s.encode(idProduct));
        float price = s.decode(res);
        return price;
    }

    /**
     * Get product's availability
     * @param idProduct Product's identifier
     * @return Product's availability
     */
    public int getAvailability(String idProduct) {
        Serializer s = this.serializer.getSerializer();
        byte[] res = this.connection.sendAndReceive("getAvailability", s.encode(idProduct));
        int availability = s.decode(res);
        return availability;
    }
}
