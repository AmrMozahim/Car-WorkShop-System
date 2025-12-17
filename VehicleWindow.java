import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.ResultSet;

public class VehicleWindow {

    private TableView<Vehicle> table = new TableView<>();
    private ObservableList<Vehicle> vehicleList = FXCollections.observableArrayList();

    private ComboBox<String> cmbCustomers = new ComboBox<>();
    private TextField txtPlate = new TextField();
    private TextField txtModel = new TextField();
    private TextField txtYear = new TextField();

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("🚗 إدارة السيارات");

        BorderPane root = new BorderPane();
        root.getStyleClass().add("window-root");

        VBox header = new VBox(10);
        header.getStyleClass().add("window-header");
        header.setPadding(new Insets(15));

        Label title = new Label("🚗 إدارة السيارات");
        title.getStyleClass().add("window-title");

        Label subtitle = new Label("إدارة سيارات العملاء وتتبعها");
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

        Label formTitle = new Label("➕ إضافة سيارة جديدة");
        formTitle.getStyleClass().add("form-title");

        VBox customerBox = new VBox(5);
        Label lblCustomer = new Label("العميل *");
        lblCustomer.getStyleClass().add("field-label");
        cmbCustomers.getStyleClass().add("field-combo");
        cmbCustomers.setPromptText("اختر العميل");
        loadCustomers();
        customerBox.getChildren().addAll(lblCustomer, cmbCustomers);

        VBox plateBox = new VBox(5);
        Label lblPlate = new Label("رقم اللوحة *");
        lblPlate.getStyleClass().add("field-label");
        txtPlate.getStyleClass().add("field-input");
        txtPlate.setPromptText("أدخل رقم اللوحة");
        plateBox.getChildren().addAll(lblPlate, txtPlate);

        VBox modelBox = new VBox(5);
        Label lblModel = new Label("الموديل");
        lblModel.getStyleClass().add("field-label");
        txtModel.getStyleClass().add("field-input");
        txtModel.setPromptText("أدخل الموديل");
        modelBox.getChildren().addAll(lblModel, txtModel);

        VBox yearBox = new VBox(5);
        Label lblYear = new Label("سنة الصنع");
        lblYear.getStyleClass().add("field-label");
        txtYear.getStyleClass().add("field-input");
        txtYear.setPromptText("أدخل سنة الصنع");
        yearBox.getChildren().addAll(lblYear, txtYear);

        HBox formButtons = new HBox(10);
        formButtons.getStyleClass().add("form-buttons");

        Button btnAdd = new Button("➕ إضافة سيارة");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setOnAction(e -> addVehicle());

        Button btnClear = new Button("🗑️ مسح الحقول");
        btnClear.getStyleClass().add("btn-secondary");
        btnClear.setOnAction(e -> clearFields());

        formButtons.getChildren().addAll(btnAdd, btnClear);

        formBox.getChildren().addAll(formTitle, customerBox, plateBox, modelBox, yearBox, formButtons);
        content.add(formBox, 0, 0);

        // قائمة السيارات
        VBox tableBox = new VBox(10);
        tableBox.getStyleClass().add("table-box");

        HBox tableHeader = new HBox();
        tableHeader.getStyleClass().add("table-header");

        Label tableTitle = new Label("📋 قائمة السيارات");
        tableTitle.getStyleClass().add("table-title");

        Button btnRefresh = new Button("🔄 تحديث");
        btnRefresh.getStyleClass().add("btn-refresh");
        btnRefresh.setOnAction(e -> loadVehicles());

        tableHeader.getChildren().addAll(tableTitle, btnRefresh);
        HBox.setHgrow(tableTitle, Priority.ALWAYS);

        createTable();
        table.setPrefHeight(400);

        tableBox.getChildren().addAll(tableHeader, table);
        content.add(tableBox, 1, 0);

        root.setCenter(content);

        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();

        loadVehicles();
    }

    private void loadCustomers() {
        try {
            ResultSet rs = DB.getCustomers();
            while (rs.next()) {
                cmbCustomers.getItems().add(rs.getString("full_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createTable() {
        table.getColumns().clear();

        TableColumn<Vehicle, Integer> colId = new TableColumn<>("#");
        colId.setCellValueFactory(new PropertyValueFactory<>("vehicleId"));
        colId.setPrefWidth(60);

        TableColumn<Vehicle, String> colCustomer = new TableColumn<>("العميل");
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colCustomer.setPrefWidth(150);

        TableColumn<Vehicle, String> colPlate = new TableColumn<>("رقم اللوحة");
        colPlate.setCellValueFactory(new PropertyValueFactory<>("plateNumber"));
        colPlate.setPrefWidth(100);

        TableColumn<Vehicle, String> colModel = new TableColumn<>("الموديل");
        colModel.setCellValueFactory(new PropertyValueFactory<>("model"));
        colModel.setPrefWidth(150);

        TableColumn<Vehicle, Integer> colYear = new TableColumn<>("سنة الصنع");
        colYear.setCellValueFactory(new PropertyValueFactory<>("year"));
        colYear.setPrefWidth(80);

        TableColumn<Vehicle, Void> colActions = new TableColumn<>("الإجراءات");
        colActions.setPrefWidth(120);
        colActions.setCellFactory(param -> new TableCell<Vehicle, Void>() {
            private final Button btnDelete = new Button("🗑️ حذف");

            {
                btnDelete.getStyleClass().add("btn-table-delete");
                btnDelete.setOnAction(e -> {
                    Vehicle vehicle = getTableView().getItems().get(getIndex());
                    deleteVehicle(vehicle);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnDelete);
                }
            }
        });

        table.getColumns().addAll(colId, colCustomer, colPlate, colModel, colYear, colActions);
        table.setItems(vehicleList);
    }

    private void loadVehicles() {
        vehicleList.clear();
        try {
            ResultSet rs = DB.getVehicles();
            while (rs.next()) {
                Vehicle vehicle = new Vehicle(
                        rs.getInt("vehicle_id"),
                        rs.getString("full_name"),
                        rs.getString("plate_number"),
                        rs.getString("model"),
                        rs.getInt("manufacture_year")
                );
                vehicleList.add(vehicle);
            }
        } catch (Exception e) {
            showAlert("خطأ", "❌ حدث خطأ أثناء تحميل السيارات");
            e.printStackTrace();
        }
    }

    private void addVehicle() {
        String customer = cmbCustomers.getValue();
        String plate = txtPlate.getText().trim();
        String model = txtModel.getText().trim();
        String year = txtYear.getText().trim();

        if (customer == null || customer.isEmpty()) {
            showAlert("تحذير", "⚠️ الرجاء اختيار عميل");
            return;
        }

        if (plate.isEmpty()) {
            showAlert("تحذير", "⚠️ الرجاء إدخال رقم اللوحة");
            return;
        }

        if (!year.isEmpty()) {
            try {
                Integer.parseInt(year);
            } catch (NumberFormatException e) {
                showAlert("تحذير", "⚠️ سنة الصنع يجب أن تكون رقماً");
                return;
            }
        }

        String sql = String.format(
                "INSERT INTO vehicle (customer_id, plate_number, model, manufacture_year) " +
                        "VALUES ((SELECT customer_id FROM customer WHERE full_name = '%s'), '%s', '%s', %s)",
                customer, plate, model, year.isEmpty() ? "NULL" : year
        );

        int result = DB.executeUpdate(sql);
        if (result > 0) {
            showAlert("نجاح", "✅ تم إضافة السيارة بنجاح");
            clearFields();
            loadVehicles();
        } else {
            showAlert("خطأ", "❌ فشل إضافة السيارة");
        }
    }

    private void deleteVehicle(Vehicle vehicle) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("تأكيد الحذف");
        alert.setHeaderText("هل أنت متأكد من حذف السيارة؟");
        alert.setContentText("رقم اللوحة: " + vehicle.getPlateNumber());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String sql = "DELETE FROM vehicle WHERE vehicle_id = " + vehicle.getVehicleId();
                int result = DB.executeUpdate(sql);
                if (result > 0) {
                    showAlert("نجاح", "✅ تم حذف السيارة بنجاح");
                    loadVehicles();
                }
            }
        });
    }

    private void clearFields() {
        cmbCustomers.setValue(null);
        txtPlate.clear();
        txtModel.clear();
        txtYear.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Vehicle {
        private int vehicleId;
        private String customerName;
        private String plateNumber;
        private String model;
        private int year;

        public Vehicle(int vehicleId, String customerName, String plateNumber, String model, int year) {
            this.vehicleId = vehicleId;
            this.customerName = customerName;
            this.plateNumber = plateNumber;
            this.model = model;
            this.year = year;
        }

        public int getVehicleId() { return vehicleId; }
        public String getCustomerName() { return customerName; }
        public String getPlateNumber() { return plateNumber; }
        public String getModel() { return model; }
        public int getYear() { return year; }
    }
}