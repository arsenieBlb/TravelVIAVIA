package client.viewmodel;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import client.model.Booking;
import client.model.Flight;
import client.model.Model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class DashboardViewModel implements PropertyChangeListener {
    private final StringProperty totalFlights;
    private final StringProperty activeBookings;
    private final StringProperty totalPassengers;
    private final Model model;

    public DashboardViewModel(Model model) {
        this.model = model;
        this.totalFlights = new SimpleStringProperty();
        this.activeBookings = new SimpleStringProperty();
        this.totalPassengers = new SimpleStringProperty();
        this.model.addPropertyChangeListener(this);

        updateStats();
    }

    public void updateStats() {
        List<Flight> flights = model.getAllFlights();
        List<Booking> bookings = model.getAllBookings();

        totalFlights.set(String.valueOf(flights.size()));
        activeBookings.set(String.valueOf(bookings.size()));

        int passengerCount = 0;
        for (int i = 0; i < bookings.size(); i++) {
            passengerCount += bookings.get(i).getPassengers().size();
        }
        totalPassengers.set(String.valueOf(passengerCount));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Platform.runLater(this::updateStats);
    }

    public StringProperty totalFlightsProperty() { return totalFlights; }
    public StringProperty activeBookingsProperty() { return activeBookings; }
    public StringProperty totalPassengersProperty() { return totalPassengers; }
}

