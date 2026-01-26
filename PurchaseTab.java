import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class PurchaseTab extends BorderPane {

    private TableView<PurchaseInvoice> table = new TableView<>();
    private ObservableList<PurchaseInvoice> purchaseList = FXCollections.observableArrayList();
    private Map<String, Integer> partQuantities = new HashMap<>();

    private ComboBox<String> cmbSupplier = new ComboBox<>();
    private ListView<String> lstParts = new ListView<>();
    private TextField txtTotal = new TextField();
    private DatePicker datePicker = new DatePicker(LocalDate.now());

    public PurchaseTab() {
        initialize();
    }

    private void initialize() {
        getStyleClass().add("window-root");

        VBox header = new VBox(8);
        header.getStyleClass().add("window-header");
        header.setPadding(new Insets(20));

        Label title = new Label("Purchase Management");
        title.getStyleClass().add("window-title");

        Label subtitle = new Label("Purchase spare parts from suppliers");
        subtitle.getStyleClass().add("window-subtitle");

        header.getChildren().addAll(title, subtitle);
        setTop(header);

        // Use HBox instead of GridPane
        HBox content = new HBox(25);
        content.getStyleClass().add("window-content");
        content.setPadding(new Insets(20));

        // Left Side - Form
        VBox formBox = new VBox(15); // Reduced spacing
        formBox.getStyleClass().add("form-box");
        formBox.setPrefWidth(320);
        formBox.setMinWidth(300);
        formBox.setMaxWidth(350);

        Label formTitle = new Label("New Purchase Invoice");
        formTitle.getStyleClass().add("form-title");

        // Supplier Selection
        VBox supplierBox = new VBox(5); // Reduced spacing
        supplierBox.getStyleClass().add("form-group");
        Label lblSupplier = new Label("Supplier *");
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

        // Parts Selection
        VBox partsBox = new VBox(5);
        partsBox.getStyleClass().add("form-group");
        Label lblParts = new Label("Parts to Purchase");

        HBox partsControls = new HBox(10);
        lstParts.getStyleClass().add("field-list");
        lstParts.setPrefHeight(120); // Reduced
        lstParts.setMaxHeight(130);

        Button btnAddPart = new Button("Add Part");
        btnAddPart.getStyleClass().add("btn-secondary");
        btnAddPart.setPrefWidth(90);
        btnAddPart.setOnAction(e -> addPurchasePart());

        Button btnRemovePart = new Button("Remove");
        btnRemovePart.getStyleClass().add("btn-secondary");
        btnRemovePart.setPrefWidth(90);
        btnRemovePart.setOnAction(e -> removePurchasePart());

        partsControls.getChildren().addAll(btnAddPart, btnRemovePart);
        partsBox.getChildren().addAll(lblParts, lstParts, partsControls);

        // Total and Date
        VBox totalBox = new VBox(5);
        totalBox.getStyleClass().add("form-group");
        Label lblTotal = new Label("Total Amount ($)");
        txtTotal.getStyleClass().add("field-input");
        txtTotal.setPromptText("0.00");
        txtTotal.setEditable(false);
        txtTotal.setPrefHeight(32);
        totalBox.getChildren().addAll(lblTotal, txtTotal);

        VBox dateBox = new VBox(5);
        dateBox.getStyleClass().add("form-group");
        Label lblDate = new Label("Purchase Date");
        datePicker.getStyleClass().add("field-combo");
        dateBox.getChildren().addAll(lblDate, datePicker);

        // Buttons
        HBox formButtons = new HBox(10); // Reduced spacing
        formButtons.getStyleClass().add("form-buttons");

        Button btnCreate = new Button("Create Purchase");
        btnCreate.getStyleClass().add("btn-primary");
        btnCreate.setPrefWidth(140);
        btnCreate.setOnAction(e -> createPurchaseInvoice());

        Button btnClear = new Button("Clear");
        btnClear.getStyleClass().add("btn-secondary");
        btnClear.setPrefWidth(80);
        btnClear.setOnAction(e -> clearFields());

        Button btnCalculate = new Button("Calculate");
        btnCalculate.getStyleClass().add("btn-primary");
        btnCalculate.setPrefWidth(100);
        btnCalculate.setOnAction(e -> calculatePurchaseTotal());

        formButtons.getChildren().addAll(btnCreate, btnClear, btnCalculate);

        formBox.getChildren().addAll(formTitle, supplierBox, partsBox, totalBox, dateBox, formButtons);
        content.getChildren().add(formBox);

        // Right Side - Table
        VBox tableBox = new VBox(10);
        tableBox.getStyleClass().add("table-box");
        HBox.setHgrow(tableBox, Priority.ALWAYS);

        HBox tableHeader = new HBox();
        tableHeader.getStyleClass().add("table-header-box");

        Label tableTitle = new Label("Purchase History");
        tableTitle.getStyleClass().add("table-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnRefresh = new Button("Refresh");
        btnRefresh.getStyleClass().add("btn-refresh");
        btnRefresh.setOnAction(e -> loadPurchases());

        tableHeader.getChildren().addAll(tableTitle, spacer, btnRefresh);

        createTable();
        table.setPrefHeight(400);
        VBox.setVgrow(table, Priority.ALWAYS);

        tableBox.getChildren().addAll(tableHeader, table);
        content.getChildren().add(tableBox);

        setCenter(content);
        loadPurchases();
    }

    private void createTable() {
        table.getColumns().clear();

        TableColumn<PurchaseInvoice, Integer> colId = new TableColumn<>("Purchase #");
        colId.setCellValueFactory(new PropertyValueFactory<>("purchaseId"));
        colId.setPrefWidth(80); // Reduced

        TableColumn<PurchaseInvoice, String> colSupplier = new TableColumn<>("Supplier");
        colSupplier.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        colSupplier.setPrefWidth(120); // Reduced

        TableColumn<PurchaseInvoice, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDate.setPrefWidth(90); // Reduced

        TableColumn<PurchaseInvoice, Double> colAmount = new TableColumn<>("Amount ($)");
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colAmount.setPrefWidth(100); // Reduced

        TableColumn<PurchaseInvoice, String> colItems = new TableColumn<>("Items");
        colItems.setCellValueFactory(new PropertyValueFactory<>("items"));
        colItems.setPrefWidth(150); // Reduced

        TableColumn<PurchaseInvoice, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(180); // Increased
        colActions.setCellFactory(param -> new TableCell<PurchaseInvoice, Void>() {
            private final Button btnView = new Button("View");
            private final Button btnDelete = new Button("Delete");
            private final HBox buttons = new HBox(10, btnView, btnDelete);

            {
                btnView.getStyleClass().add("btn-table-edit");
                btnDelete.getStyleClass().add("btn-table-delete");
                buttons.getStyleClass().add("table-actions");

                btnView.setPrefWidth(70);
                btnDelete.setPrefWidth(70);

                btnView.setOnAction(e -> {
                    PurchaseInvoice purchase = getTableView().getItems().get(getIndex());
                    viewPurchase(purchase);
                });

                btnDelete.setOnAction(e -> {
                    PurchaseInvoice purchase = getTableView().getItems().get(getIndex());
                    deletePurchase(purchase);
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

        table.getColumns().addAll(colId, colSupplier, colDate, colAmount, colItems, colActions);
        table.setItems(purchaseList);
        table.setFixedCellSize(45);
    }

    private void loadPurchases() {
        purchaseList.clear();
        try {
            ResultSet rs = DB.executeQuery(
                    "SELECT p.purchase_id, s.supplier_name, p.purchase_date, p.total_amount, " +
                            "GROUP_CONCAT(CONCAT(sp.part_name, ' x', pi.quantity) SEPARATOR ', ') as items " +
                            "FROM purchaseinvoice p " +
                            "JOIN supplier s ON p.supplier_id = s.supplier_id " +
                            "LEFT JOIN purchaseinvoiceitems pi ON p.purchase_id = pi.purchase_id " +
                            "LEFT JOIN sparepart sp ON pi.part_id = sp.part_id " +
                            "GROUP BY p.purchase_id " +
                            "ORDER BY p.purchase_date DESC"
            );

            if (rs != null) {
                while (rs.next()) {
                    PurchaseInvoice purchase = new PurchaseInvoice(
                            rs.getInt("purchase_id"),
                            rs.getString("supplier_name"),
                            rs.getString("purchase_date"),
                            rs.getDouble("total_amount"),
                            rs.getString("items") != null ? rs.getString("items") : "No items"
                    );
                    purchaseList.add(purchase);
                }
                rs.close();
            }
        } catch (Exception e) {
            showAlert("Error", "Error loading purchases: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addPurchasePart() {
        try {
            ResultSet rs = DB.executeQuery(
                    "SELECT part_name, price FROM sparepart ORDER BY part_name"
            );

            ChoiceDialog<String> dialog = new ChoiceDialog<>();
            dialog.setTitle("Add Part to Purchase");
            dialog.setHeaderText("Select a part to purchase");
            dialog.setContentText("Part:");

            if (rs != null) {
                while (rs.next()) {
                    String partName = rs.getString("part_name");
                    double price = rs.getDouble("price");
                    dialog.getItems().add(partName + " - $" + String.format("%.2f", price));
                }
                rs.close();
            }

            dialog.showAndWait().ifPresent(part -> {
                lstParts.getItems().add(part);
                calculatePurchaseTotal();
            });

        } catch (Exception e) {
            showAlert("Error", "Error loading parts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void removePurchasePart() {
        int selected = lstParts.getSelectionModel().getSelectedIndex();
        if (selected >= 0) {
            lstParts.getItems().remove(selected);
            calculatePurchaseTotal();
        }
    }

    private void calculatePurchaseTotal() {
        double total = 0.0;

        for (String part : lstParts.getItems()) {
            try {
                String[] parts = part.split(" - \\$");
                if (parts.length > 1) {
                    total += Double.parseDouble(parts[1]);
                }
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }

        txtTotal.setText(String.format("%.2f", total));
    }

    private void createPurchaseInvoice() {
        String supplier = cmbSupplier.getValue();
        String date = datePicker.getValue().toString();
        String total = txtTotal.getText().trim();

        if (supplier == null || supplier.isEmpty()) {
            showAlert("Warning", "Please select a supplier");
            return;
        }

        try {
            // Get supplier ID
            ResultSet supplierRs = DB.executeQuery(
                    "SELECT supplier_id FROM supplier WHERE supplier_name = '" + supplier + "'"
            );
            if (supplierRs == null || !supplierRs.next()) {
                showAlert("Error", "Supplier not found");
                return;
            }
            int supplierId = supplierRs.getInt("supplier_id");
            supplierRs.close();

            if (total.isEmpty() || total.equals("0.00")) {
                showAlert("Warning", "Please add parts to the purchase");
                return;
            }

            // Create purchase invoice
            PreparedStatement pstmt = DB.prepareStatement(
                    "INSERT INTO purchaseinvoice (supplier_id, purchase_date, total_amount) VALUES (?, ?, ?)"
            );
            pstmt.setInt(1, supplierId);
            pstmt.setString(2, date);
            pstmt.setDouble(3, Double.parseDouble(total));

            int result = DB.executeUpdate(pstmt);
            if (result > 0) {
                int purchaseId = DB.getLastInsertId();

                // Add purchase items
                for (String part : lstParts.getItems()) {
                    try {
                        String[] parts = part.split(" - \\$");
                        if (parts.length > 1) {
                            String partName = parts[0].trim();
                            double price = Double.parseDouble(parts[1]);

                            ResultSet partRs = DB.executeQuery(
                                    "SELECT part_id FROM sparepart WHERE part_name = '" + partName + "'"
                            );
                            if (partRs != null && partRs.next()) {
                                int partId = partRs.getInt("part_id");

                                // Add to purchaseinvoiceitems
                                PreparedStatement itemStmt = DB.prepareStatement(
                                        "INSERT INTO purchaseinvoiceitems (purchase_id, part_id, quantity, price) VALUES (?, ?, ?, ?)"
                                );
                                itemStmt.setInt(1, purchaseId);
                                itemStmt.setInt(2, partId);
                                itemStmt.setInt(3, 1);
                                itemStmt.setDouble(4, price);
                                DB.executeUpdate(itemStmt);

                                // Update sparepart quantity
                                DB.executeUpdate(
                                        "UPDATE sparepart SET quantity = quantity + 1 WHERE part_id = " + partId
                                );

                                partRs.close();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                showAlert("Success", "Purchase invoice #" + purchaseId + " created successfully");
                clearFields();
                loadPurchases();
                Main.refreshDashboardGlobal();
            }
        } catch (Exception e) {
            showAlert("Error", "Error creating purchase: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void viewPurchase(PurchaseInvoice purchase) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Purchase Details");
        alert.setHeaderText("Purchase Invoice #" + purchase.getPurchaseId());
        alert.setContentText(
                "Supplier: " + purchase.getSupplierName() + "\n" +
                        "Date: " + purchase.getDate() + "\n" +
                        "Amount: $" + purchase.getAmount() + "\n" +
                        "Items: " + purchase.getItems()
        );
        alert.showAndWait();
    }

    private void deletePurchase(PurchaseInvoice purchase) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Purchase Invoice");
        alert.setContentText("Are you sure you want to delete purchase invoice #" + purchase.getPurchaseId() + "?\n\n" +
                "This will also remove the purchased parts from inventory.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Restore quantities to inventory first
                    ResultSet items = DB.executeQuery(
                            "SELECT part_id, quantity FROM purchaseinvoiceitems WHERE purchase_id = " + purchase.getPurchaseId()
                    );

                    if (items != null) {
                        while (items.next()) {
                            int partId = items.getInt("part_id");
                            int quantity = items.getInt("quantity");
                            DB.executeUpdate(
                                    "UPDATE sparepart SET quantity = quantity - " + quantity + " WHERE part_id = " + partId
                            );
                        }
                        items.close();
                    }

                    // Delete purchase items and then the purchase
                    DB.executeUpdate("DELETE FROM purchaseinvoiceitems WHERE purchase_id = " + purchase.getPurchaseId());
                    DB.executeUpdate("DELETE FROM purchaseinvoice WHERE purchase_id = " + purchase.getPurchaseId());

                    showAlert("Success", "Purchase deleted successfully");
                    loadPurchases();
                    Main.refreshDashboardGlobal();
                } catch (Exception e) {
                    showAlert("Error", "Error deleting purchase: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void clearFields() {
        cmbSupplier.setValue(null);
        lstParts.getItems().clear();
        txtTotal.clear();
        datePicker.setValue(LocalDate.now());
        partQuantities.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class PurchaseInvoice {
        private int purchaseId;
        private String supplierName;
        private String date;
        private double amount;
        private String items;

        public PurchaseInvoice(int purchaseId, String supplierName, String date, double amount, String items) {
            this.purchaseId = purchaseId;
            this.supplierName = supplierName;
            this.date = date;
            this.amount = amount;
            this.items = items;
        }

        public int getPurchaseId() { return purchaseId; }
        public String getSupplierName() { return supplierName; }
        public String getDate() { return date; }
        public double getAmount() { return amount; }
        public String getItems() { return items; }
    }
}