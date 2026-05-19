package client.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import client.model.BusinessClass;

public class Booking
{
  private static int nextBookingId = 1;

  private int bookingId;
  private LocalDateTime bookingDate;
  private double totalPrice;
  private Customer customer;
  private Flight flight;
  private Flight returnFlight;
  private final List<Passenger> passengers;
  private boolean cancelled;
  private boolean ownedByCurrentUser;
  private BookingState state;
  private final List<BookingObserver> observers;

  public Booking(Customer customer, Flight flight, List<Passenger> passengers)
  {
    this(nextBookingId++, LocalDateTime.now(), customer, flight, passengers);
  }

  public Booking(int bookingId, Customer customer, Flight flight,
      List<Passenger> passengers)
  {
    this(bookingId, LocalDateTime.now(), customer, flight, passengers);
  }

  public Booking(int bookingId, LocalDateTime bookingDate, Customer customer,
      Flight flight, List<Passenger> passengers)
  {
    this.passengers = new ArrayList<>();
    this.observers = new ArrayList<>();
    this.state = new PendingState();
    setBookingId(bookingId);
    setBookingDate(bookingDate);
    setCustomer(customer);
    setFlight(flight);
    setPassengers(passengers);
    ownedByCurrentUser = true;
    recalculateTotalPrice();
    this.customer.addBooking(this);
    this.flight.addBooking(this);
  }

  // confirms the booking using the state pattern
  public void confirmBooking()
  {
    String oldStatus = state.getStatus();
    state.confirm(this);
    notifyObservers(oldStatus, state.getStatus());
  }

  public void cancel()
  {
    if (cancelled)
    {
      return;
    }

    String oldStatus = state.getStatus();
    state.cancel(this);

    for (Passenger passenger : passengers)
    {
      for (SeatAssignment seatAssignment : new java.util.ArrayList<>(passenger.getSeatAssignments())) {
        boolean isMatch = seatAssignment.getFlight().equals(flight);
        if (flight instanceof ConnectingFlight cf) {
            isMatch = isMatch || seatAssignment.getFlight().equals(cf.getFirstSegment()) || seatAssignment.getFlight().equals(cf.getSecondSegment());
        }
        // also check return flight seats
        if (returnFlight != null) {
            isMatch = isMatch || seatAssignment.getFlight().equals(returnFlight);
            if (returnFlight instanceof ConnectingFlight rcf) {
                isMatch = isMatch || seatAssignment.getFlight().equals(rcf.getFirstSegment()) || seatAssignment.getFlight().equals(rcf.getSecondSegment());
            }
        }
        if (isMatch) {
          seatAssignment.release();
        }
      }
    }
    cancelled = true;
    notifyObservers(oldStatus, state.getStatus());
  }

  // used by state classes to switch the internal state
  void setInternalState(BookingState newState)
  {
    this.state = newState;
  }

  public String getStateStatus()
  {
    return state.getStatus();
  }

  // observer pattern methods
  public void addObserver(BookingObserver observer)
  {
    if (!observers.contains(observer))
    {
      observers.add(observer);
    }
  }

  public void removeObserver(BookingObserver observer)
  {
    observers.remove(observer);
  }

  private void notifyObservers(String oldState, String newState)
  {
    for (BookingObserver observer : observers)
    {
      observer.onBookingStateChanged(this, oldState, newState);
    }
  }

  public String getBookingSummary()
  {
    recalculateTotalPrice();

    StringBuilder summary = new StringBuilder();
    summary.append("Booking #").append(bookingId).append(System.lineSeparator());
    summary.append("Status: ").append(getStateStatus()).append(System.lineSeparator());
    summary.append("Customer: ").append(customer.getFullName())
        .append(System.lineSeparator());
    summary.append("Booking date: ").append(bookingDate)
        .append(System.lineSeparator());

    // outbound flight details
    java.util.List<Flight> outboundSegments = new java.util.ArrayList<>();
    if (flight instanceof ConnectingFlight cf) {
        outboundSegments.add(cf.getFirstSegment());
        outboundSegments.add(cf.getSecondSegment());
    } else {
        outboundSegments.add(flight);
    }

    summary.append(System.lineSeparator());
    summary.append("--- OUTBOUND FLIGHT ---").append(System.lineSeparator());
    summary.append("Route: ").append(flight.getDepartureCity().getCityName())
        .append(" -> ").append(flight.getArrivalCity().getCityName());
    if (flight instanceof ConnectingFlight) {
        summary.append(" (1 Stop)");
    } else {
        summary.append(" (Direct)");
    }
    summary.append(System.lineSeparator());
    summary.append("Carrier: ").append(outboundSegments.get(0).getCarrier().getName())
        .append(" | ").append(flight.getFlightNumber()).append(System.lineSeparator());
    summary.append("Aircraft: ").append(outboundSegments.get(0).getPlane().getPlaneType().getModel());
    if (outboundSegments.size() > 1) {
        summary.append(" / ").append(outboundSegments.get(1).getPlane().getPlaneType().getModel());
    }
    summary.append(System.lineSeparator());

    for (int i = 0; i < outboundSegments.size(); i++) {
        Flight seg = outboundSegments.get(i);
        summary.append("  Segment ").append(i + 1).append(": ")
            .append(seg.getDepartureCity().getCityName()).append(" -> ")
            .append(seg.getArrivalCity().getCityName()).append(System.lineSeparator());
        summary.append("    Departure: ").append(seg.getDepartureTime())
            .append(" | Arrival: ").append(seg.getArrivalTime())
            .append(" | Duration: ").append(seg.getDurationString())
            .append(System.lineSeparator());
    }

    // return flight details
    java.util.List<Flight> returnSegments = new java.util.ArrayList<>();
    if (returnFlight != null) {
        if (returnFlight instanceof ConnectingFlight rcf) {
            returnSegments.add(rcf.getFirstSegment());
            returnSegments.add(rcf.getSecondSegment());
        } else {
            returnSegments.add(returnFlight);
        }

        summary.append(System.lineSeparator());
        summary.append("--- RETURN FLIGHT ---").append(System.lineSeparator());
        summary.append("Route: ").append(returnFlight.getDepartureCity().getCityName())
            .append(" -> ").append(returnFlight.getArrivalCity().getCityName());
        if (returnFlight instanceof ConnectingFlight) {
            summary.append(" (1 Stop)");
        } else {
            summary.append(" (Direct)");
        }
        summary.append(System.lineSeparator());
        summary.append("Carrier: ").append(returnSegments.get(0).getCarrier().getName())
            .append(" | ").append(returnFlight.getFlightNumber()).append(System.lineSeparator());
        summary.append("Aircraft: ").append(returnSegments.get(0).getPlane().getPlaneType().getModel());
        if (returnSegments.size() > 1) {
            summary.append(" / ").append(returnSegments.get(1).getPlane().getPlaneType().getModel());
        }
        summary.append(System.lineSeparator());

        for (int i = 0; i < returnSegments.size(); i++) {
            Flight seg = returnSegments.get(i);
            summary.append("  Segment ").append(i + 1).append(": ")
                .append(seg.getDepartureCity().getCityName()).append(" -> ")
                .append(seg.getArrivalCity().getCityName()).append(System.lineSeparator());
            summary.append("    Departure: ").append(seg.getDepartureTime())
                .append(" | Arrival: ").append(seg.getArrivalTime())
                .append(" | Duration: ").append(seg.getDurationString())
                .append(System.lineSeparator());
        }
    }

    // passenger details with per-segment seat info and baggage
    summary.append(System.lineSeparator());
    summary.append("--- PASSENGERS ---").append(System.lineSeparator());

    for (Passenger passenger : passengers)
    {
      summary.append("Passenger: ").append(passenger.getFullName())
          .append(System.lineSeparator());

      // show seat assignments grouped by segment
      for (SeatAssignment sa : passenger.getSeatAssignments()) {
          summary.append("  Seat (")
              .append(sa.getFlight().getDepartureCity().getCityName())
              .append(" -> ")
              .append(sa.getFlight().getArrivalCity().getCityName())
              .append("): ").append(sa.getSeat().getSeatNumber())
              .append(" (").append(sa.getSeat().getSeatClass()).append(")")
              .append(System.lineSeparator());
      }

      // show luggage for this passenger
      if (!passenger.getPassengerLuggage().isEmpty())
      {
        for (PassengerLuggage luggage : passenger.getPassengerLuggage())
        {
          summary.append("  Luggage: ").append(luggage.getQuantity())
              .append("x ").append(luggage.getLuggageType().getName())
              .append(" (EUR ").append(luggage.getTotalExtraPrice()).append(")")
              .append(System.lineSeparator());
        }
      }
    }

    summary.append(System.lineSeparator());
    summary.append("Total price: EUR ").append(String.format("%.2f", totalPrice));

    return summary.toString();
  }

    public void recalculateTotalPrice()
    {
        double basePrice = 0;
        double luggagePrice = 0;

        for (Passenger passenger : passengers)
        {
            double passengerBasePrice = 0;

            if (passenger.getSeatAssignments().isEmpty()) {
                passengerBasePrice = flight.getBasePrice();
                if (returnFlight != null) {
                    passengerBasePrice += returnFlight.getBasePrice();
                }
            } else {
                for (SeatAssignment sa : passenger.getSeatAssignments()) {
                    passengerBasePrice += sa.getSeat().getPrice(
                        sa.getFlight().getBasePrice());
                }
            }

            basePrice += passengerBasePrice;

            for (PassengerLuggage luggage : passenger.getPassengerLuggage())
            {
                luggagePrice += luggage.getTotalExtraPrice();
            }
        }
        totalPrice = basePrice + luggagePrice;
    }

  private void setPassengers(List<Passenger> passengers)
  {
    if (passengers == null || passengers.isEmpty())
    {
      throw new IllegalArgumentException(
          "A booking must include at least one passenger.");
    }

    for (Passenger passenger : passengers)
    {
      Objects.requireNonNull(passenger, "Passenger is required.");
      if (this.passengers.contains(passenger))
      {
        throw new IllegalArgumentException(
            "The same passenger cannot be added to one booking twice.");
      }
      if (passenger.getBooking() != null && passenger.getBooking() != this)
      {
        throw new IllegalArgumentException(
            "Passenger already belongs to another booking.");
      }
      passenger.setBooking(this);
      this.passengers.add(passenger);
    }
  }

  public int getBookingId()
  {
    return bookingId;
  }

  public void setBookingId(int bookingId)
  {
    if (bookingId <= 0)
    {
      throw new IllegalArgumentException("Booking id must be positive.");
    }
    this.bookingId = bookingId;
    if (bookingId >= nextBookingId)
    {
      nextBookingId = bookingId + 1;
    }
  }

  public LocalDateTime getBookingDate()
  {
    return bookingDate;
  }

  public void setBookingDate(LocalDateTime bookingDate)
  {
    this.bookingDate = Objects.requireNonNull(bookingDate,
        "Booking date is required.");
  }

  public double getTotalPrice()
  {
    recalculateTotalPrice();
    return totalPrice;
  }

  public void setTotalPrice(double totalPrice)
  {
    if (totalPrice < 0)
    {
      throw new IllegalArgumentException("Total price cannot be negative.");
    }
    this.totalPrice = totalPrice;
  }

  public Customer getCustomer()
  {
    return customer;
  }

  public void setCustomer(Customer customer)
  {
    this.customer = Objects.requireNonNull(customer, "Customer is required.");
  }

  public Flight getFlight()
  {
    return flight;
  }

  public void setFlight(Flight flight)
  {
    this.flight = Objects.requireNonNull(flight, "Flight is required.");
  }

  public Flight getReturnFlight()
  {
    return returnFlight;
  }

  public void setReturnFlight(Flight returnFlight)
  {
    this.returnFlight = returnFlight;
  }

  public List<Passenger> getPassengers()
  {
    return Collections.unmodifiableList(passengers);
  }

  public boolean isCancelled()
  {
    return cancelled;
  }

  public boolean isOwnedByCurrentUser()
  {
    return ownedByCurrentUser;
  }

  public void setOwnedByCurrentUser(boolean ownedByCurrentUser)
  {
    this.ownedByCurrentUser = ownedByCurrentUser;
  }

  @Override public String toString()
  {
    return "Booking #" + bookingId + " for " + flight.getFlightNumber();
  }

  @Override public boolean equals(Object object)
  {
    if (this == object)
    {
      return true;
    }
    if (!(object instanceof Booking booking))
    {
      return false;
    }
    return bookingId == booking.bookingId;
  }

  @Override public int hashCode()
  {
    return Objects.hash(bookingId);
  }
}


