import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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

        Label formTitle = new Label("Customer Information");
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

        // Address Field
        VBox addressBox = new VBox(8);
        addressBox.getStyleClass().add("form-group");
        Label lblAddress = new Label("Address");
        lblAddress.getStyleClass().add("field-label");
        txtAddress.getStyleClass().add("field-input");
        txtAddress.setPromptText("Enter address");
        addressBox.getChildren().addAll(lblAddress, txtAddress);

        // Form Buttons
        HBox formButtons = new HBox(15);
        formButtons.getStyleClass().add("form-buttons");

        btnAdd = new Button("Add Customer");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setOnAction(e -> {
            if (editingCustomer != null) {
                updateCustomer(editingCustomer);
            } else {
                addCustomer();
            }
        });

        Button btnClear = new Button("Clear");
        btnClear.getStyleClass().add("btn-secondary");
        btnClear.setOnAction(e -> clearFields());

        Button btnCancelEdit = new Button("Cancel Edit");
        btnCancelEdit.getStyleClass().add("btn-secondary");
        btnCancelEdit.setVisible(false);
        btnCancelEdit.setOnAction(e -> {
            editingCustomer = null;
            clearFields();
            btnAdd.setText("Add Customer");
            btnCancelEdit.setVisible(false);
        });

        formButtons.getChildren().addAll(btnAdd, btnClear, btnCancelEdit);

        formBox.getChildren().addAll(formTitle, nameBox, phoneBox, emailBox, addressBox, formButtons);
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

        TableColumn<Customer, String> colAddress = new TableColumn<>("Address");
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colAddress.setPrefWidth(200);

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

        table.getColumns().addAll(colId, colName, colPhone, colEmail, colAddress, colActions);
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
            if (pstmt == null) {
                showAlert("Error", "Failed to prepare statement. Check database connection.");
                return;
            }

            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            pstmt.setString(3, email);
            pstmt.setString(4, address);

            int result = DB.executeUpdate(pstmt);
            if (result > 0) {
                showAlert("Success", "Customer added successfully");
                clearFields();
                loadCustomers();

                // Refresh other components if they exist
                try {
                    CustomerManager.getInstance().refresh();
                    CustomerManager.getInstance().addCustomer(name);
                    Main.refreshDashboardGlobal();
                } catch (Exception e) {
                    // These might not exist, just continue
                }
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
            if (pstmt == null) {
                showAlert("Error", "Failed to prepare statement. Check database connection.");
                return;
            }

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

                // Reset editing state
                editingCustomer = null;
                btnAdd.setText("Add Customer");
                HBox formButtons = (HBox) ((VBox) txtAddress.getParent().getParent().getParent())
                        .getChildren().get(5);
                Button btnCancelEdit = (Button) formButtons.getChildren().get(2);
                btnCancelEdit.setVisible(false);

                // Refresh other components if they exist
                try {
                    CustomerManager.getInstance().refresh();
                    Main.refreshDashboardGlobal();
                } catch (Exception e) {
                    // These might not exist, just continue
                }
            } else {
                showAlert("Error", "Failed to update customer. Customer may not exist.");
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

        // Show cancel edit button
        HBox formButtons = (HBox) ((VBox) txtAddress.getParent().getParent().getParent())
                .getChildren().get(5);
        Button btnCancelEdit = (Button) formButtons.getChildren().get(2);
        btnCancelEdit.setVisible(true);
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

                    // Use transaction or individual deletes
                    // 1. Delete service_sparepart records
                    DB.executeUpdate(
                            "DELETE FROM service_sparepart WHERE vehicle_service_id IN (" +
                                    "SELECT vehicle_service_id FROM vehicle_service WHERE vehicle_id IN (" +
                                    "SELECT vehicle_id FROM vehicle WHERE customer_id = " + customerId + "))"
                    );

                    // 2. Delete vehicle_service records
                    DB.executeUpdate(
                            "DELETE FROM vehicle_service WHERE vehicle_id IN (" +
                                    "SELECT vehicle_id FROM vehicle WHERE customer_id = " + customerId + ")"
                    );

                    // 3. Delete vehicles
                    DB.executeUpdate("DELETE FROM vehicle WHERE customer_id = " + customerId);

                    // 4. Delete invoice items
                    DB.executeUpdate(
                            "DELETE FROM salesinvoiceitems WHERE invoice_id IN (" +
                                    "SELECT invoice_id FROM salesinvoice WHERE customer_id = " + customerId + ")"
                    );

                    // 5. Delete invoices
                    DB.executeUpdate("DELETE FROM salesinvoice WHERE customer_id = " + customerId);

                    // 6. Delete customer
                    PreparedStatement pstmt = DB.prepareStatement(
                            "DELETE FROM customer WHERE customer_id = ?"
                    );
                    pstmt.setInt(1, customerId);
                    int result = DB.executeUpdate(pstmt);

                    if (result > 0) {
                        showAlert("Success", "Customer and all associated records deleted successfully");
                        loadCustomers();

                        // If we were editing this customer, clear the form
                        if (editingCustomer != null && editingCustomer.getCustomerId() == customerId) {
                            clearFields();
                        }

                        // Refresh other components if they exist
                        try {
                            CustomerManager.getInstance().refresh();
                            CustomerManager.getInstance().removeCustomer(customer.getFullName());
                            Main.refreshDashboardGlobal();
                        } catch (Exception e) {
                            // These might not exist, just continue
                        }
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