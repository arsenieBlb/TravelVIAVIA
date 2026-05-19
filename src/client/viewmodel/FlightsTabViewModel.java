package client.viewmodel;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import client.model.Flight;
import client.model.Model;
import client.model.SearchCriteria;
import java.util.List;

import java.time.LocalDate;

public class FlightsTabViewModel {
    private final Model model;
    private final ObservableList<Flight> allFlights = FXCollections.observableArrayList();
    private final FilteredList<Flight> filteredFlights;
    private final ObjectProperty<LocalDate> dateFilter = new SimpleObjectProperty<>(null);

    private final StringProperty originFilter = new SimpleStringProperty("");
    private final StringProperty destinationFilter = new SimpleStringProperty("");
    private final StringProperty carrierFilter = new SimpleStringProperty("");
    private final StringProperty aircraftFilter = new SimpleStringProperty("All");

    public FlightsTabViewModel(Model model) {
        this.model = model;
        this.filteredFlights = new FilteredList<>(allFlights, p -> true);

        loadInitialData();

        originFilter.addListener((obs, old, val) -> updatePredicate());
        destinationFilter.addListener((obs, old, val) -> updatePredicate());
        carrierFilter.addListener((obs, old, val) -> updatePredicate());
        aircraftFilter.addListener((obs, old, val) -> updatePredicate());
        dateFilter.addListener((obs, old, val) -> updatePredicate());
    }

    private void loadInitialData() {
        javafx.concurrent.Task<java.util.List<Flight>> loadTask = new javafx.concurrent.Task<>() {
            @Override
            protected java.util.List<Flight> call() {
                return model.getAllFlights();
            }
        };
        loadTask.setOnSucceeded(event -> allFlights.setAll(loadTask.getValue()));
        loadTask.setOnFailed(event -> {
            Throwable e = loadTask.getException();
            if (e != null) {
                System.err.println("Could not load flights: " + e.getMessage());
            }
        });
        Thread thread = new Thread(loadTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void updatePredicate() {
        filteredFlights.setPredicate(flight -> {
            boolean originMatch = containsIgnoreCase(
                    flight.getDepartureCity() == null ? "" : flight.getDepartureCity().getCityName(),
                    originFilter.get());
            boolean destMatch = containsIgnoreCase(
                    flight.getArrivalCity() == null ? "" : flight.getArrivalCity().getCityName(),
                    destinationFilter.get());
            boolean carrierMatch = containsIgnoreCase(
                    flight.getCarrier() == null ? "" : flight.getCarrier().getName(),
                    carrierFilter.get());
            String selectedAircraft = aircraftFilter.get();
            String aircraftModel = flight.getPlane() == null
                    || flight.getPlane().getPlaneType() == null
                    ? "" : flight.getPlane().getPlaneType().getModel();
            boolean aircraftMatch = selectedAircraft == null ||
                    selectedAircraft.equals("All") ||
                    selectedAircraft.isEmpty() ||
                    aircraftModel.equals(selectedAircraft);
            boolean dateMatch = dateFilter.get() == null ||
                    (flight.getDepartureTime() != null
                            && flight.getDepartureTime().toLocalDate().equals(dateFilter.get()));

            return originMatch && destMatch && carrierMatch && aircraftMatch && dateMatch;
        });
    }

    private boolean containsIgnoreCase(String value, String filter)
    {
        String safeValue = value == null ? "" : value.toLowerCase();
        String safeFilter = filter == null ? "" : filter.toLowerCase();
        return safeValue.contains(safeFilter);
    }

    public ObservableList<Flight> getFilteredFlights() { return filteredFlights; }
    public StringProperty originFilterProperty() { return originFilter; }
    public StringProperty destinationFilterProperty() { return destinationFilter; }
    public StringProperty carrierFilterProperty() { return carrierFilter; }
    public StringProperty aircraftFilterProperty() { return aircraftFilter; }
    public ObjectProperty<LocalDate> dateFilterProperty() { return dateFilter; }

    public void refreshFromModel()
    {
        javafx.concurrent.Task<java.util.List<Flight>> loadTask = new javafx.concurrent.Task<>() {
            @Override
            protected java.util.List<Flight> call() {
                return model.getAllFlights();
            }
        };
        loadTask.setOnSucceeded(event -> allFlights.setAll(loadTask.getValue()));
        loadTask.setOnFailed(event -> {
            Throwable e = loadTask.getException();
            if (e != null) {
                System.err.println("Could not refresh flights: " + e.getMessage());
            }
        });
        Thread thread = new Thread(loadTask);
        thread.setDaemon(true);
        thread.start();
    }

    public void removeFlight(Flight flight)
    {
        model.removeFlight(flight);
        allFlights.remove(flight);
    }

    public void editFlight(Flight flight)
    {
        model.editFlight(flight);
        refreshFromModel();
    }

    public List<client.model.City> getCities() {
        return model.getCities();
    }

    public List<client.model.Carrier> getCarriers() {
        return model.getCarriers();
    }

    public List<client.model.Plane> getPlanes() {
        return model.getPlanes();
    }
}

