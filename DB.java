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
                System.out.println("Database connection successful");
            }
        } catch (Exception e) {
            System.out.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }

    public static ResultSet executeQuery(String sql) {
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            return stmt.executeQuery(sql);
        } catch (Exception e) {
            System.out.println("Query execution error: " + sql);
            e.printStackTrace();
            return null;
        }
    }

    public static int executeUpdate(String sql) {
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            int result = stmt.executeUpdate(sql);
            System.out.println("Operation executed: " + sql);
            return result;
        } catch (Exception e) {
            System.out.println("Update execution error: " + sql);
            e.printStackTrace();
            return 0;
        }
    }

    public static ResultSet getCustomers() {
        return executeQuery("SELECT * FROM customer ORDER BY full_name");
    }

    public static ResultSet getInvoices() {
        String sql = "SELECT s.*, c.full_name FROM salesinvoice s " +
                "JOIN customer c ON s.customer_id = c.customer_id " +
                "ORDER BY s.invoice_date DESC";
        return executeQuery(sql);
    }

    public static ResultSet getServices() {
        return executeQuery("SELECT * FROM service ORDER BY service_name");
    }

    public static ResultSet getParts() {
        String sql = "SELECT p.*, s.supplier_name FROM sparepart p " +
                "LEFT JOIN supplier s ON p.supplier_id = s.supplier_id " +
                "ORDER BY p.part_name";
        return executeQuery(sql);
    }

    public static ResultSet getVehicles() {
        String sql = "SELECT v.*, c.full_name FROM vehicle v " +
                "JOIN customer c ON v.customer_id = c.customer_id " +
                "ORDER BY v.plate_number";
        return executeQuery(sql);
    }

    public static ResultSet getMechanics() {
        return executeQuery("SELECT * FROM mechanic ORDER BY name");
    }

    public static ResultSet getSuppliers() {
        return executeQuery("SELECT * FROM supplier ORDER BY supplier_name");
    }

    public static int getTotalCustomers() {
        try {
            ResultSet rs = executeQuery("SELECT COUNT(*) as total FROM customer");
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getTodayInvoicesCount() {
        try {
            String today = LocalDate.now().toString();
            ResultSet rs = executeQuery("SELECT COUNT(*) as total FROM salesinvoice WHERE DATE(invoice_date) = '" + today + "'");
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static double getTodayRevenue() {
        try {
            String today = LocalDate.now().toString();
            ResultSet rs = executeQuery("SELECT SUM(total_amount) as total FROM salesinvoice WHERE DATE(invoice_date) = '" + today + "'");
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public static int getTotalInvoices() {
        try {
            ResultSet rs = executeQuery("SELECT COUNT(*) as total FROM salesinvoice");
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static double getTotalRevenue() {
        try {
            ResultSet rs = executeQuery("SELECT SUM(total_amount) as total FROM salesinvoice");
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public static int getTotalVehicles() {
        try {
            ResultSet rs = executeQuery("SELECT COUNT(*) as total FROM vehicle");
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getLowStockParts() {
        try {
            ResultSet rs = executeQuery("SELECT COUNT(*) as total FROM sparepart WHERE quantity < 10");
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getTotalServices() {
        try {
            ResultSet rs = executeQuery("SELECT COUNT(*) as total FROM service");
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getVehiclesInWorkshop() {
        try {
            ResultSet rs = executeQuery("SELECT COUNT(DISTINCT v.vehicle_id) as total " +
                    "FROM vehicle v " +
                    "JOIN salesinvoice s ON v.customer_id = s.customer_id " +
                    "WHERE DATE(s.invoice_date) = '" + LocalDate.now().toString() + "'");
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}