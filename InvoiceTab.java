import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.sql.ResultSet;
import java.time.LocalDate;

public class InvoiceTab extends BorderPane {

    private TableView<Invoice> table = new TableView<>();
    private ObservableList<Invoice> invoiceList = FXCollections.observableArrayList();

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

        GridPane content = new GridPane();
        content.getStyleClass().add("window-content");
        content.setPadding(new Insets(25));
        content.setVgap(20);
        content.setHgap(20);

        VBox formBox = new VBox(20);
        formBox.getStyleClass().add("form-box");
        formBox.setPrefWidth(400);

        Label formTitle = new Label("Create New Invoice");
        formTitle.getStyleClass().add("form-title");

        VBox customerBox = new VBox(8);
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

        VBox partsBox = new VBox(8);
        partsBox.getStyleClass().add("form-group");
        Label lblParts = new Label("Parts");

        HBox partsControls = new HBox(10);
        lstParts.getStyleClass().add("field-list");
        lstParts.setPrefHeight(150);

        Button btnAddPart = new Button("Add Part");
        btnAddPart.getStyleClass().add("btn-secondary");
        btnAddPart.setOnAction(e -> addPart());

        Button btnRemovePart = new Button("Remove");
        btnRemovePart.getStyleClass().add("btn-secondary");
        btnRemovePart.setOnAction(e -> removePart());

        partsControls.getChildren().addAll(btnAddPart, btnRemovePart);
        partsBox.getChildren().addAll(lblParts, lstParts, partsControls);

        VBox totalBox = new VBox(8);
        totalBox.getStyleClass().add("form-group");
        Label lblTotal = new Label("Total Amount ($)");
        txtTotal.getStyleClass().add("field-input");
        txtTotal.setPromptText("0.00");
        txtTotal.setEditable(false);
        totalBox.getChildren().addAll(lblTotal, txtTotal);

        VBox dateBox = new VBox(8);
        dateBox.getStyleClass().add("form-group");
        Label lblDate = new Label("Date");
        datePicker.getStyleClass().add("field-combo");
        dateBox.getChildren().addAll(lblDate, datePicker);

        HBox formButtons = new HBox(15);
        formButtons.getStyleClass().add("form-buttons");

        Button btnCreate = new Button("Create Invoice");
        btnCreate.getStyleClass().add("btn-primary");
        btnCreate.setOnAction(e -> createInvoice());

        Button btnClear = new Button("Clear");
        btnClear.getStyleClass().add("btn-secondary");
        btnClear.setOnAction(e -> clearFields());

        Button btnCalculate = new Button("Calculate Total");
        btnCalculate.getStyleClass().add("btn-primary");
        btnCalculate.setOnAction(e -> calculateTotal());

        formButtons.getChildren().addAll(btnCreate, btnClear, btnCalculate);

        formBox.getChildren().addAll(formTitle, customerBox,
                partsBox, totalBox, dateBox, formButtons);
        content.add(formBox, 0, 0);

        VBox tableBox = new VBox(15);
        tableBox.getStyleClass().add("table-box");

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

        tableBox.getChildren().addAll(tableHeader, table);
        content.add(tableBox, 1, 0);

        setCenter(content);
        loadInvoices();
    }

    private void createTable() {
        table.getColumns().clear();

        TableColumn<Invoice, Integer> colId = new TableColumn<>("Invoice #");
        colId.setCellValueFactory(new PropertyValueFactory<>("invoiceId"));
        colId.setPrefWidth(100);

        TableColumn<Invoice, String> colCustomer = new TableColumn<>("Customer");
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colCustomer.setPrefWidth(150);

        TableColumn<Invoice, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDate.setPrefWidth(100);

        TableColumn<Invoice, Double> colAmount = new TableColumn<>("Amount ($)");
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colAmount.setPrefWidth(120);

        TableColumn<Invoice, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(150);
        colActions.setCellFactory(param -> new TableCell<Invoice, Void>() {
            private final Button btnView = new Button("View");
            private final Button btnDelete = new Button("Delete");
            private final HBox buttons = new HBox(8, btnView, btnDelete);

            {
                btnView.getStyleClass().add("btn-table-edit");
                btnDelete.getStyleClass().add("btn-table-delete");
                buttons.getStyleClass().add("table-actions");

                btnView.setOnAction(e -> {
                    Invoice invoice = getTableView().getItems().get(getIndex());
                    viewInvoice(invoice);
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

        table.getColumns().addAll(colId, colCustomer, colDate, colAmount, colActions);
        table.setItems(invoiceList);
        table.setFixedCellSize(45);
    }

    private void loadInvoices() {
        invoiceList.clear();
        try {
            ResultSet rs = DB.executeQuery(
                    "SELECT s.invoice_id, c.full_name as customer_name, s.invoice_date, s.total_amount " +
                            "FROM salesinvoice s JOIN customer c ON s.customer_id = c.customer_id " +
                            "ORDER BY s.invoice_date DESC"
            );

            if (rs != null) {
                while (rs.next()) {
                    Invoice invoice = new Invoice(
                            rs.getInt("invoice_id"),
                            rs.getString("customer_name"),
                            rs.getString("invoice_date"),
                            rs.getDouble("total_amount")
                    );
                    invoiceList.add(invoice);
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Error loading invoices: " + e.getMessage());
        }
    }

    private void addPart() {
        try {
            ResultSet rs = DB.executeQuery("SELECT part_name, price FROM sparepart");

            ChoiceDialog<String> dialog = new ChoiceDialog<>();
            dialog.setTitle("Add Part");
            dialog.setHeaderText("Select a part");
            dialog.setContentText("Part:");

            while (rs != null && rs.next()) {
                dialog.getItems().add(rs.getString("part_name") + " - $" + rs.getDouble("price"));
            }

            dialog.showAndWait().ifPresent(part -> {
                lstParts.getItems().add(part);
                calculateTotal();
            });

        } catch (Exception e) {
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
                    total += Double.parseDouble(parts[1]);
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
            ResultSet checkRs = DB.executeQuery(
                    "SELECT customer_id FROM customer WHERE full_name = '" + customer + "'"
            );
            if (checkRs == null || !checkRs.next()) {
                showAlert("Error", "Customer not found in database. Please refresh customer list.");
                return;
            }
        } catch (Exception e) {
            showAlert("Error", "Error verifying customer: " + e.getMessage());
            return;
        }

        if (total.isEmpty() || total.equals("0.00")) {
            showAlert("Warning", "Please add parts to the invoice");
            return;
        }

        String sql = String.format(
                "INSERT INTO salesinvoice (customer_id, invoice_date, total_amount) " +
                        "VALUES ((SELECT customer_id FROM customer WHERE full_name = '%s'), '%s', %s)",
                customer, date, total
        );

        int result = DB.executeUpdate(sql);
        if (result > 0) {
            int invoiceId = DB.getLastInsertId();

            for (String part : lstParts.getItems()) {
                try {
                    String[] parts = part.split(" - \\$");
                    if (parts.length > 1) {
                        String partName = parts[0];
                        String price = parts[1];

                        ResultSet rs = DB.executeQuery(
                                "SELECT part_id FROM sparepart WHERE part_name = '" + partName + "'"
                        );
                        if (rs != null && rs.next()) {
                            int partId = rs.getInt("part_id");
                            String itemSql = String.format(
                                    "INSERT INTO salesinvoiceitems (invoice_id, part_id, quantity, price) " +
                                            "VALUES (%d, %d, 1, %s)",
                                    invoiceId, partId, price
                            );
                            DB.executeUpdate(itemSql);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            showAlert("Success", "Invoice #" + invoiceId + " created successfully");
            clearFields();
            loadInvoices();
            Main.refreshDashboardGlobal();
        } else {
            showAlert("Error", "Failed to create invoice");
        }
    }

    private void viewInvoice(Invoice invoice) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Invoice Details");
        alert.setHeaderText("Invoice #" + invoice.getInvoiceId());
        alert.setContentText(
                "Customer: " + invoice.getCustomerName() + "\n" +
                        "Date: " + invoice.getDate() + "\n" +
                        "Amount: $" + invoice.getAmount()
        );
        alert.showAndWait();
    }

    private void deleteInvoice(Invoice invoice) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Invoice");
        alert.setContentText("Are you sure you want to delete invoice #" + invoice.getInvoiceId() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                DB.executeUpdate("DELETE FROM salesinvoiceitems WHERE invoice_id = " + invoice.getInvoiceId());

                String sql = "DELETE FROM salesinvoice WHERE invoice_id = " + invoice.getInvoiceId();
                int result = DB.executeUpdate(sql);
                if (result > 0) {
                    showAlert("Success", "Invoice deleted successfully");
                    loadInvoices();
                    Main.refreshDashboardGlobal();
                }
            }
        });
    }

    private void clearFields() {
        cmbCustomer.setValue(null);
        lstParts.getItems().clear();
        txtTotal.clear();
        datePicker.setValue(LocalDate.now());
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

        public Invoice(int invoiceId, String customerName, String date, double amount) {
            this.invoiceId = invoiceId;
            this.customerName = customerName;
            this.date = date;
            this.amount = amount;
        }

        public int getInvoiceId() { return invoiceId; }
        public String getCustomerName() { return customerName; }
        public String getDate() { return date; }
        public double getAmount() { return amount; }
    }
}