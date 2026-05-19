package client.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Passenger
{
  private int passengerId;
  private String firstName;
  private String lastName;
  private Booking booking;
  private final List<SeatAssignment> seatAssignments = new ArrayList<>();
  private final List<PassengerLuggage> passengerLuggage;

  public Passenger(int passengerId, String firstName, String lastName)
  {
    passengerLuggage = new ArrayList<>();
    setPassengerId(passengerId);
    setFirstName(firstName);
    setLastName(lastName);
  }

  public void addPassengerLuggage(PassengerLuggage luggage)
  {
    Objects.requireNonNull(luggage, "Passenger luggage is required.");
    if (luggage.getPassenger() != null && luggage.getPassenger() != this)
    {
      throw new IllegalArgumentException(
          "Passenger luggage belongs to another passenger.");
    }
    luggage.setPassengerInternal(this);
    if (!passengerLuggage.contains(luggage))
    {
      passengerLuggage.add(luggage);
    }
  }

  void setBooking(Booking booking)
  {
    this.booking = Objects.requireNonNull(booking, "Booking is required.");
  }

  public void addSeatAssignment(SeatAssignment seatAssignment)
  {
    Objects.requireNonNull(seatAssignment, "Seat assignment is required.");
    if (seatAssignment.getPassenger() != null && seatAssignment.getPassenger() != this)
    {
      throw new IllegalArgumentException(
          "Seat assignment belongs to another passenger.");
    }
    if (!seatAssignments.contains(seatAssignment))
    {
      seatAssignments.add(seatAssignment);
    }
  }

  void removeSeatAssignment(SeatAssignment seatAssignment)
  {
    seatAssignments.remove(seatAssignment);
  }



  public int getPassengerId()
  {
    return passengerId;
  }

  public void setPassengerId(int passengerId)
  {
    if (passengerId <= 0)
    {
      throw new IllegalArgumentException("Passenger id must be positive.");
    }
    this.passengerId = passengerId;
  }

  public String getFirstName()
  {
    return firstName;
  }

  public void setFirstName(String firstName)
  {
    if (firstName == null || firstName.isBlank())
    {
      throw new IllegalArgumentException("First name is required.");
    }
    this.firstName = firstName;
  }

  public String getLastName()
  {
    return lastName;
  }

  public void setLastName(String lastName)
  {
    if (lastName == null || lastName.isBlank())
    {
      throw new IllegalArgumentException("Last name is required.");
    }
    this.lastName = lastName;
  }

  public Booking getBooking()
  {
    return booking;
  }

  public List<SeatAssignment> getSeatAssignments()
  {
    return Collections.unmodifiableList(seatAssignments);
  }

  public List<PassengerLuggage> getPassengerLuggage()
  {
    return Collections.unmodifiableList(passengerLuggage);
  }

  public String getFullName()
  {
    return firstName + " " + lastName;
  }

  @Override public boolean equals(Object object)
  {
    if (this == object)
    {
      return true;
    }
    if (!(object instanceof Passenger passenger))
    {
      return false;
    }
    return passengerId == passenger.passengerId;
  }
  //

  @Override public int hashCode()
  {
    return Objects.hash(passengerId);
  }

  @Override public String toString()
  {
    return getFullName();
  }

}


