package client.viewmodel;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import client.model.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AddFlightTabViewModel
{
    private Model model;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ObservableList<City> citiesList = FXCollections.observableArrayList();
    public ObservableList<Plane> planesList = FXCollections.observableArrayList();
    public ObservableList<Carrier> carriersList = FXCollections.observableArrayList();

    public StringProperty flightId = new SimpleStringProperty("");
    public ObjectProperty<Carrier> selectedCarrier = new SimpleObjectProperty<>();
    public ObjectProperty<Plane> selectedPlane = new SimpleObjectProperty<>();
    public ObjectProperty<City> selectedOrigin = new SimpleObjectProperty<>();
    public ObjectProperty<City> selectedDestination = new SimpleObjectProperty<>();

    public StringProperty departureTimeStr = new SimpleStringProperty("");
    public StringProperty arrivalTimeStr = new SimpleStringProperty("");
    public StringProperty economyPrice = new SimpleStringProperty("0.0");
    public StringProperty businessPrice = new SimpleStringProperty("0.0");

    public AddFlightTabViewModel(Model model)
    {
        this.model = model;
        refreshData();
        loadDataFromModel();
    }

    public void refreshData()
    {
        citiesList.setAll(model.getCities());
        planesList.setAll(model.getPlanes());
        carriersList.setAll(model.getCarriers());

        int maxId = model.getAllFlights().stream()
                .mapToInt(Flight::getFlightId)
                .max()
                .orElse(100);

        flightId.set(String.valueOf(maxId + 1));
    }

    private void loadDataFromModel()
    {
        List<City> cities = model.getCities();
        if (cities != null)
        {
            citiesList.addAll(cities);
        }

        List<Plane> planes = model.getPlanes();
        if (planes != null)
        {
            planesList.addAll(planes);
        }

        List<Carrier> carriers = model.getCarriers();
        if (carriers != null)
        {
            carriersList.addAll(carriers);
        }
    }

    public void addFlight() throws Exception
    {
        try
        {
            if (selectedOrigin.get() == null || selectedDestination.get() == null ||
                    selectedPlane.get() == null || selectedCarrier.get() == null)
            {
                throw new Exception("Please select all fields (City, Plane, Carrier)");
            }

            LocalDateTime depTime = LocalDateTime.parse(departureTimeStr.get(), formatter);
            LocalDateTime arrTime = LocalDateTime.parse(arrivalTimeStr.get(), formatter);

            int id = Integer.parseInt(flightId.get().replaceAll("[^0-9]", ""));
            double price = Double.parseDouble(economyPrice.get());

            Flight newFlight = new Flight(
                    id,
                    "FL-" + id,
                    depTime,
                    arrTime,
                    price,
                    selectedCarrier.get(),
                    selectedPlane.get(),
                    selectedOrigin.get(),
                    selectedDestination.get()
            );

            model.addFlight(newFlight);
            clearFields();
            refreshData();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw e;
        }
    }

    public void clearFields()
    {
        flightId.set("");
        departureTimeStr.set("");
        arrivalTimeStr.set("");
        economyPrice.set("0.0");
        businessPrice.set("0.0");
        selectedCarrier.set(null);
        selectedPlane.set(null);
        selectedOrigin.set(null);
        selectedDestination.set(null);
    }
}

