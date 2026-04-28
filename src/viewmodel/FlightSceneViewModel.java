package viewmodel;

import javafx.beans.property.*;
import model.*;
import java.util.ArrayList;
import java.util.List;

public class FlightSceneViewModel
{
    private Model model;

    private StringProperty passengerOneFirstName = new SimpleStringProperty("");
    private StringProperty passengerOneLastName = new SimpleStringProperty("");
    private IntegerProperty passengerOneBaggageCount = new SimpleIntegerProperty(0);
    private DoubleProperty totalPrice = new SimpleDoubleProperty(0.0);

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

    public DoubleProperty totalPriceProperty()
    {
        return totalPrice;
    }

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
}
