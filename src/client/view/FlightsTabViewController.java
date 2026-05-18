package client.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import client.model.Flight;
import client.viewmodel.FlightsTabViewModel;
import client.viewmodel.NavigationAdminViewModel;
import client.viewmodel.NavigationAdminViewModel.NavigationTab;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class FlightsTabViewController {
    @FXML private Button flightsNavButton, dashboardNavButton, bookingsNavButton;
    @FXML private Region flightsView, bookingsView, dashboardContainer;
    @FXML private BookingAdminViewController bookingAdminViewController;
    @FXML private DatePicker dateFilterPicker;

    @FXML private TableView<Flight> flightsTable;
    @FXML private TableColumn<Flight, String> flightIdColumn, carrierColumn, aircraftColumn, routeColumn, departureColumn, arrivalColumn;
    @FXML private TextField originFilterField, destinationFilterField, carrierFilterField;
    @FXML private ComboBox<String> aircraftFilterCombo;
    @FXML private Label authStatusLabel;
    @FXML private DashboardViewController dashboardViewController;

    private NavigationAdminViewModel navViewModel;
    private FlightsTabViewModel flightsViewModel;
    private ViewHandler viewHandler;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private Region root;

    public void init(ViewHandler viewHandler, Region root, NavigationAdminViewModel navViewModel) {
        this.viewHandler = viewHandler;
        this.navViewModel = navViewModel;
        this.flightsViewModel = navViewModel.getFlightsTabViewModel();
        this.root = root;

        setupTable();
        setupBindings();
        setupBookingsView();
        setupNavigation();

        if (dashboardViewController != null)
        {
            dashboardViewController.init(navViewModel.getDashboardViewModel(), root, viewHandler);
        }

        ObservableList<String> aircraftModels = FXCollections.observableArrayList();
        aircraftModels.add("All");
        aircraftModels.addAll(navViewModel.getUniqueAircraftModels());
        aircraftFilterCombo.setItems(aircraftModels);
        aircraftFilterCombo.getSelectionModel().select("All");

        if (flightsViewModel.aircraftFilterProperty().get() == null ||
                flightsViewModel.aircraftFilterProperty().get().isEmpty()) {
            aircraftFilterCombo.getSelectionModel().select("All");
        }
    }

    private void setupTable() {
        flightIdColumn.setCellValueFactory(data ->
                new SimpleStringProperty("FL-" + data.getValue().getFlightId()));

        carrierColumn.setCellValueFactory(data -> {
            String name = (data.getValue().getCarrier() != null) ? data.getValue().getCarrier().getName() : "Unknown";
            return new SimpleStringProperty(name);
        });

        aircraftColumn.setCellValueFactory(data -> {
            if (data.getValue().getPlane() != null && data.getValue().getPlane().getPlaneType() != null) {
                return new SimpleStringProperty(data.getValue().getPlane().getPlaneType().getModel());
            }
            return new SimpleStringProperty("N/A");
        });

        routeColumn.setCellValueFactory(data -> {
            String origin = (data.getValue().getDepartureCity() != null) ? data.getValue().getDepartureCity().getCityName() : "???";
            String dest = (data.getValue().getArrivalCity() != null) ? data.getValue().getArrivalCity().getCityName() : "???";
            return new SimpleStringProperty(origin + " → " + dest);
        });

        departureColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDepartureTime().format(formatter)));

        arrivalColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getArrivalTime().format(formatter)));

        flightsTable.setItems(flightsViewModel.getFilteredFlights());
    }

    private void setupBindings() {
        dateFilterPicker.valueProperty().bindBidirectional(flightsViewModel.dateFilterProperty());
        originFilterField.textProperty().bindBidirectional(flightsViewModel.originFilterProperty());
        destinationFilterField.textProperty().bindBidirectional(flightsViewModel.destinationFilterProperty());
        carrierFilterField.textProperty().bindBidirectional(flightsViewModel.carrierFilterProperty());
        aircraftFilterCombo.valueProperty().bindBidirectional(flightsViewModel.aircraftFilterProperty());
        if (authStatusLabel != null)
        {
            authStatusLabel.textProperty().bind(navViewModel.authStatusProperty());
        }
    }

    private void setupNavigation() {
        flightsNavButton.setOnAction(e -> showTab(NavigationTab.FLIGHTS));
        bookingsNavButton.setOnAction(e -> showTab(NavigationTab.BOOKINGS));
        dashboardNavButton.setOnAction(e -> showTab(NavigationTab.DASHBOARD));
        showTab(NavigationTab.FLIGHTS);
    }

    private void setupBookingsView() {
        if (bookingAdminViewController != null) {
            bookingAdminViewController.init(
                    navViewModel.getBookingAdminViewModel());
        }
    }

    private void showTab(NavigationTab tab) {
        navViewModel.navigateTo(tab);

        boolean showingFlights = tab == NavigationTab.FLIGHTS;
        boolean showingBookings = tab == NavigationTab.BOOKINGS;
        boolean showingDashboard = tab == NavigationTab.DASHBOARD;

        flightsView.setVisible(showingFlights);
        flightsView.setManaged(showingFlights);
        bookingsView.setVisible(showingBookings);
        bookingsView.setManaged(showingBookings);

        if (dashboardContainer != null) {
            dashboardContainer.setVisible(showingDashboard);
            dashboardContainer.setManaged(showingDashboard);
        }

        setActiveNavButton(flightsNavButton, showingFlights);
        setActiveNavButton(bookingsNavButton, showingBookings);
        setActiveNavButton(dashboardNavButton, showingDashboard);
        setActiveNavButton(dashboardNavButton, tab == NavigationTab.DASHBOARD);

        if (showingBookings && bookingAdminViewController != null) {
            bookingAdminViewController.refresh();
        }
    }


    private void setActiveNavButton(Button button, boolean active) {
        if (active && !button.getStyleClass().contains("sidebar-link-active")) {
            button.getStyleClass().add("sidebar-link-active");
        } else if (!active) {
            button.getStyleClass().remove("sidebar-link-active");
        }
    }

    public Region getRoot() {
        return root;
    }

    public void clear() {
        if (flightsViewModel != null) {
            flightsViewModel.originFilterProperty().set("");
            flightsViewModel.destinationFilterProperty().set("");
            flightsViewModel.carrierFilterProperty().set("");
            flightsViewModel.aircraftFilterProperty().set("All");
        }
        if (bookingAdminViewController != null) {
            bookingAdminViewController.clearFilters();
        }
    }

    @FXML
    public void clearAllFilters() {
        flightsViewModel.originFilterProperty().set("");
        flightsViewModel.destinationFilterProperty().set("");
        flightsViewModel.carrierFilterProperty().set("");
        flightsViewModel.aircraftFilterProperty().set("All");

        if (flightsViewModel.dateFilterProperty() != null) {
            flightsViewModel.dateFilterProperty().set(null);
        }

        aircraftFilterCombo.getSelectionModel().select("All");
    }

    @FXML
    private void onAddFlightClick()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("add_flight_tab.fxml"));
            Region addFlightRoot = loader.load();

            AddFlightTabController controller = loader.getController();
            controller.init(navViewModel.getAddFlightTabViewModel(), this::refreshTable);

            Stage dialog = new Stage();
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.initOwner(flightsTable.getScene().getWindow());
            dialog.setTitle("Add Flight");
            dialog.setScene(new Scene(addFlightRoot));
            dialog.setOnHidden(event -> refreshTable());
            dialog.showAndWait();
        }
        catch (IOException e)
        {
            showAlert(Alert.AlertType.ERROR, "Error",
                "Could not open add flight view: " + e.getMessage());
        }
    }

    public void refreshTable()
    {
        if (flightsViewModel != null)
        {
            flightsViewModel.refreshFromModel();
        }
        flightsTable.refresh();
    }

    @FXML
    private void onLogoutClick(MouseEvent event)
    {
        if (navViewModel != null)
        {
            navViewModel.logout();
            authStatusLabel.textProperty().unbind();
            authStatusLabel.setText("Not signed in");
        }

        if (viewHandler != null)
        {
            viewHandler.openView("flightScene");
        }
    }

    @FXML
    private void onRemoveFlightClick()
    {
        Flight selected = flightsTable.getSelectionModel().getSelectedItem();
        if (selected == null)
        {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                "Please select a flight to remove.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Remove flight " + selected.getFlightNumber() + "?\n"
            + "This will mark the flight as unavailable for customers.");
        confirm.setTitle("Confirm Removal");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK)
            {
                try
                {
                    flightsViewModel.removeFlight(selected);
                    refreshTable();
                    showAlert(Alert.AlertType.INFORMATION, "Flight Removed",
                        "Flight " + selected.getFlightNumber() + " has been removed.");
                }
                catch (Exception e)
                {
                    showAlert(Alert.AlertType.ERROR, "Error",
                        "Could not remove flight: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void onEditFlightClick()
    {
        Flight selected = flightsTable.getSelectionModel().getSelectedItem();
        if (selected == null)
        {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                "Please select a flight to edit.");
            return;
        }
        openEditFlightDialog(selected);
    }

    private void openEditFlightDialog(Flight flight)
    {
        try
        {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader();
            loader.setLocation(getClass().getResource("EditFlightModalView.fxml"));
            javafx.scene.layout.StackPane modalRoot = loader.load();

            EditFlightDialogController editController = loader.getController();
            editController.init(flight, flightsViewModel, this);

            javafx.scene.Scene scene = flightsTable.getScene();
            javafx.scene.layout.StackPane sceneRoot;

            if (scene.getRoot() instanceof javafx.scene.layout.StackPane sp)
            {
                sceneRoot = sp;
            }
            else
            {
                sceneRoot = new javafx.scene.layout.StackPane(scene.getRoot());
                scene.setRoot(sceneRoot);
            }

            sceneRoot.getChildren().add(modalRoot);
        }
        catch (Exception e)
        {
            showAlert(Alert.AlertType.ERROR, "Error",
                "Could not open edit dialog: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content)
    {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}


