import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

public class VehicleTab extends BorderPane {

    private TableView<Vehicle> table = new TableView<>();
    private ObservableList<Vehicle> vehicleList = FXCollections.observableArrayList();

    private ComboBox<String> cmbCustomer = new ComboBox<>();
    private TextField txtPlate = new TextField();
    private TextField txtModel = new TextField();
    private TextField txtYear = new TextField();

    private Vehicle editingVehicle = null;
    private Button btnAdd;

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

        // Use HBox instead of GridPane for better control
        HBox content = new HBox(25);
        content.getStyleClass().add("window-content");
        content.setPadding(new Insets(20));

        // Left Side - Form (30% width)
        VBox formBox = new VBox(15); // Reduced spacing
        formBox.getStyleClass().add("form-box");
        formBox.setPrefWidth(320);
        formBox.setMinWidth(300);
        formBox.setMaxWidth(350);

        Label formTitle = new Label("Register New Vehicle");
        formTitle.getStyleClass().add("form-title");

        VBox customerBox = new VBox(5); // Reduced spacing
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

        VBox plateBox = new VBox(5);
        plateBox.getStyleClass().add("form-group");
        Label lblPlate = new Label("Plate Number *");
        lblPlate.getStyleClass().add("field-label");
        txtPlate.getStyleClass().add("field-input");
        txtPlate.setPromptText("ABC-123");
        txtPlate.setPrefHeight(32);
        plateBox.getChildren().addAll(lblPlate, txtPlate);

        VBox modelBox = new VBox(5);
        modelBox.getStyleClass().add("form-group");
        Label lblModel = new Label("Model");
        lblModel.getStyleClass().add("field-label");
        txtModel.getStyleClass().add("field-input");
        txtModel.setPromptText("Toyota Camry");
        txtModel.setPrefHeight(32);
        modelBox.getChildren().addAll(lblModel, txtModel);

        VBox yearBox = new VBox(5);
        yearBox.getStyleClass().add("form-group");
        Label lblYear = new Label("Year");
        lblYear.getStyleClass().add("field-label");
        txtYear.getStyleClass().add("field-input");
        txtYear.setPromptText("2020");
        txtYear.setPrefHeight(32);
        yearBox.getChildren().addAll(lblYear, txtYear);

        HBox formButtons = new HBox(10); // Reduced spacing
        formButtons.getStyleClass().add("form-buttons");

        btnAdd = new Button("Register Vehicle");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setPrefWidth(140);
        btnAdd.setOnAction(e -> addVehicle());

        Button btnClear = new Button("Clear");
        btnClear.getStyleClass().add("btn-secondary");
        btnClear.setPrefWidth(100);
        btnClear.setOnAction(e -> clearFields());

        formButtons.getChildren().addAll(btnAdd, btnClear);

        formBox.getChildren().addAll(formTitle, customerBox, plateBox, modelBox,
                yearBox, formButtons);
        content.getChildren().add(formBox);

        // Right Side - Table (70% width)
        VBox tableBox = new VBox(10); // Reduced spacing
        tableBox.getStyleClass().add("table-box");
        HBox.setHgrow(tableBox, Priority.ALWAYS); // Make table take remaining space

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
        VBox.setVgrow(table, Priority.ALWAYS);

        tableBox.getChildren().addAll(tableHeader, table);
        content.getChildren().add(tableBox);

        setCenter(content);
        loadVehicles();
    }

    private void createTable() {
        table.getColumns().clear();

        TableColumn<Vehicle, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("vehicleId"));
        colId.setPrefWidth(60); // Reduced

        TableColumn<Vehicle, String> colCustomer = new TableColumn<>("Customer");
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colCustomer.setPrefWidth(150); // Reduced

        TableColumn<Vehicle, String> colPlate = new TableColumn<>("Plate");
        colPlate.setCellValueFactory(new PropertyValueFactory<>("plateNumber"));
        colPlate.setPrefWidth(120); // Reduced

        TableColumn<Vehicle, String> colModel = new TableColumn<>("Model");
        colModel.setCellValueFactory(new PropertyValueFactory<>("model"));
        colModel.setPrefWidth(150); // Reduced

        TableColumn<Vehicle, Integer> colYear = new TableColumn<>("Year");
        colYear.setCellValueFactory(new PropertyValueFactory<>("year"));
        colYear.setPrefWidth(80); // Reduced

        TableColumn<Vehicle, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(180); // Increased to show buttons
        colActions.setCellFactory(param -> new TableCell<Vehicle, Void>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");
            private final HBox buttons = new HBox(10, btnEdit, btnDelete); // Increased spacing

            {
                btnEdit.getStyleClass().add("btn-table-edit");
                btnDelete.getStyleClass().add("btn-table-delete");
                buttons.getStyleClass().add("table-actions");

                // Set button sizes
                btnEdit.setPrefWidth(70);
                btnDelete.setPrefWidth(70);

                btnEdit.setOnAction(e -> {
                    Vehicle vehicle = getTableView().getItems().get(getIndex());
                    editVehicle(vehicle);
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
        if (editingVehicle != null) {
            updateVehicle(editingVehicle);
            return;
        }

        String customer = cmbCustomer.getValue();
        String plate = txtPlate.getText().trim();
        String model = txtModel.getText().trim();
        String yearStr = txtYear.getText().trim();

        if (customer == null || customer.isEmpty()) {
            showAlert("Warning", "Please select a customer");
            return;
        }

        if (plate.isEmpty()) {
            showAlert("Warning", "Please enter plate number");
            return;
        }

        try {
            // Get customer ID
            ResultSet customerRs = DB.executeQuery(
                    "SELECT customer_id FROM customer WHERE full_name = '" + customer + "'"
            );

            if (customerRs == null || !customerRs.next()) {
                showAlert("Error", "Customer not found");
                return;
            }

            int customerId = customerRs.getInt("customer_id");
            customerRs.close();

            Integer year = null;
            if (!yearStr.isEmpty()) {
                year = Integer.parseInt(yearStr);
            }

            PreparedStatement pstmt = DB.prepareStatement(
                    "INSERT INTO vehicle (customer_id, plate_number, model, manufacture_year) VALUES (?, ?, ?, ?)"
            );
            pstmt.setInt(1, customerId);
            pstmt.setString(2, plate);
            pstmt.setString(3, model);
            if (year != null) {
                pstmt.setInt(4, year);
            } else {
                pstmt.setNull(4, java.sql.Types.INTEGER);
            }

            int result = DB.executeUpdate(pstmt);
            if (result > 0) {
                showAlert("Success", "Vehicle registered successfully");
                clearFields();
                loadVehicles();
                Main.refreshDashboardGlobal();
            } else {
                showAlert("Error", "Failed to register vehicle");
            }
        } catch (NumberFormatException e) {
            showAlert("Warning", "Year must be a number");
        } catch (Exception e) {
            showAlert("Error", "Error registering vehicle: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateVehicle(Vehicle vehicle) {
        String customer = cmbCustomer.getValue();
        String plate = txtPlate.getText().trim();
        String model = txtModel.getText().trim();
        String yearStr = txtYear.getText().trim();

        if (customer == null || customer.isEmpty()) {
            showAlert("Warning", "Please select a customer");
            return;
        }

        if (plate.isEmpty()) {
            showAlert("Warning", "Please enter plate number");
            return;
        }

        try {
            // Get customer ID
            ResultSet customerRs = DB.executeQuery(
                    "SELECT customer_id FROM customer WHERE full_name = '" + customer + "'"
            );

            if (customerRs == null || !customerRs.next()) {
                showAlert("Error", "Customer not found");
                return;
            }

            int customerId = customerRs.getInt("customer_id");
            customerRs.close();

            Integer year = null;
            if (!yearStr.isEmpty()) {
                year = Integer.parseInt(yearStr);
            }

            PreparedStatement pstmt = DB.prepareStatement(
                    "UPDATE vehicle SET customer_id=?, plate_number=?, model=?, manufacture_year=? WHERE vehicle_id=?"
            );
            pstmt.setInt(1, customerId);
            pstmt.setString(2, plate);
            pstmt.setString(3, model);
            if (year != null) {
                pstmt.setInt(4, year);
            } else {
                pstmt.setNull(4, java.sql.Types.INTEGER);
            }
            pstmt.setInt(5, vehicle.getVehicleId());

            int result = DB.executeUpdate(pstmt);
            if (result > 0) {
                showAlert("Success", "Vehicle updated successfully");
                clearFields();
                loadVehicles();
                Main.refreshDashboardGlobal();
            } else {
                showAlert("Error", "Failed to update vehicle");
            }
        } catch (Exception e) {
            showAlert("Error", "Error updating vehicle: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void editVehicle(Vehicle vehicle) {
        editingVehicle = vehicle;

        cmbCustomer.setValue(vehicle.getCustomerName());
        txtPlate.setText(vehicle.getPlateNumber());
        txtModel.setText(vehicle.getModel());
        txtYear.setText(String.valueOf(vehicle.getYear()));

        btnAdd.setText("Update Vehicle");
    }

    private void deleteVehicle(Vehicle vehicle) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Vehicle");
        alert.setContentText("Are you sure you want to delete vehicle " + vehicle.getPlateNumber() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    PreparedStatement pstmt = DB.prepareStatement(
                            "DELETE FROM vehicle WHERE vehicle_id = ?"
                    );
                    pstmt.setInt(1, vehicle.getVehicleId());

                    int result = DB.executeUpdate(pstmt);
                    if (result > 0) {
                        showAlert("Success", "Vehicle deleted successfully");
                        loadVehicles();
                        Main.refreshDashboardGlobal();
                    }
                } catch (Exception e) {
                    showAlert("Error", "Error deleting vehicle: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void clearFields() {
        editingVehicle = null;
        btnAdd.setText("Register Vehicle");
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