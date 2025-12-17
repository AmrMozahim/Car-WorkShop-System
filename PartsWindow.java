import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.ResultSet;

public class PartsWindow {

    private TableView<Part> table = new TableView<>();
    private ObservableList<Part> partList = FXCollections.observableArrayList();

    private TextField txtName = new TextField();
    private TextField txtQuantity = new TextField();
    private TextField txtPrice = new TextField();
    private ComboBox<String> cmbSuppliers = new ComboBox<>();

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Parts Management");

        BorderPane root = new BorderPane();
        root.getStyleClass().add("window-root");

        VBox header = new VBox(10);
        header.getStyleClass().add("window-header");
        header.setPadding(new Insets(15));

        Label title = new Label("Parts Management");
        title.getStyleClass().add("window-title");

        Label subtitle = new Label("Manage spare parts inventory and suppliers");
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

        Label formTitle = new Label("Add New Part");
        formTitle.getStyleClass().add("form-title");

        VBox nameBox = new VBox(5);
        Label lblName = new Label("Part Name *");
        lblName.getStyleClass().add("field-label");
        txtName.getStyleClass().add("field-input");
        txtName.setPromptText("Enter part name");
        nameBox.getChildren().addAll(lblName, txtName);

        VBox quantityBox = new VBox(5);
        Label lblQuantity = new Label("Quantity *");
        lblQuantity.getStyleClass().add("field-label");
        txtQuantity.getStyleClass().add("field-input");
        txtQuantity.setPromptText("Enter quantity");
        quantityBox.getChildren().addAll(lblQuantity, txtQuantity);

        VBox priceBox = new VBox(5);
        Label lblPrice = new Label("Price *");
        lblPrice.getStyleClass().add("field-label");
        txtPrice.getStyleClass().add("field-input");
        txtPrice.setPromptText("Enter price");
        priceBox.getChildren().addAll(lblPrice, txtPrice);

        VBox supplierBox = new VBox(5);
        Label lblSupplier = new Label("Supplier");
        lblSupplier.getStyleClass().add("field-label");
        cmbSuppliers.getStyleClass().add("field-combo");
        cmbSuppliers.setPromptText("Select supplier");
        loadSuppliers();
        supplierBox.getChildren().addAll(lblSupplier, cmbSuppliers);

        HBox formButtons = new HBox(10);
        formButtons.getStyleClass().add("form-buttons");

        Button btnAdd = new Button("Add Part");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setOnAction(e -> addPart());

        Button btnClear = new Button("Clear Fields");
        btnClear.getStyleClass().add("btn-secondary");
        btnClear.setOnAction(e -> clearFields());

        formButtons.getChildren().addAll(btnAdd, btnClear);

        formBox.getChildren().addAll(formTitle, nameBox, quantityBox, priceBox, supplierBox, formButtons);
        content.add(formBox, 0, 0);

        VBox tableBox = new VBox(10);
        tableBox.getStyleClass().add("table-box");

        HBox tableHeader = new HBox();
        tableHeader.getStyleClass().add("table-header");

        Label tableTitle = new Label("Parts Inventory");
        tableTitle.getStyleClass().add("table-title");

        Button btnRefresh = new Button("Refresh");
        btnRefresh.getStyleClass().add("btn-refresh");
        btnRefresh.setOnAction(e -> loadParts());

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

        loadParts();
    }

    private void loadSuppliers() {
        try {
            ResultSet rs = DB.getSuppliers();
            while (rs.next()) {
                cmbSuppliers.getItems().add(rs.getString("supplier_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createTable() {
        table.getColumns().clear();

        TableColumn<Part, Integer> colId = new TableColumn<>("#");
        colId.setCellValueFactory(new PropertyValueFactory<>("partId"));
        colId.setPrefWidth(60);

        TableColumn<Part, String> colName = new TableColumn<>("Part Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("partName"));
        colName.setPrefWidth(200);

        TableColumn<Part, Integer> colQuantity = new TableColumn<>("Quantity");
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colQuantity.setPrefWidth(80);

        TableColumn<Part, Double> colPrice = new TableColumn<>("Price");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colPrice.setPrefWidth(100);

        table.getColumns().addAll(colId, colName, colQuantity, colPrice);
        table.setItems(partList);
    }

    private void loadParts() {
        partList.clear();
        try {
            ResultSet rs = DB.getParts();
            while (rs.next()) {
                Part part = new Part(
                        rs.getInt("part_id"),
                        rs.getString("part_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price")
                );
                partList.add(part);
            }
        } catch (Exception e) {
            showAlert("Error", "Error loading parts");
            e.printStackTrace();
        }
    }

    private void addPart() {
        String name = txtName.getText().trim();
        String quantity = txtQuantity.getText().trim();
        String price = txtPrice.getText().trim();
        String supplier = cmbSuppliers.getValue();

        if (name.isEmpty()) {
            showAlert("Warning", "Please enter part name");
            return;
        }

        if (quantity.isEmpty()) {
            showAlert("Warning", "Please enter quantity");
            return;
        }

        if (price.isEmpty()) {
            showAlert("Warning", "Please enter price");
            return;
        }

        try {
            Integer.parseInt(quantity);
            Double.parseDouble(price);
        } catch (NumberFormatException e) {
            showAlert("Warning", "Quantity and price must be numbers");
            return;
        }

        String supplierId = "NULL";
        if (supplier != null && !supplier.isEmpty()) {
            supplierId = "(SELECT supplier_id FROM supplier WHERE supplier_name = '" + supplier + "')";
        }

        String sql = String.format(
                "INSERT INTO sparepart (part_name, quantity, price, supplier_id) " +
                        "VALUES ('%s', %s, %s, %s)",
                name, quantity, price, supplierId
        );

        int result = DB.executeUpdate(sql);
        if (result > 0) {
            showAlert("Success", "Part added successfully");
            clearFields();
            loadParts();
        } else {
            showAlert("Error", "Failed to add part");
        }
    }

    private void clearFields() {
        txtName.clear();
        txtQuantity.clear();
        txtPrice.clear();
        cmbSuppliers.setValue(null);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Part {
        private int partId;
        private String partName;
        private int quantity;
        private double price;

        public Part(int partId, String partName, int quantity, double price) {
            this.partId = partId;
            this.partName = partName;
            this.quantity = quantity;
            this.price = price;
        }

        public int getPartId() { return partId; }
        public String getPartName() { return partName; }
        public int getQuantity() { return quantity; }
        public double getPrice() { return price; }
    }
}