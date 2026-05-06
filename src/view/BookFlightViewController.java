package view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
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