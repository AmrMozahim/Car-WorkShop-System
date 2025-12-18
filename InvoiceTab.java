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
    private ComboBox<String> cmbVehicle = new ComboBox<>();
    private ListView<String> lstServices = new ListView<>();
    private ListView<String> lstParts = new ListView<>();
    private TextField txtTotal = new TextField();
    private DatePicker datePicker = new DatePicker(LocalDate.now());
    private TextArea txtNotes = new TextArea();

    public InvoiceTab() {
        initialize();
    }

    private void initialize() {
        getStyleClass().add("window-root");

        // Header
        VBox header = new VBox(8);
        header.getStyleClass().add("window-header");
        header.setPadding(new Insets(20));

        Label title = new Label("Invoice Management");
        title.getStyleClass().add("window-title");

        Label subtitle = new Label("Create and manage customer invoices");
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
        formBox.setPrefWidth(400);

        Label formTitle = new Label("Create New Invoice");
        formTitle.getStyleClass().add("form-title");

        // Customer Field
        VBox customerBox = new VBox(8);
        customerBox.getStyleClass().add("form-group");
        Label lblCustomer = new Label("Customer *");
        lblCustomer.getStyleClass().add("field-label");
        cmbCustomer.getStyleClass().add("field-combo");
        cmbCustomer.setPromptText("Select customer");
        cmbCustomer.setOnAction(e -> loadCustomerVehicles());
        loadCustomers();
        customerBox.getChildren().addAll(lblCustomer, cmbCustomer);

        // Vehicle Field
        VBox vehicleBox = new VBox(8);
        vehicleBox.getStyleClass().add("form-group");
        Label lblVehicle = new Label("Vehicle");
        lblVehicle.getStyleClass().add("field-label");
        cmbVehicle.getStyleClass().add("field-combo");
        cmbVehicle.setPromptText("Select vehicle");
        vehicleBox.getChildren().addAll(lblVehicle, cmbVehicle);

        // Services Field
        VBox servicesBox = new VBox(8);
        servicesBox.getStyleClass().add("form-group");
        Label lblServices = new Label("Services");
        lblServices.getStyleClass().add("field-label");

        HBox servicesControls = new HBox(10);
        lstServices.getStyleClass().add("field-list");
        lstServices.setPrefHeight(120);

        Button btnAddService = new Button("Add");
        btnAddService.getStyleClass().add("btn-secondary");
        btnAddService.setOnAction(e -> addService());

        Button btnRemoveService = new Button("Remove");
        btnRemoveService.getStyleClass().add("btn-secondary");
        btnRemoveService.setOnAction(e -> removeService());

        servicesControls.getChildren().addAll(btnAddService, btnRemoveService);
        servicesBox.getChildren().addAll(lblServices, lstServices, servicesControls);

        // Parts Field
        VBox partsBox = new VBox(8);
        partsBox.getStyleClass().add("form-group");
        Label lblParts = new Label("Parts");
        lblParts.getStyleClass().add("field-label");

        HBox partsControls = new HBox(10);
        lstParts.getStyleClass().add("field-list");
        lstParts.setPrefHeight(120);

        Button btnAddPart = new Button("Add");
        btnAddPart.getStyleClass().add("btn-secondary");
        btnAddPart.setOnAction(e -> addPart());

        Button btnRemovePart = new Button("Remove");
        btnRemovePart.getStyleClass().add("btn-secondary");
        btnRemovePart.setOnAction(e -> removePart());

        partsControls.getChildren().addAll(btnAddPart, btnRemovePart);
        partsBox.getChildren().addAll(lblParts, lstParts, partsControls);

        // Total Field
        VBox totalBox = new VBox(8);
        totalBox.getStyleClass().add("form-group");
        Label lblTotal = new Label("Total Amount ($)");
        lblTotal.getStyleClass().add("field-label");
        txtTotal.getStyleClass().add("field-input");
        txtTotal.setPromptText("0.00");
        txtTotal.setEditable(false);
        totalBox.getChildren().addAll(lblTotal, txtTotal);

        // Date Field
        VBox dateBox = new VBox(8);
        dateBox.getStyleClass().add("form-group");
        Label lblDate = new Label("Date");
        lblDate.getStyleClass().add("field-label");
        datePicker.getStyleClass().add("field-combo");
        dateBox.getChildren().addAll(lblDate, datePicker);

        // Notes Field
        VBox notesBox = new VBox(8);
        notesBox.getStyleClass().add("form-group");
        Label lblNotes = new Label("Notes");
        lblNotes.getStyleClass().add("field-label");
        txtNotes.getStyleClass().add("field-textarea");
        txtNotes.setPromptText("Additional notes...");
        txtNotes.setPrefRowCount(3);
        notesBox.getChildren().addAll(lblNotes, txtNotes);

        // Buttons
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

        formBox.getChildren().addAll(formTitle, customerBox, vehicleBox,
                servicesBox, partsBox, totalBox,
                dateBox, notesBox, formButtons);
        content.add(formBox, 0, 0);

        // Right - Table
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

    private void loadCustomers() {
        try {
            ResultSet rs = DB.getCustomers();
            while (rs.next()) {
                cmbCustomer.getItems().add(rs.getString("full_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCustomerVehicles() {
        cmbVehicle.getItems().clear();
        String customer = cmbCustomer.getValue();
        if (customer != null) {
            try {
                String sql = String.format(
                        "SELECT plate_number FROM vehicle WHERE customer_id = " +
                                "(SELECT customer_id FROM customer WHERE full_name = '%s')",
                        customer
                );
                ResultSet rs = DB.executeQuery(sql);
                while (rs.next()) {
                    cmbVehicle.getItems().add(rs.getString("plate_number"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

        TableColumn<Invoice, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setPrefWidth(100);

        TableColumn<Invoice, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(200);
        colActions.setCellFactory(param -> new TableCell<Invoice, Void>() {
            private final Button btnView = new Button("View");
            private final Button btnPrint = new Button("Print");
            private final Button btnDelete = new Button("Delete");

            {
                btnView.getStyleClass().add("btn-table-edit");
                btnPrint.getStyleClass().add("btn-table-edit");
                btnDelete.getStyleClass().add("btn-table-delete");

                HBox buttons = new HBox(8, btnView, btnPrint, btnDelete);
                buttons.getStyleClass().add("table-actions");

                btnView.setOnAction(e -> {
                    Invoice invoice = getTableView().getItems().get(getIndex());
                    viewInvoice(invoice);
                });

                btnPrint.setOnAction(e -> {
                    Invoice invoice = getTableView().getItems().get(getIndex());
                    printInvoice(invoice);
                });

                btnDelete.setOnAction(e -> {
                    Invoice invoice = getTableView().getItems().get(getIndex());
                    deleteInvoice(invoice);
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

        table.getColumns().addAll(colId, colCustomer, colDate, colAmount, colStatus, colActions);
        table.setItems(invoiceList);
    }

    private void loadInvoices() {
        invoiceList.clear();
        try {
            ResultSet rs = DB.getInvoices();
            while (rs.next()) {
                Invoice invoice = new Invoice(
                        rs.getInt("invoice_id"),
                        rs.getString("customer_name"),
                        rs.getString("invoice_date"),
                        rs.getDouble("total_amount"),
                        rs.getString("status")
                );
                invoiceList.add(invoice);
            }
        } catch (Exception e) {
            showAlert("Error", "Error loading invoices: " + e.getMessage());
        }
    }

    private void addService() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>();
        dialog.setTitle("Add Service");
        dialog.setHeaderText("Select a service");
        dialog.setContentText("Service:");

        try {
            ResultSet rs = DB.getServices();
            while (rs.next()) {
                dialog.getItems().add(rs.getString("service_name") + " - $" + rs.getDouble("price"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        dialog.showAndWait().ifPresent(service -> {
            lstServices.getItems().add(service);
            calculateTotal();
        });
    }

    private void removeService() {
        int selected = lstServices.getSelectionModel().getSelectedIndex();
        if (selected >= 0) {
            lstServices.getItems().remove(selected);
            calculateTotal();
        }
    }

    private void addPart() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>();
        dialog.setTitle("Add Part");
        dialog.setHeaderText("Select a part");
        dialog.setContentText("Part:");

        try {
            ResultSet rs = DB.getParts();
            while (rs.next()) {
                dialog.getItems().add(rs.getString("part_name") + " - $" + rs.getDouble("price"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        dialog.showAndWait().ifPresent(part -> {
            lstParts.getItems().add(part);
            calculateTotal();
        });
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

        for (String service : lstServices.getItems()) {
            try {
                String[] parts = service.split(" - \\$");
                if (parts.length > 1) {
                    total += Double.parseDouble(parts[1]);
                }
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }

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
        String vehicle = cmbVehicle.getValue();
        String date = datePicker.getValue().toString();
        String notes = txtNotes.getText().trim();
        String total = txtTotal.getText().trim();

        if (customer == null || customer.isEmpty()) {
            showAlert("Warning", "Please select a customer");
            return;
        }

        if (total.isEmpty() || total.equals("0.00")) {
            showAlert("Warning", "Please add services or parts to the invoice");
            return;
        }

        String sql = String.format(
                "INSERT INTO salesinvoice (customer_id, vehicle_id, invoice_date, total_amount, notes, status) " +
                        "VALUES ((SELECT customer_id FROM customer WHERE full_name = '%s'), " +
                        "(SELECT vehicle_id FROM vehicle WHERE plate_number = '%s'), '%s', %s, '%s', 'pending')",
                customer, vehicle == null ? "" : vehicle, date, total, notes
        );

        int result = DB.executeUpdate(sql);
        if (result > 0) {
            int invoiceId = getLastInsertId();

            for (String service : lstServices.getItems()) {
                String serviceName = service.split(" - \\$")[0];
                String serviceSql = String.format(
                        "INSERT INTO invoice_items (invoice_id, service_id, quantity, price) " +
                                "VALUES (%d, (SELECT service_id FROM service WHERE service_name = '%s'), 1, %s)",
                        invoiceId, serviceName, service.split(" - \\$")[1]
                );
                DB.executeUpdate(serviceSql);
            }

            for (String part : lstParts.getItems()) {
                String partName = part.split(" - \\$")[0];
                String partSql = String.format(
                        "INSERT INTO invoice_items (invoice_id, part_id, quantity, price) " +
                                "VALUES (%d, (SELECT part_id FROM sparepart WHERE part_name = '%s'), 1, %s)",
                        invoiceId, partName, part.split(" - \\$")[1]
                );
                DB.executeUpdate(partSql);
            }

            showAlert("Success", "Invoice created successfully #" + invoiceId);
            clearFields();
            loadInvoices();
        } else {
            showAlert("Error", "Failed to create invoice");
        }
    }

    private int getLastInsertId() {
        try {
            ResultSet rs = DB.executeQuery("SELECT LAST_INSERT_ID()");
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void viewInvoice(Invoice invoice) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Invoice Details");
        alert.setHeaderText("Invoice #" + invoice.getInvoiceId());
        alert.setContentText(
                "Customer: " + invoice.getCustomerName() + "\n" +
                        "Date: " + invoice.getDate() + "\n" +
                        "Amount: $" + invoice.getAmount() + "\n" +
                        "Status: " + invoice.getStatus()
        );
        alert.showAndWait();
    }

    private void printInvoice(Invoice invoice) {
        showAlert("Print", "Printing invoice #" + invoice.getInvoiceId());
    }

    private void deleteInvoice(Invoice invoice) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Invoice");
        alert.setContentText("Are you sure you want to delete invoice #" + invoice.getInvoiceId() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                DB.executeUpdate("DELETE FROM invoice_items WHERE invoice_id = " + invoice.getInvoiceId());

                String sql = "DELETE FROM salesinvoice WHERE invoice_id = " + invoice.getInvoiceId();
                int result = DB.executeUpdate(sql);
                if (result > 0) {
                    showAlert("Success", "Invoice deleted successfully");
                    loadInvoices();
                }
            }
        });
    }

    private void clearFields() {
        cmbCustomer.setValue(null);
        cmbVehicle.setValue(null);
        lstServices.getItems().clear();
        lstParts.getItems().clear();
        txtTotal.clear();
        datePicker.setValue(LocalDate.now());
        txtNotes.clear();
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
        private String status;

        public Invoice(int invoiceId, String customerName, String date, double amount, String status) {
            this.invoiceId = invoiceId;
            this.customerName = customerName;
            this.date = date;
            this.amount = amount;
            this.status = status;
        }

        public int getInvoiceId() { return invoiceId; }
        public String getCustomerName() { return customerName; }
        public String getDate() { return date; }
        public double getAmount() { return amount; }
        public String getStatus() { return status; }
    }
}