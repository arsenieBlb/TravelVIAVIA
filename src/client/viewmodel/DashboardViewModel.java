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
        javafx.concurrent.Task<String[]> task = new javafx.concurrent.Task<>() {
            @Override
            protected String[] call() {
                List<Flight> flights = model.getAllFlights();
                List<Booking> bookings = model.getAllBookings();

                int passengerCount = 0;
                for (int i = 0; i < bookings.size(); i++) {
                    passengerCount += bookings.get(i).getPassengers().size();
                }
                return new String[]{
                        String.valueOf(flights.size()),
                        String.valueOf(bookings.size()),
                        String.valueOf(passengerCount)
                };
            }
        };

        task.setOnSucceeded(e -> {
            String[] result = task.getValue();
            totalFlights.set(result[0]);
            activeBookings.set(result[1]);
            totalPassengers.set(result[2]);
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Platform.runLater(this::updateStats);
    }

    public StringProperty totalFlightsProperty() { return totalFlights; }
    public StringProperty activeBookingsProperty() { return activeBookings; }
    public StringProperty totalPassengersProperty() { return totalPassengers; }
}

