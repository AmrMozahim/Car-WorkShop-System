import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.ResultSet;

public class MechanicWindow {

    private TableView<Mechanic> table = new TableView<>();
    private ObservableList<Mechanic> mechanicList = FXCollections.observableArrayList();

    private TextField txtName = new TextField();
    private TextField txtPhone = new TextField();

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("👨‍🔧 إدارة الميكانيكيين");

        BorderPane root = new BorderPane();
        root.getStyleClass().add("window-root");

        VBox header = new VBox(10);
        header.getStyleClass().add("window-header");
        header.setPadding(new Insets(15));

        Label title = new Label("👨‍🔧 إدارة الميكانيكيين");
        title.getStyleClass().add("window-title");

        Label subtitle = new Label("إدارة فريق الميكانيكيين");
        subtitle.getStyleClass().add("window-subtitle");

        header.getChildren().addAll(title, subtitle);
        root.setTop(header);

        GridPane content = new GridPane();
        content.getStyleClass().add("window-content");
        content.setPadding(new Insets(20));
        content.setVgap(15);
        content.setHgap(15);

        // نموذج الإضافة
        VBox formBox = new VBox(15);
        formBox.getStyleClass().add("form-box");

        Label formTitle = new Label("➕ إضافة ميكانيكي جديد");
        formTitle.getStyleClass().add("form-title");

        VBox nameBox = new VBox(5);
        Label lblName = new Label("الاسم *");
        lblName.getStyleClass().add("field-label");
        txtName.getStyleClass().add("field-input");
        txtName.setPromptText("أدخل اسم الميكانيكي");
        nameBox.getChildren().addAll(lblName, txtName);

        VBox phoneBox = new VBox(5);
        Label lblPhone = new Label("رقم الهاتف");
        lblPhone.getStyleClass().add("field-label");
        txtPhone.getStyleClass().add("field-input");
        txtPhone.setPromptText("أدخل رقم الهاتف");
        phoneBox.getChildren().addAll(lblPhone, txtPhone);

        HBox formButtons = new HBox(10);
        formButtons.getStyleClass().add("form-buttons");

        Button btnAdd = new Button("➕ إضافة ميكانيكي");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setOnAction(e -> addMechanic());

        Button btnClear = new Button("🗑️ مسح الحقول");
        btnClear.getStyleClass().add("btn-secondary");
        btnClear.setOnAction(e -> clearFields());

        formButtons.getChildren().addAll(btnAdd, btnClear);

        formBox.getChildren().addAll(formTitle, nameBox, phoneBox, formButtons);
        content.add(formBox, 0, 0);

        // قائمة الميكانيكيين
        VBox tableBox = new VBox(10);
        tableBox.getStyleClass().add("table-box");

        HBox tableHeader = new HBox();
        tableHeader.getStyleClass().add("table-header");

        Label tableTitle = new Label("📋 قائمة الميكانيكيين");
        tableTitle.getStyleClass().add("table-title");

        Button btnRefresh = new Button("🔄 تحديث");
        btnRefresh.getStyleClass().add("btn-refresh");
        btnRefresh.setOnAction(e -> loadMechanics());

        tableHeader.getChildren().addAll(tableTitle, btnRefresh);
        HBox.setHgrow(tableTitle, Priority.ALWAYS);

        createTable();
        table.setPrefHeight(400);

        tableBox.getChildren().addAll(tableHeader, table);
        content.add(tableBox, 1, 0);

        root.setCenter(content);

        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();

        loadMechanics();
    }

    private void createTable() {
        table.getColumns().clear();

        TableColumn<Mechanic, Integer> colId = new TableColumn<>("#");
        colId.setCellValueFactory(new PropertyValueFactory<>("mechanicId"));
        colId.setPrefWidth(60);

        TableColumn<Mechanic, String> colName = new TableColumn<>("الاسم");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colName.setPrefWidth(200);

        TableColumn<Mechanic, String> colPhone = new TableColumn<>("الهاتف");
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colPhone.setPrefWidth(150);

        table.getColumns().addAll(colId, colName, colPhone);
        table.setItems(mechanicList);
    }

    private void loadMechanics() {
        mechanicList.clear();
        try {
            ResultSet rs = DB.getMechanics();
            while (rs.next()) {
                Mechanic mechanic = new Mechanic(
                        rs.getInt("mechanic_id"),
                        rs.getString("name"),
                        rs.getString("phone")
                );
                mechanicList.add(mechanic);
            }
        } catch (Exception e) {
            showAlert("خطأ", "❌ حدث خطأ أثناء تحميل الميكانيكيين");
            e.printStackTrace();
        }
    }

    private void addMechanic() {
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();

        if (name.isEmpty()) {
            showAlert("تحذير", "⚠️ الرجاء إدخال اسم الميكانيكي");
            return;
        }

        String sql = String.format(
                "INSERT INTO mechanic (name, phone) VALUES ('%s', '%s')",
                name, phone
        );

        int result = DB.executeUpdate(sql);
        if (result > 0) {
            showAlert("نجاح", "✅ تم إضافة الميكانيكي بنجاح");
            clearFields();
            loadMechanics();
        } else {
            showAlert("خطأ", "❌ فشل إضافة الميكانيكي");
        }
    }

    private void clearFields() {
        txtName.clear();
        txtPhone.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Mechanic {
        private int mechanicId;
        private String name;
        private String phone;

        public Mechanic(int mechanicId, String name, String phone) {
            this.mechanicId = mechanicId;
            this.name = name;
            this.phone = phone;
        }

        public int getMechanicId() { return mechanicId; }
        public String getName() { return name; }
        public String getPhone() { return phone; }
    }
}