package client.view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import client.model.Carrier;
import client.model.City;
import client.model.Plane;
import client.viewmodel.AddFlightTabViewModel;

public class AddFlightTabController
{
    @FXML private TextField flightIdField;
    @FXML private ComboBox<Carrier> carrierCombo;
    @FXML private ComboBox<Plane> aircraftCombo;
    @FXML private ComboBox<City> originCombo;
    @FXML private ComboBox<City> destinationCombo;
    @FXML private TextField departureField;
    @FXML private TextField arrivalField;
    @FXML private TextField economyPriceField;
    @FXML private TextField businessPriceField;
    @FXML private Button saveFlightButton;
    @FXML private Button cancelButton;

    private AddFlightTabViewModel viewModel;
    private Runnable onSaved;

    public void init(AddFlightTabViewModel viewModel)
    {
        init(viewModel, null);
    }

    public void init(AddFlightTabViewModel viewModel, Runnable onSaved)
    {
        this.viewModel = viewModel;
        this.onSaved = onSaved;

        viewModel.refreshData();

        flightIdField.setEditable(false);
        flightIdField.setFocusTraversable(false);
        flightIdField.setStyle("-fx-background-color: #eeeeee; -fx-text-fill: #6e6e6e;");

        carrierCombo.setItems(viewModel.carriersList);
        aircraftCombo.setItems(viewModel.planesList);
        originCombo.setItems(viewModel.citiesList);
        destinationCombo.setItems(viewModel.citiesList);

        flightIdField.textProperty().bindBidirectional(viewModel.flightId);
        departureField.textProperty().bindBidirectional(viewModel.departureTimeStr);
        arrivalField.textProperty().bindBidirectional(viewModel.arrivalTimeStr);
        economyPriceField.textProperty().bindBidirectional(viewModel.economyPrice);
        businessPriceField.textProperty().bindBidirectional(viewModel.businessPrice);

        carrierCombo.valueProperty().bindBidirectional(viewModel.selectedCarrier);
        aircraftCombo.valueProperty().bindBidirectional(viewModel.selectedPlane);
        originCombo.valueProperty().bindBidirectional(viewModel.selectedOrigin);
        destinationCombo.valueProperty().bindBidirectional(viewModel.selectedDestination);

        aircraftCombo.valueProperty().addListener((obs, oldPlane, newPlane) ->
        {
            if (newPlane != null)
            {
                viewModel.selectedCarrier.set(newPlane.getCarrier());
            }
        });
    }

    @FXML
    private void onSaveFlight()
    {
        try
        {
            saveFlightButton.setDisable(true);
            viewModel.addFlight();

            if (onSaved != null)
            {
                onSaved.run();
            }
            closeWindow(saveFlightButton);
        }
        catch (Exception e)
        {
            saveFlightButton.setDisable(false);
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error saving flight: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void onCancel()
    {
        closeWindow(cancelButton);
    }

    private void closeWindow(Button source)
    {
        ((Stage) source.getScene().getWindow()).close();
    }
}

