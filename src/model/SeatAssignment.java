package model;

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
    if (passenger.getBooking().getFlight() != flight)
    {
      throw new IllegalArgumentException(
          "Passenger booking must be for the same flight.");
    }
    if (passenger.getSeatAssignment() != null)
    {
      throw new IllegalArgumentException(
          "Passenger already has a seat assignment.");
    }
    flight.validateSeatBelongsToPlane(seat);
    flight.addSeatAssignment(this);
    passenger.setSeatAssignment(this);
  }

  public void release()
  {
    flight.removeSeatAssignment(this);
    passenger.clearSeatAssignment(this);
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
