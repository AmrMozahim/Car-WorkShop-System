import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DB {

    private static Connection connection = null;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                String url = "jdbc:mysql://localhost:3306/car_workship_db?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8";
                String user = "root";
                String pass = "";
                connection = DriverManager.getConnection(url, user, pass);
                System.out.println("✅ تم الاتصال بقاعدة البيانات بنجاح");
            }
        } catch (Exception e) {
            System.out.println("❌ خطأ في الاتصال بقاعدة البيانات: " + e.getMessage());
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
            System.out.println("❌ خطأ في تنفيذ الاستعلام: " + sql);
            e.printStackTrace();
            return null;
        }
    }

    public static int executeUpdate(String sql) {
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            int result = stmt.executeUpdate(sql);
            System.out.println("✅ تم تنفيذ العملية: " + sql);
            return result;
        } catch (Exception e) {
            System.out.println("❌ خطأ في تنفيذ التحديث: " + sql);
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
        String sql = "SELECT s.*, p.supplier_name FROM sparepart s " +
                "JOIN supplier p ON s.supplier_id = p.supplier_id " +
                "ORDER BY s.part_name";
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

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✅ تم إغلاق الاتصال بقاعدة البيانات");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}