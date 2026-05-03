package viewmodel;

import javafx.beans.property.*;
import model.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FlightSceneViewModel
{
    private Model model;

    private StringProperty flightNumber = new SimpleStringProperty("");
    private StringProperty departureCity = new SimpleStringProperty("");
    private StringProperty arrivalCity = new SimpleStringProperty("");
    private StringProperty departureTime = new SimpleStringProperty("");
    private StringProperty arrivalTime = new SimpleStringProperty("");
    
    private StringProperty passengerOneFirstName = new SimpleStringProperty("");
    private StringProperty passengerOneLastName = new SimpleStringProperty("");
    private IntegerProperty passengerOneBaggageCount = new SimpleIntegerProperty(0);

    private StringProperty passengerTwoFirstName = new SimpleStringProperty("");
    private StringProperty passengerTwoLastName = new SimpleStringProperty("");
    private IntegerProperty passengerTwoBaggageCount = new SimpleIntegerProperty(0);

    private DoubleProperty totalPrice = new SimpleDoubleProperty(0);

    private ObjectProperty<Flight> selectedFlight = new SimpleObjectProperty<>();

    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public FlightSceneViewModel(model.Model model)
    {
        this.model = model;

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
        }
    }

    public void confirmBooking()
    {
        try {
            List<Passenger> passengers = new ArrayList<>();

            Passenger p1 = new Passenger(1, passengerOneFirstName.get(), passengerOneLastName.get());

            if (passengerOneBaggageCount.get() > 0) {
                // luggage type will come from the database in a future update
            }

            passengers.add(p1);

            if (selectedFlight.get() != null)
            {
                model.createBooking(selectedFlight.get(), passengers);
                System.out.println("Booking confirmed!");
            }
        } catch (Exception e) {
            System.out.println("Failed to make booking");
        }
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

    public DoubleProperty totalPriceProperty()
    {
        return totalPrice;
    }

    public StringProperty flightNumberProperty() { return flightNumber; }
    public StringProperty departureCityProperty() { return departureCity; }
    public StringProperty arrivalCityProperty() { return arrivalCity; }
    public StringProperty departureTimeProperty() { return departureTime; }
    public StringProperty arrivalTimeProperty() { return arrivalTime; }

    public void setSelectedFlight(Flight flight)
    {
        this.selectedFlight.set(flight);
        updateTotalPrice();
    }
    
    // recalculates total price using the base price from the database
    private void updateTotalPrice()
    {
        if (selectedFlight.get() != null)
        {
            double base = selectedFlight.get().getBasePrice();
            totalPrice.set(base + (passengerOneBaggageCount.get() * 10));
        }
    }

    public void clear() {
        passengerOneFirstName.set("");
        passengerOneLastName.set("");
        passengerOneBaggageCount.set(0);
        passengerTwoFirstName.set("");
        passengerTwoLastName.set("");
        passengerTwoBaggageCount.set(0);
        totalPrice.set(0);
    }
}
