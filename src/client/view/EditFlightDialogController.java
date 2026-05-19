package client.view;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import client.model.Carrier;
import client.model.Flight;
import client.model.Plane;
import client.viewmodel.FlightsTabViewModel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class EditFlightDialogController
{
    @FXML private Label dialogTitleLabel;
    @FXML private TextField flightIdField;
    @FXML private ComboBox<Carrier> carrierCombo;
    @FXML private ComboBox<Plane> aircraftCombo;
    @FXML private Label economySeatsLabel;
    @FXML private Label businessSeatsLabel;
    @FXML private ComboBox<client.model.City> originCombo;
    @FXML private ComboBox<client.model.City> destinationCombo;
    @FXML private TextField departureField;
    @FXML private TextField arrivalField;
    @FXML private TextField economyPriceField;
    @FXML private TextField businessPriceField;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;

    private Flight originalFlight;
    private FlightsTabViewModel viewModel;
    private FlightsTabViewController parentController;

    private static final DateTimeFormatter DISPLAY_FORMAT =
        DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a");
    private static final DateTimeFormatter PARSE_FORMAT =
        DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a");

    public void init(Flight flight, FlightsTabViewModel viewModel,
        FlightsTabViewController parentController)
    {
        this.originalFlight = flight;
        this.viewModel = viewModel;
        this.parentController = parentController;

        populateFields();

        cancelButton.setOnAction(event -> closeModal());
        saveButton.setOnAction(event -> saveChanges());
    }

    private void populateFields()
    {
        flightIdField.setText(originalFlight.getFlightNumber());
        flightIdField.setEditable(false); // Locked because DB uses FL-ID auto generation

        carrierCombo.getItems().setAll(viewModel.getCarriers());
        carrierCombo.setValue(originalFlight.getCarrier());

        aircraftCombo.getItems().setAll(viewModel.getPlanes());
        if (originalFlight.getPlane() != null)
        {
            aircraftCombo.setValue(originalFlight.getPlane());
        }

        if (originalFlight.getPlane() != null
            && originalFlight.getPlane().getPlaneType() != null)
        {
            economySeatsLabel.setText(String.valueOf(
                originalFlight.getPlane().getPlaneType().getNumberOfEconomySeats()));
            businessSeatsLabel.setText(String.valueOf(
                originalFlight.getPlane().getPlaneType().getNumberOfBusinessSeats()));
        }

        aircraftCombo.valueProperty().addListener((obs, oldPlane, newPlane) ->
        {
            if (newPlane != null)
            {
                carrierCombo.setValue(newPlane.getCarrier());
                if (newPlane.getPlaneType() != null) {
                    economySeatsLabel.setText(String.valueOf(newPlane.getPlaneType().getNumberOfEconomySeats()));
                    businessSeatsLabel.setText(String.valueOf(newPlane.getPlaneType().getNumberOfBusinessSeats()));
                }
            }
        });

        originCombo.getItems().setAll(viewModel.getCities());
        originCombo.setValue(originalFlight.getDepartureCity());

        destinationCombo.getItems().setAll(viewModel.getCities());
        destinationCombo.setValue(originalFlight.getArrivalCity());

        departureField.setText(
            originalFlight.getDepartureTime().format(DISPLAY_FORMAT));
        arrivalField.setText(
            originalFlight.getArrivalTime().format(DISPLAY_FORMAT));
        economyPriceField.setText(
            String.format("%.0f", originalFlight.getBasePrice()));
        businessPriceField.setText(
            String.format("%.0f", originalFlight.getBasePrice() * 1.5));
    }

    private void saveChanges()
    {
        try
        {
            LocalDateTime newDeparture = LocalDateTime.parse(
                departureField.getText().trim(), PARSE_FORMAT);
            LocalDateTime newArrival = LocalDateTime.parse(
                arrivalField.getText().trim(), PARSE_FORMAT);

            if (newArrival.isBefore(newDeparture))
            {
                showAlert("Arrival time cannot be before departure time.");
                return;
            }

            double newBasePrice;
            try
            {
                newBasePrice = Double.parseDouble(
                    economyPriceField.getText().trim());
            }
            catch (NumberFormatException e)
            {
                showAlert("Economy price must be a valid number.");
                return;
            }

            Flight updatedFlight = new Flight(
                originalFlight.getFlightId(),
                flightIdField.getText().trim(),
                newDeparture,
                newArrival,
                newBasePrice,
                carrierCombo.getValue(),
                aircraftCombo.getValue(),
                originCombo.getValue(),
                destinationCombo.getValue());

            viewModel.editFlight(updatedFlight);
            parentController.refreshTable();

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Flight Updated");
            success.setHeaderText(null);
            success.setContentText("Flight " + originalFlight.getFlightNumber()
                + " has been updated.");
            success.showAndWait();

            closeModal();
        }
        catch (DateTimeParseException e)
        {
            showAlert("Please use the format MM/dd/yyyy hh:mm AM/PM for dates.");
        }
        catch (Exception e)
        {
            showAlert("Could not save changes: " + e.getMessage());
        }
    }

    private void closeModal()
    {
        StackPane modal = (StackPane) cancelButton.getScene().getRoot();
        if (modal.getChildren().size() > 1)
        {
            modal.getChildren().remove(
                modal.getChildren().size() - 1);
        }
    }

    private void showAlert(String message)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
