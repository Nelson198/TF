import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

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
    public String getCatalog() {
        String catalog = "";
        try {
            // Create and execute statement
            Statement stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM product");

            // Get result from query
            StringBuilder sb = new StringBuilder("Product ID\tName\tDescription\tPrice\tAmount");
            while(rs.next()) {
                sb.append(rs.getString("id"))
                  .append(rs.getString("name"))
                  .append(rs.getString("description"))
                  .append(rs.getDouble("price"))
                  .append(rs.getInt("amount"))
                  .append("\n");
            }
            catalog = sb.toString();

            // Clean up
            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return catalog;
    }

    /**
     * Get product's price
     * @param idProduct Product's identifier
     * @return Product's price
     */
    public double getPrice(String idProduct) {
        double price = 0;
        try {
            // Create and execute statement
            Statement stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT price FROM product WHERE id=" + idProduct);

            // Get result from query
            price = rs.getDouble("price");

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
    public boolean getAvailability(String idProduct) {
        boolean availability = false;
        try {
            // Create and execute statement
            Statement stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT amount FROM product WHERE id=" + idProduct);

            // Get result from query
            availability = rs.getInt("amount") > 0;

            // Clean up
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return availability;
    }
}
