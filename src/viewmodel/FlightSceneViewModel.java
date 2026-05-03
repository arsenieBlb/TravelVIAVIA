package viewmodel;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import model.Flight;
import model.Model;
import model.Passenger;
import model.SearchCriteria;
import model.Seat;
import model.SeatClass;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FlightSceneViewModel
{
    private static int nextDraftPassengerId = 1000;

    private Model model;

    private StringProperty flightNumber = new SimpleStringProperty("");
    private StringProperty departureCity = new SimpleStringProperty("");
    private StringProperty arrivalCity = new SimpleStringProperty("");
    private StringProperty departureTime = new SimpleStringProperty("");
    private StringProperty arrivalTime = new SimpleStringProperty("");
    private StringProperty routeSummary = new SimpleStringProperty("");

    private StringProperty passengerOneFirstName = new SimpleStringProperty("");
    private StringProperty passengerOneLastName = new SimpleStringProperty("");
    private IntegerProperty passengerOneBaggageCount = new SimpleIntegerProperty(0);

    private StringProperty passengerTwoFirstName = new SimpleStringProperty("");
    private StringProperty passengerTwoLastName = new SimpleStringProperty("");
    private IntegerProperty passengerTwoBaggageCount = new SimpleIntegerProperty(0);

    private ObjectProperty<SeatClass> passengerOneSeatClass =
        new SimpleObjectProperty<>(SeatClass.Economy);
    private ObjectProperty<SeatClass> passengerTwoSeatClass =
        new SimpleObjectProperty<>(SeatClass.Economy);
    private StringProperty passengerOneSeatText = new SimpleStringProperty("");
    private StringProperty passengerTwoSeatText = new SimpleStringProperty("");
    private ObjectProperty<Seat> passengerOneSelectedSeat =
        new SimpleObjectProperty<>();
    private ObjectProperty<Seat> passengerTwoSelectedSeat =
        new SimpleObjectProperty<>();

    private DoubleProperty totalPrice = new SimpleDoubleProperty(0);

    private ObjectProperty<Flight> selectedFlight = new SimpleObjectProperty<>();

    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public FlightSceneViewModel(Model model)
    {
        this.model = model;

        passengerOneSeatClass.addListener((observable, oldValue, newValue) ->
            clearSeatIfClassChanged(1));
        passengerTwoSeatClass.addListener((observable, oldValue, newValue) ->
            clearSeatIfClassChanged(2));
        passengerOneBaggageCount.addListener((observable, oldValue, newValue) ->
            updateTotalPrice());
        passengerTwoBaggageCount.addListener((observable, oldValue, newValue) ->
            updateTotalPrice());

        // loads the first available flight from the database and shows it on screen
        loadFirstFlight();
    }

    // grabs the first flight from the database and fills in all the labels
    private void loadFirstFlight()
    {
        if (model == null) {
            return;
        }

        SearchCriteria criteria = new SearchCriteria();
        List<Flight> flights = model.searchFlights(criteria);

        if (flights != null && !flights.isEmpty())
        {
            Flight flight = flights.get(0);
            setSelectedFlight(flight);

            departureCity.set(flight.getDepartureCity().getCityName().toUpperCase());
            arrivalCity.set(flight.getArrivalCity().getCityName().toUpperCase());
            departureTime.set(flight.getDepartureTime().format(timeFormatter));
            arrivalTime.set(flight.getArrivalTime().format(timeFormatter));

            flightNumber.set(flight.getFlightNumber());
            routeSummary.set(flight.getFlightNumber() + " | "
                + flight.getDepartureCity().getCityName() + " -> "
                + flight.getArrivalCity().getCityName());
        }
    }

    public void confirmBooking()
    {
        if (selectedFlight.get() == null)
        {
            throw new IllegalStateException("No flight is selected.");
        }

        List<Passenger> passengers = new ArrayList<>();
        List<Seat> selectedSeats = new ArrayList<>();

        passengers.add(createPassenger(passengerOneFirstName.get(),
            passengerOneLastName.get(), "Passenger 1"));
        selectedSeats.add(passengerOneSelectedSeat.get());

        if (hasPassengerTwoDetails())
        {
            passengers.add(createPassenger(passengerTwoFirstName.get(),
                passengerTwoLastName.get(), "Passenger 2"));
            selectedSeats.add(passengerTwoSelectedSeat.get());
        }

        model.createBooking(selectedFlight.get(), passengers, selectedSeats);
        System.out.println("Booking confirmed!");
    }

    private Passenger createPassenger(String firstName, String lastName,
        String passengerLabel)
    {
        if (!hasText(firstName) || !hasText(lastName))
        {
            throw new IllegalStateException(passengerLabel
                + " must have both first name and last name.");
        }
        return new Passenger(nextDraftPassengerId++, firstName.trim(),
            lastName.trim());
    }

    private boolean hasPassengerTwoDetails()
    {
        return hasText(passengerTwoFirstName.get())
            || hasText(passengerTwoLastName.get())
            || passengerTwoBaggageCount.get() > 0
            || passengerTwoSelectedSeat.get() != null;
    }

    public List<Seat> getSeatMapSeats()
    {
        List<Seat> seats = new ArrayList<>();
        if (selectedFlight.get() != null)
        {
            seats.addAll(selectedFlight.get().getPlane().getSeats());
        }
        seats.sort(seatComparator());
        return seats;
    }

    public int getPlaneColumnCount()
    {
        if (selectedFlight.get() == null)
        {
            return 6;
        }
        return selectedFlight.get().getPlane().getPlaneType().getNumberOfColumns();
    }

    public boolean isSeatTaken(Seat seat)
    {
        if (selectedFlight.get() == null || seat == null)
        {
            return true;
        }
        return !selectedFlight.get().getAvailableSeats().contains(seat);
    }

    public boolean isSeatAlreadySelectedByOtherPassenger(Seat seat,
        int passengerNumber)
    {
        if (seat == null)
        {
            return false;
        }
        if (passengerNumber == 1)
        {
            return seat.equals(passengerTwoSelectedSeat.get());
        }
        return seat.equals(passengerOneSelectedSeat.get());
    }

    public void selectSeatForPassenger(int passengerNumber, Seat seat)
    {
        if (seat == null)
        {
            clearSeatForPassenger(passengerNumber);
            return;
        }
        if (seat.getSeatClass() != getSeatClassForPassenger(passengerNumber))
        {
            throw new IllegalArgumentException(
                "Seat does not match the selected class.");
        }
        if (isSeatTaken(seat))
        {
            throw new IllegalArgumentException("Seat is already taken.");
        }
        if (isSeatAlreadySelectedByOtherPassenger(seat, passengerNumber))
        {
            throw new IllegalArgumentException(
                "The other passenger already selected this seat.");
        }

        selectedSeatProperty(passengerNumber).set(seat);
        seatTextProperty(passengerNumber).set(seat.getSeatNumber());
    }

    public void clearSeatForPassenger(int passengerNumber)
    {
        selectedSeatProperty(passengerNumber).set(null);
        seatTextProperty(passengerNumber).set("");
    }

    public Seat getSelectedSeatForPassenger(int passengerNumber)
    {
        return selectedSeatProperty(passengerNumber).get();
    }

    public SeatClass getSeatClassForPassenger(int passengerNumber)
    {
        SeatClass seatClass = seatClassProperty(passengerNumber).get();
        return seatClass == null ? SeatClass.Economy : seatClass;
    }

    public Flight getSelectedFlight()
    {
        return selectedFlight.get();
    }

    public String getSeatPickerSummary(int passengerNumber)
    {
        return routeSummary.get() + " | " + getSeatClassForPassenger(passengerNumber);
    }

    private void clearSeatIfClassChanged(int passengerNumber)
    {
        Seat selectedSeat = getSelectedSeatForPassenger(passengerNumber);
        if (selectedSeat != null
            && selectedSeat.getSeatClass() != getSeatClassForPassenger(passengerNumber))
        {
            clearSeatForPassenger(passengerNumber);
        }
    }

    private ObjectProperty<SeatClass> seatClassProperty(int passengerNumber)
    {
        return passengerNumber == 1 ? passengerOneSeatClass
            : passengerTwoSeatClass;
    }

    private StringProperty seatTextProperty(int passengerNumber)
    {
        return passengerNumber == 1 ? passengerOneSeatText
            : passengerTwoSeatText;
    }

    private ObjectProperty<Seat> selectedSeatProperty(int passengerNumber)
    {
        return passengerNumber == 1 ? passengerOneSelectedSeat
            : passengerTwoSelectedSeat;
    }

    private Comparator<Seat> seatComparator()
    {
        return Comparator.comparingInt(Seat::getRowNumber)
            .thenComparing(Seat::getSeatNumber);
    }

    private boolean hasText(String value)
    {
        return value != null && !value.isBlank();
    }

    public StringProperty passengerOneFirstNameProperty()
    {
        return passengerOneFirstName;
    }
    public StringProperty passengerOneLastNameProperty()
    {
        return passengerOneLastName;
    }

    public IntegerProperty passengerOneBaggageCountProperty()
    {
        return passengerOneBaggageCount;
    }

    public StringProperty PassengerTwoFirstNameProperty() {
        return passengerTwoFirstName;
    }

    public StringProperty PassengerTwoLastNameProperty()
    {
        return passengerTwoLastName;
    }

    public IntegerProperty PassengerTwoBaggageCountProperty()
    {
        return passengerTwoBaggageCount;
    }

    public ObjectProperty<SeatClass> passengerOneSeatClassProperty()
    {
        return passengerOneSeatClass;
    }

    public ObjectProperty<SeatClass> passengerTwoSeatClassProperty()
    {
        return passengerTwoSeatClass;
    }

    public StringProperty passengerOneSeatTextProperty()
    {
        return passengerOneSeatText;
    }

    public StringProperty passengerTwoSeatTextProperty()
    {
        return passengerTwoSeatText;
    }

    public DoubleProperty totalPriceProperty()
    {
        return totalPrice;
    }

    public StringProperty flightNumberProperty() { return flightNumber; }
    public StringProperty departureCityProperty() { return departureCity; }
    public StringProperty arrivalCityProperty() { return arrivalCity; }
    public StringProperty departureTimeProperty() { return departureTime; }
    public StringProperty arrivalTimeProperty() { return arrivalTime; }
    public StringProperty routeSummaryProperty() { return routeSummary; }

    public void setSelectedFlight(Flight flight)
    {
        this.selectedFlight.set(flight);
        clearSeatForPassenger(1);
        clearSeatForPassenger(2);
        updateTotalPrice();
    }

    // recalculates total price using the base price from the database
    private void updateTotalPrice()
    {
        if (selectedFlight.get() != null)
        {
            double base = selectedFlight.get().getBasePrice();
            totalPrice.set(base + (passengerOneBaggageCount.get() * 10)
                + (passengerTwoBaggageCount.get() * 10));
        }
    }

    public void clear() {
        passengerOneFirstName.set("");
        passengerOneLastName.set("");
        passengerOneBaggageCount.set(0);
        passengerTwoFirstName.set("");
        passengerTwoLastName.set("");
        passengerTwoBaggageCount.set(0);
        passengerOneSeatClass.set(SeatClass.Economy);
        passengerTwoSeatClass.set(SeatClass.Economy);
        clearSeatForPassenger(1);
        clearSeatForPassenger(2);
        updateTotalPrice();
    }
}
