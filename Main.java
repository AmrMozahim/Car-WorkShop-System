import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("🚗 نظام إدارة ورشة السيارات");

        // إنشاء تخطيط رئيسي
        BorderPane root = new BorderPane();

        // رأس الصفحة
        VBox header = createHeader();
        root.setTop(header);

        // محتوى الصفحة الرئيسية
        GridPane content = createContent();
        root.setCenter(content);

        // تذييل الصفحة
        HBox footer = createFooter();
        root.setBottom(footer);

        // إنشاء المشهد
        Scene scene = new Scene(root, 1000, 700);

        // تحميل ملف CSS
        try {
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("ملف CSS غير موجود، سيتم استخدام التنسيق الافتراضي");
        }

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createHeader() {
        VBox header = new VBox();
        header.getStyleClass().add("header");

        HBox topBar = new HBox();
        topBar.getStyleClass().add("top-bar");

        // الشعار والعنوان
        HBox logoBox = new HBox(10);
        Label logo = new Label("🚗");
        logo.getStyleClass().add("logo");

        Label title = new Label("نظام إدارة ورشة السيارات");
        title.getStyleClass().add("main-title");

        logoBox.getChildren().addAll(logo, title);

        // معلومات المستخدم
        HBox userBox = new HBox(10);
        userBox.getStyleClass().add("user-box");

        Label welcome = new Label("مرحباً، مدير النظام");
        welcome.getStyleClass().add("welcome-label");

        Label timeLabel = new Label(java.time.LocalDate.now().toString());
        timeLabel.getStyleClass().add("time-label");

        userBox.getChildren().addAll(welcome, timeLabel);

        // إضافة العناصر إلى الشريط العلوي
        topBar.getChildren().addAll(logoBox, userBox);
        HBox.setHgrow(logoBox, Priority.ALWAYS);

        // قائمة التنقل
        HBox navBar = createNavBar();

        header.getChildren().addAll(topBar, navBar);
        return header;
    }

    private HBox createNavBar() {
        HBox navBar = new HBox(10);
        navBar.getStyleClass().add("nav-bar");
        navBar.setPadding(new Insets(10));

        String[] navItems = {
                "🏠 الرئيسية", "👥 العملاء", "🧾 الفواتير",
                "🔧 الخدمات", "🔩 قطع الغيار", "🚗 السيارات",
                "👨‍🔧 الميكانيكيين", "🏭 الموردين", "📊 التقارير"
        };

        for (String item : navItems) {
            Button btn = new Button(item);
            btn.getStyleClass().add("nav-button");

            // إضافة أحداث النقر
            if (item.contains("العملاء")) {
                btn.setOnAction(e -> showCustomers());
            } else if (item.contains("الفواتير")) {
                btn.setOnAction(e -> showInvoices());
            } else if (item.contains("الخدمات")) {
                btn.setOnAction(e -> showServices());
            } else if (item.contains("قطع الغيار")) {
                btn.setOnAction(e -> showParts());
            } else if (item.contains("السيارات")) {
                btn.setOnAction(e -> showVehicles());
            } else if (item.contains("الميكانيكيين")) {
                btn.setOnAction(e -> showMechanics());
            } else if (item.contains("الموردين")) {
                btn.setOnAction(e -> showSuppliers());
            } else if (item.contains("التقارير")) {
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

        // بطاقة العمليات السريعة
        VBox quickActions = createQuickActions();
        content.add(quickActions, 0, 0);

        // بطاقة الإحصائيات
        VBox stats = createStats();
        content.add(stats, 1, 0);

        // بطاقة الفواتير الحديثة
        VBox recentInvoices = createRecentInvoices();
        content.add(recentInvoices, 0, 1, 2, 1);

        return content;
    }

    private VBox createQuickActions() {
        VBox box = new VBox(10);
        box.getStyleClass().add("card");

        Label title = new Label("⚡ عمليات سريعة");
        title.getStyleClass().add("card-title");

        String[] actions = {
                "➕ إضافة عميل جديد",
                "🧾 إنشاء فاتورة جديدة",
                "🔧 تسجيل خدمة جديدة",
                "🔩 إضافة قطعة غيار",
                "🚗 إضافة سيارة"
        };

        VBox buttonsBox = new VBox(8);
        for (String action : actions) {
            Button btn = new Button(action);
            btn.getStyleClass().add("quick-action");
            btn.setMaxWidth(Double.MAX_VALUE);
            buttonsBox.getChildren().add(btn);
        }

        box.getChildren().addAll(title, buttonsBox);
        return box;
    }

    private VBox createStats() {
        VBox box = new VBox(10);
        box.getStyleClass().add("card");

        Label title = new Label("📊 إحصائيات سريعة");
        title.getStyleClass().add("card-title");

        // إنشاء شبكة للإحصائيات
        GridPane statsGrid = new GridPane();
        statsGrid.setVgap(10);
        statsGrid.setHgap(15);

        // الإحصائيات
        String[][] stats = {
                {"👥 عدد العملاء", "150"},
                {"🧾 فواتير اليوم", "12"},
                {"🔧 خدمات قيد التنفيذ", "8"},
                {"⚠️ قطع منخفضة المخزون", "5"},
                {"💰 الإيرادات اليوم", "5,250 ر.س"},
                {"🚗 السيارات في الورشة", "3"}
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

        box.getChildren().addAll(title, statsGrid);
        return box;
    }

    private VBox createRecentInvoices() {
        VBox box = new VBox(10);
        box.getStyleClass().add("card");

        Label title = new Label("📋 آخر الفواتير");
        title.getStyleClass().add("card-title");

        // جدول الفواتير الحديثة (مثال)
        VBox invoicesBox = new VBox(5);

        String[][] recentInvoices = {
                {"INV-2024-001", "أحمد محمد", "1,500 ر.س", "مدفوعة"},
                {"INV-2024-002", "سارة علي", "2,300 ر.س", "مدفوعة"},
                {"INV-2024-003", "محمد خالد", "850 ر.س", "قيد الانتظار"},
                {"INV-2024-004", "نورة أحمد", "3,200 ر.س", "مدفوعة"},
                {"INV-2024-005", "خالد سعيد", "1,100 ر.س", "مدفوعة جزئياً"}
        };

        for (String[] invoice : recentInvoices) {
            HBox invoiceRow = new HBox(20);
            invoiceRow.getStyleClass().add("invoice-row");

            Label invNo = new Label(invoice[0]);
            invNo.getStyleClass().add("invoice-number");

            Label customer = new Label(invoice[1]);
            customer.getStyleClass().add("invoice-customer");

            Label amount = new Label(invoice[2]);
            amount.getStyleClass().add("invoice-amount");

            Label status = new Label(invoice[3]);
            status.getStyleClass().add(invoice[3].equals("مدفوعة") ? "status-paid" : "status-pending");

            invoiceRow.getChildren().addAll(invNo, customer, amount, status);
            HBox.setHgrow(customer, Priority.ALWAYS);

            invoicesBox.getChildren().add(invoiceRow);
        }

        // زر عرض الكل
        Button viewAll = new Button("عرض جميع الفواتير");
        viewAll.getStyleClass().add("view-all-button");
        viewAll.setOnAction(e -> showInvoices());

        box.getChildren().addAll(title, invoicesBox, viewAll);
        return box;
    }

    private HBox createFooter() {
        HBox footer = new HBox();
        footer.getStyleClass().add("footer");

        Label copyright = new Label("© 2024 نظام إدارة ورشة السيارات - جميع الحقوق محفوظة");
        copyright.getStyleClass().add("copyright");

        Label version = new Label("الإصدار 1.0.0");
        version.getStyleClass().add("version");

        footer.getChildren().addAll(copyright, version);
        HBox.setHgrow(copyright, Priority.ALWAYS);

        return footer;
    }

    // دوال عرض النوافذ المختلفة
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