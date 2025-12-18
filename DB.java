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

    // Basic table queries
    public static ResultSet getCustomers() {
        return executeQuery("SELECT * FROM customer ORDER BY full_name");
    }

    public static ResultSet getVehicles() {
        return executeQuery("SELECT * FROM vehicle ORDER BY plate_number");
    }

    public static ResultSet getServices() {
        return executeQuery("SELECT * FROM service ORDER BY service_name");
    }

    public static ResultSet getParts() {
        return executeQuery("SELECT * FROM sparepart ORDER BY part_name");
    }

    public static ResultSet getInvoices() {
        return executeQuery("SELECT * FROM salesinvoice ORDER BY invoice_date DESC");
    }

    public static ResultSet getMechanics() {
        return executeQuery("SELECT * FROM mechanic ORDER BY name");
    }

    public static ResultSet getSuppliers() {
        return executeQuery("SELECT * FROM supplier ORDER BY supplier_name");
    }

    // Dashboard queries only
    public static int getTotalCustomers() {
        try {
            ResultSet rs = executeQuery("SELECT COUNT(*) FROM customer");
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) { return 0; }
    }

    public static double getTodayRevenue() {
        try {
            String today = LocalDate.now().toString();
            ResultSet rs = executeQuery(
                    "SELECT IFNULL(SUM(total_amount), 0) FROM salesinvoice WHERE DATE(invoice_date) = '" + today + "'"
            );
            return rs.next() ? rs.getDouble(1) : 0.0;
        } catch (Exception e) { return 0.0; }
    }

    public static int getVehiclesInService() {
        try {
            ResultSet rs = executeQuery("SELECT COUNT(*) FROM vehicle");
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) { return 0; }
    }

    public static int getLowStockParts() {
        try {
            ResultSet rs = executeQuery("SELECT COUNT(*) FROM sparepart WHERE quantity < 10");
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) { return 0; }
    }

    public static ResultSet getRecentInvoices() {
        return executeQuery(
                "SELECT invoice_id, invoice_date, total_amount FROM salesinvoice " +
                        "ORDER BY invoice_date DESC LIMIT 5"
        );
    }
}