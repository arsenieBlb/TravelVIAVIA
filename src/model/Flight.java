package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Flight
{
  private int flightId;
  private String flightNumber;
  private LocalDateTime departureTime;
  private LocalDateTime arrivalTime;
  private double basePrice;
  private Carrier carrier;
  private Plane plane;
  private City departureCity;
  private City arrivalCity;
  private final List<Booking> bookings;
  private final List<SeatAssignment> seatAssignments;
  private final Set<Seat> occupiedSeats;

  public Flight(int flightId, String flightNumber, LocalDateTime departureTime,
      LocalDateTime arrivalTime, double basePrice, Carrier carrier, Plane plane,
      City departureCity, City arrivalCity)
  {
    this.bookings = new ArrayList<>();
    this.seatAssignments = new ArrayList<>();
    this.occupiedSeats = new HashSet<>();
    setFlightId(flightId);
    setFlightNumber(flightNumber);
    setDepartureTime(departureTime);
    setArrivalTime(arrivalTime);
    setBasePrice(basePrice);
    setDepartureCity(departureCity);
    setArrivalCity(arrivalCity);
    setPlane(plane);
    setCarrier(carrier);
  }

  public List<Seat> getAvailableSeats()
  {
    Set<Seat> assignedSeats = new HashSet<>();
    for (SeatAssignment seatAssignment : seatAssignments)
    {
      assignedSeats.add(seatAssignment.getSeat());
    }
    assignedSeats.addAll(occupiedSeats);

    List<Seat> availableSeats = new ArrayList<>();
    for (Seat seat : plane.getSeats())
    {
      if (!assignedSeats.contains(seat))
      {
        availableSeats.add(seat);
      }
    }
    return availableSeats;
  }

  public List<Seat> getAvailableSeatsByClass(SeatClass seatClass)
  {
    Objects.requireNonNull(seatClass, "Seat class is required.");
    List<Seat> availableSeats = new ArrayList<>();
    for (Seat seat : getAvailableSeats())
    {
      if (seat.getSeatClass() == seatClass)
      {
        availableSeats.add(seat);
      }
    }
    return availableSeats;
  }

  void addBooking(Booking booking)
  {
    Objects.requireNonNull(booking, "Booking is required.");
    if (booking.getFlight() != this)
    {
      throw new IllegalArgumentException(
          "Booking must be for this flight.");
    }
    if (!bookings.contains(booking))
    {
      bookings.add(booking);
    }
  }

  void addSeatAssignment(SeatAssignment seatAssignment)
  {
    Objects.requireNonNull(seatAssignment, "Seat assignment is required.");
    if (seatAssignment.getFlight() != this)
    {
      throw new IllegalArgumentException(
          "Seat assignment must be for this flight.");
    }
    validateSeatBelongsToPlane(seatAssignment.getSeat());
    if (isSeatAssigned(seatAssignment.getSeat()))
    {
      throw new IllegalArgumentException(
          "Seat " + seatAssignment.getSeat().getSeatNumber()
              + " is already assigned on flight " + flightNumber + ".");
    }
    if (hasPassengerSeatAssignment(seatAssignment.getPassenger()))
    {
      throw new IllegalArgumentException(
          "Passenger already has a seat assignment for this flight.");
    }
    seatAssignments.add(seatAssignment);
  }

  public void markSeatOccupied(Seat seat)
  {
    Objects.requireNonNull(seat, "Seat is required.");
    validateSeatBelongsToPlane(seat);
    occupiedSeats.add(seat);
  }

  void removeSeatAssignment(SeatAssignment seatAssignment)
  {
    seatAssignments.remove(seatAssignment);
  }

  boolean isSeatAssigned(Seat seat)
  {
    if (occupiedSeats.contains(seat))
    {
      return true;
    }
    for (SeatAssignment seatAssignment : seatAssignments)
    {
      if (seatAssignment.getSeat().equals(seat))
      {
        return true;
      }
    }
    return false;
  }

  boolean hasPassengerSeatAssignment(Passenger passenger)
  {
    for (SeatAssignment seatAssignment : seatAssignments)
    {
      if (seatAssignment.getPassenger().equals(passenger))
      {
        return true;
      }
    }
    return false;
  }

  void validateSeatBelongsToPlane(Seat seat)
  {
    if (!plane.getSeats().contains(seat))
    {
      throw new IllegalArgumentException(
          "Seat must belong to the plane used by this flight.");
    }
  }

  private void validateCities()
  {
    if (departureCity != null && arrivalCity != null
        && departureCity.equals(arrivalCity))
    {
      throw new IllegalArgumentException(
          "Departure city and arrival city cannot be the same.");
    }
  }

  private void validateTimes()
  {
    if (departureTime != null && arrivalTime != null
        && !arrivalTime.isAfter(departureTime))
    {
      throw new IllegalArgumentException(
          "Arrival time must be after departure time.");
    }
  }

  public int getFlightId()
  {
    return flightId;
  }

  public void setFlightId(int flightId)
  {
    if (flightId <= 0)
    {
      throw new IllegalArgumentException("Flight id must be positive.");
    }
    this.flightId = flightId;
  }

  public String getFlightNumber()
  {
    return flightNumber;
  }

  public void setFlightNumber(String flightNumber)
  {
    if (flightNumber == null || flightNumber.isBlank())
    {
      throw new IllegalArgumentException("Flight number is required.");
    }
    this.flightNumber = flightNumber;
  }

  public LocalDateTime getDepartureTime()
  {
    return departureTime;
  }

  public void setDepartureTime(LocalDateTime departureTime)
  {
    this.departureTime = Objects.requireNonNull(departureTime,
        "Departure time is required.");
    validateTimes();
  }

  public LocalDateTime getArrivalTime()
  {
    return arrivalTime;
  }

  public void setArrivalTime(LocalDateTime arrivalTime)
  {
    this.arrivalTime = Objects.requireNonNull(arrivalTime,
        "Arrival time is required.");
    validateTimes();
  }

  public double getBasePrice()
  {
    return basePrice;
  }

  public void setBasePrice(double basePrice)
  {
    if (basePrice < 0)
    {
      throw new IllegalArgumentException("Base price cannot be negative.");
    }
    this.basePrice = basePrice;
  }

  public Carrier getCarrier()
  {
    return carrier;
  }

  public void setCarrier(Carrier carrier)
  {
    if (this.carrier == carrier)
    {
      return;
    }
    if (this.carrier != null)
    {
      this.carrier.removeFlight(this);
    }
    this.carrier = Objects.requireNonNull(carrier, "Carrier is required.");
    this.carrier.addFlight(this);
  }

  public Plane getPlane()
  {
    return plane;
  }

  public void setPlane(Plane plane)
  {
    Objects.requireNonNull(plane, "Plane is required.");
    if (plane.getStatus() != PlaneStatus.Active)
    {
      throw new IllegalArgumentException(
          "A flight must use a plane with Active status.");
    }
    if (!seatAssignments.isEmpty())
    {
      throw new IllegalArgumentException(
          "Cannot change plane after seats have been assigned.");
    }
    this.plane = plane;
  }

  public City getDepartureCity()
  {
    return departureCity;
  }

  public void setDepartureCity(City departureCity)
  {
    this.departureCity = Objects.requireNonNull(departureCity,
        "Departure city is required.");
    validateCities();
  }

  public City getArrivalCity()
  {
    return arrivalCity;
  }

  public void setArrivalCity(City arrivalCity)
  {
    this.arrivalCity = Objects.requireNonNull(arrivalCity,
        "Arrival city is required.");
    validateCities();
  }

  public List<Booking> getBookings()
  {
    return Collections.unmodifiableList(bookings);
  }

  public List<SeatAssignment> getSeatAssignments()
  {
    return Collections.unmodifiableList(seatAssignments);
  }

  @Override public boolean equals(Object object)
  {
    if (this == object)
    {
      return true;
    }
    if (!(object instanceof Flight flight))
    {
      return false;
    }
    return flightId == flight.flightId;
  }

  @Override public int hashCode()
  {
    return Objects.hash(flightId);
  }

  @Override public String toString()
  {
    return flightNumber + " " + departureCity + " -> " + arrivalCity;
  }
}
