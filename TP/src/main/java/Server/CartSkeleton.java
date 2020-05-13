package Server;

import Helpers.Product;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import java.util.List;
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
            e.printStackTrace();
            System.exit(1);
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
    public List<Product> getProducts() {
        List<Product> res = new ArrayList<>();
        try {
            // Create and execute statement
            Statement stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM cart WHERE id=" + this.idCart); // TODO - confirm query

            // Get result from query
            while(rs.next()) {
                Product p = new Product(rs.getString("id"), rs.getString("name"), rs.getString("description"), rs.getFloat("price"), rs.getInt("amount"));
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
     * Update product in the cart
     * @param idProduct Product identifier
     */
    public void updateProduct(String idProduct, int amount) {
        try {
            // Create and execute statement
            Statement stmt = this.connection.createStatement();

            StringBuilder sb = new StringBuilder();
            String query = sb.append("EXISTS (SELECT * FROM cart WHERE id=").append(this.idCart).append(" AND idProduct=").append(idProduct).append(")").toString();
            sb.setLength(0);

            ResultSet rs = stmt.executeQuery(query);
            boolean exists = rs.getBoolean(1);

            if (exists && amount < 0) { // Remove product
                query = sb.append("SELECT amount FROM cart WHERE id=").append(this.idCart).append(" AND idProduct=").append(idProduct).append(")").toString();
                sb.setLength(0);

                rs = stmt.executeQuery(query);
                int cartAmount = rs.getInt("amount");

                if (cartAmount > Math.abs(amount)) {
                    query = sb.append("UPDATE cart SET amount = amount - ").append(amount).append(" WHERE id=").append(this.idCart).append(" AND idProduct=").append(idProduct).toString();
                } else {
                    query = sb.append("DELETE FROM cart WHERE id=").append(this.idCart).append(" AND idProduct=").append(idProduct).toString();
                }
                sb.setLength(0);
                stmt.executeUpdate(query);
            } else if (exists && amount > 0) { // Add product
                query = sb.append("UPDATE cart SET amount = amount + ").append(amount).append(" WHERE id=").append(this.idCart).append(" AND idProduct=").append(idProduct).toString();
                sb.setLength(0);
                stmt.executeUpdate(query);
            } else if (!exists && amount > 0) { // Add product
                query = sb.append("INSERT INTO cart VALUES(").append(idProduct).append(", ").append(amount).append(")").toString();
                sb.setLength(0);
                stmt.executeUpdate(query);
            }

            // Clean up
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Delete cart
     */
    public void delete() {
        try {
            // Create and execute statement
            Statement stmt = this.connection.createStatement();
            stmt.executeUpdate("DELETE FROM cart WHERE id=" + this.idCart);

            // Clean up
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Checkout cart
     * @return Status
     */
    public boolean checkout() {
        try {
            // Create and execute statement
            Statement stmt = this.connection.createStatement();
            stmt.executeUpdate("UPDATE cart SET ... WHERE id=" + this.idCart); // TODO - confirm query

            // Clean up
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }

        return true; // TODO - return if the checkout was successful
    }
}
