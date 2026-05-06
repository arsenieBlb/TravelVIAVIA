package viewmodel;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BookFlightViewModel {
    private Model model;
    private boolean isUpdating = false;

    private ObjectProperty<City> departureCity = new SimpleObjectProperty<>();
    private ObjectProperty<City> arrivalCity = new SimpleObjectProperty<>();

    private ObservableList<City> allCities = FXCollections.observableArrayList();
    private ObservableList<City> filteredDestinations = FXCollections.observableArrayList();

    private ObservableList<Flight> filteredFlights = FXCollections.observableArrayList();
    private ObjectProperty<Flight> selectedFlight = new SimpleObjectProperty<>();

    private final IntegerProperty passengerCount = new SimpleIntegerProperty(1);

    private final ObjectProperty<LocalDate> travelDate = new SimpleObjectProperty<>(LocalDate.now());

    public BookFlightViewModel(Model model) {
        this.model = model;
        loadCitiesFromDatabase();

        departureCity.addListener((obs, oldVal, newVal) -> safeUpdateFilters());
        arrivalCity.addListener((obs, oldVal, newVal) -> safeUpdateFilters());
    }

    private void safeUpdateFilters() {
        if (isUpdating) return;

        javafx.application.Platform.runLater(() -> {
            isUpdating = true;

            City currentOrigin = departureCity.get();
            City currentDest = arrivalCity.get();

            List<City> destList = model.getAllCities().stream()
                    .filter(c -> currentOrigin == null || c.getCityId() != currentOrigin.getCityId())
                    .sorted(java.util.Comparator.comparing(City::getCityName))
                    .collect(java.util.stream.Collectors.toList());

            if (!destList.equals(new java.util.ArrayList<>(filteredDestinations))) {
                filteredDestinations.setAll(destList);
            }

            List<City> originList = model.getAllCities().stream()
                    .filter(c -> currentDest == null || c.getCityId() != currentDest.getCityId())
                    .sorted(java.util.Comparator.comparing(City::getCityName))
                    .collect(java.util.stream.Collectors.toList());

            if (!originList.equals(new java.util.ArrayList<>(allCities))) {
                allCities.setAll(originList);
            }

            departureCity.set(currentOrigin);
            arrivalCity.set(currentDest);

            isUpdating = false;
        });
    }

    public ObjectProperty<LocalDate> travelDateProperty() {
        return travelDate;
    }

    public void loadFirstFlight() {
        searchFlights();
        if (!filteredFlights.isEmpty()) {
            selectedFlight.set(filteredFlights.get(0));
        }
    }

    public IntegerProperty passengerCountProperty() {
        return passengerCount;
    }

    public void incrementPassengers() {
        if (passengerCount.get() < 9) {
            passengerCount.set(passengerCount.get() + 1);
        }
    }

    public void decrementPassengers() {
        if (passengerCount.get() > 1) {
            passengerCount.set(passengerCount.get() - 1);
        } else {
            System.out.println("Minimum passenger limit reached.");
        }
    }

    public void searchFlights() {
        SearchCriteria criteria = new SearchCriteria();
        criteria.setDepartureCity(departureCity.get());
        criteria.setArrivalCity(arrivalCity.get());

        List<Flight> results = model.searchFlights(criteria);
        filteredFlights.setAll(results);
    }

    private void loadCitiesFromDatabase() {
        List<City> cities = model.getAllCities();
        cities.sort(Comparator.comparing(City::getCityName));
        allCities.setAll(cities);
    }

    private void updateFilteredDestinations() {
        City selectedOrigin = departureCity.get();

        List<City> newList = model.getAllCities().stream()
                .filter(city -> selectedOrigin == null || city.getCityId() != selectedOrigin.getCityId())
                .sorted(java.util.Comparator.comparing(City::getCityName))
                .collect(java.util.stream.Collectors.toList());

        if (!newList.equals(new java.util.ArrayList<>(filteredDestinations))) {
            filteredDestinations.setAll(newList);
        }
    }

    private void updateFilteredOrigins() {
        City selectedDest = arrivalCity.get();
        City currentOrigin = departureCity.get();

        List<City> all = model.getAllCities();

        List<City> filtered = all.stream()
                .filter(city -> selectedDest == null || city.getCityId() != selectedDest.getCityId())
                .sorted(Comparator.comparing(City::getCityName))
                .collect(Collectors.toList());

        allCities.setAll(filtered);

        if (currentOrigin != null && filtered.contains(currentOrigin)) {
            departureCity.set(currentOrigin);
        }
    }

    public ObservableList<Flight> getFilteredFlights() { return filteredFlights; }
    public ObjectProperty<Flight> selectedFlightProperty() { return selectedFlight; }
    public Flight getSelectedFlight() { return selectedFlight.get(); }
    public ObservableList<City> getAllCities() { return allCities; }
    public ObservableList<City> getFilteredDestinations() { return filteredDestinations; }
    public ObjectProperty<City> departureCityProperty() { return departureCity; }
    public ObjectProperty<City> arrivalCityProperty() { return arrivalCity; }

    public void clear() {
        departureCity.set(null);
        arrivalCity.set(null);
        filteredFlights.clear();
        selectedFlight.set(null);
    }
}
