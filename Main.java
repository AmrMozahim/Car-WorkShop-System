import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class Main extends Application {

    private Map<String, Button> navButtons = new HashMap<>();
    private Button activeNavButton = null;
    private TabPane mainTabPane;
    private Map<String, Tab> tabs = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Car Workshop Management System");

        BorderPane root = new BorderPane();
        root.getStyleClass().add("main-root");

        // === TOP NAVIGATION ===
        HBox topNav = new HBox(20);
        topNav.getStyleClass().add("top-nav");
        topNav.setPadding(new Insets(15, 25, 15, 25));

        // Logo and Title
        VBox logoContainer = new VBox(2);
        logoContainer.getStyleClass().add("logo-container");
        Label logoText = new Label("CAR WORKSHOP");
        logoText.getStyleClass().add("logo-text");
        Label logoSubtext = new Label("Vehicle Management");
        logoSubtext.getStyleClass().add("logo-subtext");
        logoContainer.getChildren().addAll(logoText, logoSubtext);

        // User Box
        HBox userBox = new HBox(10);
        userBox.getStyleClass().add("user-box");

        Circle userAvatar = new Circle(20);
        userAvatar.getStyleClass().add("user-avatar");

        VBox userInfo = new VBox(3);
        userInfo.getStyleClass().add("user-info");
        Label userName = new Label("Admin User");
        userName.getStyleClass().add("user-name");
        Label userRole = new Label("System Manager");
        userRole.getStyleClass().add("user-role");
        userInfo.getChildren().addAll(userName, userRole);

        userBox.getChildren().addAll(userAvatar, userInfo);

        topNav.getChildren().addAll(logoContainer, userBox);
        HBox.setHgrow(logoContainer, Priority.ALWAYS);

        root.setTop(topNav);

        // === SIDEBAR ===
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(240);

        // Dashboard Section
        VBox dashSection = new VBox(5);
        dashSection.getStyleClass().add("nav-section");
        Label dashTitle = new Label("DASHBOARD");
        dashTitle.getStyleClass().add("nav-title");

        Button dashboardBtn = createNavButton("📊 Dashboard", "dashboard");
        dashboardBtn.setOnAction(e -> showTab("dashboard"));
        navButtons.put("dashboard", dashboardBtn);
        setActiveNavButton(dashboardBtn);

        dashSection.getChildren().addAll(dashTitle, dashboardBtn);

        // Management Section
        VBox mgmtSection = new VBox(5);
        mgmtSection.getStyleClass().add("nav-section");
        Label mgmtTitle = new Label("MANAGEMENT");
        mgmtTitle.getStyleClass().add("nav-title");

        Button customersBtn = createNavButton("👥 Customers", "customers");
        customersBtn.setOnAction(e -> {
            showTab("customers");
            setActiveNavButton(customersBtn);
        });
        navButtons.put("customers", customersBtn);

        Button vehiclesBtn = createNavButton("🚗 Vehicles", "vehicles");
        vehiclesBtn.setOnAction(e -> {
            showTab("vehicles");
            setActiveNavButton(vehiclesBtn);
        });
        navButtons.put("vehicles", vehiclesBtn);

        Button mechanicsBtn = createNavButton("🔧 Mechanics", "mechanics");
        mechanicsBtn.setOnAction(e -> {
            showTab("mechanics");
            setActiveNavButton(mechanicsBtn);
        });
        navButtons.put("mechanics", mechanicsBtn);

        mgmtSection.getChildren().addAll(mgmtTitle, customersBtn, vehiclesBtn, mechanicsBtn);

        // Operations Section
        VBox opsSection = new VBox(5);
        opsSection.getStyleClass().add("nav-section");
        Label opsTitle = new Label("OPERATIONS");
        opsTitle.getStyleClass().add("nav-title");

        Button servicesBtn = createNavButton("⚙️ Services", "services");
        servicesBtn.setOnAction(e -> {
            showTab("services");
            setActiveNavButton(servicesBtn);
        });
        navButtons.put("services", servicesBtn);

        Button partsBtn = createNavButton("📦 Parts", "parts");
        partsBtn.setOnAction(e -> {
            showTab("parts");
            setActiveNavButton(partsBtn);
        });
        navButtons.put("parts", partsBtn);

        Button invoicesBtn = createNavButton("🧾 Invoices", "invoices");
        invoicesBtn.setOnAction(e -> {
            showTab("invoices");
            setActiveNavButton(invoicesBtn);
        });
        navButtons.put("invoices", invoicesBtn);

        Button suppliersBtn = createNavButton("🏢 Suppliers", "suppliers");
        suppliersBtn.setOnAction(e -> {
            showTab("suppliers");
            setActiveNavButton(suppliersBtn);
        });
        navButtons.put("suppliers", suppliersBtn);

        opsSection.getChildren().addAll(opsTitle, servicesBtn, partsBtn, invoicesBtn, suppliersBtn);

        // Reports Section
        VBox reportsSection = new VBox(5);
        reportsSection.getStyleClass().add("nav-section");
        Label reportsTitle = new Label("REPORTS");
        reportsTitle.getStyleClass().add("nav-title");

        Button reportsBtn = createNavButton("📊 Reports", "reports");
        reportsBtn.setOnAction(e -> {
            showTab("reports");
            setActiveNavButton(reportsBtn);
        });
        navButtons.put("reports", reportsBtn);

        reportsSection.getChildren().addAll(reportsTitle, reportsBtn);

        sidebar.getChildren().addAll(dashSection, mgmtSection, opsSection, reportsSection);
        root.setLeft(sidebar);

        // === MAIN CONTENT - TABPANE ===
        mainTabPane = new TabPane();
        mainTabPane.getStyleClass().add("tab-pane-modern");
        mainTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Create and add tabs
        createTabs();

        root.setCenter(mainTabPane);

        Scene scene = new Scene(root, 1200, 700);
        scene.getStylesheets().add("style.css");

        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();

        // Show dashboard by default
        showTab("dashboard");
    }

    private void createTabs() {
        // Dashboard Tab
        Tab dashboardTab = new Tab();
        dashboardTab.setId("dashboard");
        dashboardTab.setClosable(false);

        ScrollPane dashboardContent = createDashboardContent();
        dashboardTab.setContent(dashboardContent);
        mainTabPane.getTabs().add(dashboardTab);
        tabs.put("dashboard", dashboardTab);

        // Customers Tab
        Tab customersTab = new Tab();
        customersTab.setId("customers");
        customersTab.setClosable(false);
        CustomerTab customerTabContent = new CustomerTab();
        customersTab.setContent(customerTabContent);
        mainTabPane.getTabs().add(customersTab);
        tabs.put("customers", customersTab);

        // Vehicles Tab
        Tab vehiclesTab = new Tab();
        vehiclesTab.setId("vehicles");
        vehiclesTab.setClosable(false);
        VehicleTab vehicleTabContent = new VehicleTab();
        vehiclesTab.setContent(vehicleTabContent);
        mainTabPane.getTabs().add(vehiclesTab);
        tabs.put("vehicles", vehiclesTab);

        // Mechanics Tab
        Tab mechanicsTab = new Tab();
        mechanicsTab.setId("mechanics");
        mechanicsTab.setClosable(false);
        MechanicTab mechanicTabContent = new MechanicTab();
        mechanicsTab.setContent(mechanicTabContent);
        mainTabPane.getTabs().add(mechanicsTab);
        tabs.put("mechanics", mechanicsTab);

        // Services Tab
        Tab servicesTab = new Tab();
        servicesTab.setId("services");
        servicesTab.setClosable(false);
        ServiceTab serviceTabContent = new ServiceTab();
        servicesTab.setContent(serviceTabContent);
        mainTabPane.getTabs().add(servicesTab);
        tabs.put("services", servicesTab);

        // Parts Tab
        Tab partsTab = new Tab();
        partsTab.setId("parts");
        partsTab.setClosable(false);
        PartsTab partsTabContent = new PartsTab();
        partsTab.setContent(partsTabContent);
        mainTabPane.getTabs().add(partsTab);
        tabs.put("parts", partsTab);

        // Invoices Tab
        Tab invoicesTab = new Tab();
        invoicesTab.setId("invoices");
        invoicesTab.setClosable(false);
        InvoiceTab invoiceTabContent = new InvoiceTab();
        invoicesTab.setContent(invoiceTabContent);
        mainTabPane.getTabs().add(invoicesTab);
        tabs.put("invoices", invoicesTab);

        // Suppliers Tab
        Tab suppliersTab = new Tab();
        suppliersTab.setId("suppliers");
        suppliersTab.setClosable(false);
        SupplierTab supplierTabContent = new SupplierTab();
        suppliersTab.setContent(supplierTabContent);
        mainTabPane.getTabs().add(suppliersTab);
        tabs.put("suppliers", suppliersTab);

        // Reports Tab
        Tab reportsTab = new Tab();
        reportsTab.setId("reports");
        reportsTab.setClosable(false);
        ReportTab reportTabContent = new ReportTab();
        reportsTab.setContent(reportTabContent);
        mainTabPane.getTabs().add(reportsTab);
        tabs.put("reports", reportsTab);
    }

    private void showTab(String tabId) {
        Tab tab = tabs.get(tabId);
        if (tab != null) {
            mainTabPane.getSelectionModel().select(tab);
        }
        setActiveNavButton(navButtons.get(tabId));
    }

    private Button createNavButton(String text, String id) {
        Button btn = new Button(text);
        btn.setId(id);
        btn.getStyleClass().add("nav-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        return btn;
    }

    private void setActiveNavButton(Button button) {
        if (activeNavButton != null) {
            activeNavButton.getStyleClass().remove("active");
        }
        button.getStyleClass().add("active");
        activeNavButton = button;
    }

    private ScrollPane createDashboardContent() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);

        VBox dashboardContent = new VBox(25);
        dashboardContent.getStyleClass().add("dashboard-content");
        dashboardContent.setPadding(new Insets(25));

        // Welcome Card
        HBox welcomeCard = new HBox(20);
        welcomeCard.getStyleClass().add("welcome-card");
        welcomeCard.setAlignment(Pos.CENTER_LEFT);

        VBox welcomeText = new VBox(8);
        Label welcomeTitle = new Label("Welcome Back, Admin!");
        welcomeTitle.getStyleClass().add("welcome-title");
        Label welcomeSubtitle = new Label("Here's what's happening with your workshop today");
        welcomeSubtitle.getStyleClass().add("welcome-subtitle");
        welcomeText.getChildren().addAll(welcomeTitle, welcomeSubtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox dateTime = new HBox(20);
        dateTime.getStyleClass().add("date-time");
        dateTime.setAlignment(Pos.CENTER_RIGHT);
        Label dateLabel = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        dateLabel.getStyleClass().add("date-label");
        Label timeLabel = new Label(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
        timeLabel.getStyleClass().add("time-label");
        dateTime.getChildren().addAll(dateLabel, timeLabel);

        welcomeCard.getChildren().addAll(welcomeText, spacer, dateTime);

        // Stats Grid
        GridPane statsGrid = new GridPane();
        statsGrid.getStyleClass().add("stats-grid");

        int totalCustomers = DB.getTotalCustomers();
        double todayRevenue = DB.getTodayRevenue();
        int vehiclesInService = DB.getVehiclesInService();
        int lowStockParts = DB.getLowStockParts();

        VBox stat1 = createStatCard("💰", "Today's Revenue", String.format("$%.2f", todayRevenue),
                getRevenueTrend(todayRevenue), "#2E86AB");
        VBox stat2 = createStatCard("👥", "Total Customers", String.valueOf(totalCustomers),
                "", "#A23B72");
        VBox stat3 = createStatCard("🚗", "Vehicles in Service", String.valueOf(vehiclesInService),
                "", "#10b981");
        VBox stat4 = createStatCard("📦", "Low Stock Items", String.valueOf(lowStockParts),
                "Need reorder", "#f59e0b");

        statsGrid.add(stat1, 0, 0);
        statsGrid.add(stat2, 1, 0);
        statsGrid.add(stat3, 0, 1);
        statsGrid.add(stat4, 1, 1);
        GridPane.setHgrow(stat1, Priority.ALWAYS);
        GridPane.setHgrow(stat2, Priority.ALWAYS);
        GridPane.setHgrow(stat3, Priority.ALWAYS);
        GridPane.setHgrow(stat4, Priority.ALWAYS);

        // Recent Activity (using only invoices - no customer created_at)
        VBox recentSection = new VBox(15);
        recentSection.getStyleClass().add("recent-section");

        HBox sectionHeader = new HBox();
        sectionHeader.getStyleClass().add("section-header");
        Label recentTitle = new Label("Recent Activity");
        recentTitle.getStyleClass().add("section-title");
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        Button viewAllBtn = new Button("View All →");
        viewAllBtn.getStyleClass().add("view-all-btn");
        viewAllBtn.setOnAction(e -> showTab("invoices"));
        sectionHeader.getChildren().addAll(recentTitle, spacer2, viewAllBtn);

        VBox activityList = new VBox(10);
        activityList.getStyleClass().add("activity-list");

        try {
            ResultSet rs = DB.executeQuery(
                    "SELECT '🧾' as icon, CONCAT('Invoice #', invoice_id, ' created') as text, " +
                            "DATE_FORMAT(invoice_date, '%h:%i %p') as time " +
                            "FROM salesinvoice " +
                            "WHERE DATE(invoice_date) = CURDATE() " +
                            "ORDER BY invoice_date DESC " +
                            "LIMIT 4"
            );

            if (rs != null) {
                while (rs.next()) {
                    String icon = rs.getString("icon");
                    String text = rs.getString("text");
                    String time = rs.getString("time");
                    HBox activityItem = createActivityItem(icon, text, time);
                    activityList.getChildren().add(activityItem);
                }
            }

            // If no recent invoices, show a message
            if (activityList.getChildren().isEmpty()) {
                HBox noActivity = createActivityItem("📝", "No recent activity today", "Check back later");
                activityList.getChildren().add(noActivity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        recentSection.getChildren().addAll(sectionHeader, activityList);

        // Quick Actions
        VBox actionsSection = new VBox(15);
        actionsSection.getStyleClass().add("recent-section");

        HBox actionsHeader = new HBox();
        actionsHeader.getStyleClass().add("section-header");
        Label actionsTitle = new Label("Quick Actions");
        actionsTitle.getStyleClass().add("section-title");
        Region spacer3 = new Region();
        HBox.setHgrow(spacer3, Priority.ALWAYS);
        actionsHeader.getChildren().addAll(actionsTitle, spacer3);

        GridPane actionsGrid = new GridPane();
        actionsGrid.getStyleClass().add("actions-grid");
        actionsGrid.setHgap(15);
        actionsGrid.setVgap(15);

        Button action1 = createActionButton("➕ New Invoice", "🧾");
        action1.setOnAction(e -> showTab("invoices"));
        Button action2 = createActionButton("👥 Add Customer", "👤");
        action2.setOnAction(e -> showTab("customers"));
        Button action3 = createActionButton("🔧 Add Service", "⚙️");
        action3.setOnAction(e -> showTab("services"));
        Button action4 = createActionButton("📦 Manage Parts", "📦");
        action4.setOnAction(e -> showTab("parts"));
        Button action5 = createActionButton("🚗 Register Vehicle", "🚗");
        action5.setOnAction(e -> showTab("vehicles"));
        Button action6 = createActionButton("📊 View Reports", "📈");
        action6.setOnAction(e -> showTab("reports"));

        actionsGrid.add(action1, 0, 0);
        actionsGrid.add(action2, 1, 0);
        actionsGrid.add(action3, 2, 0);
        actionsGrid.add(action4, 0, 1);
        actionsGrid.add(action5, 1, 1);
        actionsGrid.add(action6, 2, 1);

        actionsSection.getChildren().addAll(actionsHeader, actionsGrid);

        dashboardContent.getChildren().addAll(welcomeCard, statsGrid, recentSection, actionsSection);
        scroll.setContent(dashboardContent);
        return scroll;
    }

    private String getRevenueTrend(double revenue) {
        if (revenue > 1000) return "↗️ High";
        else if (revenue > 500) return "→ Stable";
        else return "↘️ Low";
    }

    private VBox createStatCard(String icon, String title, String value, String change, String color) {
        VBox card = new VBox(15);
        card.getStyleClass().add("stat-card");
        card.setStyle("-fx-border-color: " + color + "40; -fx-background-color: " + color + "15;");
        card.setPadding(new Insets(20));

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("stat-icon");

        VBox info = new VBox(5);
        info.getStyleClass().add("stat-info");
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-title");
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");
        valueLabel.setStyle("-fx-text-fill: " + color + ";");

        info.getChildren().addAll(titleLabel, valueLabel);
        header.getChildren().addAll(iconLabel, info);

        Label changeLabel = new Label(change);
        changeLabel.getStyleClass().add("stat-change");

        card.getChildren().addAll(header, changeLabel);
        return card;
    }

    private HBox createActivityItem(String icon, String text, String time) {
        HBox item = new HBox(15);
        item.getStyleClass().add("activity-item");
        item.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("activity-icon");

        VBox content = new VBox(3);
        content.getStyleClass().add("activity-content");
        Label textLabel = new Label(text);
        textLabel.getStyleClass().add("activity-text");
        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("activity-time");

        content.getChildren().addAll(textLabel, timeLabel);
        item.getChildren().addAll(iconLabel, content);
        return item;
    }

    private Button createActionButton(String text, String icon) {
        Button btn = new Button();
        btn.getStyleClass().add("action-btn");
        btn.setPrefSize(120, 100);

        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);

        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("action-icon");

        Label textLabel = new Label(text);
        textLabel.getStyleClass().add("action-text");
        textLabel.setWrapText(true);
        textLabel.setAlignment(Pos.CENTER);

        content.getChildren().addAll(iconLabel, textLabel);
        btn.setGraphic(content);

        return btn;
    }

    public static void main(String[] args) {
        launch(args);
    }
}