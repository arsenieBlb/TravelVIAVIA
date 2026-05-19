package client.model;

import java.util.Objects;

public class SeatAssignment
{
  private int seatAssignmentId;
  private Passenger passenger;
  private Seat seat;
  private Flight flight;

  public SeatAssignment(int seatAssignmentId, Passenger passenger, Seat seat,
      Flight flight)
  {
    setSeatAssignmentId(seatAssignmentId);
    this.passenger = Objects.requireNonNull(passenger,
        "Passenger is required.");
    this.seat = Objects.requireNonNull(seat, "Seat is required.");
    this.flight = Objects.requireNonNull(flight, "Flight is required.");

    if (passenger.getBooking() == null)
    {
      throw new IllegalArgumentException(
          "Passenger must belong to a booking before seat assignment.");
    }
    boolean isValidFlight = passenger.getBooking().getFlight() == flight;
    if (passenger.getBooking().getFlight() instanceof ConnectingFlight cf) {
        isValidFlight = isValidFlight || cf.getFirstSegment() == flight || cf.getSecondSegment() == flight;
    }
    // also accept return flight segments
    Flight returnFlight = passenger.getBooking().getReturnFlight();
    if (returnFlight != null) {
        isValidFlight = isValidFlight || returnFlight == flight;
        if (returnFlight instanceof ConnectingFlight rcf) {
            isValidFlight = isValidFlight || rcf.getFirstSegment() == flight || rcf.getSecondSegment() == flight;
        }
    }
    if (!isValidFlight)
    {
      throw new IllegalArgumentException(
          "Passenger booking must be for the same flight or a segment.");
    }
    flight.validateSeatBelongsToPlane(seat);
    flight.addSeatAssignment(this);
    passenger.addSeatAssignment(this);
  }

  // used when loading from the database, skips duplicate checks
  public SeatAssignment(Flight flight, Seat seat, Passenger passenger)
  {
    this.seatAssignmentId = 0;
    this.flight = flight;
    this.seat = seat;
    this.passenger = passenger;
  }

  public void release()
  {
    flight.removeSeatAssignment(this);
    passenger.removeSeatAssignment(this);
  }

  public int getSeatAssignmentId()
  {
    return seatAssignmentId;
  }

  public void setSeatAssignmentId(int seatAssignmentId)
  {
    if (seatAssignmentId <= 0)
    {
      throw new IllegalArgumentException(
          "Seat assignment id must be positive.");
    }
    this.seatAssignmentId = seatAssignmentId;
  }

  public Passenger getPassenger()
  {
    return passenger;
  }

    public Seat getSeat()
    {
        return seat;
    }

  public Flight getFlight()
  {
    return flight;
  }

  @Override public String toString()
  {
    return passenger.getFullName() + " -> " + seat.getSeatNumber() + " on "
        + flight.getFlightNumber();
  }
}


