package client.model;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Handles flight search logic for the project.
 * It can find direct flights, build connecting flights, and create generated
 * flights when there is no matching route for the chosen date.
 */
public class FlightSearchService
{
  private final List<Flight> flights;

  /**
   * Creates an empty flight search service.
   */
  public FlightSearchService()
  {
    flights = new ArrayList<>();
  }

  /**
   * Creates a flight search service with the flights that are already loaded.
   *
   * @param flights the flights that should be available for searching
   */
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

  /**
   * Adds a flight to this service if it is not already stored.
   *
   * @param flight the flight that should be added
   */
  public void addFlight(Flight flight)
  {
    Objects.requireNonNull(flight, "Flight is required.");
    if (!flights.contains(flight))
    {
      flights.add(flight);
    }
  }

  /**
   * Creates extra recurring flights based on the normal flights from the
   * database. Already generated flights are skipped, because they should not be
   * used as templates for even more generated flights.
   *
   * @param templateFlights the original flights used as templates
   * @return the original flights together with the generated recurring flights
   */
  // takes the database flights as templates and creates weekly copies
  public static List<Flight> generateRecurringFlights(List<Flight> templateFlights)
  {
    List<Flight> allFlights = new ArrayList<>(templateFlights);
    int nextId = 10000;

    // keeps generated flight numbers stable after restarting the server
    LocalDate today = LocalDate.of(2026, 5, 1);
    LocalDate endDate = today.plusMonths(4);

    for (int flightIndex = 0; flightIndex < templateFlights.size(); flightIndex++)
    {
      Flight template = templateFlights.get(flightIndex);
      if (isGeneratedFlight(template))
      {
        continue;
      }
      DayOfWeek originalDay = template.getDepartureTime().getDayOfWeek();
      // keeps the copied flights close to the original weekday
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
          while (containsFlightId(allFlights, nextId))
          {
            nextId++;
          }

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

  /**
   * Adds a new flight to the search service.
   *
   * @param flight the flight that should be registered
   */
  public void registerFlight(Flight flight)
  {
    addFlight(flight);
  }

  /**
   * Replaces an existing flight with updated information.
   *
   * @param flight the updated flight
   */
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

  /**
   * Removes a flight from the search service.
   *
   * @param flight the flight that should be removed
   */
  public void removeFlight(Flight flight)
  {
    Objects.requireNonNull(flight, "Flight is required.");
    if (!flights.remove(flight))
    {
      throw new IllegalArgumentException("Flight was not found.");
    }
  }

  /**
   * Searches for flights that match the selected search criteria.
   * Direct flights are checked first, then connecting flights are checked.
   * If nothing is found, the service tries to create generated flights for the
   * selected route and date.
   *
   * @param allFlights all flights that can be searched
   * @param criteria the search values entered by the user
   * @return the matching direct flights and connecting flights
   */
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

        // then look for connecting flights
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

  /**
   * Creates generated flights for a route and date when no normal flights were
   * found.
   *
   * @param allFlights all flights currently loaded in the system
   * @param criteria the search values entered by the user
   * @return generated flights that can be shown in the search results
   */
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

  /**
   * Checks if a flight was created by the program instead of being one of the
   * normal seed or administrator flights.
   *
   * @param flight the flight to check
   * @return true if the flight is generated
   */
  public static boolean isGeneratedFlight(Flight flight)
  {
    return flight != null && isGeneratedFlightId(flight.getFlightId());
  }

  /**
   * Checks if a flight identifier belongs to a generated flight.
   *
   * @param flightId the flight identifier
   * @return true if the identifier is in the generated flight range
   */
  public static boolean isGeneratedFlightId(int flightId)
  {
    return flightId >= 10000;
  }

  /**
   * Checks if the list already contains a flight with the same identifier.
   *
   * @param flights the flights to check
   * @param flightId the flight identifier
   * @return true if a matching flight exists
   */
  private static boolean containsFlightId(List<Flight> flights, int flightId)
  {
    for (Flight flight : flights)
    {
      if (flight.getFlightId() == flightId)
      {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if the search has enough information to create a generated flight.
   *
   * @param allFlights all flights currently loaded in the system
   * @param criteria the search values entered by the user
   * @return true if a generated flight can be created
   */
  private boolean canGenerateFallback(List<Flight> allFlights,
      SearchCriteria criteria)
  {
    return !allFlights.isEmpty()
        && criteria.getDepartureCity() != null
        && criteria.getArrivalCity() != null
        && criteria.getDepartureDate() != null
        && !criteria.getDepartureCity().equals(criteria.getArrivalCity());
  }

  /**
   * Finds an existing generated flight, or creates it if it does not exist yet.
   *
   * @param allFlights all flights currently loaded in the system
   * @param departureCity the city where the flight starts
   * @param arrivalCity the city where the flight ends
   * @param date the flight date
   * @param departureHour the hour when the flight starts
   * @param durationHours the flight duration in hours
   * @param part the route part used to make a stable generated identifier
   * @return the generated flight, or null if it cannot be created
   */
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

  /**
   * Builds a stable identifier for a generated flight.
   * The same route, date, and route part should always give the same result.
   *
   * @param departureCity the city where the flight starts
   * @param arrivalCity the city where the flight ends
   * @param date the flight date
   * @param part the route part used for direct and connecting flights
   * @return the generated flight identifier
   */
  private int generatedFlightId(City departureCity, City arrivalCity,
      LocalDate date, int part)
  {
    String key = departureCity.getCityId() + "-" + arrivalCity.getCityId()
        + "-" + date + "-" + part;
    return 200000 + ((key.hashCode() & 0x7fffffff) % 900000);
  }

  /**
   * Calculates a simple generated price from the two city identifiers.
   *
   * @param departureCity the city where the flight starts
   * @param arrivalCity the city where the flight ends
   * @return the generated base price
   */
  private double generatedPrice(City departureCity, City arrivalCity)
  {
    return 80 + Math.abs(departureCity.getCityId()
        - arrivalCity.getCityId()) * 5;
  }

  /**
   * Finds a flight by its identifier in a list.
   *
   * @param allFlights the flights to search through
   * @param flightId the flight identifier
   * @return the matching flight, or null if it was not found
   */
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

  /**
   * Chooses a city that can be used between the departure and arrival cities.
   *
   * @param allFlights all flights currently loaded in the system
   * @param departureCity the city where the trip starts
   * @param arrivalCity the final city of the trip
   * @return a usable connection city, or null if none is found
   */
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

  /**
   * Finds a city by name from the loaded flights.
   *
   * @param allFlights all flights currently loaded in the system
   * @param cityName the city name to search for
   * @return the matching city, or null if it was not found
   */
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

  /**
   * Checks if a city can be used as a connection city.
   *
   * @param city the city that might be used as a connection
   * @param departureCity the city where the trip starts
   * @param arrivalCity the final city of the trip
   * @return true if the city is different from the start and final city
   */
  private boolean isUsableHub(City city, City departureCity, City arrivalCity)
  {
    return city != null && !city.equals(departureCity)
        && !city.equals(arrivalCity);
  }

  /**
   * Chooses a plane for a generated flight.
   *
   * @param allFlights all flights currently loaded in the system
   * @param flightId the generated flight identifier
   * @return a plane, or null if no plane is available
   */
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

  /**
   * Chooses the first carrier that can be found from the loaded flights.
   *
   * @param allFlights all flights currently loaded in the system
   * @return a carrier, or null if no carrier is available
   */
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

  /**
   * Checks if a flight has enough available seats for the passenger count.
   *
   * @param flight the flight that should be checked
   * @param criteria the search values entered by the user
   * @return true if the flight has enough seats in the selected class
   */
  private boolean hasEnoughSeats(Flight flight, SearchCriteria criteria)
  {
    return flight.getAvailableSeatsByClass(criteria.getSeatClass()).size()
        >= criteria.getPassengerCount();
  }

  /**
   * Gets the full flight details for a flight object.
   *
   * @param flight the flight that should be shown
   * @return the matching stored flight
   */
  public Flight viewFlightDetails(Flight flight)
  {
    Objects.requireNonNull(flight, "Flight is required.");
    return viewFlightDetails(flight.getFlightId());
  }

  /**
   * Gets the full flight details by flight identifier.
   *
   * @param flightId the flight identifier
   * @return the matching stored flight
   */
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

  /**
   * Gets the flights stored inside this search service.
   *
   * @return the stored flights
   */
  public List<Flight> getFlights()
  {
    return flights;
  }

}


