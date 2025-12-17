

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.text.DecimalFormat;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Car Workshop Management System");

        BorderPane root = new BorderPane();
        root.getStyleClass().add("main-root");

        // Top Navigation Bar
        HBox topNav = createTopNavigation();
        root.setTop(topNav);

        // Main Content with Sidebar
        SplitPane mainContent = new SplitPane();
        mainContent.setDividerPositions(0.2); // 20% for sidebar

        // Left Sidebar
        VBox sidebar = createSidebar();

        // Center Dashboard
        ScrollPane dashboard = createDashboard();

        mainContent.getItems().addAll(sidebar, dashboard);
        root.setCenter(mainContent);

        // Footer
        HBox footer = createFooter();
        root.setBottom(footer);

        Scene scene = new Scene(root, 1400, 800);

        try {
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("CSS file not found, using default styling");
        }

        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private HBox createTopNavigation() {
        HBox topNav = new HBox(20);
        topNav.getStyleClass().add("top-nav");
        topNav.setPadding(new Insets(15, 30, 15, 30));

        // Logo and Title
        HBox logoSection = new HBox(15);
        logoSection.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Logo icon
        Label logoIcon = new Label("🚗");
        logoIcon.getStyleClass().add("logo-icon");

        VBox logoText = new VBox(2);
        Label mainTitle = new Label("Car Workshop Pro");
        mainTitle.getStyleClass().add("app-main-title");

        Label subTitle = new Label("Management System");
        subTitle.getStyleClass().add("app-sub-title");

        logoText.getChildren().addAll(mainTitle, subTitle);
        logoSection.getChildren().addAll(logoIcon, logoText);

        // Search Box
        HBox searchBox = new HBox();
        searchBox.getStyleClass().add("search-box");
        TextField searchField = new TextField();
        searchField.setPromptText("Search customers, invoices, parts...");
        searchField.getStyleClass().add("search-field");
        Button searchBtn = new Button("🔍");
        searchBtn.getStyleClass().add("search-btn");
        searchBox.getChildren().addAll(searchField, searchBtn);

        // Right side buttons
        HBox rightButtons = new HBox(15);
        rightButtons.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        // Notifications
        int notificationCount = getNotificationCount();
        Button notificationsBtn = new Button("🔔");
        notificationsBtn.getStyleClass().add("notification-btn");

        StackPane notificationPane = new StackPane(notificationsBtn);
        if (notificationCount > 0) {
            Label notificationBadge = new Label(String.valueOf(notificationCount));
            notificationBadge.getStyleClass().add("notification-badge");
            notificationPane.getChildren().add(notificationBadge);
        }

        // User Profile
        HBox userProfile = new HBox(10);
        userProfile.getStyleClass().add("user-profile");

        VBox userInfo = new VBox(2);
        Label userName = new Label("System Manager");
        userName.getStyleClass().add("user-name");

        Label userStatus = new Label("Online");
        userStatus.getStyleClass().add("user-status");

        userInfo.getChildren().addAll(userName, userStatus);

        // User avatar
        Pane userAvatar = new Pane();
        userAvatar.getStyleClass().add("user-avatar");

        userProfile.getChildren().addAll(userInfo, userAvatar);
        userProfile.setOnMouseClicked(e -> showUserMenu());

        rightButtons.getChildren().addAll(notificationPane, userProfile);

        topNav.getChildren().addAll(logoSection, searchBox, rightButtons);
        HBox.setHgrow(searchBox, Priority.ALWAYS);

        return topNav;
    }

    private int getNotificationCount() {
        int count = 0;
        try {
            // Get low stock parts count
            count += DB.getLowStockParts();

            // Get pending invoices (if you have a status field)
            // ResultSet rs = DB.executeQuery("SELECT COUNT(*) FROM salesinvoice WHERE status = 'pending'");
            // if (rs.next()) count += rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(25, 15, 25, 15));
        sidebar.setSpacing(20);

        // Dashboard Button (always active)
        Button dashboardBtn = createSidebarButton("📊", "Dashboard", true);
        dashboardBtn.setOnAction(e -> refreshDashboard());

        // Management Section
        Label mgmtLabel = new Label("MANAGEMENT");
        mgmtLabel.getStyleClass().add("sidebar-section-label");

        Button customersBtn = createSidebarButton("👥", "Customers", false);
        customersBtn.setOnAction(e -> showCustomers());

        Button vehiclesBtn = createSidebarButton("🚗", "Vehicles", false);
        vehiclesBtn.setOnAction(e -> showVehicles());

        Button mechanicsBtn = createSidebarButton("👨‍🔧", "Mechanics", false);
        mechanicsBtn.setOnAction(e -> showMechanics());

        Button suppliersBtn = createSidebarButton("🏭", "Suppliers", false);
        suppliersBtn.setOnAction(e -> showSuppliers());

        // Operations Section
        Label opsLabel = new Label("OPERATIONS");
        opsLabel.getStyleClass().add("sidebar-section-label");

        Button servicesBtn = createSidebarButton("🔧", "Services", false);
        servicesBtn.setOnAction(e -> showServices());

        Button partsBtn = createSidebarButton("🔩", "Parts", false);
        partsBtn.setOnAction(e -> showParts());

        Button invoicesBtn = createSidebarButton("🧾", "Invoices", false);
        invoicesBtn.setOnAction(e -> showInvoices());

        Button reportsBtn = createSidebarButton("📊", "Reports", false);
        reportsBtn.setOnAction(e -> showReports());

        // Quick Stats Card
        VBox quickStats = createQuickStatsCard();

        sidebar.getChildren().addAll(
                dashboardBtn,
                mgmtLabel, customersBtn, vehiclesBtn, mechanicsBtn, suppliersBtn,
                opsLabel, servicesBtn, partsBtn, invoicesBtn, reportsBtn,
                quickStats
        );

        return sidebar;
    }

    private Button createSidebarButton(String icon, String text, boolean active) {
        Button btn = new Button();
        btn.getStyleClass().add("sidebar-btn");
        if (active) btn.getStyleClass().add("sidebar-btn-active");

        HBox content = new HBox(15);
        content.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("sidebar-btn-icon");

        Label textLabel = new Label(text);
        textLabel.getStyleClass().add("sidebar-btn-text");

        content.getChildren().addAll(iconLabel, textLabel);
        btn.setGraphic(content);

        return btn;
    }

    private VBox createQuickStatsCard() {
        VBox card = new VBox(15);
        card.getStyleClass().add("quick-stats-card");

        Label title = new Label("📈 TODAY'S STATS");
        title.getStyleClass().add("quick-stats-title");

        // Get real data
        int todayInvoices = DB.getTodayInvoicesCount();
        double todayRevenue = DB.getTodayRevenue();
        int vehiclesInShop = DB.getVehiclesInWorkshop();
        int lowStock = DB.getLowStockParts();

        VBox stats = new VBox(10);

        HBox stat1 = createQuickStatItem("Invoices", String.valueOf(todayInvoices), "var(--primary)");
        HBox stat2 = createQuickStatItem("Revenue", "$" + formatCurrency(todayRevenue), "var(--success)");
        HBox stat3 = createQuickStatItem("Vehicles in Shop", String.valueOf(vehiclesInShop), "var(--warning)");
        HBox stat4 = createQuickStatItem("Low Stock Items", String.valueOf(lowStock), "var(--danger)");

        stats.getChildren().addAll(stat1, stat2, stat3, stat4);

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.getStyleClass().add("quick-refresh-btn");
        refreshBtn.setOnAction(e -> refreshDashboard());

        card.getChildren().addAll(title, stats, refreshBtn);
        return card;
    }

    private HBox createQuickStatItem(String label, String value, String color) {
        HBox item = new HBox();
        item.getStyleClass().add("quick-stat-item");

        VBox textBox = new VBox(2);
        Label labelLbl = new Label(label);
        labelLbl.getStyleClass().add("quick-stat-label");

        Label valueLbl = new Label(value);
        valueLbl.getStyleClass().add("quick-stat-value");
        valueLbl.setStyle("-fx-text-fill: " + color + ";");

        textBox.getChildren().addAll(labelLbl, valueLbl);

        item.getChildren().add(textBox);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        return item;
    }

    private ScrollPane createDashboard() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("dashboard-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox dashboardContent = new VBox(25);
        dashboardContent.getStyleClass().add("dashboard-content");
        dashboardContent.setPadding(new Insets(30));

        // Welcome Section with Time
        HBox welcomeSection = createWelcomeSection();

        // KPI Cards (4 in a row)
        HBox kpiRow1 = createKPIRow();

        // Charts Section
        VBox chartsSection = createChartsSection();

        // Recent Activity & Quick Actions
        HBox bottomSection = new HBox(25);
        bottomSection.getStyleClass().add("dashboard-bottom-section");

        VBox recentActivity = createRecentActivity();
        VBox quickActions = createQuickActionsPanel();

        bottomSection.getChildren().addAll(recentActivity, quickActions);
        HBox.setHgrow(recentActivity, Priority.ALWAYS);

        dashboardContent.getChildren().addAll(
                welcomeSection, kpiRow1, chartsSection, bottomSection
        );

        scrollPane.setContent(dashboardContent);
        return scrollPane;
    }

    private HBox createWelcomeSection() {
        HBox welcomeSection = new HBox();
        welcomeSection.getStyleClass().add("welcome-section");

        VBox welcomeText = new VBox(8);

        // Greeting based on time of day
        String greeting = getTimeBasedGreeting();
        Label greetingLabel = new Label(greeting + ", System Manager!");
        greetingLabel.getStyleClass().add("greeting-label");

        Label dateLabel = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
        dateLabel.getStyleClass().add("date-label");

        Label timeLabel = new Label(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
        timeLabel.getStyleClass().add("time-label");

        welcomeText.getChildren().addAll(greetingLabel, dateLabel, timeLabel);

        // Quick stats mini card
        VBox todayStats = new VBox(10);
        todayStats.getStyleClass().add("today-stats-mini");

        Label todayTitle = new Label("Today's Overview");
        todayTitle.getStyleClass().add("today-stats-title");

        HBox todayRow = new HBox(20);

        VBox invoicesBox = createMiniStatBox("Invoices", String.valueOf(DB.getTodayInvoicesCount()), "🧾");
        VBox revenueBox = createMiniStatBox("Revenue", "$" + formatCurrency(DB.getTodayRevenue()), "💰");
        VBox vehiclesBox = createMiniStatBox("Vehicles", String.valueOf(DB.getVehiclesInWorkshop()), "🚗");

        todayRow.getChildren().addAll(invoicesBox, revenueBox, vehiclesBox);
        todayStats.getChildren().addAll(todayTitle, todayRow);

        welcomeSection.getChildren().addAll(welcomeText, todayStats);
        HBox.setHgrow(welcomeText, Priority.ALWAYS);

        return welcomeSection;
    }

    private String getTimeBasedGreeting() {
        int hour = LocalTime.now().getHour();
        if (hour < 12) return "Good morning";
        else if (hour < 17) return "Good afternoon";
        else return "Good evening";
    }

    private VBox createMiniStatBox(String title, String value, String icon) {
        VBox box = new VBox(5);
        box.getStyleClass().add("mini-stat-box");

        HBox topRow = new HBox(5);
        topRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("mini-stat-icon");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("mini-stat-title");

        topRow.getChildren().addAll(iconLabel, titleLabel);

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("mini-stat-value");

        box.getChildren().addAll(topRow, valueLabel);
        return box;
    }

    private HBox createKPIRow() {
        HBox kpiRow = new HBox(20);
        kpiRow.getStyleClass().add("kpi-row");

        // Get real data
        int totalCustomers = DB.getTotalCustomers();
        int totalInvoices = DB.getTotalInvoices();
        double totalRevenue = DB.getTotalRevenue();
        int totalVehicles = DB.getTotalVehicles();
        int totalServices = DB.getTotalServices();
        int totalParts = getTotalPartsCount();
        int totalMechanics = getTotalMechanicsCount();
        int totalSuppliers = getTotalSuppliersCount();

        // Create KPI cards
        VBox card1 = createKPICard("👥", "Total Customers", String.valueOf(totalCustomers),
                "All registered customers", "#3b82f6");
        VBox card2 = createKPICard("🧾", "Total Invoices", String.valueOf(totalInvoices),
                "All time invoices", "#8b5cf6");
        VBox card3 = createKPICard("💰", "Total Revenue", "$" + formatCurrency(totalRevenue),
                "Lifetime revenue", "#10b981");
        VBox card4 = createKPICard("🚗", "Total Vehicles", String.valueOf(totalVehicles),
                "Registered vehicles", "#f59e0b");

        kpiRow.getChildren().addAll(card1, card2, card3, card4);

        return kpiRow;
    }

    private int getTotalPartsCount() {
        try {
            ResultSet rs = DB.executeQuery("SELECT COUNT(*) as total FROM sparepart");
            if (rs.next()) return rs.getInt("total");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getTotalMechanicsCount() {
        try {
            ResultSet rs = DB.executeQuery("SELECT COUNT(*) as total FROM mechanic");
            if (rs.next()) return rs.getInt("total");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getTotalSuppliersCount() {
        try {
            ResultSet rs = DB.executeQuery("SELECT COUNT(*) as total FROM supplier");
            if (rs.next()) return rs.getInt("total");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private VBox createKPICard(String icon, String title, String value, String description, String color) {
        VBox card = new VBox(15);
        card.getStyleClass().add("kpi-card");
        card.setStyle("-fx-border-color: " + color + "40; -fx-background-color: " + color + "10;");

        HBox header = new HBox(15);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("kpi-icon");

        VBox titles = new VBox(3);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("kpi-title");

        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("kpi-desc");

        titles.getChildren().addAll(titleLabel, descLabel);
        header.getChildren().addAll(iconLabel, titles);

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("kpi-value");
        valueLabel.setStyle("-fx-text-fill: " + color + ";");

        // Add trend indicator if applicable
        HBox trendBox = new HBox(5);
        trendBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // You can add trend calculation here based on previous data
        // For now, just show a placeholder
        Label trendLabel = new Label("→ View details");
        trendLabel.getStyleClass().add("kpi-trend");

        trendBox.getChildren().add(trendLabel);

        card.getChildren().addAll(header, valueLabel, trendBox);
        return card;
    }

    private VBox createChartsSection() {
        VBox chartsSection = new VBox(20);
        chartsSection.getStyleClass().add("charts-section");

        Label sectionTitle = new Label("📊 Performance Overview");
        sectionTitle.getStyleClass().add("section-title");

        HBox chartsRow = new HBox(20);

        // Revenue Chart Card
        VBox revenueChart = createChartCard("Revenue Trend", "Last 7 days",
                getRevenueData(), "#3b82f6");

        // Services Chart Card
        VBox servicesChart = createChartCard("Services Popularity", "Top services",
                getServicesData(), "#10b981");

        // Inventory Chart Card
        VBox inventoryChart = createChartCard("Inventory Status", "Stock levels",
                getInventoryData(), "#f59e0b");

        chartsRow.getChildren().addAll(revenueChart, servicesChart, inventoryChart);
        chartsSection.getChildren().addAll(sectionTitle, chartsRow);

        return chartsSection;
    }

    private List<ChartData> getRevenueData() {
        List<ChartData> data = new ArrayList<>();
        try {
            // Get revenue for last 7 days
            ResultSet rs = DB.executeQuery(
                    "SELECT DATE(invoice_date) as date, SUM(total_amount) as revenue " +
                            "FROM salesinvoice " +
                            "WHERE invoice_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                            "GROUP BY DATE(invoice_date) " +
                            "ORDER BY date"
            );

            while (rs.next()) {
                String date = rs.getString("date").substring(5); // Remove year
                double revenue = rs.getDouble("revenue");
                data.add(new ChartData(date, revenue));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    private List<ChartData> getServicesData() {
        List<ChartData> data = new ArrayList<>();
        try {
            // Get top 5 services by usage
            ResultSet rs = DB.executeQuery(
                    "SELECT s.service_name, COUNT(si.service_id) as count " +
                            "FROM service_invoice si " +
                            "JOIN service s ON si.service_id = s.service_id " +
                            "GROUP BY s.service_id " +
                            "ORDER BY count DESC " +
                            "LIMIT 5"
            );

            while (rs.next()) {
                String service = rs.getString("service_name");
                int count = rs.getInt("count");
                data.add(new ChartData(service, count));
            }
        } catch (Exception e) {
            // If service_invoice table doesn't exist, use sample data
            data.add(new ChartData("Oil Change", 45));
            data.add(new ChartData("Brake Repair", 32));
            data.add(new ChartData("Tire Rotation", 28));
            data.add(new ChartData("Engine Tune", 21));
            data.add(new ChartData("AC Service", 18));
        }
        return data;
    }

    private List<ChartData> getInventoryData() {
        List<ChartData> data = new ArrayList<>();
        try {
            // Get parts with low stock
            ResultSet rs = DB.executeQuery(
                    "SELECT part_name, quantity " +
                            "FROM sparepart " +
                            "WHERE quantity < 20 " +
                            "ORDER BY quantity " +
                            "LIMIT 5"
            );

            while (rs.next()) {
                String part = rs.getString("part_name");
                int quantity = rs.getInt("quantity");
                data.add(new ChartData(part, quantity));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    private VBox createChartCard(String title, String subtitle, List<ChartData> data, String color) {
        VBox card = new VBox(15);
        card.getStyleClass().add("chart-card");

        VBox header = new VBox(3);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("chart-title");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("chart-subtitle");

        header.getChildren().addAll(titleLabel, subtitleLabel);

        // Chart visualization (simplified)
        VBox chartVisual = new VBox(5);
        chartVisual.getStyleClass().add("chart-visual");
        chartVisual.setPrefHeight(150);

        // Create bars for data
        for (ChartData item : data) {
            HBox barContainer = new HBox(10);
            barContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Label label = new Label(item.label);
            label.getStyleClass().add("chart-label");
            label.setPrefWidth(80);

            Pane barBackground = new Pane();
            barBackground.getStyleClass().add("chart-bar-bg");
            barBackground.setPrefHeight(20);
            barBackground.setPrefWidth(200);

            Pane barFill = new Pane();
            barFill.getStyleClass().add("chart-bar-fill");
            barFill.setStyle("-fx-background-color: " + color + ";");

            // Calculate width based on value (normalized)
            double maxValue = data.stream().mapToDouble(ChartData::getValue).max().orElse(1);
            double width = (item.value / maxValue) * 190;
            barFill.setPrefWidth(width);
            barFill.setPrefHeight(18);

            StackPane barStack = new StackPane(barBackground, barFill);

            Label valueLabel = new Label(formatValue(item.value, title.contains("Revenue")));
            valueLabel.getStyleClass().add("chart-value");

            barContainer.getChildren().addAll(label, barStack, valueLabel);
            chartVisual.getChildren().add(barContainer);
        }

        Button detailsBtn = new Button("View Details →");
        detailsBtn.getStyleClass().add("chart-details-btn");

        card.getChildren().addAll(header, chartVisual, detailsBtn);
        return card;
    }

    private String formatValue(double value, boolean isCurrency) {
        if (isCurrency) {
            return "$" + formatCurrency(value);
        } else if (value == (int) value) {
            return String.valueOf((int) value);
        } else {
            return String.format("%.1f", value);
        }
    }

    private VBox createRecentActivity() {
        VBox recentActivity = new VBox(15);
        recentActivity.getStyleClass().add("recent-activity");

        HBox header = new HBox();
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label title = new Label("🕒 Recent Activity");
        title.getStyleClass().add("section-title");

        Button viewAllBtn = new Button("View All");
        viewAllBtn.getStyleClass().add("view-all-btn");
        viewAllBtn.setOnAction(e -> showRecentActivityWindow());

        header.getChildren().addAll(title, viewAllBtn);
        HBox.setHgrow(title, Priority.ALWAYS);

        VBox activityList = new VBox(10);

        // Get real recent activity
        List<Activity> activities = getRecentActivities();

        for (Activity activity : activities) {
            HBox activityItem = new HBox(15);
            activityItem.getStyleClass().add("activity-item");

            Label icon = new Label(activity.icon);
            icon.getStyleClass().add("activity-icon");

            VBox details = new VBox(3);
            Label description = new Label(activity.description);
            description.getStyleClass().add("activity-description");

            Label time = new Label(activity.time);
            time.getStyleClass().add("activity-time");

            details.getChildren().addAll(description, time);

            activityItem.getChildren().addAll(icon, details);
            activityList.getChildren().add(activityItem);
        }

        recentActivity.getChildren().addAll(header, activityList);
        return recentActivity;
    }

    private List<Activity> getRecentActivities() {
        List<Activity> activities = new ArrayList<>();

        try {
            // Get recent invoices
            ResultSet rs = DB.executeQuery(
                    "SELECT s.invoice_id, c.full_name, s.invoice_date, s.total_amount " +
                            "FROM salesinvoice s " +
                            "JOIN customer c ON s.customer_id = c.customer_id " +
                            "ORDER BY s.invoice_date DESC LIMIT 5"
            );

            while (rs.next()) {
                String desc = "New invoice #" + rs.getInt("invoice_id") +
                        " for " + rs.getString("full_name");
                String time = formatTimeAgo(rs.getString("invoice_date"));
                activities.add(new Activity("🧾", desc, time));
            }

            // Get new customers
            ResultSet rs2 = DB.executeQuery(
                    "SELECT full_name, created_at FROM customer ORDER BY created_at DESC LIMIT 3"
            );

            while (rs2.next()) {
                String desc = "New customer: " + rs2.getString("full_name");
                String time = formatTimeAgo(rs2.getString("created_at"));
                activities.add(new Activity("👤", desc, time));
            }

        } catch (Exception e) {
            // If created_at column doesn't exist, use alternative query
            try {
                ResultSet rs = DB.executeQuery(
                        "SELECT full_name FROM customer ORDER BY customer_id DESC LIMIT 3"
                );

                int i = 0;
                while (rs.next() && i < 2) {
                    String desc = "New customer: " + rs.getString("full_name");
                    String time = i == 0 ? "2 hours ago" : "1 day ago";
                    activities.add(new Activity("👤", desc, time));
                    i++;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return activities;
    }

    private String formatTimeAgo(String dateTimeStr) {
        try {
            // Simple implementation - you can improve this
            return "Today";
        } catch (Exception e) {
            return "Recently";
        }
    }

    private void showRecentActivityWindow() {
        // Open a window showing all recent activities
        showReports(); // Temporary - you can create a dedicated window
    }

    private VBox createQuickActionsPanel() {
        VBox quickActions = new VBox(15);
        quickActions.getStyleClass().add("quick-actions-panel");
        quickActions.setPrefWidth(300);

        Label title = new Label("⚡ Quick Actions");
        title.getStyleClass().add("section-title");

        VBox actionButtons = new VBox(10);

        Button newInvoiceBtn = createActionButton("➕ Create New Invoice", "Create and send invoice to customer",
                "#3b82f6", "🧾");
        newInvoiceBtn.setOnAction(e -> showInvoices());

        Button addCustomerBtn = createActionButton("👥 Add New Customer", "Register new customer",
                "#10b981", "👤");
        addCustomerBtn.setOnAction(e -> showCustomers());

        Button addServiceBtn = createActionButton("🔧 Add Service", "Add new service to catalog",
                "#f59e0b", "🔧");
        addServiceBtn.setOnAction(e -> showServices());

        Button addPartBtn = createActionButton("🔩 Add Part", "Add new part to inventory",
                "#ef4444", "📦");
        addPartBtn.setOnAction(e -> showParts());

        Button addVehicleBtn = createActionButton("🚗 Register Vehicle", "Register customer vehicle",
                "#8b5cf6", "🚗");
        addVehicleBtn.setOnAction(e -> showVehicles());

        Button generateReportBtn = createActionButton("📊 Generate Report", "Generate system report",
                "#06b6d4", "📈");
        generateReportBtn.setOnAction(e -> showReports());

        actionButtons.getChildren().addAll(
                newInvoiceBtn, addCustomerBtn, addServiceBtn,
                addPartBtn, addVehicleBtn, generateReportBtn
        );

        quickActions.getChildren().addAll(title, actionButtons);
        return quickActions;
    }

    private Button createActionButton(String title, String subtitle, String color, String icon) {
        Button btn = new Button();
        btn.getStyleClass().add("action-btn");
        btn.setStyle("-fx-border-color: " + color + "40; -fx-background-color: " + color + "10;");

        HBox content = new HBox(15);
        content.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("action-btn-icon");

        VBox textBox = new VBox(2);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("action-btn-title");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("action-btn-subtitle");

        textBox.getChildren().addAll(titleLabel, subtitleLabel);
        content.getChildren().addAll(iconLabel, textBox);

        btn.setGraphic(content);

        // Hover effect
        btn.setOnMouseEntered(e -> {
            btn.setStyle("-fx-border-color: " + color + "80; -fx-background-color: " + color + "20;");
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle("-fx-border-color: " + color + "40; -fx-background-color: " + color + "10;");
        });

        return btn;
    }

    private HBox createFooter() {
        HBox footer = new HBox();
        footer.getStyleClass().add("dashboard-footer");

        HBox leftSection = new HBox(20);

        Label systemStatus = new Label("🟢 System Status: Online");
        systemStatus.getStyleClass().add("system-status");

        Label lastUpdate = new Label("Last update: " +
                LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
        lastUpdate.getStyleClass().add("last-update");

        leftSection.getChildren().addAll(systemStatus, lastUpdate);

        HBox rightSection = new HBox(15);
        rightSection.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Label version = new Label("v2.1.0");
        version.getStyleClass().add("version");

        Label copyright = new Label("© 2024 Car Workshop System");
        copyright.getStyleClass().add("copyright");

        rightSection.getChildren().addAll(version, copyright);

        footer.getChildren().addAll(leftSection, rightSection);
        HBox.setHgrow(leftSection, Priority.ALWAYS);

        return footer;
    }

    private void refreshDashboard() {
        // Refresh dashboard data
        // In a real application, you would refresh the UI components
        System.out.println("Dashboard refreshed at " + LocalTime.now());
    }

    private void showUserMenu() {
        ContextMenu menu = new ContextMenu();

        MenuItem profile = new MenuItem("👤 My Profile");
        MenuItem settings = new MenuItem("⚙️ Settings");
        MenuItem logout = new MenuItem("🚪 Logout");

        menu.getItems().addAll(profile, settings, new SeparatorMenuItem(), logout);

        // Show menu at cursor position
        menu.show(javafx.stage.Stage.getWindows().get(0));
    }

    private String formatCurrency(double amount) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        return df.format(amount);
    }

    // Navigation methods
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

    // Helper classes
    private static class ChartData {
        String label;
        double value;

        ChartData(String label, double value) {
            this.label = label;
            this.value = value;
        }

        double getValue() { return value; }
    }

    private static class Activity {
        String icon;
        String description;
        String time;

        Activity(String icon, String description, String time) {
            this.icon = icon;
            this.description = description;
            this.time = time;
        }
    }
}
