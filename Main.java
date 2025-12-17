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

public class Main extends Application {

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
        Label logoText = new Label("CAR WORKSHOP PRO");
        logoText.getStyleClass().add("logo-text");
        Label logoSubtext = new Label("Professional Vehicle Management");
        logoSubtext.getStyleClass().add("logo-subtext");
        logoContainer.getChildren().addAll(logoText, logoSubtext);

        // Search Box
        HBox searchBox = new HBox();
        searchBox.getStyleClass().add("search-box");
        TextField searchField = new TextField();
        searchField.setPromptText("Search customers, vehicles, invoices...");
        searchField.getStyleClass().add("search-field");
        searchBox.getChildren().add(searchField);

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

        topNav.getChildren().addAll(logoContainer, searchBox, userBox);
        HBox.setHgrow(searchBox, Priority.ALWAYS);

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

        Button dashboardBtn = new Button("📊 Dashboard");
        dashboardBtn.getStyleClass().add("nav-btn active");
        dashboardBtn.setMaxWidth(Double.MAX_VALUE);

        dashSection.getChildren().addAll(dashTitle, dashboardBtn);

        // Management Section
        VBox mgmtSection = new VBox(5);
        mgmtSection.getStyleClass().add("nav-section");
        Label mgmtTitle = new Label("MANAGEMENT");
        mgmtTitle.getStyleClass().add("nav-title");

        Button customersBtn = new Button("👥 Customers");
        customersBtn.getStyleClass().add("nav-btn");
        customersBtn.setMaxWidth(Double.MAX_VALUE);
        customersBtn.setOnAction(e -> showCustomers());

        Button vehiclesBtn = new Button("🚗 Vehicles");
        vehiclesBtn.getStyleClass().add("nav-btn");
        vehiclesBtn.setMaxWidth(Double.MAX_VALUE);
        vehiclesBtn.setOnAction(e -> showVehicles());

        Button mechanicsBtn = new Button("🔧 Mechanics");
        mechanicsBtn.getStyleClass().add("nav-btn");
        mechanicsBtn.setMaxWidth(Double.MAX_VALUE);
        mechanicsBtn.setOnAction(e -> showMechanics());

        mgmtSection.getChildren().addAll(mgmtTitle, customersBtn, vehiclesBtn, mechanicsBtn);

        // Operations Section
        VBox opsSection = new VBox(5);
        opsSection.getStyleClass().add("nav-section");
        Label opsTitle = new Label("OPERATIONS");
        opsTitle.getStyleClass().add("nav-title");

        Button servicesBtn = new Button("⚙️ Services");
        servicesBtn.getStyleClass().add("nav-btn");
        servicesBtn.setMaxWidth(Double.MAX_VALUE);
        servicesBtn.setOnAction(e -> showServices());

        Button partsBtn = new Button("📦 Parts");
        partsBtn.getStyleClass().add("nav-btn");
        partsBtn.setMaxWidth(Double.MAX_VALUE);
        partsBtn.setOnAction(e -> showParts());

        Button invoicesBtn = new Button("🧾 Invoices");
        invoicesBtn.getStyleClass().add("nav-btn");
        invoicesBtn.setMaxWidth(Double.MAX_VALUE);
        invoicesBtn.setOnAction(e -> showInvoices());

        opsSection.getChildren().addAll(opsTitle, servicesBtn, partsBtn, invoicesBtn);

        sidebar.getChildren().addAll(dashSection, mgmtSection, opsSection);
        root.setLeft(sidebar);

        // === MAIN CONTENT ===
        ScrollPane mainScroll = new ScrollPane();
        mainScroll.getStyleClass().add("dashboard-scroll");
        mainScroll.setFitToWidth(true);

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

        VBox stat1 = createStatCard("💰", "Today's Revenue", "$2,450.50", "+12.5%", "#2E86AB");
        VBox stat2 = createStatCard("👥", "Total Customers", "127", "+8 new", "#A23B72");
        VBox stat3 = createStatCard("🚗", "Vehicles in Service", "18", "4 completed", "#10b981");
        VBox stat4 = createStatCard("📦", "Low Stock Items", "7", "Need reorder", "#f59e0b");

        statsGrid.add(stat1, 0, 0);
        statsGrid.add(stat2, 1, 0);
        statsGrid.add(stat3, 0, 1);
        statsGrid.add(stat4, 1, 1);
        GridPane.setHgrow(stat1, Priority.ALWAYS);
        GridPane.setHgrow(stat2, Priority.ALWAYS);
        GridPane.setHgrow(stat3, Priority.ALWAYS);
        GridPane.setHgrow(stat4, Priority.ALWAYS);

        // Recent Activity
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
        sectionHeader.getChildren().addAll(recentTitle, spacer2, viewAllBtn);

        VBox activityList = new VBox(10);
        activityList.getStyleClass().add("activity-list");

        // Activity Items
        HBox activity1 = createActivityItem("🚗", "New vehicle registered", "10:30 AM");
        HBox activity2 = createActivityItem("🧾", "Invoice #1025 created", "9:15 AM");
        HBox activity3 = createActivityItem("🔧", "Oil change completed", "Yesterday");
        HBox activity4 = createActivityItem("👤", "New customer added", "Yesterday");

        activityList.getChildren().addAll(activity1, activity2, activity3, activity4);
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
        action1.setOnAction(e -> showInvoices());
        Button action2 = createActionButton("👥 Add Customer", "👤");
        action2.setOnAction(e -> showCustomers());
        Button action3 = createActionButton("🔧 Add Service", "⚙️");
        action3.setOnAction(e -> showServices());
        Button action4 = createActionButton("📦 Manage Parts", "📦");
        action4.setOnAction(e -> showParts());
        Button action5 = createActionButton("🚗 Register Vehicle", "🚗");
        action5.setOnAction(e -> showVehicles());
        Button action6 = createActionButton("📊 View Reports", "📈");
        action6.setOnAction(e -> showReports());

        actionsGrid.add(action1, 0, 0);
        actionsGrid.add(action2, 1, 0);
        actionsGrid.add(action3, 2, 0);
        actionsGrid.add(action4, 0, 1);
        actionsGrid.add(action5, 1, 1);
        actionsGrid.add(action6, 2, 1);

        actionsSection.getChildren().addAll(actionsHeader, actionsGrid);

        dashboardContent.getChildren().addAll(welcomeCard, statsGrid, recentSection, actionsSection);
        mainScroll.setContent(dashboardContent);
        root.setCenter(mainScroll);

        Scene scene = new Scene(root, 1200, 700);
        scene.getStylesheets().add("style.css");

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createStatCard(String icon, String title, String value, String change, String color) {
        VBox card = new VBox(15);
        card.getStyleClass().add("stat-card");
        card.setStyle("-fx-border-color: " + color + "40;");
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

    // Navigation methods
    private void showCustomers() {
        CustomerWindow window = new CustomerWindow();
        window.show();
    }

    private void showVehicles() {
        VehicleWindow window = new VehicleWindow();
        window.show();
    }

    private void showMechanics() {
        MechanicWindow window = new MechanicWindow();
        window.show();
    }

    private void showServices() {
        ServiceWindow window = new ServiceWindow();
        window.show();
    }

    private void showParts() {
        PartsWindow window = new PartsWindow();
        window.show();
    }

    private void showInvoices() {
        InvoiceWindow window = new InvoiceWindow();
        window.show();
    }

    private void showReports() {
        ReportWindow window = new ReportWindow();
        window.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}