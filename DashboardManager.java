import javafx.beans.property.*;
import java.text.DecimalFormat;

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
    }

    public void refreshTotalCustomers() {
        try {
            var rs = DB.executeQuery("SELECT COUNT(*) FROM customer");
            if (rs != null && rs.next()) {
                totalCustomers.set(rs.getInt(1));
            }
        } catch (Exception e) {
            totalCustomers.set(0);
        }
    }

    public void refreshTodayRevenue() {
        try {
            var rs = DB.executeQuery(
                    "SELECT IFNULL(SUM(total_amount), 0) FROM salesinvoice WHERE DATE(invoice_date) = CURDATE()"
            );
            if (rs != null && rs.next()) {
                todayRevenue.set(rs.getDouble(1));
            }
        } catch (Exception e) {
            todayRevenue.set(0.0);
        }
    }

    public void refreshVehiclesInService() {
        try {
            var rs = DB.executeQuery("SELECT COUNT(*) FROM vehicle");
            if (rs != null && rs.next()) {
                vehiclesInService.set(rs.getInt(1));
            }
        } catch (Exception e) {
            vehiclesInService.set(0);
        }
    }

    public void refreshLowStockParts() {
        try {
            var rs = DB.executeQuery("SELECT COUNT(*) FROM sparepart WHERE quantity < 10");
            if (rs != null && rs.next()) {
                lowStockParts.set(rs.getInt(1));
            }
        } catch (Exception e) {
            lowStockParts.set(0);
        }
    }

    public void refreshTotalInvoices() {
        try {
            var rs = DB.executeQuery("SELECT COUNT(*) FROM salesinvoice WHERE DATE(invoice_date) = CURDATE()");
            if (rs != null && rs.next()) {
                totalInvoices.set(rs.getInt(1));
            }
        } catch (Exception e) {
            totalInvoices.set(0);
        }
    }

    public void refreshActiveServices() {
        try {
            var rs = DB.executeQuery("SELECT COUNT(*) FROM service");
            if (rs != null && rs.next()) {
                activeServices.set(rs.getInt(1));
            }
        } catch (Exception e) {
            activeServices.set(0);
        }
    }

    public void refreshTotalSuppliers() {
        try {
            var rs = DB.executeQuery("SELECT COUNT(*) FROM supplier");
            if (rs != null && rs.next()) {
                totalSuppliers.set(rs.getInt(1));
            }
        } catch (Exception e) {
            totalSuppliers.set(0);
        }
    }

    public void refreshTotalMechanics() {
        try {
            var rs = DB.executeQuery("SELECT COUNT(*) FROM mechanic");
            if (rs != null && rs.next()) {
                totalMechanics.set(rs.getInt(1));
            }
        } catch (Exception e) {
            totalMechanics.set(0);
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
}