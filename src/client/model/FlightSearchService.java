package client.model;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
        if (criteria == null) criteria = new SearchCriteria();

        List<Flight> results = new ArrayList<>();

        // first find all direct flights that match
        for (Flight flight : allFlights) {
            if (flight.matchesCriteria(criteria)) {
                results.add(flight);
            }
        }

        if (criteria.getDepartureCity() == null
            && criteria.getArrivalCity() == null) {
            return results;
        }

        // then look for 1-stop connecting flights
        if (!criteria.isDirectOnly()) {
            for (Flight first : allFlights) {
                boolean originMatch = (criteria.getDepartureCity() == null) ||
                                      first.getDepartureCity().equals(criteria.getDepartureCity());
                boolean dateMatch = (criteria.getDepartureDate() == null) ||
                                    first.getDepartureTime().toLocalDate().equals(criteria.getDepartureDate());

                if (originMatch && dateMatch) {
                    for (Flight second : allFlights) {
                        boolean destMatch = (criteria.getArrivalCity() == null) ||
                                            second.getArrivalCity().equals(criteria.getArrivalCity());

                        boolean connects = first.getArrivalCity().equals(second.getDepartureCity());
                        boolean differentCities = !first.getDepartureCity().equals(second.getArrivalCity());

                        if (destMatch && connects && differentCities) {
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
        }

        if (results.isEmpty())
        {
            results.addAll(createFallbackFlights(allFlights, criteria));
        }

        return results;
    }

  private List<Flight> createFallbackFlights(List<Flight> allFlights,
      SearchCriteria criteria)
  {
    List<Flight> generatedFlights = new ArrayList<>();
    if (!canGenerateFallback(allFlights, criteria))
    {
      return generatedFlights;
    }

    Flight directFlight = getOrCreateGeneratedFlight(allFlights,
        criteria.getDepartureCity(), criteria.getArrivalCity(),
        criteria.getDepartureDate(), 9, 2, 1);

    if (!criteria.isDirectOnly())
    {
      City hub = pickHubCity(allFlights, criteria.getDepartureCity(),
          criteria.getArrivalCity());
      if (hub != null)
      {
        Flight first = getOrCreateGeneratedFlight(allFlights,
            criteria.getDepartureCity(), hub, criteria.getDepartureDate(),
            8, 2, 2);
        Flight second = getOrCreateGeneratedFlight(allFlights, hub,
            criteria.getArrivalCity(), criteria.getDepartureDate(), 12, 2, 3);
        if (first != null && second != null
            && hasEnoughSeats(first, criteria)
            && hasEnoughSeats(second, criteria))
        {
          generatedFlights.add(new ConnectingFlight(first, second));
        }
      }
    }

    if (directFlight != null && hasEnoughSeats(directFlight, criteria))
    {
      generatedFlights.add(directFlight);
    }
    return generatedFlights;
  }

  private boolean canGenerateFallback(List<Flight> allFlights,
      SearchCriteria criteria)
  {
    return !allFlights.isEmpty()
        && criteria.getDepartureCity() != null
        && criteria.getArrivalCity() != null
        && criteria.getDepartureDate() != null
        && !criteria.getDepartureCity().equals(criteria.getArrivalCity());
  }

  private Flight getOrCreateGeneratedFlight(List<Flight> allFlights,
      City departureCity, City arrivalCity, LocalDate date, int departureHour,
      int durationHours, int part)
  {
    int flightId = generatedFlightId(departureCity, arrivalCity, date, part);
    Flight existing = findFlightById(allFlights, flightId);
    if (existing != null)
    {
      return existing;
    }

    Plane plane = pickPlane(allFlights, flightId);
    if (plane == null)
    {
      return null;
    }
    Carrier carrier = plane.getCarrier();
    if (carrier == null)
    {
      carrier = pickCarrier(allFlights);
    }
    if (carrier == null)
    {
      return null;
    }

    LocalDateTime departureTime = date.atTime(departureHour, 0);
    Flight flight = new Flight(flightId, "FL-" + flightId, departureTime,
        departureTime.plusHours(durationHours), generatedPrice(departureCity,
        arrivalCity), carrier, plane, departureCity, arrivalCity);
    allFlights.add(flight);
    addFlight(flight);
    return flight;
  }

  private int generatedFlightId(City departureCity, City arrivalCity,
      LocalDate date, int part)
  {
    String key = departureCity.getCityId() + "-" + arrivalCity.getCityId()
        + "-" + date + "-" + part;
    return 200000 + ((key.hashCode() & 0x7fffffff) % 900000);
  }

  private double generatedPrice(City departureCity, City arrivalCity)
  {
    return 80 + Math.abs(departureCity.getCityId()
        - arrivalCity.getCityId()) * 5;
  }

  private Flight findFlightById(List<Flight> allFlights, int flightId)
  {
    for (Flight flight : allFlights)
    {
      if (flight.getFlightId() == flightId)
      {
        return flight;
      }
    }
    return null;
  }

  private City pickHubCity(List<Flight> allFlights, City departureCity,
      City arrivalCity)
  {
    City frankfurt = findCityByName(allFlights, "Frankfurt");
    if (isUsableHub(frankfurt, departureCity, arrivalCity))
    {
      return frankfurt;
    }

    City amsterdam = findCityByName(allFlights, "Amsterdam");
    if (isUsableHub(amsterdam, departureCity, arrivalCity))
    {
      return amsterdam;
    }

    for (Flight flight : allFlights)
    {
      if (isUsableHub(flight.getDepartureCity(), departureCity, arrivalCity))
      {
        return flight.getDepartureCity();
      }
      if (isUsableHub(flight.getArrivalCity(), departureCity, arrivalCity))
      {
        return flight.getArrivalCity();
      }
    }
    return null;
  }

  private City findCityByName(List<Flight> allFlights, String cityName)
  {
    for (Flight flight : allFlights)
    {
      if (flight.getDepartureCity().getCityName().equalsIgnoreCase(cityName))
      {
        return flight.getDepartureCity();
      }
      if (flight.getArrivalCity().getCityName().equalsIgnoreCase(cityName))
      {
        return flight.getArrivalCity();
      }
    }
    return null;
  }

  private boolean isUsableHub(City city, City departureCity, City arrivalCity)
  {
    return city != null && !city.equals(departureCity)
        && !city.equals(arrivalCity);
  }

  private Plane pickPlane(List<Flight> allFlights, int flightId)
  {
    List<Plane> planes = new ArrayList<>();
    for (Flight flight : allFlights)
    {
      if (flight.getPlane() != null && !planes.contains(flight.getPlane()))
      {
        planes.add(flight.getPlane());
      }
    }
    if (planes.isEmpty())
    {
      return null;
    }
    return planes.get(Math.abs(flightId) % planes.size());
  }

  private Carrier pickCarrier(List<Flight> allFlights)
  {
    for (Flight flight : allFlights)
    {
      if (flight.getCarrier() != null)
      {
        return flight.getCarrier();
      }
    }
    return null;
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


