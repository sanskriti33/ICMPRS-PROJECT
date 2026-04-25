import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class MainFX extends Application {
    private final ComplaintManager manager = new ComplaintManager();
    private final ObservableList<Complaint> complaints = FXCollections.observableArrayList();

    private final Label storageLabel = new Label();
    private final Label totalValue = new Label("0");
    private final Label highValue = new Label("0");
    private final Label openValue = new Label("0");
    private final Label resolvedValue = new Label("0");
    private final Label previewLabel = new Label("Priority: 0 | Level: - | Department: -");

    private final TextField complaintIdField = new TextField();
    private final TextArea complaintDescriptionArea = new TextArea();
    private final TextField searchIdField = new TextField();
    private final Label searchResultLabel = new Label("Search results will appear here.");
    private final TextField updateIdField = new TextField();
    private final ComboBox<String> updateStatusBox = new ComboBox<>();
    private final TextField deleteIdField = new TextField();

    private final TableView<Complaint> tableView = new TableView<>();

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");
        root.setTop(createHeader());
        root.setCenter(createBody());
        root.setBottom(createFooter());

        Scene scene = new Scene(root, 1380, 980);
        scene.getStylesheets().add(getClass().getResource("javafx-theme.css").toExternalForm());

        stage.setTitle("ICMPRS - JavaFX Customer Complaint Portal");
        stage.setScene(scene);
        stage.setMinWidth(1200);
        stage.setMinHeight(850);
        stage.show();

        refreshTable();
    }

    private VBox createHeader() {
        VBox header = new VBox(14);
        header.getStyleClass().add("hero");
        header.setPadding(new Insets(24));

        HBox topLine = new HBox();
        Label portalTag = new Label("ICMPRS JAVAFX PORTAL");
        portalTag.getStyleClass().add("portal-tag");
        storageLabel.setText("Storage: " + manager.getStorageName());
        storageLabel.getStyleClass().add("storage-label");
        topLine.getChildren().addAll(portalTag, spacer(), storageLabel);

        Label title = new Label("Customer Complaint Portal");
        title.getStyleClass().add("hero-title");

        Label subtitle = new Label("Submit complaints, auto-calculate priority, route them to the right department, and track status from one JavaFX screen.");
        subtitle.getStyleClass().add("hero-subtitle");
        subtitle.setWrapText(true);

        header.getChildren().addAll(topLine, title, subtitle);
        return header;
    }

    private VBox createBody() {
        VBox body = new VBox(18);
        body.setPadding(new Insets(20));
        body.getChildren().addAll(createStatsRow(), createMainContent());
        return body;
    }

    private HBox createStatsRow() {
        HBox row = new HBox(16);
        row.getChildren().addAll(
                statCard("Total", totalValue, "navy-card"),
                statCard("High Priority", highValue, "red-card"),
                statCard("Open Cases", openValue, "orange-card"),
                statCard("Resolved", resolvedValue, "green-card")
        );
        return row;
    }

    private HBox createMainContent() {
        HBox content = new HBox(18);
        VBox.setVgrow(content, Priority.ALWAYS);

        VBox leftPanel = createLeftPanel();
        leftPanel.setPrefWidth(390);

        VBox rightPanel = createRightPanel();
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        content.getChildren().addAll(leftPanel, rightPanel);
        return content;
    }

    private VBox createLeftPanel() {
        VBox panel = panelCard();

        Label heading = sectionHeading("Submit New Complaint");

        complaintIdField.setPromptText("Enter complaint ID");
        complaintDescriptionArea.setPromptText("Write the complaint here. Example:\nPayment failed and refund is urgent.");
        complaintDescriptionArea.setWrapText(true);
        complaintDescriptionArea.setPrefRowCount(7);
        complaintDescriptionArea.textProperty().addListener((obs, oldText, newText) -> updatePreview());

        Button submitButton = darkButton("Submit Complaint");
        submitButton.setOnAction(event -> addComplaint());

        Button clearButton = softButton("Clear Form");
        clearButton.setOnAction(event -> clearForm());

        VBox helpBox = infoBox(
                "How to explain this screen",
                "Users submit a complaint here. The system calculates priority, routes it to the correct department, stores it in MySQL, and later the admin updates its status."
        );

        panel.getChildren().addAll(
                heading,
                fieldLabel("Complaint ID"),
                complaintIdField,
                fieldLabel("Complaint Description"),
                complaintDescriptionArea,
                previewLabel,
                submitButton,
                clearButton,
                helpBox
        );
        return panel;
    }

    private VBox createRightPanel() {
        VBox panel = panelCard();
        VBox.setVgrow(panel, Priority.ALWAYS);

        HBox titleRow = new HBox();
        Label heading = sectionHeading("Complaint Records");
        Button refreshButton = softButton("Refresh");
        refreshButton.setOnAction(event -> refreshTable());
        titleRow.getChildren().addAll(heading, spacer(), refreshButton);

        configureTable();
        VBox.setVgrow(tableView, Priority.ALWAYS);

        HBox managementRow = new HBox(16, createSearchCard(), createUpdateCard(), createDeleteCard());
        HBox.setHgrow(managementRow.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(managementRow.getChildren().get(1), Priority.ALWAYS);
        HBox.setHgrow(managementRow.getChildren().get(2), Priority.ALWAYS);

        panel.getChildren().addAll(titleRow, tableView, managementRow);
        return panel;
    }

    private VBox createSearchCard() {
        VBox box = subCard();
        Label heading = subHeading("Search Complaint");
        searchIdField.setPromptText("Search by ID");

        Button searchButton = darkButton("Find Complaint");
        searchButton.setOnAction(event -> searchComplaint());

        searchResultLabel.getStyleClass().add("result-box");
        searchResultLabel.setWrapText(true);
        searchResultLabel.setMinHeight(120);

        box.getChildren().addAll(
                heading,
                fieldLabel("Complaint ID"),
                searchIdField,
                searchButton,
                searchResultLabel
        );
        return box;
    }

    private VBox createUpdateCard() {
        VBox box = subCard();
        Label heading = subHeading("Update Status");
        updateIdField.setPromptText("Enter complaint ID");
        updateStatusBox.getItems().addAll("Open", "In Progress", "Resolved", "Closed");
        updateStatusBox.setValue("Open");

        Button updateButton = successButton("Change Status");
        updateButton.setOnAction(event -> updateStatus());

        box.getChildren().addAll(
                heading,
                fieldLabel("Complaint ID"),
                updateIdField,
                fieldLabel("New Status"),
                updateStatusBox,
                updateButton
        );
        return box;
    }

    private VBox createDeleteCard() {
        VBox box = subCard();
        Label heading = subHeading("Delete Complaint");
        deleteIdField.setPromptText("Enter complaint ID");

        Button deleteButton = dangerButton("Delete Complaint");
        deleteButton.setOnAction(event -> deleteComplaint());

        box.getChildren().addAll(
                heading,
                fieldLabel("Complaint ID"),
                deleteIdField,
                deleteButton
        );
        return box;
    }

    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setPadding(new Insets(0, 20, 18, 20));
        Label text = new Label("Priority = urgency + impact | Auto-routing: Finance, IT Support, Logistics, Product Team, General Support | Desktop UI built using JavaFX");
        text.getStyleClass().add("footer-text");
        footer.getChildren().add(text);
        return footer;
    }

    private void configureTable() {
        tableView.setItems(complaints);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tableView.getStyleClass().add("complaint-table");

        TableColumn<Complaint, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getId()));

        TableColumn<Complaint, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getDescription()));

        TableColumn<Complaint, Number> priorityCol = new TableColumn<>("Priority");
        priorityCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getPriority()));

        TableColumn<Complaint, String> levelCol = new TableColumn<>("Level");
        levelCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getPriorityLevel()));

        TableColumn<Complaint, String> departmentCol = new TableColumn<>("Department");
        departmentCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getDepartment()));

        TableColumn<Complaint, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getStatus()));

        TableColumn<Complaint, String> highFlagCol = new TableColumn<>("High Flag");
        highFlagCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().isHighPriority() ? "1" : "0"));

        TableColumn<Complaint, String> createdCol = new TableColumn<>("Created");
        createdCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getFormattedCreatedAt()));

        tableView.getColumns().setAll(idCol, descCol, priorityCol, levelCol, departmentCol, statusCol, highFlagCol, createdCol);
    }

    private void updatePreview() {
        String description = complaintDescriptionArea.getText().trim();
        if (description.isEmpty()) {
            previewLabel.setText("Priority: 0 | Level: - | Department: -");
            return;
        }
        int previewPriority = PriorityCalculator.preview(description);
        String level = PriorityCalculator.getPriorityLevel(previewPriority);
        String department = DepartmentRouter.detectDepartment(description);
        previewLabel.setText("Priority: " + previewPriority + " | Level: " + level + " | Department: " + department);
    }

    private void addComplaint() {
        try {
            int id = parseId(complaintIdField.getText());
            String description = complaintDescriptionArea.getText().trim();
            if (description.isBlank()) {
                showError("Complaint description cannot be empty.");
                return;
            }
            manager.addComplaint(new Complaint(id, description));
            showInfo("Complaint added successfully.");
            clearForm();
            refreshTable();
        } catch (NumberFormatException exception) {
            showError("Please enter a valid numeric complaint ID.");
        } catch (Exception exception) {
            showError(exception.getMessage());
        }
    }

    private void clearForm() {
        complaintIdField.clear();
        complaintDescriptionArea.clear();
        updatePreview();
    }

    private void searchComplaint() {
        try {
            int id = parseId(searchIdField.getText());
            Optional<Complaint> result = manager.searchComplaint(id);
            if (result.isPresent()) {
                Complaint complaint = result.get();
                searchResultLabel.setText(
                        "ID: " + complaint.getId() + "\n" +
                        "Description: " + complaint.getDescription() + "\n" +
                        "Priority: " + complaint.getPriority() + " (" + complaint.getPriorityLevel() + ")\n" +
                        "Department: " + complaint.getDepartment() + "\n" +
                        "Status: " + complaint.getStatus() + "\n" +
                        "Created: " + complaint.getFormattedCreatedAt()
                );
            } else {
                searchResultLabel.setText("Complaint not found.");
            }
        } catch (NumberFormatException exception) {
            showError("Please enter a valid numeric complaint ID.");
        } catch (Exception exception) {
            showError(exception.getMessage());
        }
    }

    private void updateStatus() {
        try {
            int id = parseId(updateIdField.getText());
            boolean updated = manager.updateStatus(id, updateStatusBox.getValue());
            if (updated) {
                showInfo("Complaint status updated successfully.");
                refreshTable();
            } else {
                showError("Complaint ID not found.");
            }
        } catch (NumberFormatException exception) {
            showError("Please enter a valid numeric complaint ID.");
        } catch (Exception exception) {
            showError(exception.getMessage());
        }
    }

    private void deleteComplaint() {
        try {
            int id = parseId(deleteIdField.getText());
            boolean deleted = manager.deleteComplaint(id);
            if (deleted) {
                showInfo("Complaint deleted successfully.");
                deleteIdField.clear();
                refreshTable();
            } else {
                showError("Complaint ID not found.");
            }
        } catch (NumberFormatException exception) {
            showError("Please enter a valid numeric complaint ID.");
        } catch (Exception exception) {
            showError(exception.getMessage());
        }
    }

    private void refreshTable() {
        try {
            List<Complaint> all = manager.viewComplaints();
            complaints.setAll(all);
            long high = all.stream().filter(Complaint::isHighPriority).count();
            long open = all.stream().filter(c -> "Open".equalsIgnoreCase(c.getStatus()) || "In Progress".equalsIgnoreCase(c.getStatus())).count();
            long resolved = all.stream().filter(c -> "Resolved".equalsIgnoreCase(c.getStatus())).count();

            totalValue.setText(String.valueOf(all.size()));
            highValue.setText(String.valueOf(high));
            openValue.setText(String.valueOf(open));
            resolvedValue.setText(String.valueOf(resolved));
            storageLabel.setText("Storage: " + manager.getStorageName());
        } catch (Exception exception) {
            showError(exception.getMessage());
        }
    }

    private int parseId(String text) {
        return Integer.parseInt(text.trim());
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("ICMPRS");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("ICMPRS");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private VBox panelCard() {
        VBox box = new VBox(14);
        box.getStyleClass().add("panel-card");
        box.setPadding(new Insets(20));
        return box;
    }

    private VBox subCard() {
        VBox box = new VBox(12);
        box.getStyleClass().add("sub-card");
        box.setPadding(new Insets(16));
        VBox.setVgrow(box, Priority.ALWAYS);
        return box;
    }

    private StackPane statCard(String title, Label valueLabel, String styleClass) {
        StackPane card = new StackPane();
        card.getStyleClass().addAll("stat-card", styleClass);
        card.setPadding(new Insets(18));
        card.setMinHeight(120);
        card.setPrefWidth(300);

        VBox content = new VBox(8);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-title");
        valueLabel.getStyleClass().add("stat-value");
        content.getChildren().addAll(titleLabel, valueLabel);
        card.getChildren().add(content);
        StackPane.setAlignment(content, Pos.CENTER_LEFT);
        return card;
    }

    private VBox infoBox(String title, String body) {
        VBox box = new VBox(10);
        box.getStyleClass().add("info-box");
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("info-title");
        Label bodyLabel = new Label(body);
        bodyLabel.setWrapText(true);
        bodyLabel.getStyleClass().add("info-body");
        box.getChildren().addAll(titleLabel, bodyLabel);
        return box;
    }

    private Label sectionHeading(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-heading");
        return label;
    }

    private Label subHeading(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("sub-heading");
        return label;
    }

    private Label fieldLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("field-label");
        return label;
    }

    private Button darkButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("dark-button");
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    private Button softButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("soft-button");
        return button;
    }

    private Button successButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("success-button");
        return button;
    }

    private Button dangerButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("danger-button");
        return button;
    }

    private RegionSpacer spacer() {
        return new RegionSpacer();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static class RegionSpacer extends HBox {
        RegionSpacer() {
            HBox.setHgrow(this, Priority.ALWAYS);
        }
    }
}
