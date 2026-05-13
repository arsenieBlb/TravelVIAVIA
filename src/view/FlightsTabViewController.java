package view;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import model.Flight;
import viewmodel.FlightsTabViewModel;
import viewmodel.NavigationAdminViewModel;
import viewmodel.NavigationAdminViewModel.NavigationTab;

import java.time.format.DateTimeFormatter;

public class FlightsTabViewController {
    @FXML private Button flightsNavButton, dashboardNavButton, bookingsNavButton;
    @FXML private Region flightsView, bookingsView, dashboardView;
    @FXML private BookingAdminViewController bookingAdminViewController;
    @FXML private DatePicker dateFilterPicker;

    @FXML private TableView<Flight> flightsTable;
    @FXML private TableColumn<Flight, String> flightIdColumn, carrierColumn, aircraftColumn, routeColumn, departureColumn, arrivalColumn;
    @FXML private TextField originFilterField, destinationFilterField, carrierFilterField;
    @FXML private ComboBox<String> aircraftFilterCombo;
    @FXML private Label authStatusLabel;

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

        if (dashboardView != null) {
            dashboardView.setVisible(showingDashboard);
            dashboardView.setManaged(showingDashboard);
        }

        setActiveNavButton(flightsNavButton, showingFlights);
        setActiveNavButton(bookingsNavButton, showingBookings);
        setActiveNavButton(dashboardNavButton, showingDashboard);
        setActiveNavButton(dashboardNavButton, tab == NavigationTab.DASHBOARD);

        if (showingDashboard) {
            loadDashboardContent();
        }

        if (showingBookings && bookingAdminViewController != null) {
            bookingAdminViewController.refresh();
        }
    }

    private void loadDashboardContent() {
        try {
            Region dashboardContent = viewHandler.loadDashboardView("dashboard_view.fxml");

            if (dashboardView instanceof javafx.scene.layout.Pane pane) {
                pane.getChildren().setAll(dashboardContent);
            }
        } catch (java.io.IOException e) {
            System.err.println("Failed to load dashboard: " + e.getMessage());
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
        viewHandler.openView("ADD_FLIGHT");
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

}
