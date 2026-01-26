import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

public class CustomerTab extends BorderPane {

    private TableView<Customer> table = new TableView<>();
    private ObservableList<Customer> customerList = FXCollections.observableArrayList();

    private TextField txtName = new TextField();
    private TextField txtPhone = new TextField();
    private TextField txtEmail = new TextField();
    private TextField txtAddress = new TextField();

    private Customer editingCustomer = null;
    private Button btnAdd;

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

        // Content - Using HBox instead of GridPane for better control
        HBox content = new HBox(25);
        content.getStyleClass().add("window-content");
        content.setPadding(new Insets(25));

        // Left Side - Form (30% width)
        VBox formBox = new VBox(15);
        formBox.getStyleClass().add("form-box");
        formBox.setPrefWidth(320); // Reduced from 350
        formBox.setMinWidth(300);
        formBox.setMaxWidth(350);

        Label formTitle = new Label("Add New Customer");
        formTitle.getStyleClass().add("form-title");

        // Name Field
        VBox nameBox = new VBox(5); // Reduced spacing
        nameBox.getStyleClass().add("form-group");
        Label lblName = new Label("Full Name *");
        lblName.getStyleClass().add("field-label");
        txtName.getStyleClass().add("field-input");
        txtName.setPromptText("Enter customer name");
        txtName.setPrefHeight(32); // Reduced height
        nameBox.getChildren().addAll(lblName, txtName);

        // Phone Field
        VBox phoneBox = new VBox(5);
        phoneBox.getStyleClass().add("form-group");
        Label lblPhone = new Label("Phone Number");
        lblPhone.getStyleClass().add("field-label");
        txtPhone.getStyleClass().add("field-input");
        txtPhone.setPromptText("Enter phone number");
        txtPhone.setPrefHeight(32);
        phoneBox.getChildren().addAll(lblPhone, txtPhone);

        // Email Field
        VBox emailBox = new VBox(5);
        emailBox.getStyleClass().add("form-group");
        Label lblEmail = new Label("Email Address");
        lblEmail.getStyleClass().add("field-label");
        txtEmail.getStyleClass().add("field-input");
        txtEmail.setPromptText("Enter email address");
        txtEmail.setPrefHeight(32);
        emailBox.getChildren().addAll(lblEmail, txtEmail);

        // Address Field
        VBox addressBox = new VBox(5);
        addressBox.getStyleClass().add("form-group");
        Label lblAddress = new Label("Address");
        lblAddress.getStyleClass().add("field-label");
        txtAddress.getStyleClass().add("field-input");
        txtAddress.setPromptText("Enter address");
        txtAddress.setPrefHeight(32);
        addressBox.getChildren().addAll(lblAddress, txtAddress);

        // Form Buttons
        HBox formButtons = new HBox(10); // Reduced spacing
        formButtons.getStyleClass().add("form-buttons");

        btnAdd = new Button("Add Customer");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setPrefWidth(140);
        btnAdd.setOnAction(e -> addCustomer());

        Button btnClear = new Button("Clear");
        btnClear.getStyleClass().add("btn-secondary");
        btnClear.setPrefWidth(100);
        btnClear.setOnAction(e -> clearFields());

        formButtons.getChildren().addAll(btnAdd, btnClear);

        formBox.getChildren().addAll(formTitle, nameBox, phoneBox, emailBox, addressBox, formButtons);
        content.getChildren().add(formBox);

        // Right Side - Table (70% width)
        VBox tableBox = new VBox(10); // Reduced spacing
        tableBox.getStyleClass().add("table-box");
        HBox.setHgrow(tableBox, Priority.ALWAYS); // Make table take remaining space

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
        VBox.setVgrow(table, Priority.ALWAYS); // Make table expand vertically

        tableBox.getChildren().addAll(tableHeader, table);
        content.getChildren().add(tableBox);

        setCenter(content);
        loadCustomers();
    }

    private void createTable() {
        table.getColumns().clear();

        TableColumn<Customer, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        colId.setPrefWidth(60); // Reduced width

        TableColumn<Customer, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colName.setPrefWidth(180); // Reduced width

        TableColumn<Customer, String> colPhone = new TableColumn<>("Phone");
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colPhone.setPrefWidth(130); // Reduced width

        TableColumn<Customer, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(180); // Reduced width

        TableColumn<Customer, String> colAddress = new TableColumn<>("Address");
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colAddress.setPrefWidth(150); // Reduced width

        // Actions Column - Increased width to show both buttons clearly
        TableColumn<Customer, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(180); // Increased to ensure buttons are visible
        colActions.setCellFactory(param -> new TableCell<Customer, Void>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");
            private final HBox buttons = new HBox(10, btnEdit, btnDelete); // Increased spacing

            {
                btnEdit.getStyleClass().add("btn-table-edit");
                btnDelete.getStyleClass().add("btn-table-delete");
                buttons.getStyleClass().add("table-actions");

                // Set button sizes
                btnEdit.setPrefWidth(70);
                btnDelete.setPrefWidth(70);

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

        table.getColumns().addAll(colId, colName, colPhone, colEmail, colAddress, colActions);
        table.setItems(customerList);
        table.setFixedCellSize(45);

        // Make table fill available width
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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
                            rs.getString("email"),
                            rs.getString("address")
                    );
                    customerList.add(customer);
                }
                rs.close();
            }
        } catch (Exception e) {
            showAlert("Error", "Error loading customers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addCustomer() {
        // التحقق مما إذا كان في وضع التعديل
        if (editingCustomer != null) {
            updateCustomer(editingCustomer);
            return;
        }

        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String address = txtAddress.getText().trim();

        if (name.isEmpty()) {
            showAlert("Warning", "Please enter customer name");
            return;
        }

        try {
            PreparedStatement pstmt = DB.prepareStatement(
                    "INSERT INTO customer (full_name, phone, email, address) VALUES (?, ?, ?, ?)"
            );
            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            pstmt.setString(3, email);
            pstmt.setString(4, address);

            int result = DB.executeUpdate(pstmt);
            if (result > 0) {
                showAlert("Success", "Customer added successfully");
                clearFields();
                loadCustomers();

                CustomerManager.getInstance().refresh();
                CustomerManager.getInstance().addCustomer(name);
                Main.refreshDashboardGlobal();
            } else {
                showAlert("Error", "Failed to add customer");
            }
        } catch (Exception e) {
            showAlert("Error", "Error adding customer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateCustomer(Customer customer) {
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String address = txtAddress.getText().trim();

        if (name.isEmpty()) {
            showAlert("Warning", "Please enter customer name");
            return;
        }

        try {
            PreparedStatement pstmt = DB.prepareStatement(
                    "UPDATE customer SET full_name=?, phone=?, email=?, address=? WHERE customer_id=?"
            );
            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            pstmt.setString(3, email);
            pstmt.setString(4, address);
            pstmt.setInt(5, customer.getCustomerId());

            int result = DB.executeUpdate(pstmt);
            if (result > 0) {
                showAlert("Success", "Customer updated successfully");
                clearFields();
                loadCustomers();
                CustomerManager.getInstance().refresh();
                Main.refreshDashboardGlobal();
            } else {
                showAlert("Error", "Failed to update customer");
            }
        } catch (Exception e) {
            showAlert("Error", "Error updating customer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void editCustomer(Customer customer) {
        editingCustomer = customer;

        txtName.setText(customer.getFullName());
        txtPhone.setText(customer.getPhone());
        txtEmail.setText(customer.getEmail());
        txtAddress.setText(customer.getAddress());

        btnAdd.setText("Update Customer");
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

                    // 1. حذف سجلات service_sparepart المرتبطة
                    DB.executeUpdate(
                            "DELETE FROM service_sparepart WHERE vehicle_service_id IN (" +
                                    "SELECT vehicle_service_id FROM vehicle_service WHERE vehicle_id IN (" +
                                    "SELECT vehicle_id FROM vehicle WHERE customer_id = " + customerId + "))"
                    );

                    // 2. حذف سجلات vehicle_service
                    DB.executeUpdate(
                            "DELETE FROM vehicle_service WHERE vehicle_id IN (" +
                                    "SELECT vehicle_id FROM vehicle WHERE customer_id = " + customerId + ")"
                    );

                    // 3. حذف المركبات
                    DB.executeUpdate("DELETE FROM vehicle WHERE customer_id = " + customerId);

                    // 4. حذف عناصر الفواتير
                    DB.executeUpdate(
                            "DELETE FROM salesinvoiceitems WHERE invoice_id IN (" +
                                    "SELECT invoice_id FROM salesinvoice WHERE customer_id = " + customerId + ")"
                    );

                    // 5. حذف الفواتير
                    DB.executeUpdate("DELETE FROM salesinvoice WHERE customer_id = " + customerId);

                    // 6. حذف العميل
                    PreparedStatement pstmt = DB.prepareStatement(
                            "DELETE FROM customer WHERE customer_id = ?"
                    );
                    pstmt.setInt(1, customerId);
                    int result = DB.executeUpdate(pstmt);

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
        editingCustomer = null;
        btnAdd.setText("Add Customer");
        txtName.clear();
        txtPhone.clear();
        txtEmail.clear();
        txtAddress.clear();
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
        private String address;

        public Customer(int customerId, String fullName, String phone, String email, String address) {
            this.customerId = customerId;
            this.fullName = fullName;
            this.phone = phone;
            this.email = email;
            this.address = address;
        }

        public int getCustomerId() { return customerId; }
        public String getFullName() { return fullName; }
        public String getPhone() { return phone; }
        public String getEmail() { return email; }
        public String getAddress() { return address; }
    }
}