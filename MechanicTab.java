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
    private TextField txtEmail = new TextField();
    private ComboBox<String> cmbSpecialty = new ComboBox<>();
    private TextField txtExperience = new TextField();
    private ComboBox<String> cmbStatus = new ComboBox<>();

    public MechanicTab() {
        initialize();
    }

    private void initialize() {
        getStyleClass().add("window-root");

        // Header
        VBox header = new VBox(8);
        header.getStyleClass().add("window-header");
        header.setPadding(new Insets(20));

        Label title = new Label("Mechanic Management");
        title.getStyleClass().add("window-title");

        Label subtitle = new Label("Manage workshop mechanics and assignments");
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

        Label formTitle = new Label("Add New Mechanic");
        formTitle.getStyleClass().add("form-title");

        // Name Field
        VBox nameBox = new VBox(8);
        nameBox.getStyleClass().add("form-group");
        Label lblName = new Label("Full Name *");
        lblName.getStyleClass().add("field-label");
        txtName.getStyleClass().add("field-input");
        txtName.setPromptText("John Smith");
        nameBox.getChildren().addAll(lblName, txtName);

        // Phone Field
        VBox phoneBox = new VBox(8);
        phoneBox.getStyleClass().add("form-group");
        Label lblPhone = new Label("Phone Number");
        lblPhone.getStyleClass().add("field-label");
        txtPhone.getStyleClass().add("field-input");
        txtPhone.setPromptText("(123) 456-7890");
        phoneBox.getChildren().addAll(lblPhone, txtPhone);

        // Email Field
        VBox emailBox = new VBox(8);
        emailBox.getStyleClass().add("form-group");
        Label lblEmail = new Label("Email");
        lblEmail.getStyleClass().add("field-label");
        txtEmail.getStyleClass().add("field-input");
        txtEmail.setPromptText("john@workshop.com");
        emailBox.getChildren().addAll(lblEmail, txtEmail);

        // Specialty Field
        VBox specialtyBox = new VBox(8);
        specialtyBox.getStyleClass().add("form-group");
        Label lblSpecialty = new Label("Specialty");
        lblSpecialty.getStyleClass().add("field-label");
        cmbSpecialty.getStyleClass().add("field-combo");
        cmbSpecialty.getItems().addAll("Engine", "Transmission", "Brakes",
                "Electrical", "Suspension", "AC", "General");
        cmbSpecialty.setPromptText("Select specialty");
        specialtyBox.getChildren().addAll(lblSpecialty, cmbSpecialty);

        // Experience Field
        VBox expBox = new VBox(8);
        expBox.getStyleClass().add("form-group");
        Label lblExp = new Label("Experience (years)");
        lblExp.getStyleClass().add("field-label");
        txtExperience.getStyleClass().add("field-input");
        txtExperience.setPromptText("5");
        expBox.getChildren().addAll(lblExp, txtExperience);

        // Status Field
        VBox statusBox = new VBox(8);
        statusBox.getStyleClass().add("form-group");
        Label lblStatus = new Label("Status");
        lblStatus.getStyleClass().add("field-label");
        cmbStatus.getStyleClass().add("field-combo");
        cmbStatus.getItems().addAll("Active", "On Leave", "Inactive");
        cmbStatus.setValue("Active");
        statusBox.getChildren().addAll(lblStatus, cmbStatus);

        // Buttons
        HBox formButtons = new HBox(15);
        formButtons.getStyleClass().add("form-buttons");

        Button btnAdd = new Button("Add Mechanic");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setOnAction(e -> addMechanic());

        Button btnClear = new Button("Clear");
        btnClear.getStyleClass().add("btn-secondary");
        btnClear.setOnAction(e -> clearFields());

        formButtons.getChildren().addAll(btnAdd, btnClear);

        formBox.getChildren().addAll(formTitle, nameBox, phoneBox, emailBox,
                specialtyBox, expBox, statusBox, formButtons);
        content.add(formBox, 0, 0);

        // Right - Table
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

        TableColumn<Mechanic, String> colSpecialty = new TableColumn<>("Specialty");
        colSpecialty.setCellValueFactory(new PropertyValueFactory<>("specialty"));
        colSpecialty.setPrefWidth(120);

        TableColumn<Mechanic, Integer> colExp = new TableColumn<>("Exp. (yrs)");
        colExp.setCellValueFactory(new PropertyValueFactory<>("experience"));
        colExp.setPrefWidth(80);

        TableColumn<Mechanic, Integer> colJobs = new TableColumn<>("Active Jobs");
        colJobs.setCellValueFactory(new PropertyValueFactory<>("activeJobs"));
        colJobs.setPrefWidth(100);

        TableColumn<Mechanic, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setPrefWidth(100);

        TableColumn<Mechanic, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(150);
        colActions.setCellFactory(param -> new TableCell<Mechanic, Void>() {
            private final Button btnAssign = new Button("Assign");
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");

            {
                btnAssign.getStyleClass().add("btn-table-edit");
                btnEdit.getStyleClass().add("btn-table-edit");
                btnDelete.getStyleClass().add("btn-table-delete");

                HBox buttons = new HBox(8, btnAssign, btnEdit, btnDelete);
                buttons.getStyleClass().add("table-actions");

                btnAssign.setOnAction(e -> {
                    Mechanic mechanic = getTableView().getItems().get(getIndex());
                    assignJob(mechanic);
                });

                btnEdit.setOnAction(e -> {
                    Mechanic mechanic = getTableView().getItems().get(getIndex());
                    editMechanic(mechanic);
                });

                btnDelete.setOnAction(e -> {
                    Mechanic mechanic = getTableView().getItems().get(getIndex());
                    deleteMechanic(mechanic);
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

        table.getColumns().addAll(colId, colName, colPhone, colSpecialty,
                colExp, colJobs, colStatus, colActions);
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
                        rs.getString("phone"),
                        rs.getString("specialty"),
                        rs.getInt("experience"),
                        rs.getInt("active_jobs"),
                        rs.getString("status")
                );
                mechanicList.add(mechanic);
            }
        } catch (Exception e) {
            showAlert("Error", "Error loading mechanics: " + e.getMessage());
        }
    }

    private void addMechanic() {
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String specialty = cmbSpecialty.getValue();
        String experience = txtExperience.getText().trim();
        String status = cmbStatus.getValue();

        if (name.isEmpty()) {
            showAlert("Warning", "Please enter mechanic name");
            return;
        }

        if (!experience.isEmpty()) {
            try {
                Integer.parseInt(experience);
            } catch (NumberFormatException e) {
                showAlert("Warning", "Experience must be a number");
                return;
            }
        }

        String sql = String.format(
                "INSERT INTO mechanic (name, phone, email, specialty, experience, status) " +
                        "VALUES ('%s', '%s', '%s', '%s', %s, '%s')",
                name, phone, email, specialty == null ? "" : specialty,
                experience.isEmpty() ? "0" : experience, status
        );

        int result = DB.executeUpdate(sql);
        if (result > 0) {
            showAlert("Success", "Mechanic added successfully");
            clearFields();
            loadMechanics();
        } else {
            showAlert("Error", "Failed to add mechanic");
        }
    }

    private void assignJob(Mechanic mechanic) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>();
        dialog.setTitle("Assign Job");
        dialog.setHeaderText("Assign job to " + mechanic.getName());
        dialog.setContentText("Select vehicle:");

        try {
            ResultSet rs = DB.executeQuery(
                    "SELECT plate_number FROM vehicle WHERE status = 'in_service'"
            );
            while (rs.next()) {
                dialog.getItems().add(rs.getString("plate_number"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        dialog.showAndWait().ifPresent(vehicle -> {
            String sql = String.format(
                    "INSERT INTO assignments (mechanic_id, vehicle_id, assignment_date, status) " +
                            "VALUES (%d, (SELECT vehicle_id FROM vehicle WHERE plate_number = '%s'), NOW(), 'assigned')",
                    mechanic.getMechanicId(), vehicle
            );

            int result = DB.executeUpdate(sql);
            if (result > 0) {
                showAlert("Success", "Job assigned successfully to " + mechanic.getName());
                loadMechanics();
            }
        });
    }

    private void editMechanic(Mechanic mechanic) {
        txtName.setText(mechanic.getName());
        txtPhone.setText(mechanic.getPhone());
        cmbSpecialty.setValue(mechanic.getSpecialty());
        txtExperience.setText(String.valueOf(mechanic.getExperience()));
        cmbStatus.setValue(mechanic.getStatus());

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
                }
            }
        });
    }

    private void clearFields() {
        txtName.clear();
        txtPhone.clear();
        txtEmail.clear();
        cmbSpecialty.setValue(null);
        txtExperience.clear();
        cmbStatus.setValue("Active");
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
        private String specialty;
        private int experience;
        private int activeJobs;
        private String status;

        public Mechanic(int mechanicId, String name, String phone, String specialty,
                        int experience, int activeJobs, String status) {
            this.mechanicId = mechanicId;
            this.name = name;
            this.phone = phone;
            this.specialty = specialty;
            this.experience = experience;
            this.activeJobs = activeJobs;
            this.status = status;
        }

        public int getMechanicId() { return mechanicId; }
        public String getName() { return name; }
        public String getPhone() { return phone; }
        public String getSpecialty() { return specialty; }
        public int getExperience() { return experience; }
        public int getActiveJobs() { return activeJobs; }
        public String getStatus() { return status; }
    }
}