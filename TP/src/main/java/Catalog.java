import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

/**
 * Catalog
 */
public class Catalog {
    /**
     * Get product's price
     * @param connection Connection
     * @param idProduct Product's identifier
     * @return
     */
    public float getPrice(Connection connection, String idProduct) {
        float price = 0;
        try {
            // Create and execute statement
            Statement stmt = connection.createStatement();
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
}
