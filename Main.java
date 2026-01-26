import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Main extends Application {

    private Map<String, Button> navButtons = new HashMap<>();
    private Button activeNavButton = null;
    private TabPane mainTabPane;
    private Map<String, Tab> tabs = new HashMap<>();

    // خصائص Dashboard ديناميكية
    private StringProperty totalCustomers = new SimpleStringProperty("0");
    private StringProperty todayRevenue = new SimpleStringProperty("$0.00");
    private StringProperty vehiclesInService = new SimpleStringProperty("0");
    private StringProperty lowStockParts = new SimpleStringProperty("0");
    private StringProperty todayInvoices = new SimpleStringProperty("0");
    private StringProperty activeServices = new SimpleStringProperty("0");
    private StringProperty totalSuppliers = new SimpleStringProperty("0");
    private StringProperty totalMechanics = new SimpleStringProperty("0");
    private StringProperty totalVehicles = new SimpleStringProperty("0");
    private StringProperty totalParts = new SimpleStringProperty("0");

    // ObservableList للنشاطات الحديثة
    private ObservableList<HBox> recentActivities = FXCollections.observableArrayList();

    // مراجع لمكونات Dashboard للتحديث
    private VBox activityListContainer;
    private GridPane statsGrid;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Car Workshop Management System");

        // اختبار الاتصال بقاعدة البيانات
        if (!DB.testConnection()) {
            showConnectionError();
            return;
        }

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
        Label logoSubtext = new Label("Vehicle Management System");
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
        dashboardBtn.setOnAction(e -> {
            showTab("dashboard");
            refreshDashboard();
        });
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

        // إضافة الأزرار الجديدة
        Button purchasesBtn = createNavButton("🛒 Purchases", "purchases");
        purchasesBtn.setOnAction(e -> {
            showTab("purchases");
            setActiveNavButton(purchasesBtn);
        });
        navButtons.put("purchases", purchasesBtn);

        Button workHoursBtn = createNavButton("⏱ Work Hours", "workhours");
        workHoursBtn.setOnAction(e -> {
            showTab("workhours");
            setActiveNavButton(workHoursBtn);
        });
        navButtons.put("workhours", workHoursBtn);

        Button reportsBtn = createNavButton("📈 Reports", "reports");
        reportsBtn.setOnAction(e -> {
            showTab("reports");
            setActiveNavButton(reportsBtn);
        });
        navButtons.put("reports", reportsBtn);

        opsSection.getChildren().addAll(opsTitle, servicesBtn, partsBtn, invoicesBtn,
                suppliersBtn, purchasesBtn, workHoursBtn, reportsBtn);

        sidebar.getChildren().addAll(dashSection, mgmtSection, opsSection);
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
        refreshDashboard();

        primaryStage.setOnCloseRequest(e -> {
            DB.closeConnection();
        });
    }

    private void createTabs() {
        // Dashboard Tab
        Tab dashboardTab = new Tab();
        dashboardTab.setId("dashboard");
        dashboardTab.setClosable(false);
        dashboardTab.setText("Dashboard");

        ScrollPane dashboardContent = createDashboardContent();
        dashboardTab.setContent(dashboardContent);
        mainTabPane.getTabs().add(dashboardTab);
        tabs.put("dashboard", dashboardTab);

        // Customers Tab
        Tab customersTab = new Tab();
        customersTab.setId("customers");
        customersTab.setClosable(false);
        customersTab.setText("Customers");
        CustomerTab customerTabContent = new CustomerTab();
        customersTab.setContent(customerTabContent);
        mainTabPane.getTabs().add(customersTab);
        tabs.put("customers", customersTab);

        // Vehicles Tab
        Tab vehiclesTab = new Tab();
        vehiclesTab.setId("vehicles");
        vehiclesTab.setClosable(false);
        vehiclesTab.setText("Vehicles");
        VehicleTab vehicleTabContent = new VehicleTab();
        vehiclesTab.setContent(vehicleTabContent);
        mainTabPane.getTabs().add(vehiclesTab);
        tabs.put("vehicles", vehiclesTab);

        // Mechanics Tab
        Tab mechanicsTab = new Tab();
        mechanicsTab.setId("mechanics");
        mechanicsTab.setClosable(false);
        mechanicsTab.setText("Mechanics");
        MechanicTab mechanicTabContent = new MechanicTab();
        mechanicsTab.setContent(mechanicTabContent);
        mainTabPane.getTabs().add(mechanicsTab);
        tabs.put("mechanics", mechanicsTab);

        // Services Tab
        Tab servicesTab = new Tab();
        servicesTab.setId("services");
        servicesTab.setClosable(false);
        servicesTab.setText("Services");
        ServiceTab serviceTabContent = new ServiceTab();
        servicesTab.setContent(serviceTabContent);
        mainTabPane.getTabs().add(servicesTab);
        tabs.put("services", servicesTab);

        // Parts Tab
        Tab partsTab = new Tab();
        partsTab.setId("parts");
        partsTab.setClosable(false);
        partsTab.setText("Parts");
        PartsTab partsTabContent = new PartsTab();
        partsTab.setContent(partsTabContent);
        mainTabPane.getTabs().add(partsTab);
        tabs.put("parts", partsTab);

        // Invoices Tab
        Tab invoicesTab = new Tab();
        invoicesTab.setId("invoices");
        invoicesTab.setClosable(false);
        invoicesTab.setText("Invoices");
        InvoiceTab invoiceTabContent = new InvoiceTab();
        invoicesTab.setContent(invoiceTabContent);
        mainTabPane.getTabs().add(invoicesTab);
        tabs.put("invoices", invoicesTab);

        // Suppliers Tab
        Tab suppliersTab = new Tab();
        suppliersTab.setId("suppliers");
        suppliersTab.setClosable(false);
        suppliersTab.setText("Suppliers");
        SupplierTab supplierTabContent = new SupplierTab();
        suppliersTab.setContent(supplierTabContent);
        mainTabPane.getTabs().add(suppliersTab);
        tabs.put("suppliers", suppliersTab);

        // Purchases Tab
        Tab purchasesTab = new Tab();
        purchasesTab.setId("purchases");
        purchasesTab.setClosable(false);
        purchasesTab.setText("Purchases");
        PurchaseTab purchaseTabContent = new PurchaseTab();
        purchasesTab.setContent(purchaseTabContent);
        mainTabPane.getTabs().add(purchasesTab);
        tabs.put("purchases", purchasesTab);

        // Work Hours Tab
        Tab workHoursTab = new Tab();
        workHoursTab.setId("workhours");
        workHoursTab.setClosable(false);
        workHoursTab.setText("Work Hours");
        MechanicWorkHoursTab workHoursTabContent = new MechanicWorkHoursTab();
        workHoursTab.setContent(workHoursTabContent);
        mainTabPane.getTabs().add(workHoursTab);
        tabs.put("workhours", workHoursTab);

        // Reports Tab
        Tab reportsTab = new Tab();
        reportsTab.setId("reports");
        reportsTab.setClosable(false);
        reportsTab.setText("Reports");
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
        scroll.getStyleClass().add("dashboard-scroll");

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
        statsGrid = new GridPane();
        statsGrid.getStyleClass().add("stats-grid");
        statsGrid.setHgap(20);
        statsGrid.setVgap(20);

        // إنشاء البطاقات الديناميكية
        updateStatsGrid();

        // Recent Activity Section
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

        activityListContainer = new VBox(10);
        activityListContainer.getStyleClass().add("activity-list");

        // ربط قائمة النشاطات
        refreshRecentActivities();

        recentSection.getChildren().addAll(sectionHeader, activityListContainer);

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

        actionsGrid.add(action1, 0, 0);
        actionsGrid.add(action2, 1, 0);
        actionsGrid.add(action3, 2, 0);
        actionsGrid.add(action4, 0, 1);
        actionsGrid.add(action5, 1, 1);

        actionsSection.getChildren().addAll(actionsHeader, actionsGrid);

        dashboardContent.getChildren().addAll(welcomeCard, statsGrid, recentSection, actionsSection);
        scroll.setContent(dashboardContent);
        return scroll;
    }

    private void updateStatsGrid() {
        if (statsGrid == null) return;

        statsGrid.getChildren().clear();

        // ربط القيم بـ DashboardManager
        DashboardManager dashboardManager = DashboardManager.getInstance();
        dashboardManager.refreshAllStats();

        // تحديث القيم
        totalCustomers.set(dashboardManager.getFormattedStat("totalCustomers"));
        totalVehicles.set(dashboardManager.getFormattedStat("totalVehicles"));
        totalParts.set(dashboardManager.getFormattedStat("totalParts"));
        todayRevenue.set(dashboardManager.getFormattedStat("todayRevenue"));
        vehiclesInService.set(dashboardManager.getFormattedStat("vehiclesInService"));
        lowStockParts.set(dashboardManager.getFormattedStat("lowStockParts"));
        todayInvoices.set(dashboardManager.getFormattedStat("totalInvoices"));
        activeServices.set(dashboardManager.getFormattedStat("activeServices"));
        totalSuppliers.set(dashboardManager.getFormattedStat("totalSuppliers"));
        totalMechanics.set(dashboardManager.getFormattedStat("totalMechanics"));

        // إنشاء بطاقات الإحصائيات
        VBox stat1 = createDynamicStatCard("💰", "Today's Revenue", todayRevenue, "#2E86AB");
        VBox stat2 = createDynamicStatCard("👥", "Total Customers", totalCustomers, "#A23B72");
        VBox stat3 = createDynamicStatCard("🚗", "Vehicles in Service", vehiclesInService, "#10b981");
        VBox stat4 = createDynamicStatCard("📦", "Low Stock Items", lowStockParts, "#f59e0b");
        VBox stat5 = createDynamicStatCard("🧾", "Today's Invoices", todayInvoices, "#8b5cf6");
        VBox stat6 = createDynamicStatCard("⚙️", "Active Services", activeServices, "#3b82f6");
        VBox stat7 = createDynamicStatCard("🏢", "Suppliers", totalSuppliers, "#ef4444");
        VBox stat8 = createDynamicStatCard("🔧", "Mechanics", totalMechanics, "#f97316");
        VBox stat9 = createDynamicStatCard("🚙", "Total Vehicles", totalVehicles, "#06b6d4");
        VBox stat10 = createDynamicStatCard("🔩", "Total Parts", totalParts, "#a855f7");

        // ترتيب البطاقات في الشبكة (3 أعمدة × 4 صفوف)
        statsGrid.add(stat1, 0, 0);
        statsGrid.add(stat2, 1, 0);
        statsGrid.add(stat3, 2, 0);
        statsGrid.add(stat4, 0, 1);
        statsGrid.add(stat5, 1, 1);
        statsGrid.add(stat6, 2, 1);
        statsGrid.add(stat7, 0, 2);
        statsGrid.add(stat8, 1, 2);
        statsGrid.add(stat9, 2, 2);
        statsGrid.add(stat10, 0, 3);

        // تعيين التمدد
        for (int i = 0; i < 10; i++) {
            GridPane.setHgrow(statsGrid.getChildren().get(i), Priority.ALWAYS);
        }
    }

    private VBox createDynamicStatCard(String icon, String title, StringProperty valueProperty, String color) {
        VBox card = new VBox(15);
        card.getStyleClass().add("stat-card");
        card.setStyle("-fx-border-color: " + color + "40; -fx-background-color: " + color + "15;");
        card.setPadding(new Insets(20));
        card.setPrefHeight(120);

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("stat-icon");
        iconLabel.setStyle("-fx-font-size: 20px;");

        VBox info = new VBox(5);
        info.getStyleClass().add("stat-info");
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-title");
        Label valueLabel = new Label();
        valueLabel.getStyleClass().add("stat-value");
        valueLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 24px; -fx-font-weight: bold;");

        // ربط القيمة بالخاصية
        valueLabel.textProperty().bind(valueProperty);

        info.getChildren().addAll(titleLabel, valueLabel);
        header.getChildren().addAll(iconLabel, info);

        card.getChildren().addAll(header);
        return card;
    }

    private void refreshRecentActivities() {
        if (activityListContainer == null) return;

        activityListContainer.getChildren().clear();

        try {
            // استعلام للفواتير اليوم
            ResultSet rsInvoices = DB.executeQuery(
                    "SELECT '🧾' as icon, CONCAT('Invoice #', invoice_id, ' - $', total_amount) as text, " +
                            "DATE_FORMAT(invoice_date, '%h:%i %p') as time " +
                            "FROM salesinvoice " +
                            "WHERE DATE(invoice_date) = CURDATE() " +
                            "ORDER BY invoice_date DESC " +
                            "LIMIT 5"
            );

            if (rsInvoices != null) {
                while (rsInvoices.next()) {
                    String icon = rsInvoices.getString("icon");
                    String text = rsInvoices.getString("text");
                    String time = rsInvoices.getString("time");
                    HBox activityItem = createActivityItem(icon, text, time);
                    activityListContainer.getChildren().add(activityItem);
                }
                rsInvoices.close();
            }

            // إذا لم توجد نشاطات
            if (activityListContainer.getChildren().isEmpty()) {
                HBox noActivity = createActivityItem("📝", "No recent activity today", "Check back later");
                activityListContainer.getChildren().add(noActivity);
            }

        } catch (Exception e) {
            e.printStackTrace();
            HBox errorItem = createActivityItem("⚠️", "Error loading activities", "Try again");
            activityListContainer.getChildren().add(errorItem);
        }
    }

    private HBox createActivityItem(String icon, String text, String time) {
        HBox item = new HBox(15);
        item.getStyleClass().add("activity-item");
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(10));

        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("activity-icon");
        iconLabel.setStyle("-fx-font-size: 18px;");

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
        btn.setPrefSize(150, 100);
        btn.setMaxSize(150, 100);

        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);

        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("action-icon");
        iconLabel.setStyle("-fx-font-size: 32px;");

        Label textLabel = new Label(text);
        textLabel.getStyleClass().add("action-text");
        textLabel.setWrapText(true);
        textLabel.setAlignment(Pos.CENTER);
        textLabel.setMaxWidth(120);

        content.getChildren().addAll(iconLabel, textLabel);
        btn.setGraphic(content);

        return btn;
    }

    // طريقة لتحديث Dashboard
    public void refreshDashboard() {
        Platform.runLater(() -> {
            DashboardManager dashboardManager = DashboardManager.getInstance();
            dashboardManager.refreshAllStats();

            // تحديث الشبكة الإحصائية
            updateStatsGrid();

            // تحديث النشاطات الحديثة
            refreshRecentActivities();
        });
    }

    // طريقة ثابتة لتحديث Dashboard من أي مكان
    public static void refreshDashboardGlobal() {
        Platform.runLater(() -> {
            DashboardManager.getInstance().refreshAllStats();
        });
    }

    private void showConnectionError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database Connection Error");
        alert.setHeaderText("Cannot connect to database");
        alert.setContentText("Please check:\n" +
                "1. MySQL server is running\n" +
                "2. Database 'car_workshop_db' exists\n" +
                "3. Username and password are correct\n" +
                "4. MySQL connector is in classpath");
        alert.showAndWait();
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}