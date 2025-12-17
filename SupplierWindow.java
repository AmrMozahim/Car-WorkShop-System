import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.ResultSet;

public class SupplierWindow {

    private TableView<Supplier> table = new TableView<>();
    private ObservableList<Supplier> supplierList = FXCollections.observableArrayList();

    private TextField txtName = new TextField();
    private TextField txtPhone = new TextField();
    private TextField txtEmail = new TextField();
    private TextArea txtAddress = new TextArea();

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Supplier Management");

        BorderPane root = new BorderPane();
        root.getStyleClass().add("window-root");

        // Header
        VBox header = new VBox(8);
        header.getStyleClass().add("window-header");
        header.setPadding(new Insets(20));

        Label title = new Label("Supplier Management");
        title.getStyleClass().add("window-title");

        Label subtitle = new Label("Manage spare parts suppliers and contacts");
        subtitle.getStyleClass().add("window-subtitle");

        header.getChildren().addAll(title, subtitle);
        root.setTop(header);

        // Content
        GridPane content = new GridPane();
        content.getStyleClass().add("window-content");
        content.setPadding(new Insets(25));
        content.setVgap(20);
        content.setHgap(20);

        // Left - Form
        VBox formBox = new VBox(20);
        formBox.getStyleClass().add("form-box");
        formBox.setPrefWidth(350);

        Label formTitle = new Label("Add New Supplier");
        formTitle.getStyleClass().add("form-title");

        // Name Field
        VBox nameBox = new VBox(8);
        nameBox.getStyleClass().add("form-group");
        Label lblName = new Label("Supplier Name *");
        lblName.getStyleClass().add("field-label");
        txtName.getStyleClass().add("field-input");
        txtName.setPromptText("Enter supplier name");
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
        txtAddress.getStyleClass().add("field-textarea");
        txtAddress.setPromptText("Enter supplier address");
        txtAddress.setPrefRowCount(3);
        addressBox.getChildren().addAll(lblAddress, txtAddress);

        // Form Buttons
        HBox formButtons = new HBox(15);
        formButtons.getStyleClass().add("form-buttons");

        Button btnAdd = new Button("Add Supplier");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setOnAction(e -> addSupplier());

        Button btnClear = new Button("Clear");
        btnClear.getStyleClass().add("btn-secondary");
        btnClear.setOnAction(e -> clearFields());

        formButtons.getChildren().addAll(btnAdd, btnClear);

        formBox.getChildren().addAll(formTitle, nameBox, phoneBox, emailBox, addressBox, formButtons);
        content.add(formBox, 0, 0);

        // Right - Table
        VBox tableBox = new VBox(15);
        tableBox.getStyleClass().add("table-box");

        // Table Header
        HBox tableHeader = new HBox();
        tableHeader.getStyleClass().add("table-header-box");

        Label tableTitle = new Label("Suppliers List");
        tableTitle.getStyleClass().add("table-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnRefresh = new Button("Refresh");
        btnRefresh.getStyleClass().add("btn-refresh");
        btnRefresh.setOnAction(e -> loadSuppliers());

        tableHeader.getChildren().addAll(tableTitle, spacer, btnRefresh);

        // Table
        createTable();
        table.setPrefHeight(400);

        tableBox.getChildren().addAll(tableHeader, table);
        content.add(tableBox, 1, 0);

        root.setCenter(content);

        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add("style.css");
        stage.setScene(scene);
        stage.show();

        loadSuppliers();
    }

    private void createTable() {
        table.getColumns().clear();

        TableColumn<Supplier, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("supplierId"));
        colId.setPrefWidth(80);

        TableColumn<Supplier, String> colName = new TableColumn<>("Supplier Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        colName.setPrefWidth(200);

        TableColumn<Supplier, String> colPhone = new TableColumn<>("Phone");
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colPhone.setPrefWidth(150);

        TableColumn<Supplier, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(200);

        // Actions Column
        TableColumn<Supplier, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(150);
        colActions.setCellFactory(param -> new TableCell<Supplier, Void>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");

            {
                btnEdit.getStyleClass().add("btn-table-edit");
                btnDelete.getStyleClass().add("btn-table-delete");

                HBox buttons = new HBox(8, btnEdit, btnDelete);
                buttons.getStyleClass().add("table-actions");

                btnEdit.setOnAction(e -> {
                    Supplier supplier = getTableView().getItems().get(getIndex());
                    editSupplier(supplier);
                });

                btnDelete.setOnAction(e -> {
                    Supplier supplier = getTableView().getItems().get(getIndex());
                    deleteSupplier(supplier);
                });

                setGraphic(buttons);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                }
            }
        });

        table.getColumns().addAll(colId, colName, colPhone, colEmail, colActions);
        table.setItems(supplierList);
    }

    private void loadSuppliers() {
        supplierList.clear();
        try {
            ResultSet rs = DB.getSuppliers();
            while (rs.next()) {
                Supplier supplier = new Supplier(
                        rs.getInt("supplier_id"),
                        rs.getString("supplier_name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address")
                );
                supplierList.add(supplier);
            }
        } catch (Exception e) {
            showAlert("Error", "Error loading suppliers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addSupplier() {
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String address = txtAddress.getText().trim();

        if (name.isEmpty()) {
            showAlert("Warning", "Please enter supplier name");
            return;
        }

        String sql = String.format(
                "INSERT INTO supplier (supplier_name, phone, email, address) VALUES ('%s', '%s', '%s', '%s')",
                name, phone, email, address
        );

        int result = DB.executeUpdate(sql);
        if (result > 0) {
            showAlert("Success", "Supplier added successfully");
            clearFields();
            loadSuppliers();
        } else {
            showAlert("Error", "Failed to add supplier");
        }
    }

    private void editSupplier(Supplier supplier) {
        txtName.setText(supplier.getSupplierName());
        txtPhone.setText(supplier.getPhone());
        txtEmail.setText(supplier.getEmail());
        txtAddress.setText(supplier.getAddress());

        showAlert("Edit Mode", "Editing supplier: " + supplier.getSupplierName());
    }

    private void deleteSupplier(Supplier supplier) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Supplier");
        alert.setContentText("Are you sure you want to delete " + supplier.getSupplierName() + "?\nThis action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String sql = "DELETE FROM supplier WHERE supplier_id = " + supplier.getSupplierId();
                int result = DB.executeUpdate(sql);
                if (result > 0) {
                    showAlert("Success", "Supplier deleted successfully");
                    loadSuppliers();
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

    public static class Supplier {
        private int supplierId;
        private String supplierName;
        private String phone;
        private String email;
        private String address;

        public Supplier(int supplierId, String supplierName, String phone, String email, String address) {
            this.supplierId = supplierId;
            this.supplierName = supplierName;
            this.phone = phone;
            this.email = email;
            this.address = address;
        }

        public int getSupplierId() { return supplierId; }
        public String getSupplierName() { return supplierName; }
        public String getPhone() { return phone; }
        public String getEmail() { return email; }
        public String getAddress() { return address; }
    }
}