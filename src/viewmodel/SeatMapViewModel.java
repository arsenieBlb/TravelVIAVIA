package viewmodel;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import model.Seat;
import model.SeatClass;

import java.util.List;

public class SeatMapViewModel {
    private PassengerDetailsViewModel passengerDetailsViewModel;
    private int currentPassengerNumber;
    private ObjectProperty<Seat> temporarySelection = new SimpleObjectProperty<>();

    public SeatMapViewModel(PassengerDetailsViewModel passengerDetailsViewModel) {
        this.passengerDetailsViewModel = passengerDetailsViewModel;
    }

    public void startSelection(int passengerNumber) {
        this.currentPassengerNumber = passengerNumber;
        this.temporarySelection.set(passengerDetailsViewModel.getSelectedSeatForPassenger(passengerNumber));
    }

    public List<Seat> getSeats() {
        return passengerDetailsViewModel.getSeatMapSeats();
    }

    public ObjectProperty<Seat> temporarySelectionProperty() {
        return temporarySelection;
    }

    public boolean isSeatTaken(Seat seat) {
        return passengerDetailsViewModel.isSeatTaken(seat);
    }

    public boolean isSeatAlreadySelectedByOtherPassenger(Seat seat) {
        return passengerDetailsViewModel.isSeatAlreadySelectedByOtherPassenger(seat, currentPassengerNumber);
    }

    public SeatClass getSelectedClass() {
        return passengerDetailsViewModel.getSeatClassForPassenger(currentPassengerNumber);
    }

    public void confirmSelection() {
        if (temporarySelection.get() != null) {
            passengerDetailsViewModel.selectSeatForPassenger(currentPassengerNumber, temporarySelection.get());
        }
    }
    
    public void cancelSelection() {
        temporarySelection.set(null);
    }

    public void clear() {
        currentPassengerNumber = 0;
        temporarySelection.set(null);
    }
}
