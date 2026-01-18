import java.sql.ResultSet;
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
            if (rs != null) {
                while (rs.next()) {
                    customers.add(rs.getString("full_name"));
                }
                rs.close();
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

    public int getCustomerIdByName(String name) {
        try {
            ResultSet rs = DB.executeQuery(
                    "SELECT customer_id FROM customer WHERE full_name = '" + name + "'"
            );
            if (rs != null && rs.next()) {
                int id = rs.getInt("customer_id");
                rs.close();
                return id;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}