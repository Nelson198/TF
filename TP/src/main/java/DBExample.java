import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBExample {
    public static void getCustomers() {
        Connection conn = null;
        String db = "jdbc:hsqldb:file:testDB";
        String user = "SA";
        String password = "";

        try {
            // Create database connection
            conn = DriverManager.getConnection(db, user, password);

            // Create and execute statement
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select FIRSTNAME, LASTNAME from CUSTOMER");

            // Loop through the data and print all artist names
            while(rs.next()) {
                System.out.println("Customer Name: " + rs.getString("FIRSTNAME") + " " + rs.getString("LASTNAME"));
            }

            // Clean up
            rs.close();
            stmt.close();
        }
        catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        finally {
            try {
                // Close connection
                if (conn != null)
                    conn.close();
            }
            catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        // Retrieve all customers
        getCustomers();
    }
}
