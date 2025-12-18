import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.sql.ResultSet;

public class PartsTab extends BorderPane {

    private TableView<Part> table = new TableView<>();
    private ObservableList<Part> partList = FXCollections.observableArrayList();

    private TextField txtName = new TextField();
    private TextField txtQuantity = new TextField();
    private TextField txtPrice = new TextField();
    private TextField txtMinStock = new TextField();
    private ComboBox<String> cmbSupplier = new ComboBox<>();
    private ComboBox<String> cmbCategory = new ComboBox<>();

    public PartsTab() {
        initialize();
    }

    private void initialize() {
        getStyleClass().add("window-root");

        // Header
        VBox header = new VBox(8);
        header.getStyleClass().add("window-header");
        header.setPadding(new Insets(20));

        Label title = new Label("Parts Management");
        title.getStyleClass().add("window-title");

        Label subtitle = new Label("Manage spare parts inventory");
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

        Label formTitle = new Label("Add New Part");
        formTitle.getStyleClass().add("form-title");

        // Name Field
        VBox nameBox = new VBox(8);
        nameBox.getStyleClass().add("form-group");
        Label lblName = new Label("Part Name *");
        lblName.getStyleClass().add("field-label");
        txtName.getStyleClass().add("field-input");
        txtName.setPromptText("Oil Filter, Brake Pads, etc.");
        nameBox.getChildren().addAll(lblName, txtName);

        // Quantity Field
        VBox quantityBox = new VBox(8);
        quantityBox.getStyleClass().add("form-group");
        Label lblQuantity = new Label("Quantity *");
        lblQuantity.getStyleClass().add("field-label");
        txtQuantity.getStyleClass().add("field-input");
        txtQuantity.setPromptText("50");
        quantityBox.getChildren().addAll(lblQuantity, txtQuantity);

        // Price Field
        VBox priceBox = new VBox(8);
        priceBox.getStyleClass().add("form-group");
        Label lblPrice = new Label("Price ($) *");
        lblPrice.getStyleClass().add("field-label");
        txtPrice.getStyleClass().add("field-input");
        txtPrice.setPromptText("25.99");
        priceBox.getChildren().addAll(lblPrice, txtPrice);

        // Min Stock Field
        VBox minStockBox = new VBox(8);
        minStockBox.getStyleClass().add("form-group");
        Label lblMinStock = new Label("Min Stock Level");
        lblMinStock.getStyleClass().add("field-label");
        txtMinStock.getStyleClass().add("field-input");
        txtMinStock.setPromptText("10");
        minStockBox.getChildren().addAll(lblMinStock, txtMinStock);

        // Supplier Field
        VBox supplierBox = new VBox(8);
        supplierBox.getStyleClass().add("form-group");
        Label lblSupplier = new Label("Supplier");
        lblSupplier.getStyleClass().add("field-label");
        cmbSupplier.getStyleClass().add("field-combo");
        cmbSupplier.setPromptText("Select supplier");
        loadSuppliers();
        supplierBox.getChildren().addAll(lblSupplier, cmbSupplier);

        // Category Field
        VBox categoryBox = new VBox(8);
        categoryBox.getStyleClass().add("form-group");
        Label lblCategory = new Label("Category");
        lblCategory.getStyleClass().add("field-label");
        cmbCategory.getStyleClass().add("field-combo");
        cmbCategory.getItems().addAll("Engine", "Brakes", "Suspension", "Electrical",
                "Exhaust", "Filters", "Fluids", "Accessories");
        cmbCategory.setPromptText("Select category");
        categoryBox.getChildren().addAll(lblCategory, cmbCategory);

        // Buttons
        HBox formButtons = new HBox(15);
        formButtons.getStyleClass().add("form-buttons");

        Button btnAdd = new Button("Add Part");
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
                minStockBox, supplierBox, categoryBox, formButtons);
        content.add(formBox, 0, 0);

        // Right - Table
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

    private void loadSuppliers() {
        try {
            ResultSet rs = DB.getSuppliers();
            while (rs.next()) {
                cmbSupplier.getItems().add(rs.getString("supplier_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        colSupplier.setCellValueFactory(new PropertyValueFactory<>("supplier"));
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
                    } else if (status.equals("MEDIUM")) {
                        setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
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

            {
                btnEdit.getStyleClass().add("btn-table-edit");
                btnDelete.getStyleClass().add("btn-table-delete");

                HBox buttons = new HBox(8, btnEdit, btnDelete);
                buttons.getStyleClass().add("table-actions");

                btnEdit.setOnAction(e -> {
                    Part part = getTableView().getItems().get(getIndex());
                    editPart(part);
                });

                btnDelete.setOnAction(e -> {
                    Part part = getTableView().getItems().get(getIndex());
                    deletePart(part);
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

        table.getColumns().addAll(colId, colName, colQuantity, colPrice,
                colCategory, colSupplier, colStatus, colActions);
        table.setItems(partList);
    }

    private void loadParts() {
        partList.clear();
        try {
            ResultSet rs = DB.getParts();
            while (rs.next()) {
                int quantity = rs.getInt("quantity");
                int minStock = rs.getInt("min_stock");
                String status = "GOOD";
                if (quantity < 5) status = "LOW";
                else if (quantity < minStock) status = "MEDIUM";

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
        } catch (Exception e) {
            showAlert("Error", "Error loading parts: " + e.getMessage());
        }
    }

    private void addPart() {
        String name = txtName.getText().trim();
        String quantity = txtQuantity.getText().trim();
        String price = txtPrice.getText().trim();
        String minStock = txtMinStock.getText().trim();
        String supplier = cmbSupplier.getValue();
        String category = cmbCategory.getValue();

        if (name.isEmpty()) {
            showAlert("Warning", "Please enter part name");
            return;
        }

        if (quantity.isEmpty()) {
            showAlert("Warning", "Please enter quantity");
            return;
        }

        if (price.isEmpty()) {
            showAlert("Warning", "Please enter price");
            return;
        }

        try {
            Integer.parseInt(quantity);
            Double.parseDouble(price);
            if (!minStock.isEmpty()) Integer.parseInt(minStock);
        } catch (NumberFormatException e) {
            showAlert("Warning", "Quantity and price must be numbers");
            return;
        }

        String sql = String.format(
                "INSERT INTO sparepart (part_name, quantity, price, min_stock, category, supplier_id) " +
                        "VALUES ('%s', %s, %s, %s, '%s', %s)",
                name, quantity, price, minStock.isEmpty() ? "10" : minStock,
                category == null ? "" : category,
                supplier == null ? "NULL" : "(SELECT supplier_id FROM supplier WHERE supplier_name = '" + supplier + "')"
        );

        int result = DB.executeUpdate(sql);
        if (result > 0) {
            showAlert("Success", "Part added successfully");
            clearFields();
            loadParts();
        } else {
            showAlert("Error", "Failed to add part");
        }
    }

    private void editPart(Part part) {
        txtName.setText(part.getPartName());
        txtQuantity.setText(String.valueOf(part.getQuantity()));
        txtPrice.setText(String.valueOf(part.getPrice()));
        cmbCategory.setValue(part.getCategory());
        cmbSupplier.setValue(part.getSupplier());

        showAlert("Edit Mode", "Edit part details and click 'Add Part' to update");
    }

    private void deletePart(Part part) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Part");
        alert.setContentText("Are you sure you want to delete part " + part.getPartName() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String sql = "DELETE FROM sparepart WHERE part_id = " + part.getPartId();
                int result = DB.executeUpdate(sql);
                if (result > 0) {
                    showAlert("Success", "Part deleted successfully");
                    loadParts();
                }
            }
        });
    }

    private void showLowStock() {
        int lowStockCount = 0;
        StringBuilder lowStockList = new StringBuilder("Low Stock Parts:\n\n");

        for (Part part : partList) {
            if (part.getStatus().equals("LOW")) {
                lowStockCount++;
                lowStockList.append("• ").append(part.getPartName())
                        .append(" - ").append(part.getQuantity()).append(" left\n");
            }
        }

        if (lowStockCount > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Low Stock Alert");
            alert.setHeaderText(lowStockCount + " parts are low in stock");
            alert.setContentText(lowStockList.toString());
            alert.showAndWait();
        } else {
            showAlert("Stock Status", "All parts are adequately stocked");
        }
    }

    private void clearFields() {
        txtName.clear();
        txtQuantity.clear();
        txtPrice.clear();
        txtMinStock.clear();
        cmbSupplier.setValue(null);
        cmbCategory.setValue(null);
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
        private String supplier;
        private String status;

        public Part(int partId, String partName, int quantity, double price,
                    String category, String supplier, String status) {
            this.partId = partId;
            this.partName = partName;
            this.quantity = quantity;
            this.price = price;
            this.category = category;
            this.supplier = supplier;
            this.status = status;
        }

        public int getPartId() { return partId; }
        public String getPartName() { return partName; }
        public int getQuantity() { return quantity; }
        public double getPrice() { return price; }
        public String getCategory() { return category; }
        public String getSupplier() { return supplier; }
        public String getStatus() { return status; }
    }
}