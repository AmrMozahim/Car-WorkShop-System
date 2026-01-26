import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.text.DecimalFormat;

public class ReportTab extends BorderPane {

    private TabPane tabPane = new TabPane();
    private DatePicker startDate = new DatePicker(LocalDate.now().minusDays(30));
    private DatePicker endDate = new DatePicker(LocalDate.now());

    public ReportTab() {
        initialize();
    }

    private void initialize() {
        getStyleClass().add("window-root");

        VBox header = new VBox(8);
        header.getStyleClass().add("window-header");
        header.setPadding(new Insets(20));

        Label title = new Label("Reports & Analytics");
        title.getStyleClass().add("window-title");

        Label subtitle = new Label("View workshop performance and statistics");
        subtitle.getStyleClass().add("window-subtitle");

        HBox dateRange = new HBox(15);
        dateRange.setPadding(new Insets(10, 0, 0, 0));
        dateRange.setAlignment(Pos.CENTER_LEFT);

        Label lblRange = new Label("Date Range:");
        lblRange.getStyleClass().add("field-label");

        startDate.getStyleClass().add("field-combo");
        endDate.getStyleClass().add("field-combo");

        Button btnApply = new Button("Apply");
        btnApply.getStyleClass().add("btn-primary");
        btnApply.setOnAction(e -> refreshAllReports());

        dateRange.getChildren().addAll(lblRange, startDate, new Label("to"), endDate, btnApply);

        header.getChildren().addAll(title, subtitle, dateRange);
        setTop(header);

        tabPane.getStyleClass().add("tab-pane-modern");
        setCenter(tabPane);

        refreshAllReports();
    }

    private ScrollPane createSalesReport() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(20);

        String start = startDate.getValue().toString();
        String end = endDate.getValue().toString();

        try {
            ResultSet rs1 = DB.executeQuery(
                    "SELECT SUM(total_amount) as total FROM salesinvoice " +
                            "WHERE invoice_date BETWEEN '" + start + "' AND '" + end + "'"
            );
            double totalRevenue = (rs1 != null && rs1.next()) ? rs1.getDouble("total") : 0.0;

            ResultSet rs2 = DB.executeQuery(
                    "SELECT COUNT(*) as count FROM salesinvoice " +
                            "WHERE invoice_date BETWEEN '" + start + "' AND '" + end + "'"
            );
            int invoiceCount = (rs2 != null && rs2.next()) ? rs2.getInt("count") : 0;

            double avgInvoice = invoiceCount > 0 ? totalRevenue / invoiceCount : 0.0;

            ResultSet rs3 = DB.executeQuery(
                    "SELECT c.full_name, SUM(s.total_amount) as spent " +
                            "FROM salesinvoice s JOIN customer c ON s.customer_id = c.customer_id " +
                            "WHERE s.invoice_date BETWEEN '" + start + "' AND '" + end + "' " +
                            "GROUP BY c.customer_id ORDER BY spent DESC LIMIT 1"
            );
            String topCustomer = "None";
            if (rs3 != null && rs3.next()) {
                topCustomer = rs3.getString("full_name");
            }

            VBox stat1 = createReportStat("Total Revenue", "$" + formatCurrency(totalRevenue), "#2E86AB");
            VBox stat2 = createReportStat("Invoices", String.valueOf(invoiceCount), "#A23B72");
            VBox stat3 = createReportStat("Avg Invoice", "$" + formatCurrency(avgInvoice), "#10b981");
            VBox stat4 = createReportStat("Top Customer", topCustomer, "#f59e0b");

            statsGrid.add(stat1, 0, 0);
            statsGrid.add(stat2, 1, 0);
            statsGrid.add(stat3, 0, 1);
            statsGrid.add(stat4, 1, 1);

        } catch (Exception e) {
            e.printStackTrace();
        }

        VBox tableBox = createTableContainer("Recent Invoices", createRecentInvoiceTable(start, end));
        content.getChildren().addAll(statsGrid, tableBox);
        scroll.setContent(content);

        return scroll;
    }

    private ScrollPane createInventoryReport() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(20);

        try {
            ResultSet rs1 = DB.executeQuery("SELECT COUNT(*) as total FROM sparepart");
            int totalParts = (rs1 != null && rs1.next()) ? rs1.getInt("total") : 0;

            ResultSet rs2 = DB.executeQuery("SELECT COUNT(*) as low FROM sparepart WHERE quantity < 10");
            int lowStock = (rs2 != null && rs2.next()) ? rs2.getInt("low") : 0;

            ResultSet rs3 = DB.executeQuery("SELECT SUM(quantity * price) as value FROM sparepart");
            double totalValue = (rs3 != null && rs3.next()) ? rs3.getDouble("value") : 0.0;

            VBox stat1 = createReportStat("Total Parts", String.valueOf(totalParts), "#2E86AB");
            VBox stat2 = createReportStat("Low Stock", String.valueOf(lowStock), "#ef4444");
            VBox stat3 = createReportStat("Inventory Value", "$" + formatCurrency(totalValue), "#10b981");

            statsGrid.add(stat1, 0, 0);
            statsGrid.add(stat2, 1, 0);
            statsGrid.add(stat3, 0, 1);

        } catch (Exception e) {
            e.printStackTrace();
        }

        VBox tableBox = createTableContainer("Low Stock Items (Need Reorder)", createLowStockTable());
        content.getChildren().addAll(statsGrid, tableBox);
        scroll.setContent(content);

        return scroll;
    }

    private ScrollPane createCustomerReport() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(20);

        try {
            ResultSet rs1 = DB.executeQuery("SELECT COUNT(*) as total FROM customer");
            int totalCustomers = (rs1 != null && rs1.next()) ? rs1.getInt("total") : 0;

            String last30Days = LocalDate.now().minusDays(30).toString();
            ResultSet rs3 = DB.executeQuery(
                    "SELECT COUNT(DISTINCT customer_id) as active FROM salesinvoice " +
                            "WHERE invoice_date >= '" + last30Days + "'"
            );
            int activeCustomers = (rs3 != null && rs3.next()) ? rs3.getInt("active") : 0;

            ResultSet rs4 = DB.executeQuery(
                    "SELECT c.full_name, SUM(s.total_amount) as spent " +
                            "FROM salesinvoice s JOIN customer c ON s.customer_id = c.customer_id " +
                            "GROUP BY c.customer_id ORDER BY spent DESC LIMIT 1"
            );
            String topSpender = "None";
            double topAmount = 0.0;
            if (rs4 != null && rs4.next()) {
                topSpender = rs4.getString("full_name");
                topAmount = rs4.getDouble("spent");
            }

            VBox stat1 = createReportStat("Total Customers", String.valueOf(totalCustomers), "#2E86AB");
            VBox stat2 = createReportStat("Active (30 days)", String.valueOf(activeCustomers), "#10b981");
            VBox stat3 = createReportStat("Top Spender", topSpender + " ($" + formatCurrency(topAmount) + ")", "#f59e0b");

            statsGrid.add(stat1, 0, 0);
            statsGrid.add(stat2, 1, 0);
            statsGrid.add(stat3, 0, 1);

        } catch (Exception e) {
            e.printStackTrace();
        }

        VBox tableBox = createTableContainer("Top Customers by Spending", createTopCustomerTable());
        content.getChildren().addAll(statsGrid, tableBox);
        scroll.setContent(content);

        return scroll;
    }

    private ScrollPane createServiceReport() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(20);

        try {
            ResultSet rs1 = DB.executeQuery("SELECT COUNT(*) as total FROM service");
            int totalServices = (rs1 != null && rs1.next()) ? rs1.getInt("total") : 0;

            VBox stat1 = createReportStat("Total Services", String.valueOf(totalServices), "#2E86AB");

            statsGrid.add(stat1, 0, 0);

        } catch (Exception e) {
            e.printStackTrace();
        }

        VBox tableBox = createTableContainer("Available Services", createServiceTable());
        content.getChildren().addAll(statsGrid, tableBox);
        scroll.setContent(content);

        return scroll;
    }

    private VBox createTableContainer(String title, TableView<?> table) {
        VBox box = new VBox(15);
        box.getStyleClass().add("table-box");

        Label tableTitle = new Label(title);
        tableTitle.getStyleClass().add("table-title");

        box.getChildren().addAll(tableTitle, table);
        return box;
    }

    private TableView<RecentInvoice> createRecentInvoiceTable(String start, String end) {
        TableView<RecentInvoice> table = new TableView<>();
        ObservableList<RecentInvoice> list = FXCollections.observableArrayList();

        try {
            ResultSet rs = DB.executeQuery(
                    "SELECT s.invoice_id, c.full_name, s.invoice_date, s.total_amount " +
                            "FROM salesinvoice s JOIN customer c ON s.customer_id = c.customer_id " +
                            "WHERE s.invoice_date BETWEEN '" + start + "' AND '" + end + "' " +
                            "ORDER BY s.invoice_date DESC LIMIT 10"
            );

            if (rs != null) {
                while (rs.next()) {
                    list.add(new RecentInvoice(
                            rs.getInt("invoice_id"),
                            rs.getString("full_name"),
                            rs.getString("invoice_date"),
                            rs.getDouble("total_amount"),
                            "Completed"
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        TableColumn<RecentInvoice, Integer> colId = new TableColumn<>("Invoice #");
        colId.setCellValueFactory(new PropertyValueFactory<>("invoiceId"));
        colId.setPrefWidth(100);

        TableColumn<RecentInvoice, String> colCustomer = new TableColumn<>("Customer");
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customer"));
        colCustomer.setPrefWidth(150);

        TableColumn<RecentInvoice, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDate.setPrefWidth(100);

        TableColumn<RecentInvoice, Double> colAmount = new TableColumn<>("Amount");
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colAmount.setPrefWidth(100);

        TableColumn<RecentInvoice, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setPrefWidth(100);

        table.getColumns().addAll(colId, colCustomer, colDate, colAmount, colStatus);
        table.setItems(list);
        table.setPrefHeight(200);

        return table;
    }

    private TableView<LowStockItem> createLowStockTable() {
        TableView<LowStockItem> table = new TableView<>();
        ObservableList<LowStockItem> list = FXCollections.observableArrayList();

        try {
            ResultSet rs = DB.executeQuery(
                    "SELECT part_name, quantity, price FROM sparepart " +
                            "WHERE quantity < 10 ORDER BY quantity"
            );

            if (rs != null) {
                while (rs.next()) {
                    list.add(new LowStockItem(
                            rs.getString("part_name"),
                            rs.getInt("quantity"),
                            rs.getDouble("price"),
                            10
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        TableColumn<LowStockItem, String> colName = new TableColumn<>("Part Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("partName"));
        colName.setPrefWidth(200);

        TableColumn<LowStockItem, Integer> colQty = new TableColumn<>("Quantity");
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colQty.setPrefWidth(100);

        TableColumn<LowStockItem, Integer> colMin = new TableColumn<>("Min Stock");
        colMin.setCellValueFactory(new PropertyValueFactory<>("minStock"));
        colMin.setPrefWidth(100);

        TableColumn<LowStockItem, Double> colPrice = new TableColumn<>("Price");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colPrice.setPrefWidth(100);

        table.getColumns().addAll(colName, colQty, colMin, colPrice);
        table.setItems(list);
        table.setPrefHeight(200);

        return table;
    }

    private TableView<TopCustomer> createTopCustomerTable() {
        TableView<TopCustomer> table = new TableView<>();
        ObservableList<TopCustomer> list = FXCollections.observableArrayList();

        try {
            ResultSet rs = DB.executeQuery(
                    "SELECT c.full_name, COUNT(s.invoice_id) as invoices, SUM(s.total_amount) as total " +
                            "FROM salesinvoice s JOIN customer c ON s.customer_id = c.customer_id " +
                            "GROUP BY c.customer_id ORDER BY total DESC LIMIT 10"
            );

            if (rs != null) {
                while (rs.next()) {
                    list.add(new TopCustomer(
                            rs.getString("full_name"),
                            rs.getInt("invoices"),
                            rs.getDouble("total")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        TableColumn<TopCustomer, String> colName = new TableColumn<>("Customer");
        colName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colName.setPrefWidth(200);

        TableColumn<TopCustomer, Integer> colInvoices = new TableColumn<>("Invoices");
        colInvoices.setCellValueFactory(new PropertyValueFactory<>("invoiceCount"));
        colInvoices.setPrefWidth(100);

        TableColumn<TopCustomer, Double> colTotal = new TableColumn<>("Total Spent");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalSpent"));
        colTotal.setPrefWidth(120);

        table.getColumns().addAll(colName, colInvoices, colTotal);
        table.setItems(list);
        table.setPrefHeight(200);

        return table;
    }

    private TableView<ServiceData> createServiceTable() {
        TableView<ServiceData> table = new TableView<>();
        ObservableList<ServiceData> list = FXCollections.observableArrayList();

        try {
            ResultSet rs = DB.executeQuery(
                    "SELECT service_name, price FROM service ORDER BY service_name"
            );

            if (rs != null) {
                while (rs.next()) {
                    list.add(new ServiceData(
                            rs.getString("service_name"),
                            rs.getDouble("price")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        TableColumn<ServiceData, String> colName = new TableColumn<>("Service");
        colName.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        colName.setPrefWidth(200);

        TableColumn<ServiceData, Double> colPrice = new TableColumn<>("Price");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colPrice.setPrefWidth(100);

        table.getColumns().addAll(colName, colPrice);
        table.setItems(list);
        table.setPrefHeight(200);

        return table;
    }

    private VBox createReportStat(String title, String value, String color) {
        VBox stat = new VBox(10);
        stat.getStyleClass().add("stat-card");
        stat.setStyle("-fx-border-color: " + color + "40; -fx-background-color: " + color + "15;");
        stat.setPadding(new Insets(20));
        stat.setPrefWidth(200);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-font-weight: 500;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: " + color + "; -fx-font-weight: bold;");

        stat.getChildren().addAll(titleLabel, valueLabel);
        return stat;
    }

    private void refreshAllReports() {
        tabPane.getTabs().clear();

        Tab salesTab = new Tab("Sales Report", createSalesReport());
        Tab inventoryTab = new Tab("Inventory Report", createInventoryReport());
        Tab customerTab = new Tab("Customer Report", createCustomerReport());
        Tab serviceTab = new Tab("Service Report", createServiceReport());

        salesTab.setClosable(false);
        inventoryTab.setClosable(false);
        customerTab.setClosable(false);
        serviceTab.setClosable(false);

        tabPane.getTabs().addAll(salesTab, inventoryTab, customerTab, serviceTab);
    }

    private String formatCurrency(double amount) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        return df.format(amount);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class RecentInvoice {
        private int invoiceId;
        private String customer;
        private String date;
        private double amount;
        private String status;

        public RecentInvoice(int invoiceId, String customer, String date, double amount, String status) {
            this.invoiceId = invoiceId;
            this.customer = customer;
            this.date = date;
            this.amount = amount;
            this.status = status;
        }

        public int getInvoiceId() { return invoiceId; }
        public String getCustomer() { return customer; }
        public String getDate() { return date; }
        public double getAmount() { return amount; }
        public String getStatus() { return status; }
    }

    public static class LowStockItem {
        private String partName;
        private int quantity;
        private double price;
        private int minStock;

        public LowStockItem(String partName, int quantity, double price, int minStock) {
            this.partName = partName;
            this.quantity = quantity;
            this.price = price;
            this.minStock = minStock;
        }

        public String getPartName() { return partName; }
        public int getQuantity() { return quantity; }
        public double getPrice() { return price; }
        public int getMinStock() { return minStock; }
    }

    public static class TopCustomer {
        private String customerName;
        private int invoiceCount;
        private double totalSpent;

        public TopCustomer(String customerName, int invoiceCount, double totalSpent) {
            this.customerName = customerName;
            this.invoiceCount = invoiceCount;
            this.totalSpent = totalSpent;
        }

        public String getCustomerName() { return customerName; }
        public int getInvoiceCount() { return invoiceCount; }
        public double getTotalSpent() { return totalSpent; }
    }

    public static class ServiceData {
        private String serviceName;
        private double price;

        public ServiceData(String serviceName, double price) {
            this.serviceName = serviceName;
            this.price = price;
        }

        public String getServiceName() { return serviceName; }
        public double getPrice() { return price; }
    }
}