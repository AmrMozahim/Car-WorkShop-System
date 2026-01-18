import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.time.LocalDate;

public class DB {

    private static Connection connection = null;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/workshop_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Amr1135@mr";

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("✓ Connected to car_workship_db successfully!");
            }
        } catch (Exception e) {
            System.out.println("✗ Database connection error: " + e.getMessage());
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
            System.out.println("Query error: " + e.getMessage());
            System.out.println("SQL: " + sql);
            e.printStackTrace();
            return null;
        }
    }

    public static int executeUpdate(String sql) {
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            int result = stmt.executeUpdate(sql);
            stmt.close();
            return result;
        } catch (Exception e) {
            System.out.println("Update error: " + e.getMessage());
            System.out.println("SQL: " + sql);
            e.printStackTrace();
            return 0;
        }
    }

    public static PreparedStatement prepareStatement(String sql) {
        try {
            return getConnection().prepareStatement(sql);
        } catch (Exception e) {
            System.out.println("PrepareStatement error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static ResultSet executeQuery(PreparedStatement pstmt) {
        try {
            return pstmt.executeQuery();
        } catch (Exception e) {
            System.out.println("PreparedStatement Query error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static int executeUpdate(PreparedStatement pstmt) {
        try {
            return pstmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("PreparedStatement Update error: " + e.getMessage());
            e.printStackTrace();
            return 0;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (Exception e) {
                System.out.println("Error closing PreparedStatement: " + e.getMessage());
            }
        }
    }

    public static int getTotalCustomers() {
        try {
            ResultSet rs = executeQuery("SELECT COUNT(*) as count FROM customer");
            return (rs != null && rs.next()) ? rs.getInt("count") : 0;
        } catch (Exception e) {
            System.out.println("Error getting total customers: " + e.getMessage());
            return 0;
        }
    }

    public static double getTodayRevenue() {
        try {
            ResultSet rs = executeQuery(
                    "SELECT IFNULL(SUM(total_amount), 0) as revenue FROM salesinvoice WHERE DATE(invoice_date) = CURDATE()"
            );
            return (rs != null && rs.next()) ? rs.getDouble("revenue") : 0.0;
        } catch (Exception e) {
            System.out.println("Error getting today revenue: " + e.getMessage());
            return 0.0;
        }
    }

    public static int getVehiclesInService() {
        try {
            ResultSet rs = executeQuery(
                    "SELECT COUNT(DISTINCT vs.vehicle_id) as count FROM vehicle_service vs " +
                            "WHERE vs.service_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)"
            );
            return (rs != null && rs.next()) ? rs.getInt("count") : 0;
        } catch (Exception e) {
            System.out.println("Error getting vehicles in service: " + e.getMessage());
            return 0;
        }
    }

    public static int getLowStockParts() {
        try {
            ResultSet rs = executeQuery("SELECT COUNT(*) as count FROM sparepart WHERE quantity < 10");
            return (rs != null && rs.next()) ? rs.getInt("count") : 0;
        } catch (Exception e) {
            System.out.println("Error getting low stock parts: " + e.getMessage());
            return 0;
        }
    }

    public static int getLastInsertId() {
        try {
            ResultSet rs = executeQuery("SELECT LAST_INSERT_ID() as id");
            return (rs != null && rs.next()) ? rs.getInt("id") : 0;
        } catch (Exception e) {
            System.out.println("Error getting last insert ID: " + e.getMessage());
            return 0;
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✓ Database connection closed");
            }
        } catch (Exception e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }

    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            if (conn != null && !conn.isClosed()) {
                return true;
            }
        } catch (Exception e) {
            System.out.println("Connection test failed: " + e.getMessage());
        }
        return false;
    }
}