import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.sql.ResultSet;

public class VehicleTab extends BorderPane {

    private TableView<Vehicle> table = new TableView<>();
    private ObservableList<Vehicle> vehicleList = FXCollections.observableArrayList();

    private ComboBox<String> cmbCustomer = new ComboBox<>();
    private TextField txtPlate = new TextField();
    private TextField txtModel = new TextField();
    private TextField txtYear = new TextField();

    public VehicleTab() {
        initialize();
    }

    private void initialize() {
        getStyleClass().add("window-root");

        VBox header = new VBox(8);
        header.getStyleClass().add("window-header");
        header.setPadding(new Insets(20));

        Label title = new Label("Vehicle Management");
        title.getStyleClass().add("window-title");

        Label subtitle = new Label("Manage customer vehicles and service history");
        subtitle.getStyleClass().add("window-subtitle");

        header.getChildren().addAll(title, subtitle);
        setTop(header);

        GridPane content = new GridPane();
        content.getStyleClass().add("window-content");
        content.setPadding(new Insets(25));
        content.setVgap(20);
        content.setHgap(20);

        VBox formBox = new VBox(20);
        formBox.getStyleClass().add("form-box");
        formBox.setPrefWidth(350);

        Label formTitle = new Label("Register New Vehicle");
        formTitle.getStyleClass().add("form-title");

        VBox customerBox = new VBox(8);
        customerBox.getStyleClass().add("form-group");
        Label lblCustomer = new Label("Customer *");
        lblCustomer.getStyleClass().add("field-label");

        HBox customerRow = new HBox(5);
        customerRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        cmbCustomer.getStyleClass().add("field-combo");
        cmbCustomer.setPromptText("Select customer");
        cmbCustomer.setPrefWidth(250);
        cmbCustomer.setItems(CustomerManager.getInstance().getCustomers());

        Button btnRefreshCustomers = new Button("↻");
        btnRefreshCustomers.getStyleClass().add("btn-secondary");
        btnRefreshCustomers.setTooltip(new Tooltip("Refresh customer list"));
        btnRefreshCustomers.setOnAction(e -> {
            cmbCustomer.setItems(CustomerManager.getInstance().getCustomers());
        });

        customerRow.getChildren().addAll(cmbCustomer, btnRefreshCustomers);
        customerBox.getChildren().addAll(lblCustomer, customerRow);

        VBox plateBox = new VBox(8);
        plateBox.getStyleClass().add("form-group");
        Label lblPlate = new Label("Plate Number *");
        lblPlate.getStyleClass().add("field-label");
        txtPlate.getStyleClass().add("field-input");
        txtPlate.setPromptText("ABC-123");
        plateBox.getChildren().addAll(lblPlate, txtPlate);

        VBox modelBox = new VBox(8);
        modelBox.getStyleClass().add("form-group");
        Label lblModel = new Label("Model");
        lblModel.getStyleClass().add("field-label");
        txtModel.getStyleClass().add("field-input");
        txtModel.setPromptText("Toyota Camry");
        modelBox.getChildren().addAll(lblModel, txtModel);

        VBox yearBox = new VBox(8);
        yearBox.getStyleClass().add("form-group");
        Label lblYear = new Label("Year");
        lblYear.getStyleClass().add("field-label");
        txtYear.getStyleClass().add("field-input");
        txtYear.setPromptText("2020");
        yearBox.getChildren().addAll(lblYear, txtYear);

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
                yearBox, formButtons);
        content.add(formBox, 0, 0);

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

        setCenter(content);
        loadVehicles();
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

        TableColumn<Vehicle, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(120);
        colActions.setCellFactory(param -> new TableCell<Vehicle, Void>() {
            private final Button btnView = new Button("View");
            private final Button btnDelete = new Button("Delete");
            private final HBox buttons = new HBox(8, btnView, btnDelete);

            {
                btnView.getStyleClass().add("btn-table-edit");
                btnDelete.getStyleClass().add("btn-table-delete");
                buttons.getStyleClass().add("table-actions");

                btnView.setOnAction(e -> {
                    Vehicle vehicle = getTableView().getItems().get(getIndex());
                    viewVehicle(vehicle);
                });

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
                    setGraphic(buttons);
                }
            }
        });

        table.getColumns().addAll(colId, colCustomer, colPlate, colModel,
                colYear, colActions);
        table.setItems(vehicleList);
        table.setFixedCellSize(45);
    }

    private void loadVehicles() {
        vehicleList.clear();
        try {
            ResultSet rs = DB.executeQuery(
                    "SELECT v.vehicle_id, c.full_name as customer_name, v.plate_number, v.model, v.manufacture_year " +
                            "FROM vehicle v JOIN customer c ON v.customer_id = c.customer_id " +
                            "ORDER BY v.plate_number"
            );

            if (rs != null) {
                while (rs.next()) {
                    Vehicle vehicle = new Vehicle(
                            rs.getInt("vehicle_id"),
                            rs.getString("customer_name"),
                            rs.getString("plate_number"),
                            rs.getString("model"),
                            rs.getInt("manufacture_year")
                    );
                    vehicleList.add(vehicle);
                }
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

        try {
            ResultSet checkRs = DB.executeQuery(
                    "SELECT customer_id FROM customer WHERE full_name = '" + customer + "'"
            );
            if (checkRs == null || !checkRs.next()) {
                showAlert("Error", "Customer not found in database. Please refresh customer list.");
                return;
            }
        } catch (Exception e) {
            showAlert("Error", "Error verifying customer: " + e.getMessage());
            return;
        }

        String sql = String.format(
                "INSERT INTO vehicle (customer_id, plate_number, model, manufacture_year) " +
                        "VALUES ((SELECT customer_id FROM customer WHERE full_name = '%s'), '%s', '%s', %s)",
                customer, plate, model, year.isEmpty() ? "NULL" : year
        );

        int result = DB.executeUpdate(sql);
        if (result > 0) {
            showAlert("Success", "Vehicle registered successfully");
            clearFields();
            loadVehicles();
            Main.refreshDashboardGlobal();
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
                        "Plate: " + vehicle.getPlateNumber()
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
                    Main.refreshDashboardGlobal();
                }
            }
        });
    }

    private void clearFields() {
        cmbCustomer.setValue(null);
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

        public Vehicle(int vehicleId, String customerName, String plateNumber,
                       String model, int year) {
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