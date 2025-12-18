import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.*;
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

        // Header
        VBox header = new VBox(8);
        header.getStyleClass().add("window-header");
        header.setPadding(new Insets(20));

        Label title = new Label("Reports & Analytics");
        title.getStyleClass().add("window-title");

        Label subtitle = new Label("View workshop performance and statistics");
        subtitle.getStyleClass().add("window-subtitle");

        // Date Range Selector
        HBox dateRange = new HBox(15);
        dateRange.setPadding(new Insets(10, 0, 0, 0));
        dateRange.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

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

        // TabPane
        tabPane.getStyleClass().add("tab-pane-modern");

        Tab salesTab = new Tab("Sales Report", createSalesReport());
        salesTab.setClosable(false);

        Tab inventoryTab = new Tab("Inventory Report", createInventoryReport());
        inventoryTab.setClosable(false);

        Tab customerTab = new Tab("Customer Report", createCustomerReport());
        customerTab.setClosable(false);

        Tab serviceTab = new Tab("Service Report", createServiceReport());
        serviceTab.setClosable(false);

        tabPane.getTabs().addAll(salesTab, inventoryTab, customerTab, serviceTab);
        setCenter(tabPane);

        refreshAllReports();
    }

    private ScrollPane createSalesReport() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));

        // Summary Stats
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(20);

        String start = startDate.getValue().toString();
        String end = endDate.getValue().toString();

        try {
            // Total Revenue
            ResultSet rs1 = DB.executeQuery(
                    "SELECT SUM(total_amount) as total FROM salesinvoice " +
                            "WHERE invoice_date BETWEEN '" + start + "' AND '" + end + "'"
            );
            double totalRevenue = rs1.next() ? rs1.getDouble("total") : 0.0;

            // Invoice Count
            ResultSet rs2 = DB.executeQuery(
                    "SELECT COUNT(*) as count FROM salesinvoice " +
                            "WHERE invoice_date BETWEEN '" + start + "' AND '" + end + "'"
            );
            int invoiceCount = rs2.next() ? rs2.getInt("count") : 0;

            // Average Invoice
            double avgInvoice = invoiceCount > 0 ? totalRevenue / invoiceCount : 0.0;

            // Top Customer
            ResultSet rs3 = DB.executeQuery(
                    "SELECT c.full_name, SUM(s.total_amount) as spent " +
                            "FROM salesinvoice s JOIN customer c ON s.customer_id = c.customer_id " +
                            "WHERE s.invoice_date BETWEEN '" + start + "' AND '" + end + "' " +
                            "GROUP BY c.customer_id ORDER BY spent DESC LIMIT 1"
            );
            String topCustomer = "None";
            if (rs3.next()) {
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

        // Revenue Chart
        VBox chartBox = new VBox(15);
        chartBox.getStyleClass().add("chart-card");
        chartBox.setPadding(new Insets(20));

        Label chartTitle = new Label("Revenue Trend");
        chartTitle.getStyleClass().add("section-title");

        // Simple bar chart (simulated)
        VBox chartBars = new VBox(10);
        chartBars.setPadding(new Insets(20, 0, 0, 0));

        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        double[] revenues = {1250, 1800, 2200, 1950, 2400, 2100, 1650};

        for (int i = 0; i < days.length; i++) {
            HBox barRow = new HBox(15);
            barRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Label dayLabel = new Label(days[i]);
            dayLabel.setPrefWidth(40);

            Pane barBg = new Pane();
            barBg.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 3;");
            barBg.setPrefWidth(200);
            barBg.setPrefHeight(20);

            Pane barFill = new Pane();
            barFill.setStyle("-fx-background-color: #2E86AB; -fx-background-radius: 3;");
            double width = (revenues[i] / 2500) * 190;
            barFill.setPrefWidth(width);
            barFill.setPrefHeight(18);

            StackPane barStack = new StackPane(barBg, barFill);

            Label valueLabel = new Label("$" + formatCurrency(revenues[i]));
            valueLabel.setStyle("-fx-font-weight: bold;");

            barRow.getChildren().addAll(dayLabel, barStack, valueLabel);
            chartBars.getChildren().add(barRow);
        }

        chartBox.getChildren().addAll(chartTitle, chartBars);

        // Recent Invoices Table
        VBox tableBox = new VBox(15);
        tableBox.getStyleClass().add("table-box");

        Label tableTitle = new Label("Recent Invoices");
        tableTitle.getStyleClass().add("table-title");

        TableView<RecentInvoice> recentTable = new TableView<>();
        ObservableList<RecentInvoice> recentList = FXCollections.observableArrayList();

        try {
            ResultSet rs = DB.executeQuery(
                    "SELECT s.invoice_id, c.full_name, s.invoice_date, s.total_amount, s.status " +
                            "FROM salesinvoice s JOIN customer c ON s.customer_id = c.customer_id " +
                            "ORDER BY s.invoice_date DESC LIMIT 10"
            );

            while (rs.next()) {
                recentList.add(new RecentInvoice(
                        rs.getInt("invoice_id"),
                        rs.getString("full_name"),
                        rs.getString("invoice_date"),
                        rs.getDouble("total_amount"),
                        rs.getString("status")
                ));
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

        recentTable.getColumns().addAll(colId, colCustomer, colDate, colAmount, colStatus);
        recentTable.setItems(recentList);
        recentTable.setPrefHeight(200);

        tableBox.getChildren().addAll(tableTitle, recentTable);

        // Export Button
        HBox exportBox = new HBox();
        exportBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Button btnExport = new Button("Export Report");
        btnExport.getStyleClass().add("btn-primary");
        btnExport.setOnAction(e -> exportReport("sales"));

        exportBox.getChildren().add(btnExport);

        content.getChildren().addAll(statsGrid, chartBox, tableBox, exportBox);
        scroll.setContent(content);

        return scroll;
    }

    private ScrollPane createInventoryReport() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));

        // Inventory Stats
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(20);

        try {
            // Total Parts
            ResultSet rs1 = DB.executeQuery("SELECT COUNT(*) as total FROM sparepart");
            int totalParts = rs1.next() ? rs1.getInt("total") : 0;

            // Low Stock
            ResultSet rs2 = DB.executeQuery("SELECT COUNT(*) as low FROM sparepart WHERE quantity < 10");
            int lowStock = rs2.next() ? rs2.getInt("low") : 0;

            // Total Value
            ResultSet rs3 = DB.executeQuery("SELECT SUM(quantity * price) as value FROM sparepart");
            double totalValue = rs3.next() ? rs3.getDouble("value") : 0.0;

            // Most Used Part
            ResultSet rs4 = DB.executeQuery(
                    "SELECT p.part_name, SUM(ii.quantity) as used " +
                            "FROM invoice_items ii JOIN sparepart p ON ii.part_id = p.part_id " +
                            "GROUP BY p.part_id ORDER BY used DESC LIMIT 1"
            );
            String topPart = "None";
            if (rs4.next()) {
                topPart = rs4.getString("part_name");
            }

            VBox stat1 = createReportStat("Total Parts", String.valueOf(totalParts), "#2E86AB");
            VBox stat2 = createReportStat("Low Stock", String.valueOf(lowStock), "#ef4444");
            VBox stat3 = createReportStat("Inventory Value", "$" + formatCurrency(totalValue), "#10b981");
            VBox stat4 = createReportStat("Most Used", topPart, "#f59e0b");

            statsGrid.add(stat1, 0, 0);
            statsGrid.add(stat2, 1, 0);
            statsGrid.add(stat3, 0, 1);
            statsGrid.add(stat4, 1, 1);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Low Stock Table
        VBox tableBox = new VBox(15);
        tableBox.getStyleClass().add("table-box");

        Label tableTitle = new Label("Low Stock Items (Need Reorder)");
        tableTitle.getStyleClass().add("table-title");

        TableView<LowStockItem> lowStockTable = new TableView<>();
        ObservableList<LowStockItem> lowStockList = FXCollections.observableArrayList();

        try {
            ResultSet rs = DB.executeQuery(
                    "SELECT part_name, quantity, price, min_stock FROM sparepart " +
                            "WHERE quantity < min_stock OR quantity < 10 ORDER BY quantity"
            );

            while (rs.next()) {
                lowStockList.add(new LowStockItem(
                        rs.getString("part_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"),
                        rs.getInt("min_stock")
                ));
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

        lowStockTable.getColumns().addAll(colName, colQty, colMin, colPrice);
        lowStockTable.setItems(lowStockList);
        lowStockTable.setPrefHeight(200);

        tableBox.getChildren().addAll(tableTitle, lowStockTable);

        // Category Breakdown
        VBox catBox = new VBox(15);
        catBox.getStyleClass().add("chart-card");
        catBox.setPadding(new Insets(20));

        Label catTitle = new Label("Parts by Category");
        catTitle.getStyleClass().add("section-title");

        // Simple category chart
        VBox catBars = new VBox(10);
        catBars.setPadding(new Insets(20, 0, 0, 0));

        String[] categories = {"Engine", "Brakes", "Filters", "Electrical", "Other"};
        int[] counts = {45, 32, 28, 21, 15};

        for (int i = 0; i < categories.length; i++) {
            HBox barRow = new HBox(15);
            barRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Label catLabel = new Label(categories[i]);
            catLabel.setPrefWidth(80);

            Pane barBg = new Pane();
            barBg.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 3;");
            barBg.setPrefWidth(200);
            barBg.setPrefHeight(20);

            Pane barFill = new Pane();
            barFill.setStyle("-fx-background-color: #A23B72; -fx-background-radius: 3;");
            double width = (counts[i] / 50.0) * 190;
            barFill.setPrefWidth(width);
            barFill.setPrefHeight(18);

            StackPane barStack = new StackPane(barBg, barFill);

            Label countLabel = new Label(String.valueOf(counts[i]));
            countLabel.setStyle("-fx-font-weight: bold;");

            barRow.getChildren().addAll(catLabel, barStack, countLabel);
            catBars.getChildren().add(barRow);
        }

        catBox.getChildren().addAll(catTitle, catBars);

        content.getChildren().addAll(statsGrid, tableBox, catBox);
        scroll.setContent(content);

        return scroll;
    }

    private ScrollPane createCustomerReport() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));

        // Customer Stats
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(20);

        try {
            // Total Customers
            ResultSet rs1 = DB.executeQuery("SELECT COUNT(*) as total FROM customer");
            int totalCustomers = rs1.next() ? rs1.getInt("total") : 0;

            // New This Month
            String thisMonth = LocalDate.now().withDayOfMonth(1).toString();
            ResultSet rs2 = DB.executeQuery(
                    "SELECT COUNT(*) as new FROM customer WHERE created_at >= '" + thisMonth + "'"
            );
            int newCustomers = rs2.next() ? rs2.getInt("new") : 0;

            // Active Customers
            String last30Days = LocalDate.now().minusDays(30).toString();
            ResultSet rs3 = DB.executeQuery(
                    "SELECT COUNT(DISTINCT customer_id) as active FROM salesinvoice " +
                            "WHERE invoice_date >= '" + last30Days + "'"
            );
            int activeCustomers = rs3.next() ? rs3.getInt("active") : 0;

            // Top Spender
            ResultSet rs4 = DB.executeQuery(
                    "SELECT c.full_name, SUM(s.total_amount) as spent " +
                            "FROM salesinvoice s JOIN customer c ON s.customer_id = c.customer_id " +
                            "GROUP BY c.customer_id ORDER BY spent DESC LIMIT 1"
            );
            String topSpender = "None";
            double topAmount = 0.0;
            if (rs4.next()) {
                topSpender = rs4.getString("full_name");
                topAmount = rs4.getDouble("spent");
            }

            VBox stat1 = createReportStat("Total Customers", String.valueOf(totalCustomers), "#2E86AB");
            VBox stat2 = createReportStat("New This Month", String.valueOf(newCustomers), "#A23B72");
            VBox stat3 = createReportStat("Active (30 days)", String.valueOf(activeCustomers), "#10b981");
            VBox stat4 = createReportStat("Top Spender", topSpender + " ($" + formatCurrency(topAmount) + ")", "#f59e0b");

            statsGrid.add(stat1, 0, 0);
            statsGrid.add(stat2, 1, 0);
            statsGrid.add(stat3, 0, 1);
            statsGrid.add(stat4, 1, 1);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Top Customers Table
        VBox tableBox = new VBox(15);
        tableBox.getStyleClass().add("table-box");

        Label tableTitle = new Label("Top Customers by Spending");
        tableTitle.getStyleClass().add("table-title");

        TableView<TopCustomer> topTable = new TableView<>();
        ObservableList<TopCustomer> topList = FXCollections.observableArrayList();

        try {
            ResultSet rs = DB.executeQuery(
                    "SELECT c.full_name, COUNT(s.invoice_id) as invoices, SUM(s.total_amount) as total " +
                            "FROM salesinvoice s JOIN customer c ON s.customer_id = c.customer_id " +
                            "GROUP BY c.customer_id ORDER BY total DESC LIMIT 10"
            );

            while (rs.next()) {
                topList.add(new TopCustomer(
                        rs.getString("full_name"),
                        rs.getInt("invoices"),
                        rs.getDouble("total")
                ));
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

        topTable.getColumns().addAll(colName, colInvoices, colTotal);
        topTable.setItems(topList);
        topTable.setPrefHeight(200);

        tableBox.getChildren().addAll(tableTitle, topTable);

        content.getChildren().addAll(statsGrid, tableBox);
        scroll.setContent(content);

        return scroll;
    }

    private ScrollPane createServiceReport() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));

        // Service Stats
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(20);

        try {
            // Total Services
            ResultSet rs1 = DB.executeQuery("SELECT COUNT(*) as total FROM service");
            int totalServices = rs1.next() ? rs1.getInt("total") : 0;

            // Most Popular Service
            ResultSet rs2 = DB.executeQuery(
                    "SELECT s.service_name, COUNT(ii.service_id) as count " +
                            "FROM invoice_items ii JOIN service s ON ii.service_id = s.service_id " +
                            "GROUP BY s.service_id ORDER BY count DESC LIMIT 1"
            );
            String topService = "None";
            int topCount = 0;
            if (rs2.next()) {
                topService = rs2.getString("service_name");
                topCount = rs2.getInt("count");
            }

            // Services This Month
            String thisMonth = LocalDate.now().withDayOfMonth(1).toString();
            ResultSet rs3 = DB.executeQuery(
                    "SELECT COUNT(*) as count FROM invoice_items ii " +
                            "JOIN salesinvoice s ON ii.invoice_id = s.invoice_id " +
                            "WHERE s.invoice_date >= '" + thisMonth + "' AND ii.service_id IS NOT NULL"
            );
            int servicesThisMonth = rs3.next() ? rs3.getInt("count") : 0;

            // Revenue from Services
            ResultSet rs4 = DB.executeQuery(
                    "SELECT SUM(ii.price * ii.quantity) as revenue FROM invoice_items ii " +
                            "JOIN salesinvoice s ON ii.invoice_id = s.invoice_id " +
                            "WHERE ii.service_id IS NOT NULL AND s.invoice_date >= '" + thisMonth + "'"
            );
            double serviceRevenue = rs4.next() ? rs4.getDouble("revenue") : 0.0;

            VBox stat1 = createReportStat("Total Services", String.valueOf(totalServices), "#2E86AB");
            VBox stat2 = createReportStat("Most Popular", topService + " (" + topCount + ")", "#A23B72");
            VBox stat3 = createReportStat("This Month", String.valueOf(servicesThisMonth), "#10b981");
            VBox stat4 = createReportStat("Service Revenue", "$" + formatCurrency(serviceRevenue), "#f59e0b");

            statsGrid.add(stat1, 0, 0);
            statsGrid.add(stat2, 1, 0);
            statsGrid.add(stat3, 0, 1);
            statsGrid.add(stat4, 1, 1);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Top Services Table
        VBox tableBox = new VBox(15);
        tableBox.getStyleClass().add("table-box");

        Label tableTitle = new Label("Most Popular Services");
        tableTitle.getStyleClass().add("table-title");

        TableView<PopularService> serviceTable = new TableView<>();
        ObservableList<PopularService> serviceList = FXCollections.observableArrayList();

        try {
            ResultSet rs = DB.executeQuery(
                    "SELECT s.service_name, s.price, COUNT(ii.service_id) as usage " +
                            "FROM invoice_items ii JOIN service s ON ii.service_id = s.service_id " +
                            "GROUP BY s.service_id ORDER BY usage DESC LIMIT 10"
            );

            while (rs.next()) {
                serviceList.add(new PopularService(
                        rs.getString("service_name"),
                        rs.getDouble("price"),
                        rs.getInt("usage")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        TableColumn<PopularService, String> colName = new TableColumn<>("Service");
        colName.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        colName.setPrefWidth(200);

        TableColumn<PopularService, Double> colPrice = new TableColumn<>("Price");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colPrice.setPrefWidth(100);

        TableColumn<PopularService, Integer> colUsage = new TableColumn<>("Usage Count");
        colUsage.setCellValueFactory(new PropertyValueFactory<>("usageCount"));
        colUsage.setPrefWidth(100);

        serviceTable.getColumns().addAll(colName, colPrice, colUsage);
        serviceTable.setItems(serviceList);
        serviceTable.setPrefHeight(200);

        tableBox.getChildren().addAll(tableTitle, serviceTable);

        content.getChildren().addAll(statsGrid, tableBox);
        scroll.setContent(content);

        return scroll;
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
        // Refresh all tab contents
        tabPane.getTabs().clear();
        tabPane.getTabs().addAll(
                new Tab("Sales Report", createSalesReport()),
                new Tab("Inventory Report", createInventoryReport()),
                new Tab("Customer Report", createCustomerReport()),
                new Tab("Service Report", createServiceReport())
        );

        for (Tab tab : tabPane.getTabs()) {
            tab.setClosable(false);
        }
    }

    private void exportReport(String type) {
        showAlert("Export", type.substring(0, 1).toUpperCase() + type.substring(1) + " exported successfully");
        // هنا يمكنك إضافة منطق تصدير التقارير إلى PDF أو Excel
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

    // Inner classes for table data
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

    public static class PopularService {
        private String serviceName;
        private double price;
        private int usageCount;

        public PopularService(String serviceName, double price, int usageCount) {
            this.serviceName = serviceName;
            this.price = price;
            this.usageCount = usageCount;
        }

        public String getServiceName() { return serviceName; }
        public double getPrice() { return price; }
        public int getUsageCount() { return usageCount; }
    }
}