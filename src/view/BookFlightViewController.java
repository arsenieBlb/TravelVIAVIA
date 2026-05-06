package view;

import javafx.fxml.FXML;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import model.City;
import model.Flight;
import viewmodel.BookFlightViewModel;

import java.time.format.DateTimeFormatter;

public class BookFlightViewController {

    @FXML private ComboBox<City> originCombo;
    @FXML private ComboBox<City> destinationCombo;
    @FXML private ComboBox<String> sortByCombo;
    @FXML private ComboBox<String> airlineCombo;
    @FXML private CheckBox directOnlyCheck;
    @FXML private TableView<Flight> flightsTable;
    @FXML private TableColumn<Flight, String> routeColumn;
    @FXML private TableColumn<Flight, String> departureColumn;
    @FXML private TableColumn<Flight, String> durationColumn;
    @FXML private TableColumn<Flight, String> carrierColumn;
    @FXML private TableColumn<Flight, String> typeColumn;
    @FXML private TableColumn<Flight, String> priceColumn;
    @FXML private Label resultCountLabel;
    @FXML private VBox flightSummaryCard;
    @FXML private Label detailRouteLabel;
    @FXML private Label detailDateLabel;
    @FXML private Label detailInfoLabel;
    @FXML private Label detailPriceLabel;
    @FXML private Label detailAircraftLabel;
    @FXML private Button searchFlightsButton;
    @FXML private Button resetFiltersButton;
    @FXML private Button continueButton;
    private Region root;
    private ViewHandler viewHandler;
    private BookFlightViewModel viewModel;
    @FXML private Label passengerLabel;
    @FXML private Button decreasePassengersButton;
    @FXML private DatePicker travelDatePicker;

    public void init(BookFlightViewModel viewModel, Region root, ViewHandler viewHandler) {
        this.viewModel = viewModel;
        this.root = root;
        this.viewHandler = viewHandler;

        setupCityConverter();
        setupFlightTable();

        originCombo.setItems(viewModel.getAllCities());
        destinationCombo.setItems(viewModel.getFilteredDestinations());
        sortByCombo.setItems(viewModel.getSortOptions());
        airlineCombo.setItems(viewModel.getAirlines());

        originCombo.valueProperty().bindBidirectional(viewModel.departureCityProperty());
        destinationCombo.valueProperty().bindBidirectional(viewModel.arrivalCityProperty());
        sortByCombo.valueProperty().bindBidirectional(viewModel.selectedSortProperty());
        airlineCombo.valueProperty().bindBidirectional(viewModel.selectedAirlineProperty());
        directOnlyCheck.selectedProperty().bindBidirectional(viewModel.directOnlyProperty());

        flightsTable.setItems(viewModel.getFilteredFlights());
        flightsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldFlight, newFlight) -> viewModel.setSelectedFlight(newFlight));
        viewModel.selectedFlightProperty().addListener((obs, oldFlight, newFlight) -> {
            if (flightsTable.getSelectionModel().getSelectedItem() != newFlight) {
                flightsTable.getSelectionModel().select(newFlight);
            }
            updateFlightSummary(newFlight);
        });

        passengerLabel.textProperty().bind(viewModel.passengerCountProperty().asString());

        decreasePassengersButton.disableProperty().bind(
                viewModel.passengerCountProperty().lessThanOrEqualTo(1)
        );

        travelDatePicker.valueProperty().bindBidirectional(viewModel.travelDateProperty());
        resultCountLabel.textProperty().bind(Bindings.size(viewModel.getFilteredFlights()).asString());
        continueButton.disableProperty().bind(viewModel.selectedFlightProperty().isNull());

        searchFlightsButton.setOnAction(event -> viewModel.searchFlights());
        resetFiltersButton.setOnAction(event -> viewModel.clear());
        continueButton.setOnAction(event -> viewHandler.showPassengerDetails());
        updateFlightSummary(viewModel.getSelectedFlight());
    }

    private void setupFlightTable() {
        flightsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        routeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDepartureCity().getCityName() + " -> "
                        + cellData.getValue().getArrivalCity().getCityName()));
        departureColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDepartureTime()
                        .format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"))));
        durationColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDurationString()));
        carrierColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCarrier().getName() + "\n"
                        + cellData.getValue().getFlightNumber()));
        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty("Direct"));
        priceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                String.format("EUR %.0f", cellData.getValue().getBasePrice())));
    }

    private void updateFlightSummary(Flight flight) {
        boolean hasFlight = flight != null;
        flightSummaryCard.setVisible(hasFlight);
        flightSummaryCard.setManaged(hasFlight);

        if (!hasFlight) {
            return;
        }

        detailRouteLabel.setText(flight.getDepartureCity().getCityName() + " -> "
                + flight.getArrivalCity().getCityName());
        detailDateLabel.setText(flight.getDepartureTime()
                .format(DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm")));
        detailInfoLabel.setText(flight.getDurationString() + " - Direct");
        detailPriceLabel.setText(String.format("EUR %.0f", flight.getBasePrice()));
        detailAircraftLabel.setText(flight.getPlane().getPlaneType().getModel());
    }

    private void setupCityConverter() {
        StringConverter<City> cityConverter = new StringConverter<>() {
            @Override
            public String toString(City city) {
                return (city == null) ? "" : city.getCityName();
            }
            @Override
            public City fromString(String string) { return null; }
        };
        originCombo.setConverter(cityConverter);
        destinationCombo.setConverter(cityConverter);
    }

    public Region getRoot()
    {
        return root;
    }

    public void reset()
    {
        viewModel.clear();
    }
    @FXML
    private void onIncrementClick() {
        viewModel.incrementPassengers();
    }

    @FXML
    private void onDecrementClick() {
        viewModel.decrementPassengers();
    }
}
