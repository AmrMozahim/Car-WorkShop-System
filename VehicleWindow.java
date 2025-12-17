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

    private ComboBox<String> cmbCustomer = new ComboBox<>();
    private TextField txtPlate = new TextField();
    private TextField txtModel = new TextField();
    private TextField txtYear = new TextField();
    private TextField txtVIN = new TextField();
    private ComboBox<String> cmbType = new ComboBox<>();

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Vehicle Management");

        BorderPane root = new BorderPane();
        root.getStyleClass().add("window-root");

        // Header
        VBox header = new VBox(8);
        header.getStyleClass().add("window-header");
        header.setPadding(new Insets(20));

        Label title = new Label("Vehicle Management");
        title.getStyleClass().add("window-title");

        Label subtitle = new Label("Manage customer vehicles and service history");
        subtitle.getStyleClass().add("window-subtitle");

        header.getChildren().addAll(title, subtitle);
        root.setTop(header);

        // Content
        GridPane content = new GridPane();
        content.getStyleClass().add("window-content");
        content.setPadding(new Insets(25));
        content.setVgap(20);
        content.setHgap(20);

        // Left - Form
        VBox formBox = new VBox(20);
        formBox.getStyleClass().add("form-box");
        formBox.setPrefWidth(350);

        Label formTitle = new Label("Register New Vehicle");
        formTitle.getStyleClass().add("form-title");

        // Customer Field
        VBox customerBox = new VBox(8);
        customerBox.getStyleClass().add("form-group");
        Label lblCustomer = new Label("Customer *");
        lblCustomer.getStyleClass().add("field-label");
        cmbCustomer.getStyleClass().add("field-combo");
        cmbCustomer.setPromptText("Select customer");
        loadCustomers();
        customerBox.getChildren().addAll(lblCustomer, cmbCustomer);

        // Plate Field
        VBox plateBox = new VBox(8);
        plateBox.getStyleClass().add("form-group");
        Label lblPlate = new Label("Plate Number *");
        lblPlate.getStyleClass().add("field-label");
        txtPlate.getStyleClass().add("field-input");
        txtPlate.setPromptText("ABC-123");
        plateBox.getChildren().addAll(lblPlate, txtPlate);

        // Model Field
        VBox modelBox = new VBox(8);
        modelBox.getStyleClass().add("form-group");
        Label lblModel = new Label("Model");
        lblModel.getStyleClass().add("field-label");
        txtModel.getStyleClass().add("field-input");
        txtModel.setPromptText("Toyota Camry");
        modelBox.getChildren().addAll(lblModel, txtModel);

        // Year Field
        VBox yearBox = new VBox(8);
        yearBox.getStyleClass().add("form-group");
        Label lblYear = new Label("Year");
        lblYear.getStyleClass().add("field-label");
        txtYear.getStyleClass().add("field-input");
        txtYear.setPromptText("2020");
        yearBox.getChildren().addAll(lblYear, txtYear);

        // VIN Field
        VBox vinBox = new VBox(8);
        vinBox.getStyleClass().add("form-group");
        Label lblVIN = new Label("VIN Number");
        lblVIN.getStyleClass().add("field-label");
        txtVIN.getStyleClass().add("field-input");
        txtVIN.setPromptText("Vehicle Identification Number");
        vinBox.getChildren().addAll(lblVIN, txtVIN);

        // Type Field
        VBox typeBox = new VBox(8);
        typeBox.getStyleClass().add("form-group");
        Label lblType = new Label("Vehicle Type");
        lblType.getStyleClass().add("field-label");
        cmbType.getStyleClass().add("field-combo");
        cmbType.getItems().addAll("Sedan", "SUV", "Truck", "Van", "Hatchback", "Coupe", "Convertible");
        cmbType.setPromptText("Select type");
        typeBox.getChildren().addAll(lblType, cmbType);

        // Buttons
        HBox formButtons = new HBox(15);
        formButtons.getStyleClass().add("form-buttons");

        Button btnAdd = new Button("Register Vehicle");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setOnAction(e -> addVehicle());

        Button btnClear = new Button("Clear");
        btnClear.getStyleClass().add("btn-secondary");
        btnClear.setOnAction(e -> clearFields());

        formButtons.getChildren().addAll(btnAdd, btnClear);

        formBox.getChildren().addAll(formTitle, customerBox, plateBox, modelBox,
                yearBox, vinBox, typeBox, formButtons);
        content.add(formBox, 0, 0);

        // Right - Table
        VBox tableBox = new VBox(15);
        tableBox.getStyleClass().add("table-box");

        HBox tableHeader = new HBox();
        tableHeader.getStyleClass().add("table-header-box");

        Label tableTitle = new Label("Vehicle List");
        tableTitle.getStyleClass().add("table-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnRefresh = new Button("Refresh");
        btnRefresh.getStyleClass().add("btn-refresh");
        btnRefresh.setOnAction(e -> loadVehicles());

        tableHeader.getChildren().addAll(tableTitle, spacer, btnRefresh);

        createTable();
        table.setPrefHeight(400);

        tableBox.getChildren().addAll(tableHeader, table);
        content.add(tableBox, 1, 0);

        root.setCenter(content);

        Scene scene = new Scene(root, 1100, 650);
        scene.getStylesheets().add("style.css");
        stage.setScene(scene);
        stage.show();

        loadVehicles();
    }

    private void loadCustomers() {
        try {
            ResultSet rs = DB.getCustomers();
            while (rs.next()) {
                cmbCustomer.getItems().add(rs.getString("full_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createTable() {
        table.getColumns().clear();

        TableColumn<Vehicle, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("vehicleId"));
        colId.setPrefWidth(80);

        TableColumn<Vehicle, String> colCustomer = new TableColumn<>("Customer");
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colCustomer.setPrefWidth(150);

        TableColumn<Vehicle, String> colPlate = new TableColumn<>("Plate");
        colPlate.setCellValueFactory(new PropertyValueFactory<>("plateNumber"));
        colPlate.setPrefWidth(120);

        TableColumn<Vehicle, String> colModel = new TableColumn<>("Model");
        colModel.setCellValueFactory(new PropertyValueFactory<>("model"));
        colModel.setPrefWidth(150);

        TableColumn<Vehicle, Integer> colYear = new TableColumn<>("Year");
        colYear.setCellValueFactory(new PropertyValueFactory<>("year"));
        colYear.setPrefWidth(80);

        TableColumn<Vehicle, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colType.setPrefWidth(100);

        TableColumn<Vehicle, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setPrefWidth(100);

        TableColumn<Vehicle, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(150);
        colActions.setCellFactory(param -> new TableCell<Vehicle, Void>() {
            private final Button btnView = new Button("View");
            private final Button btnDelete = new Button("Delete");

            {
                btnView.getStyleClass().add("btn-table-edit");
                btnDelete.getStyleClass().add("btn-table-delete");

                HBox buttons = new HBox(8, btnView, btnDelete);
                buttons.getStyleClass().add("table-actions");

                btnView.setOnAction(e -> {
                    Vehicle vehicle = getTableView().getItems().get(getIndex());
                    viewVehicle(vehicle);
                });

                btnDelete.setOnAction(e -> {
                    Vehicle vehicle = getTableView().getItems().get(getIndex());
                    deleteVehicle(vehicle);
                });

                setGraphic(buttons);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                }
            }
        });

        table.getColumns().addAll(colId, colCustomer, colPlate, colModel,
                colYear, colType, colStatus, colActions);
        table.setItems(vehicleList);
    }

    private void loadVehicles() {
        vehicleList.clear();
        try {
            ResultSet rs = DB.getVehicles();
            while (rs.next()) {
                Vehicle vehicle = new Vehicle(
                        rs.getInt("vehicle_id"),
                        rs.getString("customer_name"),
                        rs.getString("plate_number"),
                        rs.getString("model"),
                        rs.getInt("year"),
                        rs.getString("type"),
                        rs.getString("status")
                );
                vehicleList.add(vehicle);
            }
        } catch (Exception e) {
            showAlert("Error", "Error loading vehicles: " + e.getMessage());
        }
    }

    private void addVehicle() {
        String customer = cmbCustomer.getValue();
        String plate = txtPlate.getText().trim();
        String model = txtModel.getText().trim();
        String year = txtYear.getText().trim();
        String vin = txtVIN.getText().trim();
        String type = cmbType.getValue();

        if (customer == null || customer.isEmpty()) {
            showAlert("Warning", "Please select a customer");
            return;
        }

        if (plate.isEmpty()) {
            showAlert("Warning", "Please enter plate number");
            return;
        }

        if (!year.isEmpty()) {
            try {
                Integer.parseInt(year);
            } catch (NumberFormatException e) {
                showAlert("Warning", "Year must be a number");
                return;
            }
        }

        String sql = String.format(
                "INSERT INTO vehicle (customer_id, plate_number, model, year, vin, type, status) " +
                        "VALUES ((SELECT customer_id FROM customer WHERE full_name = '%s'), '%s', '%s', %s, '%s', '%s', 'active')",
                customer, plate, model, year.isEmpty() ? "NULL" : year, vin, type == null ? "" : type
        );

        int result = DB.executeUpdate(sql);
        if (result > 0) {
            showAlert("Success", "Vehicle registered successfully");
            clearFields();
            loadVehicles();
        } else {
            showAlert("Error", "Failed to register vehicle");
        }
    }

    private void viewVehicle(Vehicle vehicle) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Vehicle Details");
        alert.setHeaderText(vehicle.getModel() + " - " + vehicle.getPlateNumber());
        alert.setContentText(
                "Customer: " + vehicle.getCustomerName() + "\n" +
                        "Model: " + vehicle.getModel() + "\n" +
                        "Year: " + vehicle.getYear() + "\n" +
                        "Type: " + vehicle.getType() + "\n" +
                        "Status: " + vehicle.getStatus()
        );
        alert.showAndWait();
    }

    private void deleteVehicle(Vehicle vehicle) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Vehicle");
        alert.setContentText("Are you sure you want to delete vehicle " + vehicle.getPlateNumber() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String sql = "DELETE FROM vehicle WHERE vehicle_id = " + vehicle.getVehicleId();
                int result = DB.executeUpdate(sql);
                if (result > 0) {
                    showAlert("Success", "Vehicle deleted successfully");
                    loadVehicles();
                }
            }
        });
    }

    private void clearFields() {
        cmbCustomer.setValue(null);
        txtPlate.clear();
        txtModel.clear();
        txtYear.clear();
        txtVIN.clear();
        cmbType.setValue(null);
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
        private String type;
        private String status;

        public Vehicle(int vehicleId, String customerName, String plateNumber,
                       String model, int year, String type, String status) {
            this.vehicleId = vehicleId;
            this.customerName = customerName;
            this.plateNumber = plateNumber;
            this.model = model;
            this.year = year;
            this.type = type;
            this.status = status;
        }

        public int getVehicleId() { return vehicleId; }
        public String getCustomerName() { return customerName; }
        public String getPlateNumber() { return plateNumber; }
        public String getModel() { return model; }
        public int getYear() { return year; }
        public String getType() { return type; }
        public String getStatus() { return status; }
    }
}