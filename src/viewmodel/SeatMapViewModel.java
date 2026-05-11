package viewmodel;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import model.Seat;
import model.SeatClass;

import java.util.List;

public class SeatMapViewModel {
    private PassengerDetailsViewModel passengerDetailsViewModel;
    private int currentPassengerNumber;
    private int currentSegmentIndex;
    private ObjectProperty<Seat> temporarySelection = new SimpleObjectProperty<>();

    public SeatMapViewModel(PassengerDetailsViewModel passengerDetailsViewModel) {
        this.passengerDetailsViewModel = passengerDetailsViewModel;
    }

    public void startSelection(int passengerNumber, int segmentIndex) {
        this.currentPassengerNumber = passengerNumber;
        this.currentSegmentIndex = segmentIndex;
        this.temporarySelection.set(passengerDetailsViewModel.getSelectedSeatForPassenger(passengerNumber, segmentIndex));
    }

    public List<Seat> getSeats() {
        return passengerDetailsViewModel.getSeatMapSeats(currentSegmentIndex);
    }

    public ObjectProperty<Seat> temporarySelectionProperty() {
        return temporarySelection;
    }

    public boolean isSeatTaken(Seat seat) {
        return passengerDetailsViewModel.isSeatTaken(seat, currentSegmentIndex);
    }

    public boolean isSeatAlreadySelectedByOtherPassenger(Seat seat) {
        return passengerDetailsViewModel.isSeatAlreadySelectedByOtherPassenger(seat, currentPassengerNumber, currentSegmentIndex);
    }

    public SeatClass getSelectedClass() {
        return passengerDetailsViewModel.getSeatClassForPassenger(currentPassengerNumber, currentSegmentIndex);
    }

    public ObjectProperty<SeatClass> passengerOneSeatClassProperty() {
        return passengerDetailsViewModel.seatClassPropertyForPassenger(1, 0);
    }

    public ObjectProperty<SeatClass> passengerTwoSeatClassProperty() {
        return passengerDetailsViewModel.seatClassPropertyForPassenger(2, 0);
    }

    public Seat getSelectedSeatForPassenger(int passengerNumber) {
        return passengerDetailsViewModel.getSelectedSeatForPassenger(passengerNumber, currentSegmentIndex);
    }

    public SeatClass getSeatClassForPassenger(int passengerNumber) {
        return passengerDetailsViewModel.getSeatClassForPassenger(passengerNumber, currentSegmentIndex);
    }

    public void clearSeatForPassenger(int passengerNumber) {
        passengerDetailsViewModel.clearSeatForPassenger(passengerNumber, currentSegmentIndex);
    }

    public void confirmSelection() {
        if (temporarySelection.get() != null) {
            passengerDetailsViewModel.selectSeatForPassenger(currentPassengerNumber, currentSegmentIndex, temporarySelection.get());
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
