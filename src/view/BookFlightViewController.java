package view;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import model.City;
import model.Flight;
import viewmodel.BookFlightViewModel;
import viewmodel.FlightSceneViewModel;

public class BookFlightViewController {

    @FXML private ComboBox<City> originCombo;
    @FXML private ComboBox<City> destinationCombo;
    @FXML private TableView<Flight> flightsTable;
    private Region root;
    private ViewHandler viewHandler;
    private BookFlightViewModel viewModel;
    @FXML private Label passengerLabel;
    @FXML private Button decreasePassengersButton;
    @FXML private DatePicker travelDatePicker;
    @FXML private Button searchFlightsButton;
    @FXML private TableColumn<Flight, String> routeColumn;
    @FXML private TableColumn<Flight, String> departureColumn;
    @FXML private TableColumn<Flight, String> carrierColumn;
    @FXML private TableColumn<Flight, Double> priceColumn;
    @FXML private VBox flightSummaryCard;
    @FXML private Label detailRouteLabel;
    @FXML private Label detailDateLabel;
    @FXML private Label detailPriceLabel;
    @FXML private Label detailAircraftLabel;
    @FXML private Label detailInfoLabel;
    @FXML private Button continueButton;
    @FXML private Label resultCountLabel;
    private final IntegerProperty resultCount = new SimpleIntegerProperty(0);

    public void init(BookFlightViewModel viewModel, Region root, ViewHandler viewHandler) {
        this.viewModel = viewModel;
        this.root = root;
        this.viewHandler = viewHandler;

        setupCityConverter();

        originCombo.setItems(viewModel.getAllCities());
        destinationCombo.setItems(viewModel.getFilteredDestinations());

        originCombo.valueProperty().bindBidirectional(viewModel.departureCityProperty());
        destinationCombo.valueProperty().bindBidirectional(viewModel.arrivalCityProperty());

        flightsTable.setItems(viewModel.getFilteredFlights());
        passengerLabel.textProperty().bind(viewModel.passengerCountProperty().asString());

        decreasePassengersButton.disableProperty().bind(
                viewModel.passengerCountProperty().lessThanOrEqualTo(1)
        );

        travelDatePicker.valueProperty().bindBidirectional(viewModel.travelDateProperty());
        searchFlightsButton.setOnAction(e -> onSearchClick());

        routeColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDepartureCity().getCityName() + " -> " +
                        data.getValue().getArrivalCity().getCityName()));
        departureColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDepartureTime().toString()));
        carrierColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCarrier().getCarrierName()));
        priceColumn.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().getBasePrice()));

        flightsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showFlightSummary(newVal);
            } else {
                flightSummaryCard.setVisible(false);
                flightSummaryCard.setManaged(false);
            }
        });

        resultCountLabel.textProperty().bind(viewModel.resultCountProperty().asString());
    }

    private void showFlightSummary(Flight flight) {
        viewModel.selectedFlightProperty().set(flight);

        detailRouteLabel.setText(flight.getDepartureCity().getCityName() + " -> " + flight.getArrivalCity().getCityName());
        detailDateLabel.setText(flight.getDepartureTime().toString());
        detailPriceLabel.setText("EUR " + flight.getBasePrice());
        detailAircraftLabel.setText(flight.getFlightNumber() + " " + flight.getFlightId());
        detailInfoLabel.setText("Direct Flight");

        flightSummaryCard.setVisible(true);
        flightSummaryCard.setManaged(true);
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

    @FXML
    private void onSearchClick() {
        if (flightsTable.getItems() != viewModel.getFilteredFlights()) {
            flightsTable.setItems(viewModel.getFilteredFlights());
        }

        viewModel.searchFlights();

        resultCountLabel.textProperty().unbind();
        resultCountLabel.textProperty().bind(viewModel.resultCountProperty().asString());

        flightsTable.refresh();
    }

    public IntegerProperty resultCountProperty() {
        return resultCount;
    }
}