package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Admin extends User
{
  private FlightSearchService flightSearchService;

  public Admin(int userId, String email, String password,
      FlightSearchService flightSearchService)
  {
    super(userId, email, password);
    setFlightSearchService(flightSearchService);
  }

  public Admin(int userId, String email, String password)
  {
    this(userId, email, password, new FlightSearchService());
  }

  public void createFlight(Flight flight)
  {
    flightSearchService.addFlight(flight);
  }

  public void updateFlight(Flight flight)
  {
    flightSearchService.updateFlight(flight);
  }

  public void deleteFlight(Flight flight)
  {
    flightSearchService.removeFlight(flight);
  }

  public List<Booking> viewBookings()
  {
    List<Booking> allBookings = new ArrayList<>();
    for (Flight flight : flightSearchService.getFlights())
    {
      allBookings.addAll(flight.getBookings());
    }
    return allBookings;
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
}
