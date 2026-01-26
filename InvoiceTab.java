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

public class InvoiceTab extends BorderPane {

    private TableView<Invoice> table = new TableView<>();
    private ObservableList<Invoice> invoiceList = FXCollections.observableArrayList();
    private Map<String, Integer> partQuantities = new HashMap<>();

    private ComboBox<String> cmbCustomer = new ComboBox<>();
    private ListView<String> lstParts = new ListView<>();
    private TextField txtTotal = new TextField();
    private DatePicker datePicker = new DatePicker(LocalDate.now());

    public InvoiceTab() {
        initialize();
    }

    private void initialize() {
        getStyleClass().add("window-root");

        VBox header = new VBox(8);
        header.getStyleClass().add("window-header");
        header.setPadding(new Insets(20));

        Label title = new Label("Invoice Management");
        title.getStyleClass().add("window-title");

        Label subtitle = new Label("Create and manage customer invoices (Parts only)");
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

        Label formTitle = new Label("Create New Invoice");
        formTitle.getStyleClass().add("form-title");

        VBox customerBox = new VBox(5); // Reduced spacing
        customerBox.getStyleClass().add("form-group");
        Label lblCustomer = new Label("Customer *");
        lblCustomer.getStyleClass().add("field-label");

        HBox customerRow = new HBox(5);
        customerRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        cmbCustomer.getStyleClass().add("field-combo");
        cmbCustomer.setPromptText("Select customer");
        cmbCustomer.setPrefWidth(250);
        cmbCustomer.setItems(CustomerManager.getInstance().getCustomers());

        Button btnRefreshCustomers = new Button("↻");
        btnRefreshCustomers.getStyleClass().add("btn-secondary");
        btnRefreshCustomers.setTooltip(new Tooltip("Refresh customer list"));
        btnRefreshCustomers.setOnAction(e -> {
            cmbCustomer.setItems(CustomerManager.getInstance().getCustomers());
        });

        customerRow.getChildren().addAll(cmbCustomer, btnRefreshCustomers);
        customerBox.getChildren().addAll(lblCustomer, customerRow);

        VBox partsBox = new VBox(5);
        partsBox.getStyleClass().add("form-group");
        Label lblParts = new Label("Parts");

        HBox partsControls = new HBox(10);
        lstParts.getStyleClass().add("field-list");
        lstParts.setPrefHeight(120); // Reduced
        lstParts.setMaxHeight(130);

        Button btnAddPart = new Button("Add Part");
        btnAddPart.getStyleClass().add("btn-secondary");
        btnAddPart.setPrefWidth(90);
        btnAddPart.setOnAction(e -> addPart());

        Button btnRemovePart = new Button("Remove");
        btnRemovePart.getStyleClass().add("btn-secondary");
        btnRemovePart.setPrefWidth(90);
        btnRemovePart.setOnAction(e -> removePart());

        partsControls.getChildren().addAll(btnAddPart, btnRemovePart);
        partsBox.getChildren().addAll(lblParts, lstParts, partsControls);

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
        Label lblDate = new Label("Date");
        datePicker.getStyleClass().add("field-combo");
        dateBox.getChildren().addAll(lblDate, datePicker);

        HBox formButtons = new HBox(10); // Reduced spacing
        formButtons.getStyleClass().add("form-buttons");

        Button btnCreate = new Button("Create Invoice");
        btnCreate.getStyleClass().add("btn-primary");
        btnCreate.setPrefWidth(120);
        btnCreate.setOnAction(e -> createInvoice());

        Button btnClear = new Button("Clear");
        btnClear.getStyleClass().add("btn-secondary");
        btnClear.setPrefWidth(80);
        btnClear.setOnAction(e -> clearFields());

        Button btnCalculate = new Button("Calculate");
        btnCalculate.getStyleClass().add("btn-primary");
        btnCalculate.setPrefWidth(100);
        btnCalculate.setOnAction(e -> calculateTotal());

        formButtons.getChildren().addAll(btnCreate, btnClear, btnCalculate);

        formBox.getChildren().addAll(formTitle, customerBox,
                partsBox, totalBox, dateBox, formButtons);
        content.getChildren().add(formBox);

        // Right Side - Table
        VBox tableBox = new VBox(10);
        tableBox.getStyleClass().add("table-box");
        HBox.setHgrow(tableBox, Priority.ALWAYS);

        HBox tableHeader = new HBox();
        tableHeader.getStyleClass().add("table-header-box");

        Label tableTitle = new Label("Invoice List");
        tableTitle.getStyleClass().add("table-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnRefresh = new Button("Refresh");
        btnRefresh.getStyleClass().add("btn-refresh");
        btnRefresh.setOnAction(e -> loadInvoices());

        tableHeader.getChildren().addAll(tableTitle, spacer, btnRefresh);

        createTable();
        table.setPrefHeight(400);
        VBox.setVgrow(table, Priority.ALWAYS);

        tableBox.getChildren().addAll(tableHeader, table);
        content.getChildren().add(tableBox);

        setCenter(content);
        loadInvoices();
    }

    private void createTable() {
        table.getColumns().clear();

        TableColumn<Invoice, Integer> colId = new TableColumn<>("Invoice #");
        colId.setCellValueFactory(new PropertyValueFactory<>("invoiceId"));
        colId.setPrefWidth(80); // Reduced

        TableColumn<Invoice, String> colCustomer = new TableColumn<>("Customer");
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colCustomer.setPrefWidth(120); // Reduced

        TableColumn<Invoice, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDate.setPrefWidth(90); // Reduced

        TableColumn<Invoice, Double> colAmount = new TableColumn<>("Amount ($)");
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colAmount.setPrefWidth(100); // Reduced

        TableColumn<Invoice, String> colItems = new TableColumn<>("Items");
        colItems.setCellValueFactory(new PropertyValueFactory<>("items"));
        colItems.setPrefWidth(150); // Reduced

        TableColumn<Invoice, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        colStatus.setPrefWidth(100); // Increased slightly
        colStatus.setCellFactory(column -> new TableCell<Invoice, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    updateStatusStyle(status);
                }
            }

            private void updateStatusStyle(String status) {
                if (status.equals("Unpaid")) {
                    setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                } else if (status.equals("Paid")) {
                    setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                } else if (status.equals("Partially Paid")) {
                    setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                } else {
                    setStyle("-fx-text-fill: #6b7280; -fx-font-weight: bold;");
                }
            }
        });

        TableColumn<Invoice, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(260); // Increased for 3 buttons
        colActions.setCellFactory(param -> new TableCell<Invoice, Void>() {
            private final Button btnView = new Button("View");
            private final Button btnEditStatus = new Button("Edit Status");
            private final Button btnDelete = new Button("Delete");
            private final HBox buttons = new HBox(8, btnView, btnEditStatus, btnDelete);

            {
                btnView.getStyleClass().add("btn-table-edit");
                btnEditStatus.getStyleClass().add("btn-table-edit");
                btnDelete.getStyleClass().add("btn-table-delete");
                buttons.getStyleClass().add("table-actions");

                btnView.setPrefWidth(70);
                btnEditStatus.setPrefWidth(90);
                btnDelete.setPrefWidth(70);

                btnView.setOnAction(e -> {
                    Invoice invoice = getTableView().getItems().get(getIndex());
                    viewInvoice(invoice);
                });

                btnEditStatus.setOnAction(e -> {
                    Invoice invoice = getTableView().getItems().get(getIndex());
                    editInvoiceStatus(invoice);
                });

                btnDelete.setOnAction(e -> {
                    Invoice invoice = getTableView().getItems().get(getIndex());
                    deleteInvoice(invoice);
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

        table.getColumns().addAll(colId, colCustomer, colDate, colAmount, colItems, colStatus, colActions);
        table.setItems(invoiceList);
        table.setFixedCellSize(45);
    }

    private void loadInvoices() {
        invoiceList.clear();
        try {
            ResultSet rs = DB.executeQuery(
                    "SELECT s.invoice_id, c.full_name as customer_name, s.invoice_date, s.total_amount, " +
                            "s.payment_status, " +
                            "GROUP_CONCAT(CONCAT(p.part_name, ' x', si.quantity) SEPARATOR ', ') as items " +
                            "FROM salesinvoice s " +
                            "JOIN customer c ON s.customer_id = c.customer_id " +
                            "LEFT JOIN salesinvoiceitems si ON s.invoice_id = si.invoice_id " +
                            "LEFT JOIN sparepart p ON si.part_id = p.part_id " +
                            "GROUP BY s.invoice_id " +
                            "ORDER BY s.invoice_date DESC"
            );

            if (rs != null) {
                while (rs.next()) {
                    Invoice invoice = new Invoice(
                            rs.getInt("invoice_id"),
                            rs.getString("customer_name"),
                            rs.getString("invoice_date"),
                            rs.getDouble("total_amount"),
                            rs.getString("items") != null ? rs.getString("items") : "No items",
                            rs.getString("payment_status")
                    );
                    invoiceList.add(invoice);
                }
                rs.close();
            }
        } catch (Exception e) {
            showAlert("Error", "Error loading invoices: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addPart() {
        try {
            ResultSet rs = DB.executeQuery(
                    "SELECT part_name, price, quantity FROM sparepart WHERE quantity > 0 ORDER BY part_name"
            );

            ChoiceDialog<String> dialog = new ChoiceDialog<>();
            dialog.setTitle("Add Part");
            dialog.setHeaderText("Select a part");
            dialog.setContentText("Part:");

            if (rs != null) {
                while (rs.next()) {
                    String partName = rs.getString("part_name");
                    double price = rs.getDouble("price");
                    int quantity = rs.getInt("quantity");
                    dialog.getItems().add(partName + " - $" + String.format("%.2f", price) + " (Stock: " + quantity + ")");
                }
                rs.close();
            }

            dialog.showAndWait().ifPresent(part -> {
                lstParts.getItems().add(part);
                calculateTotal();
            });

        } catch (Exception e) {
            showAlert("Error", "Error loading parts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void removePart() {
        int selected = lstParts.getSelectionModel().getSelectedIndex();
        if (selected >= 0) {
            lstParts.getItems().remove(selected);
            calculateTotal();
        }
    }

    private void calculateTotal() {
        double total = 0.0;

        for (String part : lstParts.getItems()) {
            try {
                String[] parts = part.split(" - \\$");
                if (parts.length > 1) {
                    total += Double.parseDouble(parts[1].split(" ")[0]);
                }
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }

        txtTotal.setText(String.format("%.2f", total));
    }

    private void createInvoice() {
        String customer = cmbCustomer.getValue();
        String date = datePicker.getValue().toString();
        String total = txtTotal.getText().trim();

        if (customer == null || customer.isEmpty()) {
            showAlert("Warning", "Please select a customer");
            return;
        }

        try {
            // التحقق من وجود العميل
            ResultSet checkRs = DB.executeQuery(
                    "SELECT customer_id FROM customer WHERE full_name = '" + customer + "'"
            );
            if (checkRs == null || !checkRs.next()) {
                showAlert("Error", "Customer not found in database. Please refresh customer list.");
                if (checkRs != null) checkRs.close();
                return;
            }
            int customerId = checkRs.getInt("customer_id");
            checkRs.close();

            if (total.isEmpty() || total.equals("0.00")) {
                showAlert("Warning", "Please add parts to the invoice");
                return;
            }

            // 1. إدخال الفاتورة الرئيسية
            PreparedStatement pstmt = DB.prepareStatement(
                    "INSERT INTO salesinvoice (customer_id, invoice_date, total_amount, payment_status) VALUES (?, ?, ?, 'Unpaid')"
            );
            pstmt.setInt(1, customerId);
            pstmt.setString(2, date);
            pstmt.setDouble(3, Double.parseDouble(total));

            int result = DB.executeUpdate(pstmt);
            if (result > 0) {
                int invoiceId = DB.getLastInsertId();
                boolean allPartsAdded = true;

                // 2. إدخال عناصر الفاتورة
                for (String part : lstParts.getItems()) {
                    try {
                        String[] parts = part.split(" - \\$");
                        if (parts.length > 1) {
                            String partName = parts[0].trim();
                            double price = Double.parseDouble(parts[1].split(" ")[0]);

                            ResultSet rs = DB.executeQuery(
                                    "SELECT part_id, quantity FROM sparepart WHERE part_name = '" + partName + "'"
                            );
                            if (rs != null && rs.next()) {
                                int partId = rs.getInt("part_id");
                                int availableQuantity = rs.getInt("quantity");

                                if (availableQuantity > 0) {
                                    // إدخال العنصر في salesinvoiceitems
                                    PreparedStatement itemStmt = DB.prepareStatement(
                                            "INSERT INTO salesinvoiceitems (invoice_id, part_id, quantity, price) VALUES (?, ?, ?, ?)"
                                    );
                                    itemStmt.setInt(1, invoiceId);
                                    itemStmt.setInt(2, partId);
                                    itemStmt.setInt(3, 1);
                                    itemStmt.setDouble(4, price);
                                    DB.executeUpdate(itemStmt);

                                    // تقليل الكمية من المخزون
                                    DB.executeUpdate(
                                            "UPDATE sparepart SET quantity = quantity - 1 WHERE part_id = " + partId
                                    );
                                } else {
                                    showAlert("Warning", "Part " + partName + " is out of stock!");
                                    allPartsAdded = false;
                                }
                                rs.close();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        allPartsAdded = false;
                    }
                }

                if (allPartsAdded) {
                    showAlert("Success", "Invoice #" + invoiceId + " created successfully");
                    clearFields();
                    loadInvoices();
                    Main.refreshDashboardGlobal();
                    DashboardManager.getInstance().refreshLowStockParts();
                } else {
                    showAlert("Partial Success", "Invoice created but some parts were not available");
                }
            } else {
                showAlert("Error", "Failed to create invoice");
            }
        } catch (Exception e) {
            showAlert("Error", "Error creating invoice: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void viewInvoice(Invoice invoice) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Invoice Details");
        alert.setHeaderText("Invoice #" + invoice.getInvoiceId());
        alert.setContentText(
                "Customer: " + invoice.getCustomerName() + "\n" +
                        "Date: " + invoice.getDate() + "\n" +
                        "Amount: $" + String.format("%.2f", invoice.getAmount()) + "\n" +
                        "Status: " + invoice.getPaymentStatus() + "\n" +
                        "Items: " + invoice.getItems()
        );
        alert.showAndWait();
    }

    private void editInvoiceStatus(Invoice invoice) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(
                invoice.getPaymentStatus(), // Default value
                "Unpaid", "Paid", "Partially Paid"
        );

        dialog.setTitle("Edit Payment Status");
        dialog.setHeaderText("Invoice #" + invoice.getInvoiceId());
        dialog.setContentText("Select new payment status:");

        dialog.showAndWait().ifPresent(newStatus -> {
            if (newStatus.equals(invoice.getPaymentStatus())) {
                return; // No change
            }

            try {
                // Update in database
                String updateSql = "UPDATE salesinvoice SET payment_status = ? WHERE invoice_id = ?";
                PreparedStatement pstmt = DB.prepareStatement(updateSql);
                pstmt.setString(1, newStatus);
                pstmt.setInt(2, invoice.getInvoiceId());

                int result = DB.executeUpdate(pstmt);
                if (result > 0) {
                    // Update local invoice object
                    invoice.setPaymentStatus(newStatus);

                    // Refresh the table to show updated status
                    table.refresh();

                    showAlert("Success", "Payment status updated to: " + newStatus);

                    // Refresh dashboard if needed
                    Main.refreshDashboardGlobal();
                } else {
                    showAlert("Error", "Failed to update payment status");
                }
            } catch (Exception e) {
                showAlert("Error", "Error updating payment status: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void deleteInvoice(Invoice invoice) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Invoice");
        alert.setContentText("Are you sure you want to delete invoice #" + invoice.getInvoiceId() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // استعادة الكميات للمخزون أولاً
                    ResultSet items = DB.executeQuery(
                            "SELECT part_id, quantity FROM salesinvoiceitems WHERE invoice_id = " + invoice.getInvoiceId()
                    );

                    if (items != null) {
                        while (items.next()) {
                            int partId = items.getInt("part_id");
                            int quantity = items.getInt("quantity");
                            DB.executeUpdate(
                                    "UPDATE sparepart SET quantity = quantity + " + quantity + " WHERE part_id = " + partId
                            );
                        }
                        items.close();
                    }

                    // حذف العناصر ثم الفاتورة
                    DB.executeUpdate("DELETE FROM salesinvoiceitems WHERE invoice_id = " + invoice.getInvoiceId());
                    DB.executeUpdate("DELETE FROM salesinvoice WHERE invoice_id = " + invoice.getInvoiceId());

                    showAlert("Success", "Invoice deleted successfully");
                    loadInvoices();
                    Main.refreshDashboardGlobal();
                    DashboardManager.getInstance().refreshLowStockParts();
                } catch (Exception e) {
                    showAlert("Error", "Error deleting invoice: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void clearFields() {
        cmbCustomer.setValue(null);
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

    public static class Invoice {
        private int invoiceId;
        private String customerName;
        private String date;
        private double amount;
        private String items;
        private String paymentStatus;

        public Invoice(int invoiceId, String customerName, String date, double amount, String items, String paymentStatus) {
            this.invoiceId = invoiceId;
            this.customerName = customerName;
            this.date = date;
            this.amount = amount;
            this.items = items;
            this.paymentStatus = paymentStatus;
        }

        public int getInvoiceId() { return invoiceId; }
        public String getCustomerName() { return customerName; }
        public String getDate() { return date; }
        public double getAmount() { return amount; }
        public String getItems() { return items; }
        public String getPaymentStatus() { return paymentStatus; }

        // Setter for payment status
        public void setPaymentStatus(String paymentStatus) {
            this.paymentStatus = paymentStatus;
        }
    }
}