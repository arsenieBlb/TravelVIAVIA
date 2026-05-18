package client.viewmodel;

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
import client.model.Booking;
import client.model.Customer;
import client.model.Flight;
import client.model.LuggageType;
import client.model.Model;
import client.model.Passenger;
import client.model.PassengerLuggage;
import client.model.Seat;
import client.model.SeatClass;
import client.model.User;
import client.model.EconomyClass;
import client.model.BusinessClass;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class PassengerDetailsViewModel
{
  private static int nextDraftPassengerId = 1000;
  private static int nextDraftLuggageId = 1000;
  public static final double CARRY_ON_UNIT_PRICE = 15;
  //public static final int MAX_CARRY_ON_BAGS = 2;
  public static final double BUSINESS_CLASS_MULTIPLIER = 1.5;

  private final Model model;
  private final FlightSceneViewModel flightSceneViewModel;
  private final ObservableList<PassengerForm> passengerForms =
      FXCollections.observableArrayList();
  private final ObjectProperty<Flight> selectedFlight =
      new SimpleObjectProperty<>();
  private final ObjectProperty<Flight> selectedReturnFlight =
      new SimpleObjectProperty<>();
  private final DoubleProperty baseFare = new SimpleDoubleProperty(0);
  private final DoubleProperty carryOnFare = new SimpleDoubleProperty(0);
  private final DoubleProperty baggageFare = new SimpleDoubleProperty(0);
  private final DoubleProperty totalFare = new SimpleDoubleProperty(0);

  public PassengerDetailsViewModel(Model model,
      FlightSceneViewModel bookFlightViewModel)
  {
    this.model = model;
    this.flightSceneViewModel = bookFlightViewModel;
  }

  public void prepare()
  {
    Flight flight = flightSceneViewModel.getSelectedFlight();
    if (flight == null)
    {
      throw new IllegalStateException("Please select a flight first.");
    }

    boolean changedFlight = selectedFlight.get() == null
        || !selectedFlight.get().equals(flight);
    selectedFlight.set(flight);
    selectedReturnFlight.set(flightSceneViewModel.getSelectedReturnFlight());

    int passengerCount = flightSceneViewModel.passengerCountProperty().get();
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
        for (int i = 0; i < getMaxSegmentCount(); i++) {
            form.setSelectedSeat(i, null);
        }
      }
    }

//    prefillFirstPassenger();
    updateFareTotals();
  }

  private PassengerForm createPassengerForm(int passengerNumber)
  {
    PassengerForm form = new PassengerForm(passengerNumber);
    form.carryOnQuantityProperty().addListener((obs, oldValue, newValue) ->
        updateFareTotals());
    form.baggageQuantityProperty().addListener((obs, oldValue, newValue) ->
        updateFareTotals());
        
    for (int i = 0; i < 4; i++) {
        final int index = i;
        form.seatClassProperty(index).addListener((obs, oldValue, newValue) -> {
          Seat selectedSeat = form.getSelectedSeat(index);
          if (selectedSeat != null && !selectedSeat.getSeatClass().getClass().equals(newValue.getClass()))
          {
            form.setSelectedSeat(index, null);
          }
          updateFareTotals();
        });
    }
    return form;
  }

//  private void prefillFirstPassenger()
//  {
//    if (passengerForms.isEmpty())
//    {
//      return;
//    }
//
//    User user = model.getLoggedInUser();
//    if (!(user instanceof Customer customer))
//    {
//      return;
//    }
//
//    PassengerForm firstPassenger = passengerForms.get(0);
//    if (firstPassenger.getFirstName().isBlank())
//    {
//      firstPassenger.setFirstName(customer.getFirstName());
//    }
//    if (firstPassenger.getLastName().isBlank())
//    {
//      firstPassenger.setLastName(customer.getLastName());
//    }
//  }

  public Booking confirmBooking()
  {
    Flight flight = selectedFlight.get();
    if (flight == null)
    {
      throw new IllegalStateException("Please select a flight first.");
    }

    List<Flight> segments = getFlightSegments();
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

      for (int segmentIndex = 0; segmentIndex < segments.size(); segmentIndex++) {
          selectedSeats.add(form.getSelectedSeat(segmentIndex));
      }
      passengers.add(passenger);
    }

    // collect return seats if roundtrip
    Flight returnFlight = selectedReturnFlight.get();
    List<Seat> returnSeats = null;
    if (returnFlight != null) {
        returnSeats = new ArrayList<>();
        List<Flight> returnSegments = getReturnFlightSegments();
        int outboundSegmentCount = getFlightSegments().size();

        for (PassengerForm form : passengerForms) {
            for (int segmentIndex = 0; segmentIndex < returnSegments.size(); segmentIndex++) {
                returnSeats.add(form.getSelectedSeat(outboundSegmentCount + segmentIndex));
            }
        }
    }

    // create a single booking with both outbound and return
    Booking booking = model.createBooking(flight, passengers, selectedSeats,
        returnFlight, returnSeats);

    clearPassengerForms();
    flightSceneViewModel.clear();
    return booking;
  }

    private void clearPassengerForms()
    {
        for (PassengerForm form : passengerForms)
        {
            form.setFirstName("");
            form.setLastName("");
            form.carryOnQuantityProperty().set(0);
            form.baggageQuantityProperty().set(0);
            for (int i = 0; i < getMaxSegmentCount(); i++)
            {
                form.seatClassProperty(i).set(new EconomyClass());
                form.setSelectedSeat(i, null);
            }
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

    private double calculateBaseFareForPassenger(Flight flight, PassengerForm form)
    {
        double passengerBaseFare = 0;
        List<Flight> segments = getAllSegments();

        for (int i = 0; i < segments.size(); i++)
        {
            double segmentBaseFare = segments.get(i).getBasePrice();
            passengerBaseFare += segmentBaseFare * form.getSeatClass(i).getPriceMultiplier();
        }
        return passengerBaseFare;
    }

  public List<Flight> getFlightSegments()
  {
    List<Flight> segments = new ArrayList<>();
    if (selectedFlight.get() != null)
    {
      // if it is a connecting flight we need to add both segments
      if (selectedFlight.get() instanceof client.model.ConnectingFlight connectingFlight) {
        segments.add(connectingFlight.getFirstSegment());
        segments.add(connectingFlight.getSecondSegment());
      } else {
        segments.add(selectedFlight.get());
      }
    }
    return segments;
  }

  public List<Flight> getReturnFlightSegments()
  {
    List<Flight> segments = new ArrayList<>();
    if (selectedReturnFlight.get() != null)
    {
      if (selectedReturnFlight.get() instanceof client.model.ConnectingFlight connectingFlight) {
        segments.add(connectingFlight.getFirstSegment());
        segments.add(connectingFlight.getSecondSegment());
      } else {
        segments.add(selectedReturnFlight.get());
      }
    }
    return segments;
  }

  public List<Flight> getAllSegments()
  {
    List<Flight> all = new ArrayList<>(getFlightSegments());
    all.addAll(getReturnFlightSegments());
    return all;
  }

  public int getMaxSegmentCount()
  {
    return getAllSegments().size();
  }

  public Flight getSelectedReturnFlight()
  {
    return selectedReturnFlight.get();
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

  public List<Seat> getSeatMapSeats(int segmentIndex)
  {
    List<Seat> seats = new ArrayList<>();
    List<Flight> segments = getAllSegments();
    if (segmentIndex < segments.size())
    {
      seats.addAll(segments.get(segmentIndex).getPlane().getSeats());
    }
    seats.sort(Comparator.comparingInt(Seat::getRowNumber)
        .thenComparing(Seat::getSeatNumber));
    return seats;
  }

  public boolean isSeatTaken(Seat seat, int segmentIndex)
  {
    List<Flight> segments = getAllSegments();
    if (seat == null || segmentIndex >= segments.size())
    {
      return true;
    }
    return !segments.get(segmentIndex).getAvailableSeats().contains(seat);
  }

  public boolean isSeatAlreadySelectedByOtherPassenger(Seat seat,
      int passengerNumber, int segmentIndex)
  {
    if (seat == null)
    {
      return false;
    }

    for (PassengerForm form : passengerForms)
    {
      if (form.getPassengerNumber() != passengerNumber
          && seat.equals(form.getSelectedSeat(segmentIndex)))
      {
        return true;
      }
    }
    return false;
  }

    public SeatClass getSeatClassForPassenger(int passengerNumber, int segmentIndex)
    {
        PassengerForm form = getPassengerForm(passengerNumber);
        return form == null ? new EconomyClass() : form.getSeatClass(segmentIndex);
    }

  public ObjectProperty<SeatClass> seatClassPropertyForPassenger(
      int passengerNumber, int segmentIndex)
  {
    return ensurePassengerForm(passengerNumber).seatClassProperty(segmentIndex);
  }

  public Seat getSelectedSeatForPassenger(int passengerNumber, int segmentIndex)
  {
    PassengerForm form = getPassengerForm(passengerNumber);
    return form == null ? null : form.getSelectedSeat(segmentIndex);
  }

  public void selectSeatForPassenger(int passengerNumber, int segmentIndex, Seat seat)
  {
    PassengerForm form = getPassengerForm(passengerNumber);
    if (form == null)
    {
      return;
    }
    if (seat == null)
    {
      form.setSelectedSeat(segmentIndex, null);
      return;
    }
    if (!seat.getSeatClass().getClass().equals(form.getSeatClass(segmentIndex).getClass()))
    {
        throw new IllegalArgumentException("Seat does not match the selected class.");
    }
    if (isSeatTaken(seat, segmentIndex))
    {
      throw new IllegalArgumentException("Seat is already taken.");
    }
    if (isSeatAlreadySelectedByOtherPassenger(seat, passengerNumber, segmentIndex))
    {
      throw new IllegalArgumentException(
          "Another passenger already selected this seat.");
    }
    form.setSelectedSeat(segmentIndex, seat);
  }

  public void clearSeatForPassenger(int passengerNumber, int segmentIndex)
  {
    PassengerForm form = getPassengerForm(passengerNumber);
    if (form != null)
    {
      form.setSelectedSeat(segmentIndex, null);
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
        new SimpleIntegerProperty(0);
    private final IntegerProperty baggageQuantity =
        new SimpleIntegerProperty(0);
    private final List<ObjectProperty<SeatClass>> seatClasses = new ArrayList<>();
    private final List<ObjectProperty<Seat>> selectedSeats = new ArrayList<>();
    private final List<StringProperty> selectedSeatTexts = new ArrayList<>();

      PassengerForm(int passengerNumber)
      {
          this.passengerNumber = passengerNumber;

          for (int i = 0; i < 4; i++)
          {
              seatClasses.add(new SimpleObjectProperty<>(new client.model.EconomyClass()));

              ObjectProperty<client.model.Seat> seatProp = new SimpleObjectProperty<>();
              selectedSeats.add(seatProp);

              StringProperty textProp = new SimpleStringProperty("Not selected");
              selectedSeatTexts.add(textProp);

              seatProp.addListener((obs, oldVal, newVal) ->
                      textProp.set(Objects.toString(newVal, "Not selected")));
          }
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

    public SeatClass getSeatClass(int segmentIndex)
    {
        return Objects.requireNonNullElse(seatClasses.get(segmentIndex).get(), new EconomyClass());
    }

    public ObjectProperty<SeatClass> seatClassProperty(int segmentIndex)
    {
      return seatClasses.get(segmentIndex);
    }

    public Seat getSelectedSeat(int segmentIndex)
    {
      return selectedSeats.get(segmentIndex).get();
    }

    public void setSelectedSeat(int segmentIndex, Seat seat)
    {
      selectedSeats.get(segmentIndex).set(seat);
    }

    public ObjectProperty<Seat> selectedSeatProperty(int segmentIndex)
    {
      return selectedSeats.get(segmentIndex);
    }

    public StringProperty selectedSeatTextProperty(int segmentIndex)
    {
      return selectedSeatTexts.get(segmentIndex);
    }
  }
}


