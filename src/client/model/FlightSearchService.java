package client.model;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

  // takes the DB flights as templates and creates copies on a weekly schedule
  public static List<Flight> generateRecurringFlights(List<Flight> templateFlights)
  {
    List<Flight> allFlights = new ArrayList<>(templateFlights);
    int nextId = 10000;

    // Use a fixed anchor date so generated flight IDs remain deterministic across server restarts.
    // This ensures database bookings always map to the correct dynamically generated flight.
    LocalDate today = LocalDate.of(2026, 5, 1);
    LocalDate endDate = today.plusMonths(4);

    for (int flightIndex = 0; flightIndex < templateFlights.size(); flightIndex++)
    {
      Flight template = templateFlights.get(flightIndex);
      DayOfWeek originalDay = template.getDepartureTime().getDayOfWeek();
      // create a schedule based on the original day so connections stay valid
      DayOfWeek[] schedule = {
          originalDay,
          originalDay.plus(2),
          originalDay.plus(4)
      };

      Duration flightDuration = Duration.between(
          template.getDepartureTime(), template.getArrivalTime());

      for (DayOfWeek day : schedule)
      {
        LocalDate current = today.with(java.time.temporal.TemporalAdjusters.nextOrSame(day));

        while (!current.isAfter(endDate))
        {
          // skip the original date since it already exists
          if (current.equals(template.getDepartureTime().toLocalDate()))
          {
            current = current.plusWeeks(1);
            continue;
          }

          LocalDateTime newDeparture = current.atTime(
              template.getDepartureTime().toLocalTime());
          LocalDateTime newArrival = newDeparture.plus(flightDuration);

          Flight copy = new Flight(nextId++,
              template.getFlightNumber() + "-" + current.toString(),
              newDeparture, newArrival,
              template.getBasePrice(),
              template.getCarrier(),
              template.getPlane(),
              template.getDepartureCity(),
              template.getArrivalCity());

          allFlights.add(copy);
          current = current.plusWeeks(1);
        }
      }
    }
    return allFlights;
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

        // Optimization: Do not compute millions of connecting flights if the user didn't specify an origin or destination
        if (criteria.getDepartureCity() == null && criteria.getArrivalCity() == null) {
            return results;
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
                    boolean differentCities = !first.getDepartureCity().equals(second.getArrivalCity());
                    
                    if (destMatch && connects && differentCities) {
                        // checking the layover time, it should be between 1 and 336 hours (14 days)
                        long layoverHours = java.time.Duration.between(first.getArrivalTime(), second.getDepartureTime()).toHours();
                        if (layoverHours >= 1 && layoverHours <= 336) {
                            if (hasEnoughSeats(first, criteria) &&
                                hasEnoughSeats(second, criteria)) {
                                ConnectingFlight connection = new ConnectingFlight(first, second);
                                results.add(connection);
                            }
                        }
                    }
                }
            }
        }

        return results;
    }

  private boolean hasEnoughSeats(Flight flight, SearchCriteria criteria)
  {
    return flight.getAvailableSeatsByClass(criteria.getSeatClass()).size()
        >= criteria.getPassengerCount();
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


