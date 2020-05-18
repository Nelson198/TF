package Client;

import Helpers.Product;
import Helpers.Serializers;
import Middleware.ClientConnection;

import io.atomix.utils.serializer.Serializer;

import java.util.List;

/**
 * Catalog Stub
 */
public class CatalogStub {
    private final ClientConnection connection;
    private final Serializer serializer = Serializers.clientSerializer;

    /**
     * Parameterized constructor
     * @param connection Connection to the cluster
     */
    public CatalogStub(ClientConnection connection) {
        this.connection = connection;
    }

    /**
     * Get catalog information
     * @return Catalog information
     */
    public String getCatalog() {
        byte[] res = this.connection.sendAndReceive("getCatalog", this.serializer.encode(null));
        List<Product> catalog = this.serializer.decode(res);

        StringBuilder sb = new StringBuilder("Catalog:\n");
        for (Product p : catalog) {
            sb.append("\tProduct nº ").append(p.getId()).append("\n")
              .append("\t\t--> Name: ").append(p.getName()).append("\n")
              .append("\t\t--> Description: ").append(p.getDescription()).append("\n")
              .append("\t\t--> Price: ").append(p.getPrice()).append(" €\n")
              .append("\t\t--> Amount: ").append(p.getAmount()).append(" unit(s)\n");
        }
        return sb.toString();
    }

    /**
     * Get product's price
     * @param idProduct Product's identifier
     * @return Product's price
     */
    public float getPrice(String idProduct) {
        byte[] res = this.connection.sendAndReceive("getPrice", this.serializer.encode(idProduct));
        return this.serializer.decode(res);
    }

    /**
     * Get product's amount
     * @param idProduct Product's identifier
     * @return Product's amount
     */
    public int getAmount(String idProduct) {
        byte[] res = this.connection.sendAndReceive("getAmount", this.serializer.encode(idProduct));
        return this.serializer.decode(res);
    }

    public void updateAmount(String idProduct, int amount) {
        CartUpdate toSend = new CartUpdate(null, idProduct, amount);
        this.connection.sendAndReceive("updateAmount", this.serializer.encode(toSend));
    }

    public void addNewProduct(Product newProduct) {
        this.connection.sendAndReceive("newProduct", this.serializer.encode(newProduct));
    }

    public void removeProduct(String idProduct) {
        this.connection.sendAndReceive("removeProduct", this.serializer.encode(idProduct));
    }

    public void updateProduct(Product p) {
        this.connection.sendAndReceive("updateProduct", this.serializer.encode(p));
    }

    public Product getProduct(productId) {
        byte[] res = this.connection.sendAndReceive("getProduct", this.serializer.encode(idProduct));
        return this.serializer.decode(res);
    }
}
