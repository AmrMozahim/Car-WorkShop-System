import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

public class ServiceTab extends BorderPane {

    private TableView<Service> table = new TableView<>();
    private ObservableList<Service> serviceList = FXCollections.observableArrayList();

    private TextField txtName = new TextField();
    private TextField txtPrice = new TextField();

    private Service editingService = null;
    private Button btnAdd;

    public ServiceTab() {
        initialize();
    }

    private void initialize() {
        getStyleClass().add("window-root");

        VBox header = new VBox(8);
        header.getStyleClass().add("window-header");
        header.setPadding(new Insets(20));

        Label title = new Label("Service Management");
        title.getStyleClass().add("window-title");

        Label subtitle = new Label("Manage workshop services and pricing");
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

        Label formTitle = new Label("Add New Service");
        formTitle.getStyleClass().add("form-title");

        VBox nameBox = new VBox(8);
        nameBox.getStyleClass().add("form-group");
        Label lblName = new Label("Service Name *");
        lblName.getStyleClass().add("field-label");
        txtName.getStyleClass().add("field-input");
        txtName.setPromptText("Oil Change, Brake Repair, etc.");
        nameBox.getChildren().addAll(lblName, txtName);

        VBox priceBox = new VBox(8);
        priceBox.getStyleClass().add("form-group");
        Label lblPrice = new Label("Price ($) *");
        lblPrice.getStyleClass().add("field-label");
        txtPrice.getStyleClass().add("field-input");
        txtPrice.setPromptText("50.00");
        priceBox.getChildren().addAll(lblPrice, txtPrice);

        HBox formButtons = new HBox(15);
        formButtons.getStyleClass().add("form-buttons");

        btnAdd = new Button("Add Service");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setOnAction(e -> addService());

        Button btnClear = new Button("Clear");
        btnClear.getStyleClass().add("btn-secondary");
        btnClear.setOnAction(e -> clearFields());

        formButtons.getChildren().addAll(btnAdd, btnClear);

        formBox.getChildren().addAll(formTitle, nameBox, priceBox, formButtons);
        content.add(formBox, 0, 0);

        VBox tableBox = new VBox(15);
        tableBox.getStyleClass().add("table-box");

        HBox tableHeader = new HBox();
        tableHeader.getStyleClass().add("table-header-box");

        Label tableTitle = new Label("Service List");
        tableTitle.getStyleClass().add("table-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnRefresh = new Button("Refresh");
        btnRefresh.getStyleClass().add("btn-refresh");
        btnRefresh.setOnAction(e -> loadServices());

        tableHeader.getChildren().addAll(tableTitle, spacer, btnRefresh);

        createTable();
        table.setPrefHeight(400);

        tableBox.getChildren().addAll(tableHeader, table);
        content.add(tableBox, 1, 0);

        setCenter(content);
        loadServices();
    }

    private void createTable() {
        table.getColumns().clear();

        TableColumn<Service, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("serviceId"));
        colId.setPrefWidth(80);

        TableColumn<Service, String> colName = new TableColumn<>("Service");
        colName.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        colName.setPrefWidth(200);

        TableColumn<Service, Double> colPrice = new TableColumn<>("Price ($)");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colPrice.setPrefWidth(100);

        TableColumn<Service, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(120);
        colActions.setCellFactory(param -> new TableCell<Service, Void>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");
            private final HBox buttons = new HBox(8, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().add("btn-table-edit");
                btnDelete.getStyleClass().add("btn-table-delete");
                buttons.getStyleClass().add("table-actions");

                btnEdit.setOnAction(e -> {
                    Service service = getTableView().getItems().get(getIndex());
                    editService(service);
                });

                btnDelete.setOnAction(e -> {
                    Service service = getTableView().getItems().get(getIndex());
                    deleteService(service);
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

        table.getColumns().addAll(colId, colName, colPrice, colActions);
        table.setItems(serviceList);
        table.setFixedCellSize(45);
    }

    private void loadServices() {
        serviceList.clear();
        try {
            ResultSet rs = DB.executeQuery("SELECT service_id, service_name, price FROM service");
            if (rs != null) {
                while (rs.next()) {
                    Service service = new Service(
                            rs.getInt("service_id"),
                            rs.getString("service_name"),
                            rs.getDouble("price")
                    );
                    serviceList.add(service);
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Error loading services: " + e.getMessage());
        }
    }

    private void addService() {
        if (editingService != null) {
            updateService(editingService);
            return;
        }

        String name = txtName.getText().trim();
        String priceStr = txtPrice.getText().trim();

        if (name.isEmpty()) {
            showAlert("Warning", "Please enter service name");
            return;
        }

        if (priceStr.isEmpty()) {
            showAlert("Warning", "Please enter price");
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);

            if (price <= 0) {
                showAlert("Warning", "Price must be greater than 0");
                return;
            }

            PreparedStatement pstmt = DB.prepareStatement(
                    "INSERT INTO service (service_name, price) VALUES (?, ?)"
            );
            pstmt.setString(1, name);
            pstmt.setDouble(2, price);

            int result = DB.executeUpdate(pstmt);
            if (result > 0) {
                showAlert("Success", "Service added successfully");
                clearFields();
                loadServices();
                Main.refreshDashboardGlobal();
            } else {
                showAlert("Error", "Failed to add service");
            }
        } catch (NumberFormatException e) {
            showAlert("Warning", "Price must be a number");
        } catch (Exception e) {
            showAlert("Error", "Error adding service: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateService(Service service) {
        String name = txtName.getText().trim();
        String priceStr = txtPrice.getText().trim();

        if (name.isEmpty()) {
            showAlert("Warning", "Please enter service name");
            return;
        }

        if (priceStr.isEmpty()) {
            showAlert("Warning", "Please enter price");
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);

            if (price <= 0) {
                showAlert("Warning", "Price must be greater than 0");
                return;
            }

            PreparedStatement pstmt = DB.prepareStatement(
                    "UPDATE service SET service_name=?, price=? WHERE service_id=?"
            );
            pstmt.setString(1, name);
            pstmt.setDouble(2, price);
            pstmt.setInt(3, service.getServiceId());

            int result = DB.executeUpdate(pstmt);
            if (result > 0) {
                showAlert("Success", "Service updated successfully");
                clearFields();
                loadServices();
                Main.refreshDashboardGlobal();
            } else {
                showAlert("Error", "Failed to update service");
            }
        } catch (Exception e) {
            showAlert("Error", "Error updating service: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void editService(Service service) {
        editingService = service;

        txtName.setText(service.getServiceName());
        txtPrice.setText(String.valueOf(service.getPrice()));

        btnAdd.setText("Update Service");
    }

    private void deleteService(Service service) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Service");
        alert.setContentText("Are you sure you want to delete service " + service.getServiceName() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    PreparedStatement pstmt = DB.prepareStatement(
                            "DELETE FROM service WHERE service_id = ?"
                    );
                    pstmt.setInt(1, service.getServiceId());

                    int result = DB.executeUpdate(pstmt);
                    if (result > 0) {
                        showAlert("Success", "Service deleted successfully");
                        loadServices();
                        Main.refreshDashboardGlobal();
                    }
                } catch (Exception e) {
                    showAlert("Error", "Error deleting service: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void clearFields() {
        editingService = null;
        btnAdd.setText("Add Service");
        txtName.clear();
        txtPrice.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Service {
        private int serviceId;
        private String serviceName;
        private double price;

        public Service(int serviceId, String serviceName, double price) {
            this.serviceId = serviceId;
            this.serviceName = serviceName;
            this.price = price;
        }

        public int getServiceId() { return serviceId; }
        public String getServiceName() { return serviceName; }
        public double getPrice() { return price; }
    }
}