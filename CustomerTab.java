import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.sql.ResultSet;

public class CustomerTab extends BorderPane {

    private TableView<Customer> table = new TableView<>();
    private ObservableList<Customer> customerList = FXCollections.observableArrayList();

    private TextField txtName = new TextField();
    private TextField txtPhone = new TextField();
    private TextField txtEmail = new TextField();

    public CustomerTab() {
        initialize();
    }

    private void initialize() {
        getStyleClass().add("window-root");

        // Header
        VBox header = new VBox(8);
        header.getStyleClass().add("window-header");
        header.setPadding(new Insets(20));

        Label title = new Label("Customer Management");
        title.getStyleClass().add("window-title");

        Label subtitle = new Label("Manage customer information and relationships");
        subtitle.getStyleClass().add("window-subtitle");

        header.getChildren().addAll(title, subtitle);
        setTop(header);

        // Content
        GridPane content = new GridPane();
        content.getStyleClass().add("window-content");
        content.setPadding(new Insets(25));
        content.setVgap(20);
        content.setHgap(20);

        // Left Side - Form
        VBox formBox = new VBox(20);
        formBox.getStyleClass().add("form-box");
        formBox.setPrefWidth(350);

        Label formTitle = new Label("Add New Customer");
        formTitle.getStyleClass().add("form-title");

        // Name Field
        VBox nameBox = new VBox(8);
        nameBox.getStyleClass().add("form-group");
        Label lblName = new Label("Full Name *");
        lblName.getStyleClass().add("field-label");
        txtName.getStyleClass().add("field-input");
        txtName.setPromptText("Enter customer name");
        nameBox.getChildren().addAll(lblName, txtName);

        // Phone Field
        VBox phoneBox = new VBox(8);
        phoneBox.getStyleClass().add("form-group");
        Label lblPhone = new Label("Phone Number");
        lblPhone.getStyleClass().add("field-label");
        txtPhone.getStyleClass().add("field-input");
        txtPhone.setPromptText("Enter phone number");
        phoneBox.getChildren().addAll(lblPhone, txtPhone);

        // Email Field
        VBox emailBox = new VBox(8);
        emailBox.getStyleClass().add("form-group");
        Label lblEmail = new Label("Email Address");
        lblEmail.getStyleClass().add("field-label");
        txtEmail.getStyleClass().add("field-input");
        txtEmail.setPromptText("Enter email address");
        emailBox.getChildren().addAll(lblEmail, txtEmail);

        // Form Buttons
        HBox formButtons = new HBox(15);
        formButtons.getStyleClass().add("form-buttons");

        Button btnAdd = new Button("Add Customer");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setOnAction(e -> addCustomer());

        Button btnClear = new Button("Clear");
        btnClear.getStyleClass().add("btn-secondary");
        btnClear.setOnAction(e -> clearFields());

        formButtons.getChildren().addAll(btnAdd, btnClear);

        formBox.getChildren().addAll(formTitle, nameBox, phoneBox, emailBox, formButtons);
        content.add(formBox, 0, 0);

        // Right Side - Table
        VBox tableBox = new VBox(15);
        tableBox.getStyleClass().add("table-box");

        // Table Header
        HBox tableHeader = new HBox();
        tableHeader.getStyleClass().add("table-header-box");

        Label tableTitle = new Label("Customer List");
        tableTitle.getStyleClass().add("table-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnRefresh = new Button("Refresh");
        btnRefresh.getStyleClass().add("btn-refresh");
        btnRefresh.setOnAction(e -> loadCustomers());

        tableHeader.getChildren().addAll(tableTitle, spacer, btnRefresh);

        // Table
        createTable();
        table.setPrefHeight(400);

        tableBox.getChildren().addAll(tableHeader, table);
        content.add(tableBox, 1, 0);

        setCenter(content);

        loadCustomers();
    }

    private void createTable() {
        table.getColumns().clear();

        TableColumn<Customer, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        colId.setPrefWidth(80);

        TableColumn<Customer, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colName.setPrefWidth(200);

        TableColumn<Customer, String> colPhone = new TableColumn<>("Phone");
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colPhone.setPrefWidth(150);

        TableColumn<Customer, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(200);

        // Actions Column
        TableColumn<Customer, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(150);
        colActions.setCellFactory(param -> new TableCell<Customer, Void>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");
            private final HBox buttons = new HBox(8, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().add("btn-table-edit");
                btnDelete.getStyleClass().add("btn-table-delete");
                buttons.getStyleClass().add("table-actions");

                btnEdit.setOnAction(e -> {
                    Customer customer = getTableView().getItems().get(getIndex());
                    editCustomer(customer);
                });

                btnDelete.setOnAction(e -> {
                    Customer customer = getTableView().getItems().get(getIndex());
                    deleteCustomer(customer);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });

        table.getColumns().addAll(colId, colName, colPhone, colEmail, colActions);
        table.setItems(customerList);
        table.setFixedCellSize(45);
    }

    private void loadCustomers() {
        customerList.clear();
        try {
            ResultSet rs = DB.executeQuery("SELECT * FROM customer ORDER BY full_name");
            if (rs != null) {
                while (rs.next()) {
                    Customer customer = new Customer(
                            rs.getInt("customer_id"),
                            rs.getString("full_name"),
                            rs.getString("phone"),
                            rs.getString("email")
                    );
                    customerList.add(customer);
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Error loading customers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addCustomer() {
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();

        if (name.isEmpty()) {
            showAlert("Warning", "Please enter customer name");
            return;
        }

        String sql = String.format(
                "INSERT INTO customer (full_name, phone, email) VALUES ('%s', '%s', '%s')",
                name, phone, email
        );

        int result = DB.executeUpdate(sql);
        if (result > 0) {
            showAlert("Success", "Customer added successfully");
            clearFields();
            loadCustomers();

            // تحديث CustomerManager
            CustomerManager.getInstance().refresh();
            CustomerManager.getInstance().addCustomer(name);

            // تحديث Dashboard تلقائياً
            Main.refreshDashboardGlobal();
        } else {
            showAlert("Error", "Failed to add customer");
        }
    }

    private void editCustomer(Customer customer) {
        txtName.setText(customer.getFullName());
        txtPhone.setText(customer.getPhone());
        txtEmail.setText(customer.getEmail());

        showAlert("Edit Mode", "Edit customer details and click 'Add Customer' to update");
    }

    private void deleteCustomer(Customer customer) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Customer");
        alert.setContentText("Are you sure you want to delete " + customer.getFullName() + "?\n\n" +
                "⚠️ This will also delete ALL associated records:\n" +
                "• All vehicles of this customer\n" +
                "• All invoices of this customer\n" +
                "• All invoice items of this customer\n\n" +
                "This action cannot be undone!");

        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    int customerId = customer.getCustomerId();

                    // 1. حذف جميع الفواتير والعناصر المرتبطة
                    ResultSet invoices = DB.executeQuery(
                            "SELECT invoice_id FROM salesinvoice WHERE customer_id = " + customerId
                    );

                    // حذف عناصر الفواتير أولاً
                    if (invoices != null) {
                        while (invoices.next()) {
                            int invoiceId = invoices.getInt("invoice_id");
                            DB.executeUpdate("DELETE FROM salesinvoiceitems WHERE invoice_id = " + invoiceId);
                        }
                    }

                    // حذف الفواتير نفسها
                    DB.executeUpdate("DELETE FROM salesinvoice WHERE customer_id = " + customerId);

                    // 2. حذف جميع المركبات المرتبطة
                    DB.executeUpdate("DELETE FROM vehicle WHERE customer_id = " + customerId);

                    // 3. حذف العميل نفسه
                    String sql = "DELETE FROM customer WHERE customer_id = " + customerId;
                    int result = DB.executeUpdate(sql);

                    if (result > 0) {
                        showAlert("Success", "Customer and all associated records deleted successfully");
                        loadCustomers();
                        CustomerManager.getInstance().refresh();
                        CustomerManager.getInstance().removeCustomer(customer.getFullName());
                        Main.refreshDashboardGlobal();
                    }
                } catch (Exception e) {
                    showAlert("Error", "Error deleting customer: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void clearFields() {
        txtName.clear();
        txtPhone.clear();
        txtEmail.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Customer {
        private int customerId;
        private String fullName;
        private String phone;
        private String email;

        public Customer(int customerId, String fullName, String phone, String email) {
            this.customerId = customerId;
            this.fullName = fullName;
            this.phone = phone;
            this.email = email;
        }

        public int getCustomerId() { return customerId; }
        public String getFullName() { return fullName; }
        public String getPhone() { return phone; }
        public String getEmail() { return email; }
    }
}