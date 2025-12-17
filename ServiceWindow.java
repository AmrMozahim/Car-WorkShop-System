import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.ResultSet;

public class ServiceWindow {

    private TableView<Service> table = new TableView<>();
    private ObservableList<Service> serviceList = FXCollections.observableArrayList();

    private TextField txtName = new TextField();
    private TextField txtPrice = new TextField();

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Service Management");

        BorderPane root = new BorderPane();
        root.getStyleClass().add("window-root");

        VBox header = new VBox(10);
        header.getStyleClass().add("window-header");
        header.setPadding(new Insets(15));

        Label title = new Label("Service Management");
        title.getStyleClass().add("window-title");

        Label subtitle = new Label("Manage services available in the workshop");
        subtitle.getStyleClass().add("window-subtitle");

        header.getChildren().addAll(title, subtitle);
        root.setTop(header);

        GridPane content = new GridPane();
        content.getStyleClass().add("window-content");
        content.setPadding(new Insets(20));
        content.setVgap(15);
        content.setHgap(15);

        VBox formBox = new VBox(15);
        formBox.getStyleClass().add("form-box");

        Label formTitle = new Label("Add New Service");
        formTitle.getStyleClass().add("form-title");

        VBox nameBox = new VBox(5);
        Label lblName = new Label("Service Name *");
        lblName.getStyleClass().add("field-label");
        txtName.getStyleClass().add("field-input");
        txtName.setPromptText("Enter service name");
        nameBox.getChildren().addAll(lblName, txtName);

        VBox priceBox = new VBox(5);
        Label lblPrice = new Label("Price *");
        lblPrice.getStyleClass().add("field-label");
        txtPrice.getStyleClass().add("field-input");
        txtPrice.setPromptText("Enter price");
        priceBox.getChildren().addAll(lblPrice, txtPrice);

        HBox formButtons = new HBox(10);
        formButtons.getStyleClass().add("form-buttons");

        Button btnAdd = new Button("Add Service");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setOnAction(e -> addService());

        Button btnClear = new Button("Clear Fields");
        btnClear.getStyleClass().add("btn-secondary");
        btnClear.setOnAction(e -> clearFields());

        formButtons.getChildren().addAll(btnAdd, btnClear);

        formBox.getChildren().addAll(formTitle, nameBox, priceBox, formButtons);
        content.add(formBox, 0, 0);

        VBox tableBox = new VBox(10);
        tableBox.getStyleClass().add("table-box");

        HBox tableHeader = new HBox();
        tableHeader.getStyleClass().add("table-header");

        Label tableTitle = new Label("Service List");
        tableTitle.getStyleClass().add("table-title");

        Button btnRefresh = new Button("Refresh");
        btnRefresh.getStyleClass().add("btn-refresh");
        btnRefresh.setOnAction(e -> loadServices());

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

        loadServices();
    }

    private void createTable() {
        table.getColumns().clear();

        TableColumn<Service, Integer> colId = new TableColumn<>("#");
        colId.setCellValueFactory(new PropertyValueFactory<>("serviceId"));
        colId.setPrefWidth(60);

        TableColumn<Service, String> colName = new TableColumn<>("Service Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        colName.setPrefWidth(250);

        TableColumn<Service, Double> colPrice = new TableColumn<>("Price");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colPrice.setPrefWidth(100);

        TableColumn<Service, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(120);
        colActions.setCellFactory(param -> new TableCell<Service, Void>() {
            private final Button btnDelete = new Button("Delete");

            {
                btnDelete.getStyleClass().add("btn-table-delete");
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
                    setGraphic(btnDelete);
                }
            }
        });

        table.getColumns().addAll(colId, colName, colPrice, colActions);
        table.setItems(serviceList);
    }

    private void loadServices() {
        serviceList.clear();
        try {
            ResultSet rs = DB.getServices();
            while (rs.next()) {
                Service service = new Service(
                        rs.getInt("service_id"),
                        rs.getString("service_name"),
                        rs.getDouble("price")
                );
                serviceList.add(service);
            }
        } catch (Exception e) {
            showAlert("Error", "Error loading services");
            e.printStackTrace();
        }
    }

    private void addService() {
        String name = txtName.getText().trim();
        String price = txtPrice.getText().trim();

        if (name.isEmpty()) {
            showAlert("Warning", "Please enter service name");
            return;
        }

        if (price.isEmpty()) {
            showAlert("Warning", "Please enter price");
            return;
        }

        try {
            Double.parseDouble(price);
        } catch (NumberFormatException e) {
            showAlert("Warning", "Price must be a number");
            return;
        }

        String sql = String.format(
                "INSERT INTO service (service_name, price) VALUES ('%s', %s)",
                name, price
        );

        int result = DB.executeUpdate(sql);
        if (result > 0) {
            showAlert("Success", "Service added successfully");
            clearFields();
            loadServices();
        } else {
            showAlert("Error", "Failed to add service");
        }
    }

    private void deleteService(Service service) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Are you sure you want to delete this service?");
        alert.setContentText("Service: " + service.getServiceName());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String sql = "DELETE FROM service WHERE service_id = " + service.getServiceId();
                int result = DB.executeUpdate(sql);
                if (result > 0) {
                    showAlert("Success", "Service deleted successfully");
                    loadServices();
                }
            }
        });
    }

    private void clearFields() {
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