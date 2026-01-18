import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

public class PartsTab extends BorderPane {

    private TableView<Part> table = new TableView<>();
    private ObservableList<Part> partList = FXCollections.observableArrayList();

    private TextField txtName = new TextField();
    private TextField txtQuantity = new TextField();
    private TextField txtPrice = new TextField();
    private TextField txtCategory = new TextField();
    private ComboBox<String> cmbSupplier = new ComboBox<>();

    private Part editingPart = null;
    private Button btnAdd;

    public PartsTab() {
        initialize();
    }

    private void initialize() {
        getStyleClass().add("window-root");

        VBox header = new VBox(8);
        header.getStyleClass().add("window-header");
        header.setPadding(new Insets(20));

        Label title = new Label("Parts Management");
        title.getStyleClass().add("window-title");

        Label subtitle = new Label("Manage spare parts inventory");
        subtitle.getStyleClass().add("window-subtitle");

        header.getChildren().addAll(title, subtitle);
        setTop(header);

        GridPane content = new GridPane();
        content.getStyleClass().add("window-content");
        content.setPadding(new Insets(25));
        content.setVgap(20);
        content.setHgap(20);

        VBox formBox = new VBox(20);
        formBox.getStyleClass().add("form-box");
        formBox.setPrefWidth(350);

        Label formTitle = new Label("Add New Part");
        formTitle.getStyleClass().add("form-title");

        VBox nameBox = new VBox(8);
        nameBox.getStyleClass().add("form-group");
        Label lblName = new Label("Part Name *");
        lblName.getStyleClass().add("field-label");
        txtName.getStyleClass().add("field-input");
        txtName.setPromptText("Oil Filter, Brake Pads, etc.");
        nameBox.getChildren().addAll(lblName, txtName);

        VBox quantityBox = new VBox(8);
        quantityBox.getStyleClass().add("form-group");
        Label lblQuantity = new Label("Quantity *");
        lblQuantity.getStyleClass().add("field-label");
        txtQuantity.getStyleClass().add("field-input");
        txtQuantity.setPromptText("50");
        quantityBox.getChildren().addAll(lblQuantity, txtQuantity);

        VBox priceBox = new VBox(8);
        priceBox.getStyleClass().add("form-group");
        Label lblPrice = new Label("Price ($) *");
        lblPrice.getStyleClass().add("field-label");
        txtPrice.getStyleClass().add("field-input");
        txtPrice.setPromptText("25.99");
        priceBox.getChildren().addAll(lblPrice, txtPrice);

        VBox categoryBox = new VBox(8);
        categoryBox.getStyleClass().add("form-group");
        Label lblCategory = new Label("Category");
        lblCategory.getStyleClass().add("field-label");
        txtCategory.getStyleClass().add("field-input");
        txtCategory.setPromptText("Engine, Brakes, Electrical, etc.");
        categoryBox.getChildren().addAll(lblCategory, txtCategory);

        VBox supplierBox = new VBox(8);
        supplierBox.getStyleClass().add("form-group");
        Label lblSupplier = new Label("Supplier (Optional)");
        lblSupplier.getStyleClass().add("field-label");

        HBox supplierRow = new HBox(5);
        supplierRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        cmbSupplier.getStyleClass().add("field-combo");
        cmbSupplier.setPromptText("Select supplier");
        cmbSupplier.setPrefWidth(250);
        cmbSupplier.setItems(SupplierManager.getInstance().getSuppliers());

        Button btnRefreshSuppliers = new Button("↻");
        btnRefreshSuppliers.getStyleClass().add("btn-secondary");
        btnRefreshSuppliers.setTooltip(new Tooltip("Refresh supplier list"));
        btnRefreshSuppliers.setOnAction(e -> {
            cmbSupplier.setItems(SupplierManager.getInstance().getSuppliers());
        });

        supplierRow.getChildren().addAll(cmbSupplier, btnRefreshSuppliers);
        supplierBox.getChildren().addAll(lblSupplier, supplierRow);

        HBox formButtons = new HBox(15);
        formButtons.getStyleClass().add("form-buttons");

        btnAdd = new Button("Add Part");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setOnAction(e -> addPart());

        Button btnClear = new Button("Clear");
        btnClear.getStyleClass().add("btn-secondary");
        btnClear.setOnAction(e -> clearFields());

        Button btnReorder = new Button("Reorder Low Stock");
        btnReorder.getStyleClass().add("btn-primary");
        btnReorder.setOnAction(e -> showLowStock());

        formButtons.getChildren().addAll(btnAdd, btnClear, btnReorder);

        formBox.getChildren().addAll(formTitle, nameBox, quantityBox, priceBox,
                categoryBox, supplierBox, formButtons);
        content.add(formBox, 0, 0);

        VBox tableBox = new VBox(15);
        tableBox.getStyleClass().add("table-box");

        HBox tableHeader = new HBox();
        tableHeader.getStyleClass().add("table-header-box");

        Label tableTitle = new Label("Parts Inventory");
        tableTitle.getStyleClass().add("table-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnRefresh = new Button("Refresh");
        btnRefresh.getStyleClass().add("btn-refresh");
        btnRefresh.setOnAction(e -> loadParts());

        tableHeader.getChildren().addAll(tableTitle, spacer, btnRefresh);

        createTable();
        table.setPrefHeight(400);

        tableBox.getChildren().addAll(tableHeader, table);
        content.add(tableBox, 1, 0);

        setCenter(content);
        loadParts();
    }

    private void createTable() {
        table.getColumns().clear();

        TableColumn<Part, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("partId"));
        colId.setPrefWidth(80);

        TableColumn<Part, String> colName = new TableColumn<>("Part Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("partName"));
        colName.setPrefWidth(200);

        TableColumn<Part, Integer> colQuantity = new TableColumn<>("Quantity");
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colQuantity.setPrefWidth(100);

        TableColumn<Part, Double> colPrice = new TableColumn<>("Price ($)");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colPrice.setPrefWidth(100);

        TableColumn<Part, String> colCategory = new TableColumn<>("Category");
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colCategory.setPrefWidth(120);

        TableColumn<Part, String> colSupplier = new TableColumn<>("Supplier");
        colSupplier.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        colSupplier.setPrefWidth(150);

        TableColumn<Part, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setPrefWidth(100);
        colStatus.setCellFactory(column -> new TableCell<Part, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    if (status.equals("LOW")) {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    } else if (status.equals("OUT")) {
                        setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                    }
                }
            }
        });

        TableColumn<Part, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(150);
        colActions.setCellFactory(param -> new TableCell<Part, Void>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");
            private final HBox buttons = new HBox(8, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().add("btn-table-edit");
                btnDelete.getStyleClass().add("btn-table-delete");
                buttons.getStyleClass().add("table-actions");

                btnEdit.setOnAction(e -> {
                    Part part = getTableView().getItems().get(getIndex());
                    editPart(part);
                });

                btnDelete.setOnAction(e -> {
                    Part part = getTableView().getItems().get(getIndex());
                    deletePart(part);
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

        table.getColumns().addAll(colId, colName, colQuantity, colPrice,
                colCategory, colSupplier, colStatus, colActions);
        table.setItems(partList);
        table.setFixedCellSize(45);
    }

    private void loadParts() {
        partList.clear();
        try {
            ResultSet rs = DB.executeQuery(
                    "SELECT p.part_id, p.part_name, p.quantity, p.price, p.category, " +
                            "s.supplier_name FROM sparepart p " +
                            "LEFT JOIN supplier s ON p.supplier_id = s.supplier_id " +
                            "ORDER BY p.part_name"
            );

            if (rs != null) {
                while (rs.next()) {
                    int quantity = rs.getInt("quantity");
                    String status;
                    if (quantity == 0) {
                        status = "OUT";
                    } else if (quantity < 10) {
                        status = "LOW";
                    } else {
                        status = "GOOD";
                    }

                    Part part = new Part(
                            rs.getInt("part_id"),
                            rs.getString("part_name"),
                            quantity,
                            rs.getDouble("price"),
                            rs.getString("category"),
                            rs.getString("supplier_name"),
                            status
                    );
                    partList.add(part);
                }
                rs.close();
            }
        } catch (Exception e) {
            showAlert("Error", "Error loading parts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addPart() {
        if (editingPart != null) {
            updatePart(editingPart);
            return;
        }

        String name = txtName.getText().trim();
        String quantityStr = txtQuantity.getText().trim();
        String priceStr = txtPrice.getText().trim();
        String category = txtCategory.getText().trim();
        String supplierName = cmbSupplier.getValue();

        if (name.isEmpty()) {
            showAlert("Warning", "Please enter part name");
            return;
        }

        if (quantityStr.isEmpty()) {
            showAlert("Warning", "Please enter quantity");
            return;
        }

        if (priceStr.isEmpty()) {
            showAlert("Warning", "Please enter price");
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityStr);
            double price = Double.parseDouble(priceStr);
            int supplierId = 0;

            if (supplierName != null && !supplierName.isEmpty()) {
                supplierId = SupplierManager.getInstance().getSupplierIdByName(supplierName);
            }

            if (quantity < 0) {
                showAlert("Warning", "Quantity cannot be negative");
                return;
            }

            if (price <= 0) {
                showAlert("Warning", "Price must be greater than 0");
                return;
            }

            PreparedStatement pstmt = DB.prepareStatement(
                    "INSERT INTO sparepart (part_name, quantity, price, category, supplier_id) VALUES (?, ?, ?, ?, ?)"
            );
            pstmt.setString(1, name);
            pstmt.setInt(2, quantity);
            pstmt.setDouble(3, price);
            pstmt.setString(4, category.isEmpty() ? null : category);
            pstmt.setInt(5, supplierId > 0 ? supplierId : null);

            int result = DB.executeUpdate(pstmt);
            if (result > 0) {
                showAlert("Success", "Part added successfully");
                clearFields();
                loadParts();
                Main.refreshDashboardGlobal();
            } else {
                showAlert("Error", "Failed to add part");
            }
        } catch (NumberFormatException e) {
            showAlert("Warning", "Quantity and price must be valid numbers");
        } catch (Exception e) {
            showAlert("Error", "Error adding part: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updatePart(Part part) {
        String name = txtName.getText().trim();
        String quantityStr = txtQuantity.getText().trim();
        String priceStr = txtPrice.getText().trim();
        String category = txtCategory.getText().trim();
        String supplierName = cmbSupplier.getValue();

        if (name.isEmpty()) {
            showAlert("Warning", "Please enter part name");
            return;
        }

        if (quantityStr.isEmpty()) {
            showAlert("Warning", "Please enter quantity");
            return;
        }

        if (priceStr.isEmpty()) {
            showAlert("Warning", "Please enter price");
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityStr);
            double price = Double.parseDouble(priceStr);
            int supplierId = 0;

            if (supplierName != null && !supplierName.isEmpty()) {
                supplierId = SupplierManager.getInstance().getSupplierIdByName(supplierName);
            }

            PreparedStatement pstmt = DB.prepareStatement(
                    "UPDATE sparepart SET part_name=?, quantity=?, price=?, category=?, supplier_id=? WHERE part_id=?"
            );
            pstmt.setString(1, name);
            pstmt.setInt(2, quantity);
            pstmt.setDouble(3, price);
            pstmt.setString(4, category.isEmpty() ? null : category);
            pstmt.setInt(5, supplierId > 0 ? supplierId : null);
            pstmt.setInt(6, part.getPartId());

            int result = DB.executeUpdate(pstmt);
            if (result > 0) {
                showAlert("Success", "Part updated successfully");
                clearFields();
                loadParts();
                Main.refreshDashboardGlobal();
            } else {
                showAlert("Error", "Failed to update part");
            }
        } catch (Exception e) {
            showAlert("Error", "Error updating part: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void editPart(Part part) {
        editingPart = part;

        txtName.setText(part.getPartName());
        txtQuantity.setText(String.valueOf(part.getQuantity()));
        txtPrice.setText(String.format("%.2f", part.getPrice()));
        txtCategory.setText(part.getCategory() != null ? part.getCategory() : "");
        cmbSupplier.setValue(part.getSupplierName());

        btnAdd.setText("Update Part");
    }

    private void deletePart(Part part) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Part");
        alert.setContentText("Are you sure you want to delete part " + part.getPartName() + "?\n\n" +
                "⚠️ This will also remove this part from any invoices.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // حذف العنصر من salesinvoiceitems أولاً
                    DB.executeUpdate("DELETE FROM salesinvoiceitems WHERE part_id = " + part.getPartId());

                    // حذف من service_sparepart إذا كان موجوداً
                    DB.executeUpdate("DELETE FROM service_sparepart WHERE part_id = " + part.getPartId());

                    // حذف الجزء نفسه
                    PreparedStatement pstmt = DB.prepareStatement(
                            "DELETE FROM sparepart WHERE part_id = ?"
                    );
                    pstmt.setInt(1, part.getPartId());

                    int result = DB.executeUpdate(pstmt);
                    if (result > 0) {
                        showAlert("Success", "Part deleted successfully");
                        loadParts();
                        Main.refreshDashboardGlobal();
                    }
                } catch (Exception e) {
                    showAlert("Error", "Error deleting part: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void showLowStock() {
        int lowStockCount = 0;
        int outOfStockCount = 0;
        StringBuilder lowStockList = new StringBuilder("Stock Alert:\n\n");

        for (Part part : partList) {
            if (part.getStatus().equals("LOW")) {
                lowStockCount++;
                lowStockList.append("⚠️ LOW: ").append(part.getPartName())
                        .append(" - ").append(part.getQuantity()).append(" left\n");
            } else if (part.getStatus().equals("OUT")) {
                outOfStockCount++;
                lowStockList.append("❌ OUT: ").append(part.getPartName())
                        .append(" - Out of stock\n");
            }
        }

        if (lowStockCount > 0 || outOfStockCount > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Stock Alert");
            alert.setHeaderText(lowStockCount + " parts low, " + outOfStockCount + " parts out of stock");
            alert.setContentText(lowStockList.toString());
            alert.showAndWait();
        } else {
            showAlert("Stock Status", "All parts are adequately stocked");
        }
    }

    private void clearFields() {
        editingPart = null;
        btnAdd.setText("Add Part");
        txtName.clear();
        txtQuantity.clear();
        txtPrice.clear();
        txtCategory.clear();
        cmbSupplier.setValue(null);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Part {
        private int partId;
        private String partName;
        private int quantity;
        private double price;
        private String category;
        private String supplierName;
        private String status;

        public Part(int partId, String partName, int quantity, double price,
                    String category, String supplierName, String status) {
            this.partId = partId;
            this.partName = partName;
            this.quantity = quantity;
            this.price = price;
            this.category = category;
            this.supplierName = supplierName;
            this.status = status;
        }

        public int getPartId() { return partId; }
        public String getPartName() { return partName; }
        public int getQuantity() { return quantity; }
        public double getPrice() { return price; }
        public String getCategory() { return category; }
        public String getSupplierName() { return supplierName; }
        public String getStatus() { return status; }
    }
}