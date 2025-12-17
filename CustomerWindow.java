import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.ResultSet;

public class CustomerWindow {

    private TableView<Customer> table = new TableView<>();
    private ObservableList<Customer> customerList = FXCollections.observableArrayList();

    private TextField txtName = new TextField();
    private TextField txtPhone = new TextField();
    private TextField txtEmail = new TextField();

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("👥 إدارة العملاء");

        // تخطيط رئيسي
        BorderPane root = new BorderPane();
        root.getStyleClass().add("window-root");

        // رأس النافذة
        VBox header = new VBox(10);
        header.getStyleClass().add("window-header");
        header.setPadding(new Insets(15));

        Label title = new Label("👥 إدارة العملاء");
        title.getStyleClass().add("window-title");

        Label subtitle = new Label("إدارة معلومات العملاء وإضافة عملاء جدد");
        subtitle.getStyleClass().add("window-subtitle");

        header.getChildren().addAll(title, subtitle);
        root.setTop(header);

        // محتوى النافذة
        GridPane content = new GridPane();
        content.getStyleClass().add("window-content");
        content.setPadding(new Insets(20));
        content.setVgap(15);
        content.setHgap(15);

        // قسم النموذج
        VBox formBox = new VBox(15);
        formBox.getStyleClass().add("form-box");

        Label formTitle = new Label("📝 نموذج إضافة عميل");
        formTitle.getStyleClass().add("form-title");

        // حقل الاسم
        VBox nameBox = new VBox(5);
        Label lblName = new Label("الاسم الكامل *");
        lblName.getStyleClass().add("field-label");
        txtName.getStyleClass().add("field-input");
        txtName.setPromptText("أدخل الاسم الكامل للعميل");
        nameBox.getChildren().addAll(lblName, txtName);

        // حقل الهاتف
        VBox phoneBox = new VBox(5);
        Label lblPhone = new Label("رقم الهاتف");
        lblPhone.getStyleClass().add("field-label");
        txtPhone.getStyleClass().add("field-input");
        txtPhone.setPromptText("أدخل رقم الهاتف");
        phoneBox.getChildren().addAll(lblPhone, txtPhone);

        // حقل البريد الإلكتروني
        VBox emailBox = new VBox(5);
        Label lblEmail = new Label("البريد الإلكتروني");
        lblEmail.getStyleClass().add("field-label");
        txtEmail.getStyleClass().add("field-input");
        txtEmail.setPromptText("أدخل البريد الإلكتروني");
        emailBox.getChildren().addAll(lblEmail, txtEmail);

        // أزرار النموذج
        HBox formButtons = new HBox(10);
        formButtons.getStyleClass().add("form-buttons");

        Button btnAdd = new Button("➕ إضافة عميل");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setOnAction(e -> addCustomer());

        Button btnClear = new Button("🗑️ مسح الحقول");
        btnClear.getStyleClass().add("btn-secondary");
        btnClear.setOnAction(e -> clearFields());

        formButtons.getChildren().addAll(btnAdd, btnClear);

        // تجميع النموذج
        formBox.getChildren().addAll(formTitle, nameBox, phoneBox, emailBox, formButtons);
        content.add(formBox, 0, 0);

        // قسم الجدول
        VBox tableBox = new VBox(10);
        tableBox.getStyleClass().add("table-box");

        HBox tableHeader = new HBox();
        tableHeader.getStyleClass().add("table-header");

        Label tableTitle = new Label("📋 قائمة العملاء");
        tableTitle.getStyleClass().add("table-title");

        Button btnRefresh = new Button("🔄 تحديث القائمة");
        btnRefresh.getStyleClass().add("btn-refresh");
        btnRefresh.setOnAction(e -> loadCustomers());

        tableHeader.getChildren().addAll(tableTitle, btnRefresh);
        HBox.setHgrow(tableTitle, Priority.ALWAYS);

        // إنشاء الجدول
        createTable();
        table.setPrefHeight(300);

        tableBox.getChildren().addAll(tableHeader, table);
        content.add(tableBox, 1, 0);

        root.setCenter(content);

        // إنشاء المشهد
        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();

        // تحميل البيانات
        loadCustomers();
    }

    private void createTable() {
        table.getColumns().clear();

        // أعمدة الجدول
        TableColumn<Customer, Integer> colId = new TableColumn<>("#");
        colId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        colId.setPrefWidth(60);

        TableColumn<Customer, String> colName = new TableColumn<>("الاسم");
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colName.setPrefWidth(200);

        TableColumn<Customer, String> colPhone = new TableColumn<>("الهاتف");
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colPhone.setPrefWidth(150);

        TableColumn<Customer, String> colEmail = new TableColumn<>("البريد الإلكتروني");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(200);

        // عمود الإجراءات
        TableColumn<Customer, Void> colActions = new TableColumn<>("الإجراءات");
        colActions.setPrefWidth(150);
        colActions.setCellFactory(param -> new TableCell<Customer, Void>() {
            private final Button btnEdit = new Button("✏️ تعديل");
            private final Button btnDelete = new Button("🗑️ حذف");

            {
                btnEdit.getStyleClass().add("btn-table-edit");
                btnDelete.getStyleClass().add("btn-table-delete");

                btnEdit.setOnAction(e -> {
                    Customer customer = getTableView().getItems().get(getIndex());
                    editCustomer(customer);
                });

                btnDelete.setOnAction(e -> {
                    Customer customer = getTableView().getItems().get(getIndex());
                    deleteCustomer(customer);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, btnEdit, btnDelete);
                    setGraphic(buttons);
                }
            }
        });

        table.getColumns().addAll(colId, colName, colPhone, colEmail, colActions);
        table.setItems(customerList);
    }

    private void loadCustomers() {
        customerList.clear();
        try {
            ResultSet rs = DB.getCustomers();
            while (rs.next()) {
                Customer customer = new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("full_name"),
                        rs.getString("phone"),
                        rs.getString("email")
                );
                customerList.add(customer);
            }
        } catch (Exception e) {
            showAlert("خطأ", "❌ حدث خطأ أثناء تحميل العملاء: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addCustomer() {
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();

        if (name.isEmpty()) {
            showAlert("تحذير", "⚠️ الرجاء إدخال اسم العميل");
            return;
        }

        String sql = String.format(
                "INSERT INTO customer (full_name, phone, email) VALUES ('%s', '%s', '%s')",
                name, phone, email
        );

        int result = DB.executeUpdate(sql);
        if (result > 0) {
            showAlert("نجاح", "✅ تم إضافة العميل بنجاح");
            clearFields();
            loadCustomers();
        } else {
            showAlert("خطأ", "❌ فشل إضافة العميل");
        }
    }

    private void editCustomer(Customer customer) {
        // يمكنك إضافة نافذة تعديل هنا
        showAlert("تعديل", "تعديل بيانات العميل: " + customer.getFullName());
    }

    private void deleteCustomer(Customer customer) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("تأكيد الحذف");
        alert.setHeaderText("هل أنت متأكد من حذف العميل؟");
        alert.setContentText("العميل: " + customer.getFullName() + "\nهذا الإجراء لا يمكن التراجع عنه!");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String sql = "DELETE FROM customer WHERE customer_id = " + customer.getCustomerId();
                int result = DB.executeUpdate(sql);
                if (result > 0) {
                    showAlert("نجاح", "✅ تم حذف العميل بنجاح");
                    loadCustomers();
                }
            }
        });
    }

    private void clearFields() {
        txtName.clear();
        txtPhone.clear();
        txtEmail.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // فئة Customer للجدول
    public static class Customer {
        private int customerId;
        private String fullName;
        private String phone;
        private String email;

        public Customer(int customerId, String fullName, String phone, String email) {
            this.customerId = customerId;
            this.fullName = fullName;
            this.phone = phone;
            this.email = email;
        }

        public int getCustomerId() { return customerId; }
        public String getFullName() { return fullName; }
        public String getPhone() { return phone; }
        public String getEmail() { return email; }
    }
}