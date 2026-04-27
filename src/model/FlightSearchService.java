package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class FlightSearchService
{
  private final List<Flight> flights;

  public FlightSearchService()
  {
    flights = new ArrayList<>();
  }

  public FlightSearchService(List<Flight> flights)
  {
    this();
    if (flights != null)
    {
      for (Flight flight : flights)
      {
        addFlight(flight);
      }
    }
  }

  public void addFlight(Flight flight)
  {
    Objects.requireNonNull(flight, "Flight is required.");
    if (!flights.contains(flight))
    {
      flights.add(flight);
    }
  }

  public void registerFlight(Flight flight)
  {
    addFlight(flight);
  }

  public void updateFlight(Flight flight)
  {
    Objects.requireNonNull(flight, "Flight is required.");
    for (int i = 0; i < flights.size(); i++)
    {
      if (flights.get(i).getFlightId() == flight.getFlightId())
      {
        flights.set(i, flight);
        return;
      }
    }
    throw new IllegalArgumentException("Flight was not found.");
  }

  public void removeFlight(Flight flight)
  {
    Objects.requireNonNull(flight, "Flight is required.");
    if (!flights.remove(flight))
    {
      throw new IllegalArgumentException("Flight was not found.");
    }
  }

  public List<Flight> searchFlights(SearchCriteria criteria)
  {
    Objects.requireNonNull(criteria, "Search criteria is required.");
    List<Flight> results = new ArrayList<>();
    for (Flight flight : flights)
    {
      if (matchesCriteria(flight, criteria))
      {
        results.add(flight);
      }
    }
    return results;
  }

  public Flight viewFlightDetails(Flight flight)
  {
    Objects.requireNonNull(flight, "Flight is required.");
    return viewFlightDetails(flight.getFlightId());
  }

  public Flight viewFlightDetails(int flightId)
  {
    for (Flight flight : flights)
    {
      if (flight.getFlightId() == flightId)
      {
        return flight;
      }
    }
    throw new IllegalArgumentException("Flight was not found.");
  }

  public List<Flight> getFlights()
  {
    return flights;
  }

  private boolean matchesCriteria(Flight flight, SearchCriteria criteria)
  {
    if (criteria.getDepartureCity() != null
        && !criteria.getDepartureCity().equals(flight.getDepartureCity()))
    {
      return false;
    }
    if (criteria.getArrivalCity() != null
        && !criteria.getArrivalCity().equals(flight.getArrivalCity()))
    {
      return false;
    }

    LocalDate departureDate = criteria.getDepartureDate();
    if (departureDate != null
        && !departureDate.equals(flight.getDepartureTime().toLocalDate()))
    {
      return false;
    }

    if (criteria.getSeatClass() != null)
    {
      return flight.getAvailableSeatsByClass(criteria.getSeatClass()).size()
          >= criteria.getPassengerCount();
    }

    return flight.getAvailableSeats().size() >= criteria.getPassengerCount();
  }
  //
}
