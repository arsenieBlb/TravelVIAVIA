package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Customer extends User
{
  private String firstName;
  private String lastName;
  private FlightSearchService flightSearchService;
  private final List<Booking> bookings;

  public Customer(int userId, String email, String password, String firstName,
      String lastName, FlightSearchService flightSearchService)
  {
    super(userId, email, password);
    this.bookings = new ArrayList<>();
    setFirstName(firstName);
    setLastName(lastName);
    setFlightSearchService(flightSearchService);
  }

  public Customer(int userId, String email, String password, String firstName,
      String lastName)
  {
    this(userId, email, password, firstName, lastName,
        new FlightSearchService());
  }

  public List<Flight> searchFlights(SearchCriteria criteria)
  {
    return flightSearchService.searchFlights(criteria);
  }

  public Flight viewFlightDetails(Flight flight)
  {
    return flightSearchService.viewFlightDetails(flight);
  }

  public Booking createBooking(Flight flight, List<Passenger> passengers)
  {
    return new Booking(this, flight, passengers);
  }

  public void cancelBooking(Booking booking)
  {
    if (!bookings.contains(booking))
    {
      throw new IllegalArgumentException(
          "The booking does not belong to this customer.");
    }
    booking.cancel();
  }

  public List<Booking> viewBookings()
  {
    return Collections.unmodifiableList(bookings);
  }

  void addBooking(Booking booking)
  {
    Objects.requireNonNull(booking, "Booking is required.");
    if (!bookings.contains(booking))
    {
      bookings.add(booking);
    }
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

  public FlightSearchService getFlightSearchService()
  {
    return flightSearchService;
  }

  public void setFlightSearchService(FlightSearchService flightSearchService)
  {
    this.flightSearchService = Objects.requireNonNull(flightSearchService,
        "Flight search service is required.");
  }

  public String getFullName()
  {
    return firstName + " " + lastName;
  }

  @Override public String toString()
  {
    return getFullName() + " (" + getEmail() + ")";
  }
}
