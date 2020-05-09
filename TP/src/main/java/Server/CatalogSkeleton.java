package Server;

import Types.Product;

import java.sql.*;
import java.util.ArrayList;

/**
 * Catalog Skeleton
 */
public class CatalogSkeleton {
    private final Connection connection;

    /**
     * Parameterized constructor
     * @param connection DB's connection
     */
    public CatalogSkeleton(Connection connection) {
        this.connection = connection;
    }

    /**
     * Get catalog information
     * @return Catalog information
     */
    public ArrayList<Product> getCatalog() {
        ArrayList<Product> res = new ArrayList<>();
        try {
            // Create and execute statement
            Statement stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM product");

            // Get result from query
            while(rs.next()) {
                Product p = new Product(rs.getString("id"), rs.getString("name"), rs.getString("description"), rs.getFloat("price"), rs.getInt("amount"));
                res.add(p);
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
     * Get product's price
     * @param idProduct Product's identifier
     * @return Product's price
     */
    public float getPrice(String idProduct) {
        float price = 0;
        try {
            // Create and execute statement
            Statement stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT price FROM product WHERE id=" + idProduct);

            // Get result from query
            price = rs.getFloat("price");

            // Clean up
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return price;
    }

    /**
     * Get product's availability
     * @param idProduct Product's identifier
     * @return Product's availability
     */
    public int getAvailability(String idProduct) {
        int availability = 0;
        try {
            // Create and execute statement
            Statement stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT amount FROM product WHERE id=" + idProduct);

            // Get result from query
            availability = rs.getInt("amount");

            // Clean up
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return availability;
    }
}
