package viewmodel;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Booking;
import model.Customer;
import model.Flight;
import model.LuggageType;
import model.Model;
import model.Passenger;
import model.PassengerLuggage;
import model.Seat;
import model.SeatClass;
import model.User;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PassengerDetailsViewModel
{
  private static int nextDraftPassengerId = 1000;
  private static int nextDraftLuggageId = 1000;
  public static final double CARRY_ON_UNIT_PRICE = 15;
  public static final int MAX_CARRY_ON_BAGS = 2;
  public static final double BUSINESS_CLASS_MULTIPLIER = 1.5;

  private final Model model;
  private final BookFlightViewModel bookFlightViewModel;
  private final ObservableList<PassengerForm> passengerForms =
      FXCollections.observableArrayList();
  private final ObjectProperty<Flight> selectedFlight =
      new SimpleObjectProperty<>();
  private final DoubleProperty baseFare = new SimpleDoubleProperty(0);
  private final DoubleProperty carryOnFare = new SimpleDoubleProperty(0);
  private final DoubleProperty baggageFare = new SimpleDoubleProperty(0);
  private final DoubleProperty totalFare = new SimpleDoubleProperty(0);

  public PassengerDetailsViewModel(Model model,
      BookFlightViewModel bookFlightViewModel)
  {
    this.model = model;
    this.bookFlightViewModel = bookFlightViewModel;
  }

  public void prepare()
  {
    Flight flight = bookFlightViewModel.getSelectedFlight();
    if (flight == null)
    {
      throw new IllegalStateException("Please select a flight first.");
    }

    boolean changedFlight = selectedFlight.get() == null
        || !selectedFlight.get().equals(flight);
    selectedFlight.set(flight);

    int passengerCount = bookFlightViewModel.passengerCountProperty().get();
    while (passengerForms.size() < passengerCount)
    {
      PassengerForm form = createPassengerForm(passengerForms.size() + 1);
      passengerForms.add(form);
    }
    while (passengerForms.size() > passengerCount)
    {
      passengerForms.remove(passengerForms.size() - 1);
    }

    if (changedFlight)
    {
      for (PassengerForm form : passengerForms)
      {
        form.setSelectedSeat(null);
      }
    }

    prefillFirstPassenger();
    updateFareTotals();
  }

  private PassengerForm createPassengerForm(int passengerNumber)
  {
    PassengerForm form = new PassengerForm(passengerNumber);
    form.carryOnQuantityProperty().addListener((obs, oldValue, newValue) ->
        updateFareTotals());
    form.baggageQuantityProperty().addListener((obs, oldValue, newValue) ->
        updateFareTotals());
    form.seatClassProperty().addListener((obs, oldValue, newValue) -> {
      Seat selectedSeat = form.getSelectedSeat();
      if (selectedSeat != null && selectedSeat.getSeatClass() != newValue)
      {
        form.setSelectedSeat(null);
      }
      updateFareTotals();
    });
    return form;
  }

  private void prefillFirstPassenger()
  {
    if (passengerForms.isEmpty())
    {
      return;
    }

    User user = model.getLoggedInUser();
    if (!(user instanceof Customer customer))
    {
      return;
    }

    PassengerForm firstPassenger = passengerForms.get(0);
    if (firstPassenger.getFirstName().isBlank())
    {
      firstPassenger.setFirstName(customer.getFirstName());
    }
    if (firstPassenger.getLastName().isBlank())
    {
      firstPassenger.setLastName(customer.getLastName());
    }
  }

  public Booking confirmBooking()
  {
    Flight flight = selectedFlight.get();
    if (flight == null)
    {
      throw new IllegalStateException("Please select a flight first.");
    }

    List<Passenger> passengers = new ArrayList<>();
    List<Seat> selectedSeats = new ArrayList<>();
    LuggageType carryOnLuggage = getCarryOnLuggageType();
    LuggageType checkedLuggage = getCheckedLuggageType();

    for (PassengerForm form : passengerForms)
    {
      Passenger passenger = new Passenger(nextDraftPassengerId++,
          requireText(form.getFirstName(), "First name"),
          requireText(form.getLastName(), "Last name"));

      int carryOnQuantity = form.getCarryOnQuantity();
      if (carryOnQuantity > 0 && carryOnLuggage != null)
      {
        passenger.addPassengerLuggage(new PassengerLuggage(
            nextDraftLuggageId++, carryOnQuantity, carryOnLuggage));
      }

      int baggageQuantity = form.getBaggageQuantity();
      if (baggageQuantity > 0 && checkedLuggage != null)
      {
        passenger.addPassengerLuggage(new PassengerLuggage(
            nextDraftLuggageId++, baggageQuantity, checkedLuggage));
      }

      passengers.add(passenger);
      selectedSeats.add(form.getSelectedSeat());
    }

    Booking booking = model.createBooking(flight, passengers, selectedSeats);
    clearPassengerForms();
    bookFlightViewModel.clear();
    return booking;
  }

  private void clearPassengerForms()
  {
    for (PassengerForm form : passengerForms)
    {
      form.setFirstName("");
      form.setLastName("");
      form.carryOnQuantityProperty().set(1);
      form.baggageQuantityProperty().set(0);
      form.seatClassProperty().set(SeatClass.Economy);
      form.setSelectedSeat(null);
    }
  }

  private String requireText(String value, String fieldName)
  {
    if (value == null || value.isBlank())
    {
      throw new IllegalStateException(fieldName + " is required.");
    }
    return value.trim();
  }

  private void updateFareTotals()
  {
    Flight flight = selectedFlight.get();
    if (flight == null)
    {
      baseFare.set(0);
      carryOnFare.set(0);
      baggageFare.set(0);
      totalFare.set(0);
      return;
    }

    double base = 0;
    for (PassengerForm form : passengerForms)
    {
      base += calculateBaseFareForPassenger(flight, form);
    }
    double carryOn = CARRY_ON_UNIT_PRICE * getTotalCarryOnQuantity();
    double baggage = getBaggageUnitPrice() * getTotalBaggageQuantity();

    baseFare.set(base);
    carryOnFare.set(carryOn);
    baggageFare.set(baggage);
    totalFare.set(base + carryOn + baggage);
  }

  public int getTotalCarryOnQuantity()
  {
    int total = 0;
    for (PassengerForm form : passengerForms)
    {
      total += form.getCarryOnQuantity();
    }
    return total;
  }

  public int getTotalBaggageQuantity()
  {
    int total = 0;
    for (PassengerForm form : passengerForms)
    {
      total += form.getBaggageQuantity();
    }
    return total;
  }

  public double getBaggageUnitPrice()
  {
    LuggageType checkedLuggage = getCheckedLuggageType();
    return checkedLuggage == null ? 0 : checkedLuggage.getExtraPrice();
  }

  private double calculateBaseFareForPassenger(Flight flight,
      PassengerForm form)
  {
    double passengerBaseFare = flight.getBasePrice();
    if (form.getSeatClass() == SeatClass.Business)
    {
      passengerBaseFare *= BUSINESS_CLASS_MULTIPLIER;
    }
    return passengerBaseFare;
  }

  public List<Flight> getFlightSegments()
  {
    List<Flight> segments = new ArrayList<>();
    if (selectedFlight.get() != null)
    {
      segments.add(selectedFlight.get());
    }
    return segments;
  }

  private LuggageType getCarryOnLuggageType()
  {
    for (LuggageType luggageType : model.getLuggageTypes())
    {
      if (luggageType.getName().toLowerCase().contains("carry"))
      {
        luggageType.setExtraPrice(CARRY_ON_UNIT_PRICE);
        return luggageType;
      }
    }
    return null;
  }

  private LuggageType getCheckedLuggageType()
  {
    List<LuggageType> luggageTypes = model.getLuggageTypes();
    if (luggageTypes == null || luggageTypes.isEmpty())
    {
      return null;
    }

    for (LuggageType luggageType : luggageTypes)
    {
      if (luggageType.getName().toLowerCase().contains("baggage"))
      {
        return luggageType;
      }
    }
    for (LuggageType luggageType : luggageTypes)
    {
      if (luggageType.getExtraPrice() > 0)
      {
        return luggageType;
      }
    }
    return luggageTypes.get(0);
  }

  public List<Seat> getSeatMapSeats()
  {
    List<Seat> seats = new ArrayList<>();
    if (selectedFlight.get() != null)
    {
      seats.addAll(selectedFlight.get().getPlane().getSeats());
    }
    seats.sort(Comparator.comparingInt(Seat::getRowNumber)
        .thenComparing(Seat::getSeatNumber));
    return seats;
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

    for (PassengerForm form : passengerForms)
    {
      if (form.getPassengerNumber() != passengerNumber
          && seat.equals(form.getSelectedSeat()))
      {
        return true;
      }
    }
    return false;
  }

  public SeatClass getSeatClassForPassenger(int passengerNumber)
  {
    PassengerForm form = getPassengerForm(passengerNumber);
    return form == null ? SeatClass.Economy : form.getSeatClass();
  }

  public ObjectProperty<SeatClass> seatClassPropertyForPassenger(
      int passengerNumber)
  {
    return ensurePassengerForm(passengerNumber).seatClassProperty();
  }

  public Seat getSelectedSeatForPassenger(int passengerNumber)
  {
    PassengerForm form = getPassengerForm(passengerNumber);
    return form == null ? null : form.getSelectedSeat();
  }

  public void selectSeatForPassenger(int passengerNumber, Seat seat)
  {
    PassengerForm form = getPassengerForm(passengerNumber);
    if (form == null)
    {
      return;
    }
    if (seat == null)
    {
      form.setSelectedSeat(null);
      return;
    }
    if (seat.getSeatClass() != form.getSeatClass())
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
          "Another passenger already selected this seat.");
    }
    form.setSelectedSeat(seat);
  }

  public void clearSeatForPassenger(int passengerNumber)
  {
    PassengerForm form = getPassengerForm(passengerNumber);
    if (form != null)
    {
      form.setSelectedSeat(null);
    }
  }

  private PassengerForm ensurePassengerForm(int passengerNumber)
  {
    while (passengerForms.size() < passengerNumber)
    {
      passengerForms.add(createPassengerForm(passengerForms.size() + 1));
    }
    return passengerForms.get(passengerNumber - 1);
  }

  private PassengerForm getPassengerForm(int passengerNumber)
  {
    for (PassengerForm form : passengerForms)
    {
      if (form.getPassengerNumber() == passengerNumber)
      {
        return form;
      }
    }
    return null;
  }

  public ObservableList<PassengerForm> getPassengerForms()
  {
    return passengerForms;
  }

  public Flight getSelectedFlight()
  {
    return selectedFlight.get();
  }

  public DoubleProperty baseFareProperty()
  {
    return baseFare;
  }

  public DoubleProperty baggageFareProperty()
  {
    return baggageFare;
  }

  public DoubleProperty carryOnFareProperty()
  {
    return carryOnFare;
  }

  public DoubleProperty totalFareProperty()
  {
    return totalFare;
  }

  public static class PassengerForm
  {
    private final int passengerNumber;
    private final StringProperty firstName = new SimpleStringProperty("");
    private final StringProperty lastName = new SimpleStringProperty("");
    private final IntegerProperty carryOnQuantity =
        new SimpleIntegerProperty(1);
    private final IntegerProperty baggageQuantity =
        new SimpleIntegerProperty(0);
    private final ObjectProperty<SeatClass> seatClass =
        new SimpleObjectProperty<>(SeatClass.Economy);
    private final ObjectProperty<Seat> selectedSeat =
        new SimpleObjectProperty<>();
    private final StringProperty selectedSeatText =
        new SimpleStringProperty("Not selected");

    PassengerForm(int passengerNumber)
    {
      this.passengerNumber = passengerNumber;
      selectedSeat.addListener((obs, oldSeat, newSeat) ->
          selectedSeatText.set(newSeat == null ? "Not selected"
              : newSeat.getSeatNumber()));
    }

    public int getPassengerNumber()
    {
      return passengerNumber;
    }

    public String getFirstName()
    {
      return firstName.get();
    }

    public void setFirstName(String value)
    {
      firstName.set(value == null ? "" : value);
    }

    public StringProperty firstNameProperty()
    {
      return firstName;
    }

    public String getLastName()
    {
      return lastName.get();
    }

    public void setLastName(String value)
    {
      lastName.set(value == null ? "" : value);
    }

    public StringProperty lastNameProperty()
    {
      return lastName;
    }

    public int getBaggageQuantity()
    {
      return baggageQuantity.get();
    }

    public int getCarryOnQuantity()
    {
      return carryOnQuantity.get();
    }

    public IntegerProperty carryOnQuantityProperty()
    {
      return carryOnQuantity;
    }

    public IntegerProperty baggageQuantityProperty()
    {
      return baggageQuantity;
    }

    public SeatClass getSeatClass()
    {
      SeatClass value = seatClass.get();
      return value == null ? SeatClass.Economy : value;
    }

    public ObjectProperty<SeatClass> seatClassProperty()
    {
      return seatClass;
    }

    public Seat getSelectedSeat()
    {
      return selectedSeat.get();
    }

    public void setSelectedSeat(Seat seat)
    {
      selectedSeat.set(seat);
    }

    public ObjectProperty<Seat> selectedSeatProperty()
    {
      return selectedSeat;
    }

    public StringProperty selectedSeatTextProperty()
    {
      return selectedSeatText;
    }
  }
}
