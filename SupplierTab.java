import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.sql.ResultSet;

public class SupplierTab extends BorderPane {

    private TableView<Supplier> table = new TableView<>();
    private ObservableList<Supplier> supplierList = FXCollections.observableArrayList();

    private TextField txtName = new TextField();
    private TextField txtPhone = new TextField();

    public SupplierTab() {
        initialize();
    }

    private void initialize() {
        getStyleClass().add("window-root");

        // Header
        VBox header = new VBox(8);
        header.getStyleClass().add("window-header");
        header.setPadding(new Insets(20));

        Label title = new Label("Supplier Management");
        title.getStyleClass().add("window-title");

        Label subtitle = new Label("Manage spare parts suppliers");
        subtitle.getStyleClass().add("window-subtitle");

        header.getChildren().addAll(title, subtitle);
        setTop(header);

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

        formBox.getChildren().addAll(formTitle, nameBox, phoneBox, formButtons);
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

        setCenter(content);

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

        // Actions Column - تم الإصلاح هنا
        TableColumn<Supplier, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(120);
        colActions.setCellFactory(param -> new TableCell<Supplier, Void>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");
            private final HBox buttons = new HBox(8, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().add("btn-table-edit");
                btnDelete.getStyleClass().add("btn-table-delete");
                buttons.getStyleClass().add("table-actions");

                btnEdit.setOnAction(e -> {
                    Supplier supplier = getTableView().getItems().get(getIndex());
                    editSupplier(supplier);
                });

                btnDelete.setOnAction(e -> {
                    Supplier supplier = getTableView().getItems().get(getIndex());
                    deleteSupplier(supplier);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons); // هذا هو الإصلاح
                }
            }
        });

        table.getColumns().addAll(colId, colName, colPhone, colActions);
        table.setItems(supplierList);
        table.setFixedCellSize(45);
    }

    private void loadSuppliers() {
        supplierList.clear();
        try {
            ResultSet rs = DB.executeQuery("SELECT supplier_id, supplier_name, phone FROM supplier ORDER BY supplier_name");
            if (rs != null) {
                while (rs.next()) {
                    Supplier supplier = new Supplier(
                            rs.getInt("supplier_id"),
                            rs.getString("supplier_name"),
                            rs.getString("phone")
                    );
                    supplierList.add(supplier);
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Error loading suppliers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addSupplier() {
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();

        if (name.isEmpty()) {
            showAlert("Warning", "Please enter supplier name");
            return;
        }

        String sql = String.format(
                "INSERT INTO supplier (supplier_name, phone) VALUES ('%s', '%s')",
                name, phone
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

        public Supplier(int supplierId, String supplierName, String phone) {
            this.supplierId = supplierId;
            this.supplierName = supplierName;
            this.phone = phone;
        }

        public int getSupplierId() { return supplierId; }
        public String getSupplierName() { return supplierName; }
        public String getPhone() { return phone; }
    }
}