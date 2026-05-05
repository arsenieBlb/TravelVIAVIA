package viewmodel;
 
import javafx.beans.property.*;
import model.Flight;
import model.Seat;
import model.SeatClass;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
 
public class SeatMapViewModel {
    private FlightSceneViewModel flightSceneViewModel;
    private int currentPassengerNumber;
    private ObjectProperty<Seat> temporarySelection = new SimpleObjectProperty<>();
 
    private ObjectProperty<SeatClass> passengerOneSeatClass = new SimpleObjectProperty<>(SeatClass.Economy);
    private ObjectProperty<SeatClass> passengerTwoSeatClass = new SimpleObjectProperty<>(SeatClass.Economy);
    private StringProperty passengerOneSeatText = new SimpleStringProperty("");
    private StringProperty passengerTwoSeatText = new SimpleStringProperty("");
    private ObjectProperty<Seat> passengerOneSelectedSeat = new SimpleObjectProperty<>();
    private ObjectProperty<Seat> passengerTwoSelectedSeat = new SimpleObjectProperty<>();
 
    public SeatMapViewModel(FlightSceneViewModel flightSceneViewModel) {
        this.flightSceneViewModel = flightSceneViewModel;
        
        passengerOneSeatClass.addListener((obs, old, newVal) -> clearSeatIfClassChanged(1));
        passengerTwoSeatClass.addListener((obs, old, newVal) -> clearSeatIfClassChanged(2));
    }
 
    public void startSelection(int passengerNumber) {
        this.currentPassengerNumber = passengerNumber;
        this.temporarySelection.set(getSelectedSeatForPassenger(passengerNumber));
    }
 
    public List<Seat> getSeats() {
        List<Seat> seats = new ArrayList<>();
        Flight flight = flightSceneViewModel.getSelectedFlight();
        if (flight != null) {
            seats.addAll(flight.getPlane().getSeats());
        }
        seats.sort(Comparator.comparingInt(Seat::getRowNumber)
                .thenComparing(Seat::getSeatNumber));
        return seats;
    }
 
    public ObjectProperty<Seat> temporarySelectionProperty() {
        return temporarySelection;
    }
 
    public boolean isSeatTaken(Seat seat) {
        Flight flight = flightSceneViewModel.getSelectedFlight();
        if (flight == null || seat == null) return true;
        return !flight.getAvailableSeats().contains(seat);
    }
 
    public boolean isSeatAlreadySelectedByOtherPassenger(Seat seat) {
        if (seat == null) return false;
        if (currentPassengerNumber == 1) return seat.equals(passengerTwoSelectedSeat.get());
        return seat.equals(passengerOneSelectedSeat.get());
    }
 
    public SeatClass getSelectedClass() {
        return getSeatClassForPassenger(currentPassengerNumber);
    }
 
    public void confirmSelection() {
        if (temporarySelection.get() != null) {
            selectSeatForPassenger(currentPassengerNumber, temporarySelection.get());
        }
    }
 
    public void selectSeatForPassenger(int passengerNumber, Seat seat) {
        if (seat == null) {
            clearSeatForPassenger(passengerNumber);
            return;
        }
        selectedSeatProperty(passengerNumber).set(seat);
        seatTextProperty(passengerNumber).set(seat.getSeatNumber());
    }
 
    public void clearSeatForPassenger(int passengerNumber) {
        selectedSeatProperty(passengerNumber).set(null);
        seatTextProperty(passengerNumber).set("");
    }
 
    public Seat getSelectedSeatForPassenger(int passengerNumber) {
        return selectedSeatProperty(passengerNumber).get();
    }
 
    public SeatClass getSeatClassForPassenger(int passengerNumber) {
        return seatClassProperty(passengerNumber).get();
    }
 
    private void clearSeatIfClassChanged(int passengerNumber) {
        Seat selectedSeat = getSelectedSeatForPassenger(passengerNumber);
        if (selectedSeat != null && selectedSeat.getSeatClass() != getSeatClassForPassenger(passengerNumber)) {
            clearSeatForPassenger(passengerNumber);
        }
    }
 
    private ObjectProperty<SeatClass> seatClassProperty(int passengerNumber) {
        return passengerNumber == 1 ? passengerOneSeatClass : passengerTwoSeatClass;
    }
 
    private StringProperty seatTextProperty(int passengerNumber) {
        return passengerNumber == 1 ? passengerOneSeatText : passengerTwoSeatText;
    }
 
    private ObjectProperty<Seat> selectedSeatProperty(int passengerNumber) {
        return passengerNumber == 1 ? passengerOneSelectedSeat : passengerTwoSelectedSeat;
    }
 
    public ObjectProperty<SeatClass> passengerOneSeatClassProperty() { return passengerOneSeatClass; }
    public ObjectProperty<SeatClass> passengerTwoSeatClassProperty() { return passengerTwoSeatClass; }
    public StringProperty passengerOneSeatTextProperty() { return passengerOneSeatText; }
    public StringProperty passengerTwoSeatTextProperty() { return passengerTwoSeatText; }
    public ObjectProperty<Seat> passengerOneSelectedSeatProperty() { return passengerOneSelectedSeat; }
    public ObjectProperty<Seat> passengerTwoSelectedSeatProperty() { return passengerTwoSelectedSeat; }
 
    public void cancelSelection() {
        temporarySelection.set(null);
    }
 
    public void clear() {
        passengerOneSeatClass.set(SeatClass.Economy);
        passengerTwoSeatClass.set(SeatClass.Economy);
        clearSeatForPassenger(1);
        clearSeatForPassenger(2);
    }
}
