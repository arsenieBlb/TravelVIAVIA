import client.model.ActiveState;
import client.model.Booking;
import client.model.BusinessClass;
import client.model.Carrier;
import client.model.City;
import client.model.ConnectingFlight;
import client.model.Customer;
import client.model.EconomyClass;
import client.model.Flight;
import client.model.FlightSearchService;
import client.model.LuggageType;
import client.model.Model;
import client.model.Passenger;
import client.model.Plane;
import client.model.PlaneType;
import client.model.SearchCriteria;
import client.model.Seat;
import client.model.SeatAssignment;
import client.model.SeatClass;
import client.model.User;
import client.viewmodel.FlightSceneViewModel;
import client.viewmodel.PassengerDetailsViewModel;
import client.viewmodel.SeatMapViewModel;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.beans.PropertyChangeListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class BookingTestBase
{
  protected FakeModel model;
  protected City berlin;
  protected City paris;
  protected City rome;
  protected Carrier viaAir;
  protected Carrier skyLine;
  protected Flight outbound;
  protected Flight returnFlight;
  protected Flight cheapFlight;
  protected Flight expensiveFlight;
  protected ConnectingFlight connectingFlight;
  protected LuggageType carryOn;
  protected LuggageType checkedBaggage;
  protected Customer customer;
  protected List<Flight> flights;
  protected int nextSeatId;
  protected int nextPlaneId;

  @BeforeAll
  static void startJavaFx() throws InterruptedException
  {
    CountDownLatch startupLatch = new CountDownLatch(1);
    try
    {
      Platform.startup(startupLatch::countDown);
    }
    catch (IllegalStateException alreadyStarted)
    {
      startupLatch.countDown();
    }
    assertTrue(startupLatch.await(5, TimeUnit.SECONDS));
  }

  @BeforeEach
  void setUp()
  {
    nextSeatId = 1;
    nextPlaneId = 1;

    berlin = new City(1, "Berlin", "Germany");
    paris = new City(2, "Paris", "France");
    rome = new City(3, "Rome", "Italy");
    viaAir = new Carrier(1, "VIA Air");
    skyLine = new Carrier(2, "SkyLine");

    LocalDateTime baseTime = LocalDateTime.now().plusDays(14).withHour(8)
        .withMinute(0).withSecond(0).withNano(0);
    outbound = createFlight(1, "VIA101", berlin, paris, viaAir, 100,
        baseTime);
    returnFlight = createFlight(2, "VIA202", paris, berlin, viaAir, 120,
        baseTime.plusDays(4));
    cheapFlight = createFlight(3, "SKY303", berlin, paris, skyLine, 80,
        baseTime.plusHours(2));
    expensiveFlight = createFlight(4, "VIA404", berlin, rome, viaAir, 240,
        baseTime.plusHours(4));

    Flight firstSegment = createFlight(5, "VIA501", berlin, rome, viaAir, 75,
        baseTime.plusHours(1));
    Flight secondSegment = createFlight(6, "VIA502", rome, paris, viaAir, 95,
        baseTime.plusHours(5));
    connectingFlight = new ConnectingFlight(firstSegment, secondSegment);

    flights = List.of(outbound, returnFlight, cheapFlight, expensiveFlight,
        connectingFlight);
    carryOn = new LuggageType(1, "Carry-on", "Cabin bag", 0);
    checkedBaggage = new LuggageType(2, "Checked Baggage", "23 kg bag", 65);
    customer = new Customer(7, "artem@gmail.com", "ArtemGetsA12", "Artem",
        "Twelve", new FlightSearchService());

    model = new FakeModel(new ArrayList<>(flights),
        new ArrayList<>(List.of(berlin, paris, rome)),
        new ArrayList<>(List.of(viaAir, skyLine)),
        new ArrayList<>(List.of(carryOn, checkedBaggage)), customer);
  }

  protected FlightSceneViewModel createFlightSceneViewModel()
  {
    return new FlightSceneViewModel(model);
  }

  protected Flight createFlight(int id, String flightNumber, City departure,
      City arrival, Carrier carrier, double basePrice,
      LocalDateTime departureTime)
  {
    Plane plane = createPlane(carrier);
    return new Flight(id, flightNumber, departureTime,
        departureTime.plusHours(2), basePrice, carrier, plane, departure,
        arrival);
  }

  protected Plane createPlane(Carrier carrier)
  {
    PlaneType type = new PlaneType(nextPlaneId, "Passenger", "T-" + nextPlaneId,
        4, 8, 2);
    Plane plane = new Plane(nextPlaneId, "OY-" + nextPlaneId,
        new ActiveState(), type, carrier);
    nextPlaneId++;

    plane.addSeat(new Seat(nextSeatId++, "1A", 1, new BusinessClass()));
    plane.addSeat(new Seat(nextSeatId++, "1B", 1, new BusinessClass()));
    for (int row = 2; row <= 3; row++)
    {
      for (String letter : List.of("A", "B", "C", "D"))
      {
        plane.addSeat(new Seat(nextSeatId++, row + letter, row,
            new EconomyClass()));
      }
    }
    return plane;
  }

  protected List<Flight> findMatchingFlights(SearchCriteria criteria)
  {
    List<Flight> results = new ArrayList<>();
    for (Flight flight : flights)
    {
      if (flight.matchesCriteria(criteria))
      {
        results.add(flight);
      }
    }
    results.sort(Comparator.comparing(Flight::getDepartureTime));
    return results;
  }

  protected Flight findFlight(List<Flight> flights, City departureCity,
      City arrivalCity, java.time.LocalDate date)
  {
    for (Flight flight : flights)
    {
      if (flight.getDepartureCity().equals(departureCity)
          && flight.getArrivalCity().equals(arrivalCity)
          && flight.getDepartureTime().toLocalDate().equals(date))
      {
        return flight;
      }
    }
    return null;
  }

  protected void waitUntil(java.util.function.BooleanSupplier condition)
      throws InterruptedException
  {
    long end = System.currentTimeMillis() + 3000;
    while (System.currentTimeMillis() < end)
    {
      if (condition.getAsBoolean())
      {
        return;
      }
      Thread.sleep(25);
    }
    assertTrue(condition.getAsBoolean());
  }

  protected Seat firstSeat(Flight flight,
      Class<? extends SeatClass> seatClassType)
  {
    return flight.getPlane().getSeats().stream()
        .filter(seat -> seatClassType.isInstance(seat.getSeatClass()))
        .findFirst()
        .orElseThrow();
  }

  protected List<Seat> economySeats(Flight flight)
  {
    return flight.getPlane().getSeats().stream()
        .filter(seat -> seat.getSeatClass() instanceof EconomyClass)
        .toList();
  }

  protected void chooseSeat(PassengerDetailsViewModel passengerDetails,
      int passengerNumber, int segmentIndex, Seat seat)
  {
    SeatMapViewModel seatMapViewModel = new SeatMapViewModel(passengerDetails);
    seatMapViewModel.startSelection(passengerNumber, segmentIndex);
    seatMapViewModel.temporarySelectionProperty().set(seat);
    seatMapViewModel.confirmSelection();
  }

  protected CapturedBooking verifyCreatedBooking(Flight expectedOutbound,
      Flight expectedReturn)
  {
    assertEquals(1, model.createBookingCalls);
    assertSame(expectedOutbound, model.createdFlight);
    assertSame(expectedReturn, model.createdReturnFlight);
    return new CapturedBooking(model.createdPassengers,
        model.createdOutboundSeats, model.createdReturnSeats);
  }

  protected record CapturedBooking(List<Passenger> passengers,
      List<Seat> outboundSeats, List<Seat> returnSeats)
  {
  }

  protected class FakeModel implements Model
  {
    final List<Flight> allFlights;
    final List<City> cities;
    final List<Carrier> carriers;
    final List<LuggageType> luggageTypes;
    User loggedInUser;
    int createBookingCalls;
    Flight createdFlight;
    Flight createdReturnFlight;
    List<Passenger> createdPassengers = Collections.emptyList();
    List<Seat> createdOutboundSeats = Collections.emptyList();
    List<Seat> createdReturnSeats;
    Booking createdBooking;
    int nextSeatAssignmentId = 1;

    FakeModel(List<Flight> allFlights, List<City> cities,
        List<Carrier> carriers, List<LuggageType> luggageTypes,
        User loggedInUser)
    {
      this.allFlights = allFlights;
      this.cities = cities;
      this.carriers = carriers;
      this.luggageTypes = luggageTypes;
      this.loggedInUser = loggedInUser;
    }

    @Override public List<Flight> searchFlights(SearchCriteria criteria)
    {
      return findMatchingFlights(criteria);
    }

    @Override public Flight getFlightDetails(int flightId)
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

    @Override public boolean login(String email, String password)
    {
      return loggedInUser != null && loggedInUser.login(email, password);
    }

    @Override public boolean register(String firstName, String lastName,
        String email, String password)
    {
      return true;
    }

    @Override public void logout()
    {
      if (loggedInUser != null)
      {
        loggedInUser.logout();
      }
      loggedInUser = null;
    }

    @Override public User getLoggedInUser()
    {
      return loggedInUser;
    }

    @Override public Booking createBooking(Flight flight,
        List<Passenger> passengers)
    {
      return createBooking(flight, passengers, Collections.emptyList());
    }

    @Override public Booking createBooking(Flight flight,
        List<Passenger> passengers, List<Seat> selectedSeats)
    {
      return createBooking(flight, passengers, selectedSeats, null, null);
    }

    @Override public Booking createBooking(Flight flight,
        List<Passenger> passengers, List<Seat> selectedSeats,
        Flight returnFlight, List<Seat> returnSeats)
    {
      createBookingCalls++;
      createdFlight = flight;
      createdReturnFlight = returnFlight;
      createdPassengers = new ArrayList<>(passengers);
      createdOutboundSeats = selectedSeats == null
          ? null : new ArrayList<>(selectedSeats);
      createdReturnSeats = returnSeats == null ? null : new ArrayList<>(
          returnSeats);
      createdBooking = new Booking((Customer) loggedInUser, flight,
          passengers);
      createdBooking.setReturnFlight(returnFlight);
      assignSeats(passengers, getSegments(flight), selectedSeats);
      if (returnFlight != null)
      {
        assignSeats(passengers, getSegments(returnFlight), returnSeats);
      }
      return createdBooking;
    }

    private void assignSeats(List<Passenger> passengers, List<Flight> segments,
        List<Seat> seats)
    {
      if (seats == null)
      {
        return;
      }

      int seatIndex = 0;
      for (Passenger passenger : passengers)
      {
        for (Flight segment : segments)
        {
          if (seatIndex < seats.size() && seats.get(seatIndex) != null)
          {
            new SeatAssignment(nextSeatAssignmentId++, passenger,
                seats.get(seatIndex), segment);
          }
          seatIndex++;
        }
      }
    }

    private List<Flight> getSegments(Flight flight)
    {
      List<Flight> segments = new ArrayList<>();
      if (flight instanceof ConnectingFlight connectingFlight)
      {
        segments.add(connectingFlight.getFirstSegment());
        segments.add(connectingFlight.getSecondSegment());
      }
      else if (flight != null)
      {
        segments.add(flight);
      }
      return segments;
    }

    @Override public void cancelBooking(Booking booking)
    {
      booking.cancel();
    }

    @Override public List<Booking> getAllBookings()
    {
      return Collections.emptyList();
    }

    @Override public List<Booking> getUserBookings()
    {
      if (loggedInUser instanceof Customer loggedInCustomer)
      {
        return new ArrayList<>(loggedInCustomer.viewBookings());
      }
      return Collections.emptyList();
    }

    @Override public Booking addBookingToCurrentUserById(int bookingId,
        String passengerLastName)
    {
      throw new UnsupportedOperationException(
          "Adding existing bookings is outside this test fake.");
    }

    @Override public void removeBookingFromCurrentUser(int bookingId)
    {
      throw new UnsupportedOperationException(
          "Removing existing bookings is outside this test fake.");
    }

    @Override public void addFlight(Flight flight)
    {
      allFlights.add(flight);
    }

    @Override public void removeFlight(Flight flight)
    {
      allFlights.remove(flight);
    }

    @Override public void editFlight(Flight flight)
    {
      for (int i = 0; i < allFlights.size(); i++)
      {
        if (allFlights.get(i).getFlightId() == flight.getFlightId())
        {
          allFlights.set(i, flight);
          return;
        }
      }
      allFlights.add(flight);
    }

    @Override public List<LuggageType> getLuggageTypes()
    {
      return new ArrayList<>(luggageTypes);
    }

    @Override public List<City> getAllCities()
    {
      return new ArrayList<>(cities);
    }

    @Override public List<City> getCities()
    {
      return getAllCities();
    }

    @Override public List<Plane> getPlanes()
    {
      List<Plane> planes = new ArrayList<>();
      for (Flight flight : allFlights)
      {
        if (!planes.contains(flight.getPlane()))
        {
          planes.add(flight.getPlane());
        }
      }
      return planes;
    }

    @Override public List<Carrier> getCarriers()
    {
      return new ArrayList<>(carriers);
    }

    @Override public List<Flight> getAllFlights()
    {
      return new ArrayList<>(allFlights);
    }

    @Override public boolean isLoggedIn()
    {
      return loggedInUser != null;
    }

    @Override public void addPropertyChangeListener(
        PropertyChangeListener listener)
    {
    }

    @Override public void removePropertyChangeListener(
        PropertyChangeListener listener)
    {
    }
  }
}
