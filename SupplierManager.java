import java.sql.ResultSet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SupplierManager {
    private static SupplierManager instance;
    private ObservableList<String> suppliers = FXCollections.observableArrayList();

    private SupplierManager() {
        loadSuppliers();
    }

    public static SupplierManager getInstance() {
        if (instance == null) {
            instance = new SupplierManager();
        }
        return instance;
    }

    public void loadSuppliers() {
        suppliers.clear();
        try {
            ResultSet rs = DB.executeQuery("SELECT supplier_name FROM supplier ORDER BY supplier_name");
            while (rs.next()) {
                suppliers.add(rs.getString("supplier_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ObservableList<String> getSuppliers() {
        return suppliers;
    }

    public void refresh() {
        loadSuppliers();
    }

    public void addSupplier(String name) {
        if (!suppliers.contains(name)) {
            suppliers.add(name);
            FXCollections.sort(suppliers);
        }
    }

    public void removeSupplier(String name) {
        suppliers.remove(name);
    }
}