import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.sql.ResultSet;

public class ServiceTab extends BorderPane {

    private TableView<Service> table = new TableView<>();
    private ObservableList<Service> serviceList = FXCollections.observableArrayList();

    private TextField txtName = new TextField();
    private TextField txtPrice = new TextField();
    private TextField txtDuration = new TextField();
    private TextArea txtDescription = new TextArea();
    private ComboBox<String> cmbCategory = new ComboBox<>();

    public ServiceTab() {
        initialize();
    }

    private void initialize() {
        getStyleClass().add("window-root");

        // Header
        VBox header = new VBox(8);
        header.getStyleClass().add("window-header");
        header.setPadding(new Insets(20));

        Label title = new Label("Service Management");
        title.getStyleClass().add("window-title");

        Label subtitle = new Label("Manage workshop services and pricing");
        subtitle.getStyleClass().add("window-subtitle");

        header.getChildren().addAll(title, subtitle);
        setTop(header);

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

        Label formTitle = new Label("Add New Service");
        formTitle.getStyleClass().add("form-title");

        // Name Field
        VBox nameBox = new VBox(8);
        nameBox.getStyleClass().add("form-group");
        Label lblName = new Label("Service Name *");
        lblName.getStyleClass().add("field-label");
        txtName.getStyleClass().add("field-input");
        txtName.setPromptText("Oil Change, Brake Repair, etc.");
        nameBox.getChildren().addAll(lblName, txtName);

        // Price Field
        VBox priceBox = new VBox(8);
        priceBox.getStyleClass().add("form-group");
        Label lblPrice = new Label("Price ($) *");
        lblPrice.getStyleClass().add("field-label");
        txtPrice.getStyleClass().add("field-input");
        txtPrice.setPromptText("50.00");
        priceBox.getChildren().addAll(lblPrice, txtPrice);

        // Duration Field
        VBox durationBox = new VBox(8);
        durationBox.getStyleClass().add("form-group");
        Label lblDuration = new Label("Duration (hours)");
        lblDuration.getStyleClass().add("field-label");
        txtDuration.getStyleClass().add("field-input");
        txtDuration.setPromptText("1.5");
        durationBox.getChildren().addAll(lblDuration, txtDuration);

        // Category Field
        VBox categoryBox = new VBox(8);
        categoryBox.getStyleClass().add("form-group");
        Label lblCategory = new Label("Category");
        lblCategory.getStyleClass().add("field-label");
        cmbCategory.getStyleClass().add("field-combo");
        cmbCategory.getItems().addAll("Maintenance", "Repair", "Inspection", "Tire", "Electrical", "Body Work");
        cmbCategory.setPromptText("Select category");
        categoryBox.getChildren().addAll(lblCategory, cmbCategory);

        // Description Field
        VBox descBox = new VBox(8);
        descBox.getStyleClass().add("form-group");
        Label lblDesc = new Label("Description");
        lblDesc.getStyleClass().add("field-label");
        txtDescription.getStyleClass().add("field-textarea");
        txtDescription.setPromptText("Service details...");
        txtDescription.setPrefRowCount(3);
        descBox.getChildren().addAll(lblDesc, txtDescription);

        // Buttons
        HBox formButtons = new HBox(15);
        formButtons.getStyleClass().add("form-buttons");

        Button btnAdd = new Button("Add Service");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setOnAction(e -> addService());

        Button btnClear = new Button("Clear");
        btnClear.getStyleClass().add("btn-secondary");
        btnClear.setOnAction(e -> clearFields());

        formButtons.getChildren().addAll(btnAdd, btnClear);

        formBox.getChildren().addAll(formTitle, nameBox, priceBox, durationBox,
                categoryBox, descBox, formButtons);
        content.add(formBox, 0, 0);

        // Right - Table
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

        TableColumn<Service, String> colDuration = new TableColumn<>("Duration");
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colDuration.setPrefWidth(100);

        TableColumn<Service, String> colCategory = new TableColumn<>("Category");
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colCategory.setPrefWidth(120);

        TableColumn<Service, Integer> colUsage = new TableColumn<>("Usage Count");
        colUsage.setCellValueFactory(new PropertyValueFactory<>("usageCount"));
        colUsage.setPrefWidth(100);

        TableColumn<Service, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(120);
        colActions.setCellFactory(param -> new TableCell<Service, Void>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");

            {
                btnEdit.getStyleClass().add("btn-table-edit");
                btnDelete.getStyleClass().add("btn-table-delete");

                HBox buttons = new HBox(8, btnEdit, btnDelete);
                buttons.getStyleClass().add("table-actions");

                btnEdit.setOnAction(e -> {
                    Service service = getTableView().getItems().get(getIndex());
                    editService(service);
                });

                btnDelete.setOnAction(e -> {
                    Service service = getTableView().getItems().get(getIndex());
                    deleteService(service);
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

        table.getColumns().addAll(colId, colName, colPrice, colDuration,
                colCategory, colUsage, colActions);
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
                        rs.getDouble("price"),
                        rs.getString("duration"),
                        rs.getString("category"),
                        rs.getInt("usage_count")
                );
                serviceList.add(service);
            }
        } catch (Exception e) {
            showAlert("Error", "Error loading services: " + e.getMessage());
        }
    }

    private void addService() {
        String name = txtName.getText().trim();
        String price = txtPrice.getText().trim();
        String duration = txtDuration.getText().trim();
        String category = cmbCategory.getValue();
        String description = txtDescription.getText().trim();

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
                "INSERT INTO service (service_name, price, duration, category, description) " +
                        "VALUES ('%s', %s, '%s', '%s', '%s')",
                name, price, duration, category == null ? "" : category, description
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

    private void editService(Service service) {
        txtName.setText(service.getServiceName());
        txtPrice.setText(String.valueOf(service.getPrice()));
        txtDuration.setText(service.getDuration());
        cmbCategory.setValue(service.getCategory());

        showAlert("Edit Mode", "Edit service details and click 'Add Service' to update");
    }

    private void deleteService(Service service) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Service");
        alert.setContentText("Are you sure you want to delete service " + service.getServiceName() + "?");

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
        txtDuration.clear();
        txtDescription.clear();
        cmbCategory.setValue(null);
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
        private String duration;
        private String category;
        private int usageCount;

        public Service(int serviceId, String serviceName, double price,
                       String duration, String category, int usageCount) {
            this.serviceId = serviceId;
            this.serviceName = serviceName;
            this.price = price;
            this.duration = duration;
            this.category = category;
            this.usageCount = usageCount;
        }

        public int getServiceId() { return serviceId; }
        public String getServiceName() { return serviceName; }
        public double getPrice() { return price; }
        public String getDuration() { return duration; }
        public String getCategory() { return category; }
        public int getUsageCount() { return usageCount; }
    }
}