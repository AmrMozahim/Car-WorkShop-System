import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.sql.ResultSet;
import java.time.LocalDate;

public class MechanicWorkHoursTab extends BorderPane {

    private TableView<WorkHour> table = new TableView<>();
    private ObservableList<WorkHour> workHourList = FXCollections.observableArrayList();

    private ComboBox<String> cmbMechanic = new ComboBox<>();
    private DatePicker datePicker = new DatePicker(LocalDate.now());
    private TextField txtHours = new TextField();

    private WorkHour editingWorkHour = null;
    private Button btnAdd;

    public MechanicWorkHoursTab() {
        initialize();
    }

    private void initialize() {
        getStyleClass().add("window-root");

        VBox header = new VBox(8);
        header.getStyleClass().add("window-header");
        header.setPadding(new Insets(20));

        Label title = new Label("Mechanic Work Hours");
        title.getStyleClass().add("window-title");

        Label subtitle = new Label("Track mechanic working hours");
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

        Label formTitle = new Label("Add Work Hours");
        formTitle.getStyleClass().add("form-title");

        // Mechanic Selection
        VBox mechanicBox = new VBox(8);
        mechanicBox.getStyleClass().add("form-group");
        Label lblMechanic = new Label("Mechanic *");
        cmbMechanic.getStyleClass().add("field-combo");
        cmbMechanic.setPromptText("Select mechanic");
        loadMechanics();
        mechanicBox.getChildren().addAll(lblMechanic, cmbMechanic);

        // Date
        VBox dateBox = new VBox(8);
        dateBox.getStyleClass().add("form-group");
        Label lblDate = new Label("Date *");
        datePicker.getStyleClass().add("field-combo");
        dateBox.getChildren().addAll(lblDate, datePicker);

        // Hours
        VBox hoursBox = new VBox(8);
        hoursBox.getStyleClass().add("form-group");
        Label lblHours = new Label("Hours Worked *");
        txtHours.getStyleClass().add("field-input");
        txtHours.setPromptText("8.5");
        hoursBox.getChildren().addAll(lblHours, txtHours);

        // Buttons
        HBox formButtons = new HBox(15);
        formButtons.getStyleClass().add("form-buttons");

        btnAdd = new Button("Add Hours");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setOnAction(e -> addWorkHours());

        Button btnClear = new Button("Clear");
        btnClear.getStyleClass().add("btn-secondary");
        btnClear.setOnAction(e -> clearFields());

        formButtons.getChildren().addAll(btnAdd, btnClear);

        formBox.getChildren().addAll(formTitle, mechanicBox, dateBox, hoursBox, formButtons);
        content.add(formBox, 0, 0);

        // Table
        VBox tableBox = new VBox(15);
        tableBox.getStyleClass().add("table-box");

        HBox tableHeader = new HBox();
        tableHeader.getStyleClass().add("table-header-box");

        Label tableTitle = new Label("Work Hours History");
        tableTitle.getStyleClass().add("table-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnRefresh = new Button("Refresh");
        btnRefresh.getStyleClass().add("btn-refresh");
        btnRefresh.setOnAction(e -> loadWorkHours());

        tableHeader.getChildren().addAll(tableTitle, spacer, btnRefresh);

        createTable();
        table.setPrefHeight(400);

        tableBox.getChildren().addAll(tableHeader, table);
        content.add(tableBox, 1, 0);

        setCenter(content);
        loadWorkHours();
    }

    private void loadMechanics() {
        cmbMechanic.getItems().clear();
        try {
            ResultSet rs = DB.executeQuery("SELECT name FROM mechanic ORDER BY name");
            if (rs != null) {
                while (rs.next()) {
                    cmbMechanic.getItems().add(rs.getString("name"));
                }
                rs.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createTable() {
        table.getColumns().clear();

        TableColumn<WorkHour, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("workId"));
        colId.setPrefWidth(80);

        TableColumn<WorkHour, String> colMechanic = new TableColumn<>("Mechanic");
        colMechanic.setCellValueFactory(new PropertyValueFactory<>("mechanicName"));
        colMechanic.setPrefWidth(150);

        TableColumn<WorkHour, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("workDate"));
        colDate.setPrefWidth(100);

        TableColumn<WorkHour, Double> colHours = new TableColumn<>("Hours");
        colHours.setCellValueFactory(new PropertyValueFactory<>("hoursWorked"));
        colHours.setPrefWidth(100);

        TableColumn<WorkHour, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(120);
        colActions.setCellFactory(param -> new TableCell<WorkHour, Void>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");
            private final HBox buttons = new HBox(8, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().add("btn-table-edit");
                btnDelete.getStyleClass().add("btn-table-delete");
                buttons.getStyleClass().add("table-actions");

                btnEdit.setOnAction(e -> {
                    WorkHour workHour = getTableView().getItems().get(getIndex());
                    editWorkHours(workHour);
                });

                btnDelete.setOnAction(e -> {
                    WorkHour workHour = getTableView().getItems().get(getIndex());
                    deleteWorkHours(workHour);
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

        table.getColumns().addAll(colId, colMechanic, colDate, colHours, colActions);
        table.setItems(workHourList);
    }

    private void loadWorkHours() {
        workHourList.clear();
        try {
            ResultSet rs = DB.executeQuery(
                    "SELECT w.work_id, m.name as mechanic_name, w.work_date, w.hours_worked " +
                            "FROM mechanic_work_hours w " +
                            "JOIN mechanic m ON w.mechanic_id = m.mechanic_id " +
                            "ORDER BY w.work_date DESC"
            );

            if (rs != null) {
                while (rs.next()) {
                    WorkHour workHour = new WorkHour(
                            rs.getInt("work_id"),
                            rs.getString("mechanic_name"),
                            rs.getString("work_date"),
                            rs.getDouble("hours_worked")
                    );
                    workHourList.add(workHour);
                }
                rs.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addWorkHours() {
        if (editingWorkHour != null) {
            updateWorkHours(editingWorkHour);
            return;
        }

        String mechanicName = cmbMechanic.getValue();
        String date = datePicker.getValue().toString();
        String hoursStr = txtHours.getText().trim();

        if (mechanicName == null || mechanicName.isEmpty()) {
            showAlert("Warning", "Please select a mechanic");
            return;
        }

        if (hoursStr.isEmpty()) {
            showAlert("Warning", "Please enter hours worked");
            return;
        }

        try {
            double hours = Double.parseDouble(hoursStr);

            // Get mechanic ID
            ResultSet rs = DB.executeQuery(
                    "SELECT mechanic_id FROM mechanic WHERE name = '" + mechanicName + "'"
            );
            if (rs != null && rs.next()) {
                int mechanicId = rs.getInt("mechanic_id");
                rs.close();

                String sql = String.format(
                        "INSERT INTO mechanic_work_hours (mechanic_id, work_date, hours_worked) VALUES (%d, '%s', %.2f)",
                        mechanicId, date, hours
                );

                int result = DB.executeUpdate(sql);
                if (result > 0) {
                    showAlert("Success", "Work hours added successfully");
                    clearFields();
                    loadWorkHours();
                    Main.refreshDashboardGlobal();
                } else {
                    showAlert("Error", "Failed to add work hours");
                }
            }
        } catch (NumberFormatException e) {
            showAlert("Warning", "Hours must be a number");
        } catch (Exception e) {
            showAlert("Error", "Error adding work hours: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateWorkHours(WorkHour workHour) {
        String mechanicName = cmbMechanic.getValue();
        String date = datePicker.getValue().toString();
        String hoursStr = txtHours.getText().trim();

        if (mechanicName == null || mechanicName.isEmpty()) {
            showAlert("Warning", "Please select a mechanic");
            return;
        }

        if (hoursStr.isEmpty()) {
            showAlert("Warning", "Please enter hours worked");
            return;
        }

        try {
            double hours = Double.parseDouble(hoursStr);

            // Get mechanic ID
            ResultSet rs = DB.executeQuery(
                    "SELECT mechanic_id FROM mechanic WHERE name = '" + mechanicName + "'"
            );
            if (rs != null && rs.next()) {
                int mechanicId = rs.getInt("mechanic_id");
                rs.close();

                String sql = String.format(
                        "UPDATE mechanic_work_hours SET mechanic_id = %d, work_date = '%s', hours_worked = %.2f WHERE work_id = %d",
                        mechanicId, date, hours, workHour.getWorkId()
                );

                int result = DB.executeUpdate(sql);
                if (result > 0) {
                    showAlert("Success", "Work hours updated successfully");
                    clearFields();
                    loadWorkHours();
                    Main.refreshDashboardGlobal();
                } else {
                    showAlert("Error", "Failed to update work hours");
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Error updating work hours: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void editWorkHours(WorkHour workHour) {
        editingWorkHour = workHour;

        cmbMechanic.setValue(workHour.getMechanicName());
        datePicker.setValue(LocalDate.parse(workHour.getWorkDate()));
        txtHours.setText(String.valueOf(workHour.getHoursWorked()));

        btnAdd.setText("Update Hours");
    }

    private void deleteWorkHours(WorkHour workHour) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Work Hours");
        alert.setContentText("Are you sure you want to delete this work hour record?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String sql = "DELETE FROM mechanic_work_hours WHERE work_id = " + workHour.getWorkId();
                int result = DB.executeUpdate(sql);
                if (result > 0) {
                    showAlert("Success", "Work hours deleted successfully");
                    loadWorkHours();
                    Main.refreshDashboardGlobal();
                }
            }
        });
    }

    private void clearFields() {
        editingWorkHour = null;
        btnAdd.setText("Add Hours");
        cmbMechanic.setValue(null);
        datePicker.setValue(LocalDate.now());
        txtHours.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class WorkHour {
        private int workId;
        private String mechanicName;
        private String workDate;
        private double hoursWorked;

        public WorkHour(int workId, String mechanicName, String workDate, double hoursWorked) {
            this.workId = workId;
            this.mechanicName = mechanicName;
            this.workDate = workDate;
            this.hoursWorked = hoursWorked;
        }

        public int getWorkId() { return workId; }
        public String getMechanicName() { return mechanicName; }
        public String getWorkDate() { return workDate; }
        public double getHoursWorked() { return hoursWorked; }
    }
}