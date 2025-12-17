import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.text.DecimalFormat;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Car Workshop Management System");

        BorderPane root = new BorderPane();

        VBox header = createHeader();
        root.setTop(header);

        GridPane content = createContent();
        root.setCenter(content);

        HBox footer = createFooter();
        root.setBottom(footer);

        Scene scene = new Scene(root, 1000, 700);

        try {
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("CSS file not found, using default styling");
        }

        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private VBox createHeader() {
        VBox header = new VBox();
        header.getStyleClass().add("header");

        HBox topBar = new HBox();
        topBar.getStyleClass().add("top-bar");

        HBox logoBox = new HBox(10);
        Label logo = new Label("🚗");
        logo.getStyleClass().add("logo");

        Label title = new Label("Car Workshop Management System");
        title.getStyleClass().add("main-title");

        logoBox.getChildren().addAll(logo, title);

        HBox userBox = new HBox(10);
        userBox.getStyleClass().add("user-box");

        Label welcome = new Label("Welcome, System Manager");
        welcome.getStyleClass().add("welcome-label");

        Label timeLabel = new Label(java.time.LocalDate.now().toString());
        timeLabel.getStyleClass().add("time-label");

        userBox.getChildren().addAll(welcome, timeLabel);

        topBar.getChildren().addAll(logoBox, userBox);
        HBox.setHgrow(logoBox, Priority.ALWAYS);

        HBox navBar = createNavBar();

        header.getChildren().addAll(topBar, navBar);
        return header;
    }

    private HBox createNavBar() {
        HBox navBar = new HBox(10);
        navBar.getStyleClass().add("nav-bar");
        navBar.setPadding(new Insets(10));

        String[] navItems = {
                "🏠 Dashboard", "👥 Customers", "🧾 Invoices",
                "🔧 Services", "🔩 Parts", "🚗 Vehicles",
                "👨‍🔧 Mechanics", "🏭 Suppliers", "📊 Reports"
        };

        for (String item : navItems) {
            Button btn = new Button(item);
            btn.getStyleClass().add("nav-button");

            if (item.contains("Customers")) {
                btn.setOnAction(e -> showCustomers());
            } else if (item.contains("Invoices")) {
                btn.setOnAction(e -> showInvoices());
            } else if (item.contains("Services")) {
                btn.setOnAction(e -> showServices());
            } else if (item.contains("Parts")) {
                btn.setOnAction(e -> showParts());
            } else if (item.contains("Vehicles")) {
                btn.setOnAction(e -> showVehicles());
            } else if (item.contains("Mechanics")) {
                btn.setOnAction(e -> showMechanics());
            } else if (item.contains("Suppliers")) {
                btn.setOnAction(e -> showSuppliers());
            } else if (item.contains("Reports")) {
                btn.setOnAction(e -> showReports());
            }

            navBar.getChildren().add(btn);
        }

        return navBar;
    }

    private GridPane createContent() {
        GridPane content = new GridPane();
        content.getStyleClass().add("main-content");
        content.setPadding(new Insets(20));
        content.setVgap(20);
        content.setHgap(20);

        VBox quickActions = createQuickActions();
        content.add(quickActions, 0, 0);

        VBox stats = createStats();
        content.add(stats, 1, 0);

        VBox recentInvoices = createRecentInvoices();
        content.add(recentInvoices, 0, 1, 2, 1);

        return content;
    }

    private VBox createQuickActions() {
        VBox box = new VBox(10);
        box.getStyleClass().add("card");

        Label title = new Label("⚡ Quick Actions");
        title.getStyleClass().add("card-title");

        String[] actions = {
                "➕ Add New Customer",
                "🧾 Create New Invoice",
                "🔧 Register New Service",
                "🔩 Add New Part",
                "🚗 Add Vehicle"
        };

        VBox buttonsBox = new VBox(8);
        for (String action : actions) {
            Button btn = new Button(action);
            btn.getStyleClass().add("quick-action");
            btn.setMaxWidth(Double.MAX_VALUE);

            if (action.contains("Customer")) {
                btn.setOnAction(e -> showCustomers());
            } else if (action.contains("Invoice")) {
                btn.setOnAction(e -> showInvoices());
            } else if (action.contains("Service")) {
                btn.setOnAction(e -> showServices());
            } else if (action.contains("Part")) {
                btn.setOnAction(e -> showParts());
            } else if (action.contains("Vehicle")) {
                btn.setOnAction(e -> showVehicles());
            }

            buttonsBox.getChildren().add(btn);
        }

        box.getChildren().addAll(title, buttonsBox);
        return box;
    }

    private VBox createStats() {
        VBox box = new VBox(10);
        box.getStyleClass().add("card");

        Label title = new Label("📊 Live Statistics");
        title.getStyleClass().add("card-title");

        GridPane statsGrid = new GridPane();
        statsGrid.setVgap(10);
        statsGrid.setHgap(15);

        DecimalFormat df = new DecimalFormat("#,##0.00");

        String[][] stats = {
                {"👥 Total Customers", String.valueOf(DB.getTotalCustomers())},
                {"🧾 Today's Invoices", String.valueOf(DB.getTodayInvoicesCount())},
                {"🔧 Total Services", String.valueOf(DB.getTotalServices())},
                {"⚠️ Low Stock Items", String.valueOf(DB.getLowStockParts())},
                {"💰 Today's Revenue", "$" + df.format(DB.getTodayRevenue())},
                {"🚗 Total Vehicles", String.valueOf(DB.getTotalVehicles())},
                {"📊 Total Invoices", String.valueOf(DB.getTotalInvoices())},
                {"💵 Total Revenue", "$" + df.format(DB.getTotalRevenue())}
        };

        for (int i = 0; i < stats.length; i++) {
            VBox statBox = new VBox(5);
            statBox.getStyleClass().add("stat-item");

            Label statLabel = new Label(stats[i][0]);
            statLabel.getStyleClass().add("stat-label");

            Label statValue = new Label(stats[i][1]);
            statValue.getStyleClass().add("stat-value");

            statBox.getChildren().addAll(statLabel, statValue);

            statsGrid.add(statBox, i % 2, i / 2);
        }

        Button refreshBtn = new Button("🔄 Refresh Statistics");
        refreshBtn.getStyleClass().add("btn-refresh");
        refreshBtn.setOnAction(e -> refreshStatistics(statsGrid));

        box.getChildren().addAll(title, statsGrid, refreshBtn);
        return box;
    }

    private void refreshStatistics(GridPane statsGrid) {
        DecimalFormat df = new DecimalFormat("#,##0.00");

        String[][] stats = {
                {"👥 Total Customers", String.valueOf(DB.getTotalCustomers())},
                {"🧾 Today's Invoices", String.valueOf(DB.getTodayInvoicesCount())},
                {"🔧 Total Services", String.valueOf(DB.getTotalServices())},
                {"⚠️ Low Stock Items", String.valueOf(DB.getLowStockParts())},
                {"💰 Today's Revenue", "$" + df.format(DB.getTodayRevenue())},
                {"🚗 Total Vehicles", String.valueOf(DB.getTotalVehicles())},
                {"📊 Total Invoices", String.valueOf(DB.getTotalInvoices())},
                {"💵 Total Revenue", "$" + df.format(DB.getTotalRevenue())}
        };

        for (int i = 0; i < stats.length; i++) {
            VBox statBox = (VBox) statsGrid.getChildren().get(i);
            Label statValue = (Label) statBox.getChildren().get(1);
            statValue.setText(stats[i][1]);
        }
    }

    private VBox createRecentInvoices() {
        VBox box = new VBox(10);
        box.getStyleClass().add("card");

        Label title = new Label("📋 Recent Invoices");
        title.getStyleClass().add("card-title");

        VBox invoicesBox = new VBox(5);

        try {
            String sql = "SELECT s.invoice_id, c.full_name, s.total_amount, s.invoice_date " +
                    "FROM salesinvoice s " +
                    "JOIN customer c ON s.customer_id = c.customer_id " +
                    "ORDER BY s.invoice_date DESC " +
                    "LIMIT 5";

            var rs = DB.executeQuery(sql);
            int count = 0;
            while (rs.next() && count < 5) {
                HBox invoiceRow = new HBox(20);
                invoiceRow.getStyleClass().add("invoice-row");

                Label invNo = new Label("INV-" + rs.getInt("invoice_id"));
                invNo.getStyleClass().add("invoice-number");

                Label customer = new Label(rs.getString("full_name"));
                customer.getStyleClass().add("invoice-customer");

                Label amount = new Label("$" + rs.getDouble("total_amount"));
                amount.getStyleClass().add("invoice-amount");

                Label date = new Label(rs.getString("invoice_date"));
                date.getStyleClass().add("invoice-date");

                invoiceRow.getChildren().addAll(invNo, customer, amount, date);
                HBox.setHgrow(customer, Priority.ALWAYS);

                invoicesBox.getChildren().add(invoiceRow);
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Button viewAll = new Button("View All Invoices");
        viewAll.getStyleClass().add("view-all-button");
        viewAll.setOnAction(e -> showInvoices());

        box.getChildren().addAll(title, invoicesBox, viewAll);
        return box;
    }

    private HBox createFooter() {
        HBox footer = new HBox();
        footer.getStyleClass().add("footer");

        Label copyright = new Label("© 2024 Car Workshop Management System - All Rights Reserved");
        copyright.getStyleClass().add("copyright");

        Label version = new Label("Version 1.0.0");
        version.getStyleClass().add("version");

        footer.getChildren().addAll(copyright, version);
        HBox.setHgrow(copyright, Priority.ALWAYS);

        return footer;
    }

    private void showCustomers() {
        try {
            CustomerWindow window = new CustomerWindow();
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showInvoices() {
        try {
            InvoiceWindow window = new InvoiceWindow();
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showServices() {
        try {
            ServiceWindow window = new ServiceWindow();
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showParts() {
        try {
            PartsWindow window = new PartsWindow();
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showVehicles() {
        try {
            VehicleWindow window = new VehicleWindow();
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showMechanics() {
        try {
            MechanicWindow window = new MechanicWindow();
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showSuppliers() {
        try {
            SupplierWindow window = new SupplierWindow();
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showReports() {
        try {
            ReportWindow window = new ReportWindow();
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}