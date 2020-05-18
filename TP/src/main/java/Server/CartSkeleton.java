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
    private int idCart;
    private final Connection connection;

    /**
     * Parameterized constructor
     * @param connection Connection to the DB
     */
    public CartSkeleton(Connection connection) {
        this.connection = connection;

        try {
            Statement st = this.connection.createStatement();
            st.executeUpdate("INSERT INTO cart (id) VALUES (NULL)");
            st.execute("CALL IDENTITY()");

            ResultSet rs = st.getResultSet();
            rs.next();

            this.idCart = rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Get cart's identifier
     * @return Cart's identifier
     */
    public int getIdCart() {
        return this.idCart;
    }

    /**
     * Get cart's products
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
     * Update product in the cart
     * @param idProduct Product identifier
     */
    public void updateProduct(int idProduct, int amount) {
        try {
            // Create and execute statement
            Statement stmt = this.connection.createStatement();

            StringBuilder sb = new StringBuilder();
            String query = sb.append("SELECT EXISTS (SELECT * FROM cart WHERE id=").append(this.idCart).append(" AND idProduct=").append(idProduct).append(")").toString();
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
