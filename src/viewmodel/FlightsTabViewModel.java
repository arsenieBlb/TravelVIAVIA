package viewmodel;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import model.Flight;
import model.Model;

import java.time.LocalDate;
import java.util.stream.Collectors;

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
        allFlights.setAll(model.getAllFlights());
    }

    private void updatePredicate() {
        filteredFlights.setPredicate(flight -> {
            boolean originMatch = flight.getDepartureCity().getCityName().toLowerCase()
                    .contains(originFilter.get().toLowerCase());
            boolean destMatch = flight.getArrivalCity().getCityName().toLowerCase()
                    .contains(destinationFilter.get().toLowerCase());
            boolean carrierMatch = flight.getCarrier().getName().toLowerCase()
                    .contains(carrierFilter.get().toLowerCase());
            String selectedAircraft = aircraftFilter.get();
            boolean aircraftMatch = selectedAircraft == null ||
                    selectedAircraft.equals("All") ||
                    selectedAircraft.isEmpty() ||
                    flight.getPlane().getPlaneType().getModel().equals(selectedAircraft);
            boolean dateMatch = dateFilter.get() == null ||
                    flight.getDepartureTime().toLocalDate().equals(dateFilter.get());

            return originMatch && destMatch && carrierMatch && aircraftMatch && dateMatch;
        });
    }

    public ObservableList<Flight> getFilteredFlights() { return filteredFlights; }
    public StringProperty originFilterProperty() { return originFilter; }
    public StringProperty destinationFilterProperty() { return destinationFilter; }
    public StringProperty carrierFilterProperty() { return carrierFilter; }
    public StringProperty aircraftFilterProperty() { return aircraftFilter; }
    public ObjectProperty<LocalDate> dateFilterProperty() { return dateFilter; }

    public void refreshFromModel()
    {
        allFlights.setAll(model.getAllFlights());
    }

    public ObservableList<String> getUniqueAircraftModels()
    {
        return allFlights.stream()
                .filter(flight -> flight.getPlane() != null
                        && flight.getPlane().getPlaneType() != null
                        && flight.getPlane().getPlaneType().getModel() != null)
                .map(flight -> flight.getPlane().getPlaneType().getModel())
                .distinct()
                .sorted()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }
}
