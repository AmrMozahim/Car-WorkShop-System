import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.text.DecimalFormat;

public class ReportWindow {

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Reports and Statistics");

        TabPane tabPane = new TabPane();

        Tab salesTab = new Tab("Sales Report", createSalesReport());
        salesTab.setClosable(false);

        Tab inventoryTab = new Tab("Inventory Report", createInventoryReport());
        inventoryTab.setClosable(false);

        Tab customerTab = new Tab("Customer Report", createCustomerReport());
        customerTab.setClosable(false);

        tabPane.getTabs().addAll(salesTab, inventoryTab, customerTab);

        Scene scene = new Scene(tabPane, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    private VBox createSalesReport() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        Label title = new Label("📈 Sales Report");
        title.getStyleClass().add("window-title");

        DecimalFormat df = new DecimalFormat("#,##0.00");

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(15);
        statsGrid.setVgap(10);

        try {
            String today = LocalDate.now().toString();

            ResultSet rs1 = DB.executeQuery("SELECT SUM(total_amount) as total FROM salesinvoice WHERE DATE(invoice_date) = '" + today + "'");
            double todaySales = 0;
            if (rs1.next()) todaySales = rs1.getDouble("total");

            ResultSet rs2 = DB.executeQuery("SELECT COUNT(*) as count FROM salesinvoice WHERE DATE(invoice_date) = '" + today + "'");
            int todayCount = 0;
            if (rs2.next()) todayCount = rs2.getInt("count");

            ResultSet rs3 = DB.executeQuery("SELECT SUM(total_amount) as total FROM salesinvoice");
            double totalSales = 0;
            if (rs3.next()) totalSales = rs3.getDouble("total");

            ResultSet rs4 = DB.executeQuery("SELECT COUNT(*) as count FROM salesinvoice");
            int totalCount = 0;
            if (rs4.next()) totalCount = rs4.getInt("count");

            VBox stat1 = createStatBox("Today's Sales", "$" + df.format(todaySales));
            VBox stat2 = createStatBox("Today's Invoices", String.valueOf(todayCount));
            VBox stat3 = createStatBox("Total Sales", "$" + df.format(totalSales));
            VBox stat4 = createStatBox("Total Invoices", String.valueOf(totalCount));

            statsGrid.add(stat1, 0, 0);
            statsGrid.add(stat2, 1, 0);
            statsGrid.add(stat3, 0, 1);
            statsGrid.add(stat4, 1, 1);

        } catch (Exception e) {
            e.printStackTrace();
        }

        vbox.getChildren().addAll(title, statsGrid);
        return vbox;
    }

    private VBox createInventoryReport() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        Label title = new Label("📦 Inventory Report");
        title.getStyleClass().add("window-title");

        TableView<InventoryItem> table = new TableView<>();

        TableColumn<InventoryItem, String> colName = new TableColumn<>("Part Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("partName"));
        colName.setPrefWidth(200);

        TableColumn<InventoryItem, Integer> colQuantity = new TableColumn<>("Quantity");
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colQuantity.setPrefWidth(100);

        TableColumn<InventoryItem, Double> colPrice = new TableColumn<>("Price");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colPrice.setPrefWidth(100);

        TableColumn<InventoryItem, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setPrefWidth(100);

        table.getColumns().addAll(colName, colQuantity, colPrice, colStatus);

        try {
            ResultSet rs = DB.executeQuery("SELECT * FROM sparepart ORDER BY part_name");
            while (rs.next()) {
                int quantity = rs.getInt("quantity");
                String status = quantity < 10 ? "⚠️ Low" : quantity < 20 ? "⚠️ Medium" : "✅ Good";

                InventoryItem item = new InventoryItem(
                        rs.getString("part_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"),
                        status
                );
                table.getItems().add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        vbox.getChildren().addAll(title, table);
        return vbox;
    }

    private VBox createCustomerReport() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        Label title = new Label("👥 Customer Report");
        title.getStyleClass().add("window-title");

        TableView<CustomerStat> table = new TableView<>();

        TableColumn<CustomerStat, String> colName = new TableColumn<>("Customer Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colName.setPrefWidth(200);

        TableColumn<CustomerStat, Integer> colInvoices = new TableColumn<>("Invoices");
        colInvoices.setCellValueFactory(new PropertyValueFactory<>("invoiceCount"));
        colInvoices.setPrefWidth(100);

        TableColumn<CustomerStat, Double> colTotal = new TableColumn<>("Total Spent");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalSpent"));
        colTotal.setPrefWidth(100);

        table.getColumns().addAll(colName, colInvoices, colTotal);

        try {
            ResultSet rs = DB.executeQuery(
                    "SELECT c.full_name, COUNT(s.invoice_id) as invoice_count, SUM(s.total_amount) as total_spent " +
                            "FROM customer c LEFT JOIN salesinvoice s ON c.customer_id = s.customer_id " +
                            "GROUP BY c.customer_id ORDER BY total_spent DESC"
            );

            while (rs.next()) {
                CustomerStat stat = new CustomerStat(
                        rs.getString("full_name"),
                        rs.getInt("invoice_count"),
                        rs.getDouble("total_spent")
                );
                table.getItems().add(stat);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        vbox.getChildren().addAll(title, table);
        return vbox;
    }

    private VBox createStatBox(String title, String value) {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 5px; -fx-border-color: #ddd;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        box.getChildren().addAll(titleLabel, valueLabel);
        return box;
    }

    public static class InventoryItem {
        private String partName;
        private int quantity;
        private double price;
        private String status;

        public InventoryItem(String partName, int quantity, double price, String status) {
            this.partName = partName;
            this.quantity = quantity;
            this.price = price;
            this.status = status;
        }

        public String getPartName() { return partName; }
        public int getQuantity() { return quantity; }
        public double getPrice() { return price; }
        public String getStatus() { return status; }
    }

    public static class CustomerStat {
        private String customerName;
        private int invoiceCount;
        private double totalSpent;

        public CustomerStat(String customerName, int invoiceCount, double totalSpent) {
            this.customerName = customerName;
            this.invoiceCount = invoiceCount;
            this.totalSpent = totalSpent;
        }

        public String getCustomerName() { return customerName; }
        public int getInvoiceCount() { return invoiceCount; }
        public double getTotalSpent() { return totalSpent; }
    }
}