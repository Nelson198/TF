package Server;

import Helpers.Product;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

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
    public List<Product> getCatalog() {
        List<Product> res = new ArrayList<>();
        try {
            // Create and execute statement
            Statement stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM product");

            // Get result from query
            while(rs.next()) {
                Product p = new Product(rs.getInt("id"), rs.getString("name"), rs.getString("description"), rs.getFloat("price"), rs.getInt("amount"));
                res.add(p);
            }

            // Clean up
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return res;
    }

    /**
     * Get product info
     * @param idProduct Product's identifier
     * @return Product info
     */
    public Product getProduct(int idProduct) {
        Product res = null;
        try {
            // Create and execute statement
            Statement stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM product WHERE id = " + idProduct);

            // Get result from query
            if (rs.next())
                res = new Product(rs.getInt("id"), rs.getString("name"), rs.getString("description"), rs.getFloat("price"), rs.getInt("amount"));

            // Clean up
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return res;
    }

    /**
     * Get product's price
     * @param idProduct Product's identifier
     * @return Product's price
     */
    public float getPrice(int idProduct) {
        float price = -1;
        try {
            // Create and execute statement
            Statement stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT price FROM product WHERE id = " + idProduct);

            // Get result from query
            if (rs.next())
                price = rs.getFloat("price");

            // Clean up
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return price;
    }

    /**
     * Get product's amount
     * @param idProduct Product's identifier
     * @return Product's amount
     */
    public int getAmount(int idProduct) {
        int amount = -1;
        try {
            // Create and execute statement
            Statement stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT amount FROM product WHERE id = " + idProduct);

            // Get result from query
            if (rs.next())
                amount = rs.getInt("amount");

            // Clean up
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return amount;
    }

    /**
     * Update the amount of a product
     * @param productId Product's identifier
     * @param amount Product's amount
     */
    public void updateAmount(int productId, int amount) {
        try {
            // Create and execute statement
            Statement stmt = this.connection.createStatement();

            StringBuilder sb = new StringBuilder();
            String query = sb.append("SELECT COUNT(*) FROM product WHERE id=").append(productId).toString();
            sb.setLength(0);

            ResultSet rs = stmt.executeQuery(query);
            rs.next();

            // Get result from query
            boolean exists = rs.getInt(1) == 1;
            if (exists) {
                query = sb.append("UPDATE product SET amount = amount + ").append(amount)
                          .append(" WHERE id = ").append(productId)
                          .toString();
                sb.setLength(0);

                stmt.executeUpdate(query);
            }

            // Clean up
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add a product to the catalog
     * @param p Product
     */
    public void addProduct(Product p) {
        try {
            // Create and execute statement
            Statement stmt = this.connection.createStatement();

            StringBuilder sb = new StringBuilder();
            String query = sb.append("SELECT COUNT(*) FROM product WHERE id = ").append(p.getId()).toString();
            sb.setLength(0);

            ResultSet rs = stmt.executeQuery(query);
            rs.next();
            boolean exists = rs.getInt(1) == 1;
            if (exists) {
                query = sb.append("UPDATE product SET amount = amount + ").append(p.getAmount())
                          .append(" WHERE id=").append(p.getId())
                          .toString();
            } else {
                query = sb.append("INSERT INTO product (name, description, price, amount) VALUES('")
                          .append(p.getName()).append("', '")
                          .append(p.getDescription()).append("', ")
                          .append(p.getPrice()).append(", ")
                          .append(p.getAmount()).append(")")
                          .toString();
            }
            sb.setLength(0);
            stmt.executeUpdate(query);

            // Clean up
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Remove a product from the catalog
     * @param productId Product's identifier
     */
    public void removeProduct(int productId) {
        try {
            // Create and execute statement
            Statement stmt = this.connection.createStatement();

            StringBuilder sb = new StringBuilder();
            String query = sb.append("DELETE FROM product WHERE id = ").append(productId).toString();
            sb.setLength(0);

            stmt.executeUpdate(query);

            // Clean up
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update a product's information in the catalog
     * @param p Product
     */
    public void updateProduct(Product p) {
        try {
            // Create and execute statement
            Statement stmt = this.connection.createStatement();

            StringBuilder sb = new StringBuilder();
            String query = sb.append("UPDATE product SET name = '").append(p.getName())
                             .append("', description = '").append(p.getDescription())
                             .append("', price = ").append(p.getPrice())
                             .append(" WHERE id = ").append(p.getId())
                             .toString();
            sb.setLength(0);

            stmt.executeUpdate(query);

            // Clean up
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}