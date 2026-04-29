package view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import viewmodel.FlightSceneViewModel;

public class FlightSceneViewController {
    @FXML private TextField passengerOneFirstNameField;
    @FXML private TextField passengerOneLastNameField;
    @FXML private Spinner<Integer> passengerOneBaggageSpinner;

    @FXML private TextField passengerTwoFirstNameField;
    @FXML private TextField passengerTwoLastNameField;
    @FXML private Spinner<Integer> passengerTwoBaggageSpinner;

    @FXML private Label departureTimeLabel;
    @FXML private Label departureCityLabel;
    @FXML private Label arrivalTimeLabel;
    @FXML private Label arrivalCityLabel;
    @FXML private Label fareTitleLabel;

    private Region root;
    private ViewHandler viewHandler;
    private FlightSceneViewModel flightSceneViewModel;

    public void init(Region root, ViewHandler viewHandler, FlightSceneViewModel flightSceneViewModel)
    {
        this.root = root;
        this.viewHandler = viewHandler;
        this.flightSceneViewModel = flightSceneViewModel;

        departureTimeLabel.textProperty().bind(flightSceneViewModel.departureTimeProperty());
        departureCityLabel.textProperty().bind(flightSceneViewModel.departureCityProperty());
        arrivalTimeLabel.textProperty().bind(flightSceneViewModel.arrivalTimeProperty());
        arrivalCityLabel.textProperty().bind(flightSceneViewModel.arrivalCityProperty());
        passengerOneFirstNameField.textProperty().bindBidirectional(flightSceneViewModel.passengerOneFirstNameProperty());
        passengerOneLastNameField.textProperty().bindBidirectional(flightSceneViewModel.passengerOneLastNameProperty());
        passengerOneBaggageSpinner.getValueFactory().valueProperty().bindBidirectional(flightSceneViewModel.passengerOneBaggageCountProperty().asObject());
        passengerTwoFirstNameField.textProperty().bindBidirectional(flightSceneViewModel.PassengerTwoFirstNameProperty());
        passengerTwoLastNameField.textProperty().bindBidirectional(flightSceneViewModel.PassengerTwoLastNameProperty());
        passengerTwoBaggageSpinner.getValueFactory().valueProperty().bindBidirectional(flightSceneViewModel.PassengerTwoBaggageCountProperty().asObject());
        fareTitleLabel.textProperty().bind(flightSceneViewModel.totalPriceProperty().asString("Total: %.2f Eur"));
    }

    public Region getRoot()
    {
        return root;
    }

    public void reset()
    {
        flightSceneViewModel.clear();
    }

    public void confirmButton()
    {
        flightSceneViewModel.confirmBooking();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Booking confirmed successfully!");
        alert.showAndWait();

        //will open a view later / will be modified
    }

    @FXML
    public void cancelButton()
    {
        flightSceneViewModel.clear();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Cancellation");
        alert.setHeaderText(null);
        alert.setContentText("Booking cancelled successfully!");
        alert.showAndWait();

        //will open a view later / will be modified
    }

    @FXML
    public void passengerTwoSegmentTwoChooseSeatButton()
    {
        //will open another view later
    }

    @FXML
    public void passengerTwoSegmentOneChooseSeatButton()
    {
        //will open another view
    }

    @FXML
    public void passengerOneSegmentTwoChooseSeatButton()
    {
        //will open another view
    }

    @FXML
    public void passengerOneSegmentOneChooseSeatButton()
    {
        //will open another view
    }

    @FXML
    public void loginButton()
    {
        //will open another view
    }
}
