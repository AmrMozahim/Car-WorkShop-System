import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.sql.ResultSet;

public class MechanicTab extends BorderPane {

    private TableView<Mechanic> table = new TableView<>();
    private ObservableList<Mechanic> mechanicList = FXCollections.observableArrayList();

    private TextField txtName = new TextField();
    private TextField txtPhone = new TextField();

    public MechanicTab() {
        initialize();
    }

    private void initialize() {
        getStyleClass().add("window-root");

        VBox header = new VBox(8);
        header.getStyleClass().add("window-header");
        header.setPadding(new Insets(20));

        Label title = new Label("Mechanic Management");
        title.getStyleClass().add("window-title");

        Label subtitle = new Label("Manage workshop mechanics");
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

        Label formTitle = new Label("Add New Mechanic");
        formTitle.getStyleClass().add("form-title");

        VBox nameBox = new VBox(8);
        nameBox.getStyleClass().add("form-group");
        Label lblName = new Label("Name *");
        lblName.getStyleClass().add("field-label");
        txtName.getStyleClass().add("field-input");
        txtName.setPromptText("John Smith");
        nameBox.getChildren().addAll(lblName, txtName);

        VBox phoneBox = new VBox(8);
        phoneBox.getStyleClass().add("form-group");
        Label lblPhone = new Label("Phone Number");
        lblPhone.getStyleClass().add("field-label");
        txtPhone.getStyleClass().add("field-input");
        txtPhone.setPromptText("(123) 456-7890");
        phoneBox.getChildren().addAll(lblPhone, txtPhone);

        HBox formButtons = new HBox(15);
        formButtons.getStyleClass().add("form-buttons");

        Button btnAdd = new Button("Add Mechanic");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setOnAction(e -> addMechanic());

        Button btnClear = new Button("Clear");
        btnClear.getStyleClass().add("btn-secondary");
        btnClear.setOnAction(e -> clearFields());

        formButtons.getChildren().addAll(btnAdd, btnClear);

        formBox.getChildren().addAll(formTitle, nameBox, phoneBox, formButtons);
        content.add(formBox, 0, 0);

        VBox tableBox = new VBox(15);
        tableBox.getStyleClass().add("table-box");

        HBox tableHeader = new HBox();
        tableHeader.getStyleClass().add("table-header-box");

        Label tableTitle = new Label("Mechanics List");
        tableTitle.getStyleClass().add("table-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnRefresh = new Button("Refresh");
        btnRefresh.getStyleClass().add("btn-refresh");
        btnRefresh.setOnAction(e -> loadMechanics());

        tableHeader.getChildren().addAll(tableTitle, spacer, btnRefresh);

        createTable();
        table.setPrefHeight(400);

        tableBox.getChildren().addAll(tableHeader, table);
        content.add(tableBox, 1, 0);

        setCenter(content);
        loadMechanics();
    }

    private void createTable() {
        table.getColumns().clear();

        TableColumn<Mechanic, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("mechanicId"));
        colId.setPrefWidth(80);

        TableColumn<Mechanic, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colName.setPrefWidth(150);

        TableColumn<Mechanic, String> colPhone = new TableColumn<>("Phone");
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colPhone.setPrefWidth(120);

        TableColumn<Mechanic, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(120);
        colActions.setCellFactory(param -> new TableCell<Mechanic, Void>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");
            private final HBox buttons = new HBox(8, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().add("btn-table-edit");
                btnDelete.getStyleClass().add("btn-table-delete");
                buttons.getStyleClass().add("table-actions");

                btnEdit.setOnAction(e -> {
                    Mechanic mechanic = getTableView().getItems().get(getIndex());
                    editMechanic(mechanic);
                });

                btnDelete.setOnAction(e -> {
                    Mechanic mechanic = getTableView().getItems().get(getIndex());
                    deleteMechanic(mechanic);
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

        table.getColumns().addAll(colId, colName, colPhone, colActions);
        table.setItems(mechanicList);
        table.setFixedCellSize(45);
    }

    private void loadMechanics() {
        mechanicList.clear();
        try {
            ResultSet rs = DB.executeQuery("SELECT mechanic_id, name, phone FROM mechanic ORDER BY name");
            if (rs != null) {
                while (rs.next()) {
                    Mechanic mechanic = new Mechanic(
                            rs.getInt("mechanic_id"),
                            rs.getString("name"),
                            rs.getString("phone")
                    );
                    mechanicList.add(mechanic);
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Error loading mechanics: " + e.getMessage());
        }
    }

    private void addMechanic() {
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();

        if (name.isEmpty()) {
            showAlert("Warning", "Please enter mechanic name");
            return;
        }

        String sql = String.format(
                "INSERT INTO mechanic (name, phone) VALUES ('%s', '%s')",
                name, phone
        );

        int result = DB.executeUpdate(sql);
        if (result > 0) {
            showAlert("Success", "Mechanic added successfully");
            clearFields();
            loadMechanics();
            Main.refreshDashboardGlobal();
        } else {
            showAlert("Error", "Failed to add mechanic");
        }
    }

    private void editMechanic(Mechanic mechanic) {
        txtName.setText(mechanic.getName());
        txtPhone.setText(mechanic.getPhone());

        showAlert("Edit Mode", "Edit mechanic details and click 'Add Mechanic' to update");
    }

    private void deleteMechanic(Mechanic mechanic) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Mechanic");
        alert.setContentText("Are you sure you want to delete " + mechanic.getName() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String sql = "DELETE FROM mechanic WHERE mechanic_id = " + mechanic.getMechanicId();
                int result = DB.executeUpdate(sql);
                if (result > 0) {
                    showAlert("Success", "Mechanic deleted successfully");
                    loadMechanics();
                    Main.refreshDashboardGlobal();
                }
            }
        });
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