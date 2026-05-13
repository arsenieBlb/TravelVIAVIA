package viewmodel;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;
import model.*;
import model.EconomyClass;
import model.BusinessClass;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FlightSceneViewModel {
    private static int nextDraftPassengerId = 1000;

    private Model model;
    private SeatMapViewModel seatMapViewModel;
    private boolean isUpdating = false;

    private ObjectProperty<City> departureCity = new SimpleObjectProperty<>();
    private ObjectProperty<City> arrivalCity = new SimpleObjectProperty<>();
    private ObservableList<City> allCities = FXCollections.observableArrayList();
    private ObservableList<City> filteredDestinations = FXCollections.observableArrayList();
    private final IntegerProperty passengerCount = new SimpleIntegerProperty(1);
    private final ObjectProperty<LocalDate> travelDate = new SimpleObjectProperty<>(LocalDate.now());

    private StringProperty flightNumber = new SimpleStringProperty("");
    private StringProperty departureTime = new SimpleStringProperty("");
    private StringProperty arrivalTime = new SimpleStringProperty("");
    private StringProperty routeSummary = new SimpleStringProperty("");

    private StringProperty passengerOneFirstName = new SimpleStringProperty("");
    private StringProperty passengerOneLastName = new SimpleStringProperty("");
    private IntegerProperty passengerOneBaggageCount = new SimpleIntegerProperty(0);

    private StringProperty passengerTwoFirstName = new SimpleStringProperty("");
    private StringProperty passengerTwoLastName = new SimpleStringProperty("");
    private IntegerProperty passengerTwoBaggageCount = new SimpleIntegerProperty(0);

    private DoubleProperty totalPrice = new SimpleDoubleProperty(0);

    private ObjectProperty<Flight> selectedFlight = new SimpleObjectProperty<>();
    private ObservableList<Flight> filteredFlights = FXCollections.observableArrayList();
    private List<Flight> allFlights = new ArrayList<>();

    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    private final BooleanProperty directOnly = new SimpleBooleanProperty(false);
    private final BooleanProperty roundTrip = new SimpleBooleanProperty(false);
    private final ObjectProperty<LocalDate> returnDate = new SimpleObjectProperty<>();
    private final ObservableList<Flight> returnFlights = FXCollections.observableArrayList();
    private final ObjectProperty<Flight> selectedReturnFlight = new SimpleObjectProperty<>();
    private PassengerDetailsViewModel passengerDetailsViewModel;

    public FlightSceneViewModel(Model model) {
        this.model = model;

        loadCitiesFromDatabase();
        departureCity.addListener((obs, oldVal, newVal) -> safeUpdateFilters());
        arrivalCity.addListener((obs, oldVal, newVal) -> safeUpdateFilters());

        passengerOneBaggageCount.addListener((observable, oldValue, newValue) -> updateTotalPrice());
        passengerTwoBaggageCount.addListener((observable, oldValue, newValue) -> updateTotalPrice());

        loadFirstFlight();
    }

    private void safeUpdateFilters() {
        if (isUpdating) return;

        Platform.runLater(() -> {
            isUpdating = true;
            City currentOrigin = departureCity.get();
            City currentDest = arrivalCity.get();

            List<City> destList = model.getAllCities().stream()
                    .filter(c -> currentOrigin == null || c.getCityId() != currentOrigin.getCityId())
                    .sorted(Comparator.comparing(City::getCityName))
                    .collect(Collectors.toList());

            if (!destList.equals(new ArrayList<>(filteredDestinations))) {
                filteredDestinations.setAll(destList);
            }

            List<City> originList = model.getAllCities().stream()
                    .filter(c -> currentDest == null || c.getCityId() != currentDest.getCityId())
                    .sorted(Comparator.comparing(City::getCityName))
                    .collect(Collectors.toList());

            if (!originList.equals(new ArrayList<>(allCities))) {
                allCities.setAll(originList);
            }

            departureCity.set(currentOrigin);
            arrivalCity.set(currentDest);
            isUpdating = false;
        });
    }

    private void loadCitiesFromDatabase() {
        List<City> cities = model.getAllCities();
        cities.sort(Comparator.comparing(City::getCityName));
        allCities.setAll(cities);
    }

    public void incrementPassengers() {
        if (passengerCount.get() < 9) {
            passengerCount.set(passengerCount.get() + 1);
        }
    }

    public void decrementPassengers() {
        if (passengerCount.get() > 1) {
            passengerCount.set(passengerCount.get() - 1);
        }
    }

    public void loadFirstFlight() {
        searchFlights();
    }

    public void searchFlights() {
        if (model == null) return;

        boolean noCriteria = departureCity.get() == null
            && arrivalCity.get() == null;

        List<Flight> flights;

        if (noCriteria) {
            // show first 10 upcoming flights when no search criteria is set
            List<Flight> upcoming = new ArrayList<>(model.getAllFlights());
            upcoming.sort(Comparator.comparing(Flight::getDepartureTime));
            // only show flights that haven't departed yet
            upcoming.removeIf(f -> f.getDepartureTime().isBefore(java.time.LocalDateTime.now()));
            flights = upcoming.size() > 10 ? upcoming.subList(0, 10) : upcoming;
        } else {
            SearchCriteria criteria = new SearchCriteria();
            criteria.setDepartureCity(departureCity.get());
            criteria.setArrivalCity(arrivalCity.get());
            criteria.setDepartureDate(travelDate.get());
            flights = model.searchFlights(criteria);
        }

        allFlights.clear();
        filteredFlights.clear();

        if (flights != null) {
            int needed = passengerCount.get();

            for (Flight f : flights) {
                // filter out flights with not enough seats
                if (!hasEnoughSeats(f, needed)) continue;

                if (directOnly.get() && f instanceof ConnectingFlight) continue;

                allFlights.add(f);
            }
            filteredFlights.addAll(allFlights);
        }

        // search return flights if roundtrip is selected
        returnFlights.clear();
        if (roundTrip.get() && departureCity.get() != null && arrivalCity.get() != null && returnDate.get() != null) {
            SearchCriteria returnCriteria = new SearchCriteria();
            returnCriteria.setDepartureCity(arrivalCity.get());
            returnCriteria.setArrivalCity(departureCity.get());
            returnCriteria.setDepartureDate(returnDate.get());

            List<Flight> returnResults = model.searchFlights(returnCriteria);
            if (returnResults != null) {
                int needed = passengerCount.get();
                for (Flight f : returnResults) {
                    if (!hasEnoughSeats(f, needed)) continue;
                    if (directOnly.get() && f instanceof ConnectingFlight) continue;
                    returnFlights.add(f);
                }
            }
        }
    }

    // checks if a flight has enough available seats for the number of passengers
    private boolean hasEnoughSeats(Flight flight, int needed) {
        if (flight instanceof ConnectingFlight conn) {
            int seg1Seats = conn.getFirstSegment().getAvailableSeats().size();
            int seg2Seats = conn.getSecondSegment().getAvailableSeats().size();
            return seg1Seats >= needed && seg2Seats >= needed;
        }
        return flight.getAvailableSeats().size() >= needed;
    }

    private boolean isFlightDirect(Flight f) {
        if (departureCity.get() == null || arrivalCity.get() == null) {
            return true;
        }

        return f.getDepartureCity().equals(departureCity.get()) &&
                f.getArrivalCity().equals(arrivalCity.get());
    }

    public void confirmBooking() {
        if (selectedFlight.get() == null) {
            throw new IllegalStateException("No flight is selected.");
        }

        List<Passenger> passengers = new ArrayList<>();
        List<Seat> selectedSeats = new ArrayList<>();

        List<LuggageType> luggageTypes = model.getLuggageTypes();
        LuggageType standardLuggage = (luggageTypes != null && !luggageTypes.isEmpty())
                ? luggageTypes.get(0) : null;

        Passenger p1 = createPassenger(passengerOneFirstName.get(), passengerOneLastName.get(), "Passenger 1");

        if (passengerOneBaggageCount.get() > 0 && standardLuggage != null) {
            p1.addPassengerLuggage(new PassengerLuggage(0, passengerOneBaggageCount.get(), standardLuggage));
        }
        passengers.add(p1);
        selectedSeats.add(seatMapViewModel.getSelectedSeatForPassenger(1));

        if (hasPassengerTwoDetails()) {
            Passenger p2 = createPassenger(passengerTwoFirstName.get(), passengerTwoLastName.get(), "Passenger 2");

            if (passengerTwoBaggageCount.get() > 0 && standardLuggage != null) {
                p2.addPassengerLuggage(new PassengerLuggage(0, passengerTwoBaggageCount.get(), standardLuggage));
            }
            passengers.add(p2);
            selectedSeats.add(seatMapViewModel.getSelectedSeatForPassenger(2));
        }

        model.createBooking(selectedFlight.get(), passengers, selectedSeats);
        System.out.println("Booking successfully saved to Database!");
        clear();
    }

    public void clear() {
        departureCity.set(null);
        arrivalCity.set(null);
        travelDate.set(LocalDate.now());

        passengerOneFirstName.set("");
        passengerOneLastName.set("");
        passengerOneBaggageCount.set(0);
        passengerTwoFirstName.set("");
        passengerTwoLastName.set("");
        passengerTwoBaggageCount.set(0);

        directOnly.set(false);
        roundTrip.set(false);
        searchFlights();

        selectedFlight.set(null);
        selectedReturnFlight.set(null);
        returnDate.set(null);
        returnFlights.clear();
        if (seatMapViewModel != null) {
            seatMapViewModel.clear();
        }

        updateTotalPrice();
    }


    public void setSeatMapViewModel(SeatMapViewModel seatMapViewModel) {
        this.seatMapViewModel = seatMapViewModel;
        if (seatMapViewModel != null) {
            seatMapViewModel.passengerOneSeatClassProperty().addListener((obs, old, newVal) -> updateTotalPrice());
            seatMapViewModel.passengerTwoSeatClassProperty().addListener((obs, old, newVal) -> updateTotalPrice());
        }
    }

    public BooleanProperty directOnlyProperty() {
        return directOnly;
    }

    public boolean login(String email, String password) { return model.login(email, password); }
    public boolean register(String firstName, String lastName, String email, String password) {
        return model.register(firstName, lastName, email, password);
    }
    public void logout() { model.logout(); }
    public User getLoggedInUser() { return model.getLoggedInUser(); }

    private Passenger createPassenger(String firstName, String lastName, String passengerLabel) {
        if (!hasText(firstName) || !hasText(lastName)) {
            throw new IllegalStateException(passengerLabel + " must have both first name and last name.");
        }
        return new Passenger(nextDraftPassengerId++, firstName.trim(), lastName.trim());
    }

    private boolean hasPassengerTwoDetails() {
        return hasText(passengerTwoFirstName.get())
                || hasText(passengerTwoLastName.get())
                || passengerTwoBaggageCount.get() > 0
                || (seatMapViewModel != null && seatMapViewModel.getSelectedSeatForPassenger(2) != null);
    }

    public void setSelectedFlight(Flight flight) {
        this.selectedFlight.set(flight);
        if (seatMapViewModel != null) {
            seatMapViewModel.clearSeatForPassenger(1);
            seatMapViewModel.clearSeatForPassenger(2);
        }
        updateTotalPrice();
    }

    public void updateTotalPrice()
    {
        if (selectedFlight.get() == null) return;

        double base = selectedFlight.get().getBasePrice();
        if (selectedReturnFlight.get() != null)
        {
            base += selectedReturnFlight.get().getBasePrice();
        }

        double currentTotal = 0;

        SeatClass p1Class = (seatMapViewModel != null)
                ? seatMapViewModel.getSeatClassForPassenger(1)
                : new EconomyClass();

        currentTotal += calculatePassengerPrice(base, p1Class, passengerOneBaggageCount.get());

        if (hasPassengerTwoDetails())
        {
            SeatClass p2Class = (seatMapViewModel != null)
                    ? seatMapViewModel.getSeatClassForPassenger(2)
                    : new EconomyClass();

            currentTotal += calculatePassengerPrice(base, p2Class, passengerTwoBaggageCount.get());
        }

        totalPrice.set(currentTotal);
    }

    private double calculatePassengerPrice(double base, SeatClass seatClass, int baggageQuantity)
    {
        double price = base * seatClass.getPriceMultiplier();

        price += (baggageQuantity * 20.0);
        return price;
    }

    public void sortFlights(String criteria) {
        if (filteredFlights == null) return;
        switch (criteria) {
            case "Price (Low to High)":
                filteredFlights.sort(Comparator.comparingDouble(Flight::getBasePrice));
                break;
            case "Duration (Shortest First)":
                filteredFlights.sort(Comparator.comparingLong(Flight::getDurationInSeconds));
                break;
            case "Departure (Early First)":
                filteredFlights.sort(Comparator.comparing(Flight::getDepartureTime));
                break;
        }
    }

    public ObservableList<String> getUniqueCarriers() {
        List<String> names = new ArrayList<>();
        for (Carrier c : model.getCarriers()) {
            names.add(c.getName());
        }
        names.sort(String::compareTo);
        return FXCollections.observableArrayList(names);
    }

    public void filterByCarrier(String carrierName) {
        if (carrierName == null || carrierName.equals("All Airlines")) {
            filteredFlights.setAll(allFlights);
        } else {
            filteredFlights.setAll(allFlights.stream().filter(f -> f.getCarrier().getName().equals(carrierName)).toList());
        }
    }

    public ObservableList<Flight> getFilteredFlights() { return filteredFlights; }
    public ObjectProperty<Flight> selectedFlightProperty() { return selectedFlight; }
    public Flight getSelectedFlight() { return selectedFlight.get(); }
    public ObservableList<City> getAllCities() { return allCities; }
    public ObservableList<City> getFilteredDestinations() { return filteredDestinations; }
    public ObjectProperty<City> departureCityProperty() { return departureCity; }
    public ObjectProperty<City> arrivalCityProperty() { return arrivalCity; }
    public ObjectProperty<LocalDate> travelDateProperty() { return travelDate; }
    public IntegerProperty passengerCountProperty() { return passengerCount; }
    public StringProperty passengerOneFirstNameProperty() { return passengerOneFirstName; }
    public StringProperty passengerOneLastNameProperty() { return passengerOneLastName; }
    public IntegerProperty passengerOneBaggageCountProperty() { return passengerOneBaggageCount; }
    public StringProperty PassengerTwoFirstNameProperty() { return passengerTwoFirstName; }
    public StringProperty PassengerTwoLastNameProperty() { return passengerTwoLastName; }
    public IntegerProperty PassengerTwoBaggageCountProperty() { return passengerTwoBaggageCount; }
    public DoubleProperty totalPriceProperty() { return totalPrice; }
    public StringProperty flightNumberProperty() { return flightNumber; }
    public StringProperty departureTimeProperty() { return departureTime; }
    public StringProperty arrivalTimeProperty() { return arrivalTime; }
    public StringProperty routeSummaryProperty() { return routeSummary; }
    private boolean hasText(String value) { return value != null && !value.isBlank(); }

    public BooleanProperty roundTripProperty() { return roundTrip; }
    public ObjectProperty<LocalDate> returnDateProperty() { return returnDate; }
    public ObservableList<Flight> getReturnFlights() { return returnFlights; }
    public ObjectProperty<Flight> selectedReturnFlightProperty() { return selectedReturnFlight; }
    public Flight getSelectedReturnFlight() { return selectedReturnFlight.get(); }
    public void setSelectedReturnFlight(Flight flight) {
        this.selectedReturnFlight.set(flight);
        updateTotalPrice();
    }

    public MyBookingsViewModel getMyBookingsViewModel() {
        return new MyBookingsViewModel(this.model);
    }

    public PassengerDetailsViewModel getPassengerDetailsViewModel() {
        if (this.passengerDetailsViewModel == null) {
            this.passengerDetailsViewModel = new PassengerDetailsViewModel(this.model, this);
        }
        return this.passengerDetailsViewModel;
    }

    public SeatMapViewModel getSeatMapViewModel() {
        return new SeatMapViewModel(getPassengerDetailsViewModel());
    }
}