package viewmodel;

import javafx.beans.property.*;
import model.*;

import java.time.LocalDateTime;
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

    public FlightSceneViewModel(model.Model model)
    {
        this.model = model;
    }

    public void confirmBooking()
    {
        try
        {
            List<Passenger> passengers = new ArrayList<>();

            Passenger p1 = new Passenger(1, passengerOneFirstName.get(), passengerOneLastName.get());

            if (passengerOneBaggageCount.get() > 0)
            {
                // In theory, the type of luggage should be derived from the model... in short, we'll figure it out
            }

            passengers.add(p1);

            if (selectedFlight.get() != null)
            {
                model.createBooking(selectedFlight.get(), passengers);
                System.out.println("Booking confirmed!");
            }

        }
        catch (Exception e)
        {
            System.err.println("Booking failed: " + e.getMessage());
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

    public StringProperty PassengerTwoFirstNameProperty()
    {
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

    private void updateTotalPrice()
    {
        if (selectedFlight.get() != null)
        {
            double base = selectedFlight.get().getBasePrice();
            totalPrice.set(base + (passengerOneBaggageCount.get() * 10));
        }
    }

    public void clear()
    {
        passengerOneFirstName.set("");
        passengerOneLastName.set("");
        passengerOneBaggageCount.set(0);
        passengerTwoFirstName.set("");
        passengerTwoLastName.set("");
        passengerTwoBaggageCount.set(0);
        totalPrice.set(0);
    }
}
