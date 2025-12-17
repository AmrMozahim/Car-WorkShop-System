import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.ResultSet;
import java.time.LocalDate;

public class InvoiceWindow {

    private TableView<Invoice> table = new TableView<>();
    private ObservableList<Invoice> invoiceList = FXCollections.observableArrayList();

    private ComboBox<String> cmbCustomers = new ComboBox<>();
    private TextField txtAmount = new TextField();
    private DatePicker datePicker = new DatePicker(LocalDate.now());
    private TextArea txtNotes = new TextArea();

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Invoice Management");

        BorderPane root = new BorderPane();
        root.getStyleClass().add("window-root");

        VBox header = new VBox(10);
        header.getStyleClass().add("window-header");
        header.setPadding(new Insets(15));

        Label title = new Label("Invoice Management");
        title.getStyleClass().add("window-title");

        Label subtitle = new Label("Create and view sales invoices");
        subtitle.getStyleClass().add("window-subtitle");

        header.getChildren().addAll(title, subtitle);
        root.setTop(header);

        GridPane content = new GridPane();
        content.getStyleClass().add("window-content");
        content.setPadding(new Insets(20));
        content.setVgap(15);
        content.setHgap(15);

        VBox formBox = new VBox(15);
        formBox.getStyleClass().add("form-box");

        Label formTitle = new Label("Create New Invoice");
        formTitle.getStyleClass().add("form-title");

        VBox customerBox = new VBox(5);
        Label lblCustomer = new Label("Customer *");
        lblCustomer.getStyleClass().add("field-label");
        cmbCustomers.getStyleClass().add("field-combo");
        cmbCustomers.setPromptText("Select customer");
        loadCustomers();
        customerBox.getChildren().addAll(lblCustomer, cmbCustomers);

        VBox amountBox = new VBox(5);
        Label lblAmount = new Label("Amount *");
        lblAmount.getStyleClass().add("field-label");
        txtAmount.getStyleClass().add("field-input");
        txtAmount.setPromptText("Enter amount");
        amountBox.getChildren().addAll(lblAmount, txtAmount);

        VBox dateBox = new VBox(5);
        Label lblDate = new Label("Date");
        lblDate.getStyleClass().add("field-label");
        datePicker.getStyleClass().add("field-date");
        dateBox.getChildren().addAll(lblDate, datePicker);

        VBox notesBox = new VBox(5);
        Label lblNotes = new Label("Notes");
        lblNotes.getStyleClass().add("field-label");
        txtNotes.getStyleClass().add("field-textarea");
        txtNotes.setPromptText("Enter any notes");
        txtNotes.setPrefRowCount(3);
        notesBox.getChildren().addAll(lblNotes, txtNotes);

        HBox formButtons = new HBox(10);
        formButtons.getStyleClass().add("form-buttons");

        Button btnAdd = new Button("Create Invoice");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setOnAction(e -> addInvoice());

        Button btnClear = new Button("Clear Fields");
        btnClear.getStyleClass().add("btn-secondary");
        btnClear.setOnAction(e -> clearFields());

        formButtons.getChildren().addAll(btnAdd, btnClear);

        formBox.getChildren().addAll(formTitle, customerBox, amountBox, dateBox, notesBox, formButtons);
        content.add(formBox, 0, 0);

        VBox tableBox = new VBox(10);
        tableBox.getStyleClass().add("table-box");

        HBox tableHeader = new HBox();
        tableHeader.getStyleClass().add("table-header");

        Label tableTitle = new Label("Invoice List");
        tableTitle.getStyleClass().add("table-title");

        Button btnRefresh = new Button("Refresh List");
        btnRefresh.getStyleClass().add("btn-refresh");
        btnRefresh.setOnAction(e -> loadInvoices());

        tableHeader.getChildren().addAll(tableTitle, btnRefresh);
        HBox.setHgrow(tableTitle, Priority.ALWAYS);

        createTable();
        table.setPrefHeight(300);

        tableBox.getChildren().addAll(tableHeader, table);
        content.add(tableBox, 1, 0);

        root.setCenter(content);

        Scene scene = new Scene(root, 1100, 600);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();

        loadInvoices();
    }

    private void loadCustomers() {
        try {
            ResultSet rs = DB.getCustomers();
            while (rs.next()) {
                cmbCustomers.getItems().add(rs.getString("full_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createTable() {
        table.getColumns().clear();

        TableColumn<Invoice, Integer> colId = new TableColumn<>("Invoice #");
        colId.setCellValueFactory(new PropertyValueFactory<>("invoiceId"));
        colId.setPrefWidth(100);

        TableColumn<Invoice, String> colCustomer = new TableColumn<>("Customer");
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colCustomer.setPrefWidth(200);

        TableColumn<Invoice, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDate.setPrefWidth(100);

        TableColumn<Invoice, Double> colAmount = new TableColumn<>("Amount");
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colAmount.setPrefWidth(100);

        table.getColumns().addAll(colId, colCustomer, colDate, colAmount);
        table.setItems(invoiceList);
    }

    private void loadInvoices() {
        invoiceList.clear();
        try {
            ResultSet rs = DB.getInvoices();
            while (rs.next()) {
                Invoice invoice = new Invoice(
                        rs.getInt("invoice_id"),
                        rs.getString("full_name"),
                        rs.getString("invoice_date"),
                        rs.getDouble("total_amount")
                );
                invoiceList.add(invoice);
            }
        } catch (Exception e) {
            showAlert("Error", "Error loading invoices");
            e.printStackTrace();
        }
    }

    private void addInvoice() {
        String customer = cmbCustomers.getValue();
        String amount = txtAmount.getText().trim();
        String date = datePicker.getValue().toString();

        if (customer == null || customer.isEmpty()) {
            showAlert("Warning", "Please select a customer");
            return;
        }

        if (amount.isEmpty()) {
            showAlert("Warning", "Please enter amount");
            return;
        }

        try {
            Double.parseDouble(amount);
        } catch (NumberFormatException e) {
            showAlert("Warning", "Amount must be a number");
            return;
        }

        String sql = String.format(
                "INSERT INTO salesinvoice (customer_id, invoice_date, total_amount) " +
                        "VALUES ((SELECT customer_id FROM customer WHERE full_name = '%s'), '%s', %s)",
                customer, date, amount
        );

        int result = DB.executeUpdate(sql);
        if (result > 0) {
            showAlert("Success", "Invoice created successfully");
            clearFields();
            loadInvoices();
        } else {
            showAlert("Error", "Failed to create invoice");
        }
    }

    private void clearFields() {
        cmbCustomers.setValue(null);
        txtAmount.clear();
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