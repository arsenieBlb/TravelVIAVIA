package viewmodel;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import model.Seat;
import model.SeatClass;

import java.util.List;

public class SeatMapViewModel {
    private FlightSceneViewModel flightSceneViewModel;
    private int currentPassengerNumber;
    private ObjectProperty<Seat> temporarySelection = new SimpleObjectProperty<>();

    public SeatMapViewModel(FlightSceneViewModel flightSceneViewModel) {
        this.flightSceneViewModel = flightSceneViewModel;
    }

    public void startSelection(int passengerNumber) {
        this.currentPassengerNumber = passengerNumber;
        this.temporarySelection.set(flightSceneViewModel.getSelectedSeatForPassenger(passengerNumber));
    }

    public List<Seat> getSeats() {
        return flightSceneViewModel.getSeatMapSeats();
    }

    public ObjectProperty<Seat> temporarySelectionProperty() {
        return temporarySelection;
    }

    public boolean isSeatTaken(Seat seat) {
        return flightSceneViewModel.isSeatTaken(seat);
    }

    public boolean isSeatAlreadySelectedByOtherPassenger(Seat seat) {
        return flightSceneViewModel.isSeatAlreadySelectedByOtherPassenger(seat, currentPassengerNumber);
    }

    public SeatClass getSelectedClass() {
        return flightSceneViewModel.getSeatClassForPassenger(currentPassengerNumber);
    }

    public void confirmSelection() {
        if (temporarySelection.get() != null) {
            flightSceneViewModel.selectSeatForPassenger(currentPassengerNumber, temporarySelection.get());
        }
    }
    
    public void cancelSelection() {
        temporarySelection.set(null);
    }
}
