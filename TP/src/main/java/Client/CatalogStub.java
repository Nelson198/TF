package Client;

import Helpers.CartUpdate;
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
            sb.append("Product nº ").append(p.getId()).append("\n")
              .append("\t--> Name: ").append(p.getName()).append("\n")
              .append("\t--> Description: ").append(p.getDescription()).append("\n")
              .append("\t--> Price: ").append(p.getPrice()).append(" €\n")
              .append("\t--> Amount: ").append(p.getAmount()).append(" unit(s)\n");
        }
        return sb.toString();
    }

    /**
     * Get product from catalog
     * @param productId Product's identifier
     * @return Product
     */
    public Product getProduct(int productId) {
        byte[] res = this.connection.sendAndReceive("getProduct", this.serializer.encode(productId));
        return this.serializer.decode(res);
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

    /**
     * Update the amount of a product
     * @param idProduct Product's identifier
     * @param amount Product's amount
     */
    public void updateAmount(int idProduct, int amount) {
        CartUpdate toSend = new CartUpdate(-1, idProduct, amount);
        this.connection.sendAndReceive("updateAmount", this.serializer.encode(toSend));
    }

    /**
     * Add a new product to the catalog
     * @param p Product
     */
    public void addProduct(Product p) {
        this.connection.sendAndReceive("addProduct", this.serializer.encode(p));
    }

    /**
     * Remove a product from the catalog
     * @param idProduct Product's identifier
     */
    public void removeProduct(int idProduct) {
        this.connection.sendAndReceive("removeProduct", this.serializer.encode(idProduct));
    }

    /**
     * Update a product's information in the catalog
     * @param p Product
     */
    public void updateProduct(Product p) {
        this.connection.sendAndReceive("updateProduct", this.serializer.encode(p));
    }
}
