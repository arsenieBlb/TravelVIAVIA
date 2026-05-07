package view;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Flight;
import viewmodel.NavigationAdminViewModel;
import viewmodel.FlightsTabViewModel;

import java.time.format.DateTimeFormatter;

public class NavigationAdminViewController {
    @FXML private Button flightsNavButton, dashboardNavButton, bookingsNavButton;
    @FXML private VBox flightsView;
    @FXML private DatePicker dateFilterPicker;

    @FXML private TableView<Flight> flightsTable;
    @FXML private TableColumn<Flight, String> flightIdColumn, carrierColumn, aircraftColumn, routeColumn, departureColumn, arrivalColumn;
    @FXML private TextField originFilterField, destinationFilterField, carrierFilterField;
    @FXML private ComboBox<String> aircraftFilterCombo;

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
    }

    private void setupNavigation() {
        flightsNavButton.setOnAction(e -> flightsView.setVisible(true));
    }

    public Region getRoot() {
        return root;
    }

    public void clear() {
        flightsViewModel.originFilterProperty().set("");
        flightsViewModel.destinationFilterProperty().set("");
        flightsViewModel.carrierFilterProperty().set("");
        flightsViewModel.aircraftFilterProperty().set("All");
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
}