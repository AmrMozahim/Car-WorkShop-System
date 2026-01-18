import javafx.beans.property.*;
import java.text.DecimalFormat;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

public class DashboardManager {
    private static DashboardManager instance;

    // Properties للإحصائيات
    private IntegerProperty totalCustomers = new SimpleIntegerProperty(0);
    private DoubleProperty todayRevenue = new SimpleDoubleProperty(0.0);
    private IntegerProperty vehiclesInService = new SimpleIntegerProperty(0);
    private IntegerProperty lowStockParts = new SimpleIntegerProperty(0);
    private IntegerProperty totalInvoices = new SimpleIntegerProperty(0);
    private IntegerProperty activeServices = new SimpleIntegerProperty(0);
    private IntegerProperty totalSuppliers = new SimpleIntegerProperty(0);
    private IntegerProperty totalMechanics = new SimpleIntegerProperty(0);
    private IntegerProperty totalVehicles = new SimpleIntegerProperty(0);
    private IntegerProperty totalParts = new SimpleIntegerProperty(0);
    private IntegerProperty totalPurchases = new SimpleIntegerProperty(0);
    private DoubleProperty todayWorkHours = new SimpleDoubleProperty(0.0);

    private DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");

    private DashboardManager() {
        refreshAllStats();
    }

    public static DashboardManager getInstance() {
        if (instance == null) {
            instance = new DashboardManager();
        }
        return instance;
    }

    public void refreshAllStats() {
        refreshTotalCustomers();
        refreshTodayRevenue();
        refreshVehiclesInService();
        refreshLowStockParts();
        refreshTotalInvoices();
        refreshActiveServices();
        refreshTotalSuppliers();
        refreshTotalMechanics();
        refreshTotalVehicles();
        refreshTotalParts();
        refreshTotalPurchases();
        refreshTodayWorkHours();
    }

    public void refreshTotalCustomers() {
        try {
            ResultSet rs = DB.executeQuery("SELECT COUNT(*) as count FROM customer");
            if (rs != null && rs.next()) {
                totalCustomers.set(rs.getInt("count"));
            }
            if (rs != null) rs.close();
        } catch (Exception e) {
            System.out.println("Error refreshing total customers: " + e.getMessage());
            totalCustomers.set(0);
        }
    }

    public void refreshTodayRevenue() {
        try {
            ResultSet rs = DB.executeQuery(
                    "SELECT IFNULL(SUM(total_amount), 0) as revenue FROM salesinvoice WHERE DATE(invoice_date) = CURDATE()"
            );
            if (rs != null && rs.next()) {
                todayRevenue.set(rs.getDouble("revenue"));
            }
            if (rs != null) rs.close();
        } catch (Exception e) {
            System.out.println("Error refreshing today revenue: " + e.getMessage());
            todayRevenue.set(0.0);
        }
    }

    public void refreshVehiclesInService() {
        try {
            ResultSet rs = DB.executeQuery(
                    "SELECT COUNT(DISTINCT vs.vehicle_id) as count FROM vehicle_service vs " +
                            "WHERE vs.service_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)"
            );
            if (rs != null && rs.next()) {
                vehiclesInService.set(rs.getInt("count"));
            }
            if (rs != null) rs.close();
        } catch (Exception e) {
            System.out.println("Error refreshing vehicles in service: " + e.getMessage());
            vehiclesInService.set(0);
        }
    }

    public void refreshLowStockParts() {
        try {
            ResultSet rs = DB.executeQuery("SELECT COUNT(*) as count FROM sparepart WHERE quantity < 10");
            if (rs != null && rs.next()) {
                lowStockParts.set(rs.getInt("count"));
            }
            if (rs != null) rs.close();
        } catch (Exception e) {
            System.out.println("Error refreshing low stock parts: " + e.getMessage());
            lowStockParts.set(0);
        }
    }

    public void refreshTotalInvoices() {
        try {
            ResultSet rs = DB.executeQuery(
                    "SELECT COUNT(*) as count FROM salesinvoice WHERE DATE(invoice_date) = CURDATE()"
            );
            if (rs != null && rs.next()) {
                totalInvoices.set(rs.getInt("count"));
            }
            if (rs != null) rs.close();
        } catch (Exception e) {
            System.out.println("Error refreshing total invoices: " + e.getMessage());
            totalInvoices.set(0);
        }
    }

    public void refreshActiveServices() {
        try {
            ResultSet rs = DB.executeQuery(
                    "SELECT COUNT(DISTINCT vs.vehicle_id) as count FROM vehicle_service vs " +
                            "WHERE vs.service_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)"
            );
            if (rs != null && rs.next()) {
                activeServices.set(rs.getInt("count"));
            }
            if (rs != null) rs.close();
        } catch (Exception e) {
            System.out.println("Error refreshing active services: " + e.getMessage());
            activeServices.set(0);
        }
    }

    public void refreshTotalSuppliers() {
        try {
            ResultSet rs = DB.executeQuery("SELECT COUNT(*) as count FROM supplier");
            if (rs != null && rs.next()) {
                totalSuppliers.set(rs.getInt("count"));
            }
            if (rs != null) rs.close();
        } catch (Exception e) {
            System.out.println("Error refreshing total suppliers: " + e.getMessage());
            totalSuppliers.set(0);
        }
    }

    public void refreshTotalMechanics() {
        try {
            ResultSet rs = DB.executeQuery("SELECT COUNT(*) as count FROM mechanic");
            if (rs != null && rs.next()) {
                totalMechanics.set(rs.getInt("count"));
            }
            if (rs != null) rs.close();
        } catch (Exception e) {
            System.out.println("Error refreshing total mechanics: " + e.getMessage());
            totalMechanics.set(0);
        }
    }

    public void refreshTotalVehicles() {
        try {
            ResultSet rs = DB.executeQuery("SELECT COUNT(*) as count FROM vehicle");
            if (rs != null && rs.next()) {
                totalVehicles.set(rs.getInt("count"));
            }
            if (rs != null) rs.close();
        } catch (Exception e) {
            System.out.println("Error refreshing total vehicles: " + e.getMessage());
            totalVehicles.set(0);
        }
    }

    public void refreshTotalParts() {
        try {
            ResultSet rs = DB.executeQuery("SELECT COUNT(*) as count FROM sparepart");
            if (rs != null && rs.next()) {
                totalParts.set(rs.getInt("count"));
            }
            if (rs != null) rs.close();
        } catch (Exception e) {
            System.out.println("Error refreshing total parts: " + e.getMessage());
            totalParts.set(0);
        }
    }

    public void refreshTotalPurchases() {
        try {
            ResultSet rs = DB.executeQuery("SELECT COUNT(*) as count FROM purchaseinvoice");
            if (rs != null && rs.next()) {
                totalPurchases.set(rs.getInt("count"));
            }
            if (rs != null) rs.close();
        } catch (Exception e) {
            System.out.println("Error refreshing total purchases: " + e.getMessage());
            totalPurchases.set(0);
        }
    }

    public void refreshTodayWorkHours() {
        try {
            ResultSet rs = DB.executeQuery(
                    "SELECT IFNULL(SUM(hours_worked), 0) as total FROM mechanic_work_hours WHERE work_date = CURDATE()"
            );
            if (rs != null && rs.next()) {
                todayWorkHours.set(rs.getDouble("total"));
            }
            if (rs != null) rs.close();
        } catch (Exception e) {
            System.out.println("Error refreshing today work hours: " + e.getMessage());
            todayWorkHours.set(0.0);
        }
    }

    // Getters للخصائص
    public IntegerProperty totalCustomersProperty() { return totalCustomers; }
    public DoubleProperty todayRevenueProperty() { return todayRevenue; }
    public IntegerProperty vehiclesInServiceProperty() { return vehiclesInService; }
    public IntegerProperty lowStockPartsProperty() { return lowStockParts; }
    public IntegerProperty totalInvoicesProperty() { return totalInvoices; }
    public IntegerProperty activeServicesProperty() { return activeServices; }
    public IntegerProperty totalSuppliersProperty() { return totalSuppliers; }
    public IntegerProperty totalMechanicsProperty() { return totalMechanics; }
    public IntegerProperty totalVehiclesProperty() { return totalVehicles; }
    public IntegerProperty totalPartsProperty() { return totalParts; }
    public IntegerProperty totalPurchasesProperty() { return totalPurchases; }
    public DoubleProperty todayWorkHoursProperty() { return todayWorkHours; }

    // Getters للقيم
    public int getTotalCustomers() { return totalCustomers.get(); }
    public double getTodayRevenue() { return todayRevenue.get(); }
    public String getTodayRevenueFormatted() { return "$" + currencyFormat.format(todayRevenue.get()); }
    public int getVehiclesInService() { return vehiclesInService.get(); }
    public int getLowStockParts() { return lowStockParts.get(); }
    public int getTotalInvoices() { return totalInvoices.get(); }
    public int getActiveServices() { return activeServices.get(); }
    public int getTotalSuppliers() { return totalSuppliers.get(); }
    public int getTotalMechanics() { return totalMechanics.get(); }
    public int getTotalVehicles() { return totalVehicles.get(); }
    public int getTotalParts() { return totalParts.get(); }
    public int getTotalPurchases() { return totalPurchases.get(); }
    public double getTodayWorkHours() { return todayWorkHours.get(); }

    public String getFormattedStat(String statName) {
        switch (statName) {
            case "todayRevenue":
                return "$" + currencyFormat.format(getTodayRevenue());
            case "totalCustomers":
                return String.valueOf(getTotalCustomers());
            case "vehiclesInService":
                return String.valueOf(getVehiclesInService());
            case "lowStockParts":
                return String.valueOf(getLowStockParts());
            case "totalInvoices":
                return String.valueOf(getTotalInvoices());
            case "activeServices":
                return String.valueOf(getActiveServices());
            case "totalSuppliers":
                return String.valueOf(getTotalSuppliers());
            case "totalMechanics":
                return String.valueOf(getTotalMechanics());
            case "totalVehicles":
                return String.valueOf(getTotalVehicles());
            case "totalParts":
                return String.valueOf(getTotalParts());
            case "totalPurchases":
                return String.valueOf(getTotalPurchases());
            case "todayWorkHours":
                return String.format("%.1f", getTodayWorkHours());
            default:
                return "0";
        }
    }
}