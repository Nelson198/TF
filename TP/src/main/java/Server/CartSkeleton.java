package Server;

import Helpers.Product;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import java.util.ArrayList;

/**
 * Cart Skeleton
 */
public class CartSkeleton {
    private String idCart;
    private final Connection connection;

    /**
     * Parameterized constructor
     * @param connection Connection to the DB
     */
    public CartSkeleton(Connection connection) {
        this.connection = connection;

        try {
            Statement st = this.connection.createStatement();
            int res = st.executeUpdate("INSERT INTO cart () VALUES ()"); // TODO : put the generated key in res

            this.idCart = Integer.toString(res);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
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
    public ArrayList<Product> getProducts() {
        ArrayList<Helpers.Product> res = new ArrayList<>();
        try {
            // Create and execute statement
            Statement stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM cart WHERE idCart=" + this.idCart); // TODO - confirm query

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

    /*
    StringBuilder sb = new StringBuilder();
    String query = sb.append("IF EXISTS (SELECT * FROM cart WHERE id=").append(cu.getIdProduct()).append(") THEN\n")
                             .append("UPDATE cart SET amount = amount + 1").append("WHERE id=").append(cu.getIdProduct()).append(";\n")
                             .append("ELSE\n")
                             .append("INSERT INTO cart VALUES(").append(cu.getIdProduct()).append(", ").append(cu.getAmount()).append(");\n")
                             .append("END IF;")
                             .toString();
    sb.setLength(0);
     */

    /* TODO - confirm query
     * Remove a product from the cart
     * @param idProduct Product identifier
     */
    public void removeProduct(String idProduct, int quantity) {
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

    public void delete() {
        try {
            // Create and execute statement
            Statement stmt = this.connection.createStatement();
            int res = stmt.executeUpdate("DELETE FROM cart WHERE id=" + this.idCart); // TODO - confirm query

            // Clean up
            stmt.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public boolean checkout() {
        try {
            // Create and execute statement
            Statement stmt = this.connection.createStatement();
            int res = stmt.executeUpdate("UPDATE cart SET ... WHERE id=" + this.idCart); // TODO - confirm query

            // Clean up
            stmt.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return true; // TODO - return if the checkout was successful
    }
}
