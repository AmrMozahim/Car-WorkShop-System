import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;

public class DB {

    private static Connection connection = null;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                String url = "jdbc:mysql://localhost:3306/car_workship_db";
                String user = "root";
                String pass = "Amr1135@mr";
                connection = DriverManager.getConnection(url, user, pass);
                System.out.println("✓ Database connected!");
            }
        } catch (Exception e) {
            System.out.println("✗ Database error: " + e.getMessage());
        }
        return connection;
    }

    public static ResultSet executeQuery(String sql) {
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            return stmt.executeQuery(sql);
        } catch (Exception e) {
            System.out.println("Query error: " + e.getMessage());
            return null;
        }
    }

    public static int executeUpdate(String sql) {
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            return stmt.executeUpdate(sql);
        } catch (Exception e) {
            System.out.println("Update error: " + e.getMessage());
            return 0;
        }
    }

    // Dashboard queries
    public static int getTotalCustomers() {
        try {
            ResultSet rs = executeQuery("SELECT COUNT(*) FROM customer");
            return (rs != null && rs.next()) ? rs.getInt(1) : 0;
        } catch (Exception e) { return 0; }
    }

    public static double getTodayRevenue() {
        try {
            ResultSet rs = executeQuery(
                    "SELECT IFNULL(SUM(total_amount), 0) FROM salesinvoice WHERE DATE(invoice_date) = CURDATE()"
            );
            return (rs != null && rs.next()) ? rs.getDouble(1) : 0.0;
        } catch (Exception e) { return 0.0; }
    }

    public static int getVehiclesInService() {
        try {
            ResultSet rs = executeQuery("SELECT COUNT(*) FROM vehicle");
            return (rs != null && rs.next()) ? rs.getInt(1) : 0;
        } catch (Exception e) { return 0; }
    }

    public static int getLowStockParts() {
        try {
            ResultSet rs = executeQuery("SELECT COUNT(*) FROM sparepart WHERE quantity < 10");
            return (rs != null && rs.next()) ? rs.getInt(1) : 0;
        } catch (Exception e) { return 0; }
    }

    // Helper method for last insert ID
    public static int getLastInsertId() {
        try {
            ResultSet rs = executeQuery("SELECT LAST_INSERT_ID()");
            return (rs != null && rs.next()) ? rs.getInt(1) : 0;
        } catch (Exception e) { return 0; }
    }
}