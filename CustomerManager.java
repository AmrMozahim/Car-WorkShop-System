import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CustomerManager {
    private static CustomerManager instance;
    private ObservableList<String> customers = FXCollections.observableArrayList();

    private CustomerManager() {
        loadCustomers();
    }

    public static CustomerManager getInstance() {
        if (instance == null) {
            instance = new CustomerManager();
        }
        return instance;
    }

    public void loadCustomers() {
        customers.clear();
        try {
            ResultSet rs = DB.executeQuery("SELECT full_name FROM customer ORDER BY full_name");
            while (rs.next()) {
                customers.add(rs.getString("full_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ObservableList<String> getCustomers() {
        return customers;
    }

    public void refresh() {
        loadCustomers();
    }

    public void addCustomer(String name) {
        if (!customers.contains(name)) {
            customers.add(name);
            FXCollections.sort(customers);
        }
    }

    public void removeCustomer(String name) {
        customers.remove(name);
    }
}