package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    public List<Flight> searchFlights(List<Flight> allFlights, SearchCriteria criteria) {
        if (allFlights == null) return new ArrayList<>();

        List<Flight> results = new ArrayList<>();

        // first find all direct flights that match
        for (Flight flight : allFlights) {
            if (flight.matchesCriteria(criteria)) {
                results.add(flight);
            }
        }

        // then look for 1-stop connecting flights
        for (Flight first : allFlights) {
            boolean originMatch = (criteria.getDepartureCity() == null) || 
                                  first.getDepartureCity().equals(criteria.getDepartureCity());
            boolean dateMatch = (criteria.getDepartureDate() == null) || 
                                first.getDepartureTime().toLocalDate().equals(criteria.getDepartureDate());

            if (originMatch && dateMatch) {
                for (Flight second : allFlights) {
                    boolean destMatch = (criteria.getArrivalCity() == null) || 
                                        second.getArrivalCity().equals(criteria.getArrivalCity());
                    
                    // checking if the cities connect
                    boolean connects = first.getArrivalCity().equals(second.getDepartureCity());
                    
                    if (destMatch && connects) {
                        // checking the layover time, it should be between 1 and 12 hours
                        long layoverHours = java.time.Duration.between(first.getArrivalTime(), second.getDepartureTime()).toHours();
                        if (layoverHours >= 1 && layoverHours <= 12) {
                            ConnectingFlight connection = new ConnectingFlight(first, second);
                            results.add(connection);
                        }
                    }
                }
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

}
