package viewmodel;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.*;

import java.time.LocalDate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BookFlightViewModel {
    private static final String ALL_AIRLINES = "All airlines";
    private static final String SORT_DEPARTURE = "Departure time";
    private static final String SORT_PRICE_LOW = "Price: low to high";
    private static final String SORT_PRICE_HIGH = "Price: high to low";
    private static final String SORT_DURATION = "Duration";

    private Model model;
    private boolean isUpdating = false;

    private ObjectProperty<City> departureCity = new SimpleObjectProperty<>();
    private ObjectProperty<City> arrivalCity = new SimpleObjectProperty<>();

    private ObservableList<City> allCities = FXCollections.observableArrayList();
    private ObservableList<City> filteredDestinations = FXCollections.observableArrayList();

    private ObservableList<Flight> filteredFlights = FXCollections.observableArrayList();
    private ObservableList<Flight> baseSearchResults = FXCollections.observableArrayList();
    private ObjectProperty<Flight> selectedFlight = new SimpleObjectProperty<>();

    private final IntegerProperty passengerCount = new SimpleIntegerProperty(1);

    private final ObjectProperty<LocalDate> travelDate = new SimpleObjectProperty<>();
    private final StringProperty selectedAirline =
            new SimpleStringProperty(ALL_AIRLINES);
    private final StringProperty selectedSort =
            new SimpleStringProperty(SORT_DEPARTURE);
    private final BooleanProperty directOnly = new SimpleBooleanProperty(false);

    private ObservableList<String> airlines =
            FXCollections.observableArrayList(ALL_AIRLINES);
    private ObservableList<String> sortOptions = FXCollections.observableArrayList(
            SORT_DEPARTURE, SORT_PRICE_LOW, SORT_PRICE_HIGH, SORT_DURATION);

    public BookFlightViewModel(Model model) {
        this.model = model;
        loadCitiesFromDatabase();
        searchFlights();

        departureCity.addListener((obs, oldVal, newVal) -> safeUpdateFilters());
        arrivalCity.addListener((obs, oldVal, newVal) -> safeUpdateFilters());
        selectedAirline.addListener((obs, oldVal, newVal) -> applyClientFilters());
        selectedSort.addListener((obs, oldVal, newVal) -> applyClientFilters());
        directOnly.addListener((obs, oldVal, newVal) -> applyClientFilters());
    }

    private void safeUpdateFilters() {
        if (isUpdating) return;

        isUpdating = true;

        City currentOrigin = departureCity.get();
        City currentDest = arrivalCity.get();

        List<City> destList = model.getAllCities().stream()
                .filter(c -> currentOrigin == null
                        || c.getCityId() != currentOrigin.getCityId())
                .sorted(java.util.Comparator.comparing(City::getCityName))
                .collect(java.util.stream.Collectors.toList());

        if (!destList.equals(new java.util.ArrayList<>(filteredDestinations))) {
            filteredDestinations.setAll(destList);
        }

        List<City> originList = model.getAllCities().stream()
                .filter(c -> currentDest == null
                        || c.getCityId() != currentDest.getCityId())
                .sorted(java.util.Comparator.comparing(City::getCityName))
                .collect(java.util.stream.Collectors.toList());

        if (!originList.equals(new java.util.ArrayList<>(allCities))) {
            allCities.setAll(originList);
        }

        if (currentOrigin != null && !originList.contains(currentOrigin)) {
            departureCity.set(null);
        }
        if (currentDest != null && !destList.contains(currentDest)) {
            arrivalCity.set(null);
        }

        isUpdating = false;
    }

    public ObjectProperty<LocalDate> travelDateProperty() {
        return travelDate;
    }

    public void loadFirstFlight() {
        searchFlights();
        if (!filteredFlights.isEmpty()) {
            setSelectedFlight(filteredFlights.get(0));
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
        criteria.setDepartureDate(travelDate.get());
        criteria.setPassengerCount(passengerCount.get());

        List<Flight> results = model.searchFlights(criteria);
        baseSearchResults.setAll(results);
        refreshAirlines();
        applyClientFilters();
    }

    private void refreshAirlines() {
        String currentAirline = selectedAirline.get();
        List<String> airlineNames = baseSearchResults.stream()
                .map(flight -> flight.getCarrier().getName())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        List<String> values = new ArrayList<>();
        values.add(ALL_AIRLINES);
        values.addAll(airlineNames);
        airlines.setAll(values);

        if (!values.contains(currentAirline)) {
            selectedAirline.set(ALL_AIRLINES);
        }
    }

    private void applyClientFilters() {
        List<Flight> flights = new ArrayList<>(baseSearchResults);

        String airline = selectedAirline.get();
        if (airline != null && !ALL_AIRLINES.equals(airline)) {
            flights = flights.stream()
                    .filter(flight -> airline.equals(flight.getCarrier().getName()))
                    .collect(Collectors.toList());
        }

        String sort = selectedSort.get();
        if (SORT_PRICE_LOW.equals(sort)) {
            flights.sort(Comparator.comparingDouble(Flight::getBasePrice));
        } else if (SORT_PRICE_HIGH.equals(sort)) {
            flights.sort(Comparator.comparingDouble(Flight::getBasePrice).reversed());
        } else if (SORT_DURATION.equals(sort)) {
            flights.sort(Comparator.comparing(flight ->
                    Duration.between(flight.getDepartureTime(),
                            flight.getArrivalTime())));
        } else {
            flights.sort(Comparator.comparing(Flight::getDepartureTime));
        }

        filteredFlights.setAll(flights);
        if (!filteredFlights.contains(selectedFlight.get())) {
            setSelectedFlight(null);
        }
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
    public void setSelectedFlight(Flight flight) { selectedFlight.set(flight); }
    public ObservableList<City> getAllCities() { return allCities; }
    public ObservableList<City> getFilteredDestinations() { return filteredDestinations; }
    public ObjectProperty<City> departureCityProperty() { return departureCity; }
    public ObjectProperty<City> arrivalCityProperty() { return arrivalCity; }
    public ObservableList<String> getAirlines() { return airlines; }
    public ObservableList<String> getSortOptions() { return sortOptions; }
    public StringProperty selectedAirlineProperty() { return selectedAirline; }
    public StringProperty selectedSortProperty() { return selectedSort; }
    public BooleanProperty directOnlyProperty() { return directOnly; }

    public void clear() {
        departureCity.set(null);
        arrivalCity.set(null);
        travelDate.set(null);
        passengerCount.set(1);
        selectedAirline.set(ALL_AIRLINES);
        selectedSort.set(SORT_DEPARTURE);
        directOnly.set(false);
        safeUpdateFilters();
        searchFlights();
    }
}
