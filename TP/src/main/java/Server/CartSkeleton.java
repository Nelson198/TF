package Server;

import java.sql.Connection;
import java.util.HashMap;

/**
 * Cart Skeleton
 */
public class CartSkeleton {
    private final String idCart;
    private final Connection connection;

    /**
     * Parameterized constructor
     * @param id Cart's identifier
     * @param connection Connection to the DB
     */
    public CartSkeleton(String id, Connection connection) {
        this.idCart = id;
        this.connection = connection;
    }

    /**
     * Get the cart's identifier
     * @return Cart's identifier
     */
    public String getIdCart() {
        return this.idCart;
    }

    /**
     * Get the cart's products
     * @return Cart's products
     */
    public List<Product> getProducts() {
        List<Product> res = new ArrayList<>();
        try {
            // Create and execute statement
            Statement stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM cart WHERE idCart=" + this.idCart); // TODO - confirm query

            // Get result from query
            while(rs.next()) {
                Product p = new Product(rs.getString("id"), rs.getString("name"), rs.getString("description"), rs.getFloat("price"), rs.getInt("amount"));
                res.add(p)
            }

            // Clean up
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return res;
    }

    /**
     * Add a product to the cart
     * @param idProduct Product identifier
     */
    public void addProduct(String idProduct, int quantity) {
        try {
            // Create and execute statement
            Statement stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery("UPDATE cart SET ... WHERE idCart=" + this.idCart); // TODO - query

            // Clean up
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    /**// TODO - confirm query
     * Remove a product from the cart
     * @param idProduct Product identifier
     */
    public void removeProduct(String idProduct) {
        try {
            // Create and execute statement
            Statement stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery("DELETE FROM cart WHERE id=" + this.idCart + " AND idProduct=" + idProduct); // TODO - confirm query

            // Clean up
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}
