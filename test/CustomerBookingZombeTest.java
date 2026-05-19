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
import client.view.PassengerDetailsViewController;
import client.viewmodel.BookingAdminViewModel;
import client.viewmodel.FlightSceneViewModel;
import client.viewmodel.PassengerDetailsViewModel;
import client.viewmodel.SeatMapViewModel;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.network.dto.BookingDto;
import server.network.dto.DtoMapper;

import java.beans.PropertyChangeListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CustomerBookingZombeTest
{
  private FakeModel model;
  private City berlin;
  private City paris;
  private City rome;
  private Carrier viaAir;
  private Carrier skyLine;
  private Flight outbound;
  private Flight returnFlight;
  private Flight cheapFlight;
  private Flight expensiveFlight;
  private ConnectingFlight connectingFlight;
  private LuggageType carryOn;
  private LuggageType checkedBaggage;
  private Customer customer;
  private List<Flight> flights;
  private int nextSeatId;
  private int nextPlaneId;

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
    customer = new Customer(7, "jane@example.com", "secret", "Jane", "Doe",
        new FlightSearchService());

    model = new FakeModel(new ArrayList<>(flights),
        new ArrayList<>(List.of(berlin, paris, rome)),
        new ArrayList<>(List.of(viaAir, skyLine)),
        new ArrayList<>(List.of(carryOn, checkedBaggage)), customer);
  }

  @Test
  public void zeroNoSelectedFlightShouldBlockPassengerDetailsAndBooking()
  {
    // Z - Zero: testing the booking flow before any price/flight row is selected.
    FlightSceneViewModel flightSceneViewModel = createFlightSceneViewModel();
    PassengerDetailsViewModel passengerDetails =
        flightSceneViewModel.getPassengerDetailsViewModel();

    IllegalStateException prepareError = assertThrows(
        IllegalStateException.class, passengerDetails::prepare);
    IllegalStateException bookingError = assertThrows(
        IllegalStateException.class, passengerDetails::confirmBooking);

    assertEquals("Please select a flight first.", prepareError.getMessage());
    assertEquals("Please select a flight first.", bookingError.getMessage());
    assertEquals(0, model.createBookingCalls);
  }

  @Test
  public void onePassengerShouldBookOneWayEconomyFlight()
  {
    // O - One: testing one customer selecting one price, one seat, and confirming.
    FlightSceneViewModel flightSceneViewModel = createFlightSceneViewModel();
    flightSceneViewModel.setSelectedFlight(outbound);
    PassengerDetailsViewModel passengerDetails =
        flightSceneViewModel.getPassengerDetailsViewModel();
    passengerDetails.prepare();

    Seat selectedSeat = firstSeat(outbound, EconomyClass.class);
    chooseSeat(passengerDetails, 1, 0, selectedSeat);

    Booking booking = passengerDetails.confirmBooking();

    assertSame(model.createdBooking, booking);
    assertEquals(100, passengerDetails.baseFareProperty().get(), 0.001);
    assertEquals(15, passengerDetails.carryOnFareProperty().get(), 0.001);
    assertEquals(0, passengerDetails.baggageFareProperty().get(), 0.001);
    assertEquals(115, passengerDetails.totalFareProperty().get(), 0.001);

    CapturedBooking captured = verifyCreatedBooking(outbound, null);
    assertEquals(1, captured.passengers().size());
    assertEquals("Jane", captured.passengers().get(0).getFirstName());
    assertEquals("Doe", captured.passengers().get(0).getLastName());
    assertEquals(1,
        captured.passengers().get(0).getPassengerLuggage().size());
    assertEquals("Carry-on", captured.passengers().get(0)
        .getPassengerLuggage().get(0).getLuggageType().getName());
    assertEquals(List.of(selectedSeat), captured.outboundSeats());
  }

  @Test
  public void bookingWithoutEverySeatShouldBeBlocked()
  {
    FlightSceneViewModel flightSceneViewModel = createFlightSceneViewModel();
    flightSceneViewModel.setSelectedFlight(outbound);
    PassengerDetailsViewModel passengerDetails =
        flightSceneViewModel.getPassengerDetailsViewModel();
    passengerDetails.prepare();

    IllegalStateException error = assertThrows(IllegalStateException.class,
        passengerDetails::confirmBooking);

    assertEquals("Please choose a seat for every passenger and flight segment.",
        error.getMessage());
    assertEquals(0, model.createBookingCalls);
  }

  @Test
  public void firstPassengerAutofillShouldRefreshFromLoggedInCustomer()
  {
    FlightSceneViewModel flightSceneViewModel = createFlightSceneViewModel();
    flightSceneViewModel.setSelectedFlight(outbound);
    PassengerDetailsViewModel passengerDetails =
        flightSceneViewModel.getPassengerDetailsViewModel();
    passengerDetails.prepare();

    PassengerDetailsViewModel.PassengerForm firstPassenger =
        passengerDetails.getPassengerForms().get(0);
    firstPassenger.setFirstName("Old");
    firstPassenger.setLastName("Memory");

    passengerDetails.prepare();

    assertEquals("Jane", firstPassenger.getFirstName());
    assertEquals("Doe", firstPassenger.getLastName());
  }

  @Test
  public void manyPassengersShouldKeepSeatsLuggageAndFareTotals()
  {
    // M - Many: testing several passengers, several luggage choices, and seat order.
    FlightSceneViewModel flightSceneViewModel = createFlightSceneViewModel();
    flightSceneViewModel.incrementPassengers();
    flightSceneViewModel.setSelectedFlight(outbound);
    PassengerDetailsViewModel passengerDetails =
        flightSceneViewModel.getPassengerDetailsViewModel();
    passengerDetails.prepare();

    PassengerDetailsViewModel.PassengerForm first =
        passengerDetails.getPassengerForms().get(0);
    PassengerDetailsViewModel.PassengerForm second =
        passengerDetails.getPassengerForms().get(1);
    first.baggageQuantityProperty().set(2);
    second.setFirstName("Alex");
    second.setLastName("Smith");
    second.carryOnQuantityProperty().set(2);
    second.baggageQuantityProperty().set(1);
    second.seatClassProperty(0).set(new BusinessClass());

    Seat firstSeat = firstSeat(outbound, EconomyClass.class);
    Seat secondSeat = firstSeat(outbound, BusinessClass.class);
    chooseSeat(passengerDetails, 1, 0, firstSeat);
    chooseSeat(passengerDetails, 2, 0, secondSeat);

    assertEquals(350, passengerDetails.baseFareProperty().get(), 0.001);
    assertEquals(45, passengerDetails.carryOnFareProperty().get(), 0.001);
    assertEquals(195, passengerDetails.baggageFareProperty().get(), 0.001);
    assertEquals(590, passengerDetails.totalFareProperty().get(), 0.001);

    passengerDetails.confirmBooking();

    CapturedBooking captured = verifyCreatedBooking(outbound, null);
    assertEquals(2, captured.passengers().size());
    assertEquals(List.of(firstSeat, secondSeat), captured.outboundSeats());
    assertEquals(2, captured.passengers().get(0).getPassengerLuggage().size());
    assertEquals(2, captured.passengers().get(1).getPassengerLuggage().size());
  }

  @Test
  public void boundaryPassengerAndLuggageControlsShouldUseExpectedLimits()
  {
    // B - Boundary: testing min/max passenger counts and luggage edge values.
    FlightSceneViewModel flightSceneViewModel = createFlightSceneViewModel();
    for (int i = 0; i < 20; i++)
    {
      flightSceneViewModel.incrementPassengers();
    }
    assertEquals(9, flightSceneViewModel.passengerCountProperty().get());

    for (int i = 0; i < 20; i++)
    {
      flightSceneViewModel.decrementPassengers();
    }
    assertEquals(1, flightSceneViewModel.passengerCountProperty().get());

    flightSceneViewModel.setSelectedFlight(outbound);
    PassengerDetailsViewModel passengerDetails =
        flightSceneViewModel.getPassengerDetailsViewModel();
    passengerDetails.prepare();
    PassengerDetailsViewModel.PassengerForm form =
        passengerDetails.getPassengerForms().get(0);

    form.carryOnQuantityProperty().set(0);
    form.baggageQuantityProperty().set(0);
    assertEquals(0, passengerDetails.carryOnFareProperty().get(), 0.001);
    assertEquals(0, passengerDetails.baggageFareProperty().get(), 0.001);

    form.carryOnQuantityProperty().set(
        PassengerDetailsViewModel.MAX_CARRY_ON_BAGS);
    form.baggageQuantityProperty().set(5);
    assertEquals(30, passengerDetails.carryOnFareProperty().get(), 0.001);
    assertEquals(325, passengerDetails.baggageFareProperty().get(), 0.001);
  }

  @Test
  public void interfaceSearchSortRoundTripAndReserveShouldCallModelCorrectly()
  {
    // I - Interface: testing the actions behind search, filters, seat OK, and reserve.
    FlightSceneViewModel flightSceneViewModel = createFlightSceneViewModel();
    flightSceneViewModel.sortFlights("Price (Low to High)");
    assertEquals(cheapFlight, flightSceneViewModel.getFilteredFlights().get(0));

    flightSceneViewModel.filterByCarrier("VIA Air");
    assertTrue(flightSceneViewModel.getFilteredFlights().stream()
        .allMatch(flight -> flight.getCarrier().equals(viaAir)));

    flightSceneViewModel.directOnlyProperty().set(true);
    flightSceneViewModel.searchFlights();
    assertFalse(flightSceneViewModel.getFilteredFlights().stream()
        .anyMatch(ConnectingFlight.class::isInstance));

    flightSceneViewModel.roundTripProperty().set(true);
    flightSceneViewModel.departureCityProperty().set(berlin);
    flightSceneViewModel.arrivalCityProperty().set(paris);
    flightSceneViewModel.travelDateProperty().set(
        outbound.getDepartureTime().toLocalDate());
    flightSceneViewModel.returnDateProperty().set(
        returnFlight.getDepartureTime().toLocalDate());
    flightSceneViewModel.searchFlights();
    flightSceneViewModel.setSelectedFlight(outbound);
    flightSceneViewModel.setSelectedReturnFlight(returnFlight);

    assertTrue(flightSceneViewModel.getReturnFlights().contains(returnFlight));

    PassengerDetailsViewModel passengerDetails =
        flightSceneViewModel.getPassengerDetailsViewModel();
    passengerDetails.prepare();
    Seat outboundSeat = firstSeat(outbound, EconomyClass.class);
    Seat returnSeat = firstSeat(returnFlight, EconomyClass.class);
    chooseSeat(passengerDetails, 1, 0, outboundSeat);
    chooseSeat(passengerDetails, 1, 1, returnSeat);

    passengerDetails.confirmBooking();

    CapturedBooking captured = verifyCreatedBooking(outbound, returnFlight);
    assertEquals(List.of(outboundSeat), captured.outboundSeats());
    assertEquals(List.of(returnSeat), captured.returnSeats());
  }

  @Test
  public void exceptionInvalidPassengerAndSeatActionsShouldThrow()
  {
    // E - Exception: testing invalid names, wrong class seats, taken seats, and duplicates.
    FlightSceneViewModel flightSceneViewModel = createFlightSceneViewModel();
    flightSceneViewModel.incrementPassengers();
    flightSceneViewModel.setSelectedFlight(outbound);
    PassengerDetailsViewModel passengerDetails =
        flightSceneViewModel.getPassengerDetailsViewModel();
    passengerDetails.prepare();

    PassengerDetailsViewModel.PassengerForm first =
        passengerDetails.getPassengerForms().get(0);
    PassengerDetailsViewModel.PassengerForm second =
        passengerDetails.getPassengerForms().get(1);
    passengerDetails.selectSeatForPassenger(1, 0, economySeats(outbound).get(0));
    passengerDetails.selectSeatForPassenger(2, 0, economySeats(outbound).get(1));
    first.setFirstName("");
    assertThrows(IllegalStateException.class, passengerDetails::confirmBooking);

    first.setFirstName("Jane");
    second.setFirstName("Alex");
    second.setLastName("Smith");

    Seat businessSeat = firstSeat(outbound, BusinessClass.class);
    assertThrows(IllegalArgumentException.class,
        () -> passengerDetails.selectSeatForPassenger(1, 0, businessSeat));

    Seat takenSeat = economySeats(outbound).get(1);
    outbound.markSeatOccupied(takenSeat);
    assertThrows(IllegalArgumentException.class,
        () -> passengerDetails.selectSeatForPassenger(1, 0, takenSeat));

    Seat sharedSeat = economySeats(outbound).get(2);
    passengerDetails.selectSeatForPassenger(1, 0, sharedSeat);
    assertThrows(IllegalArgumentException.class,
        () -> passengerDetails.selectSeatForPassenger(2, 0, sharedSeat));
    assertEquals(0, model.createBookingCalls);
  }

  @Test
  public void bookingConfirmationMessageShouldUseReferenceCode()
  {
    Booking booking = new Booking(1234, customer, outbound,
        List.of(new Passenger(2000, "Jane", "Doe")));

    String message =
        PassengerDetailsViewController.formatBookingSuccessMessage(booking);

    assertEquals("Booking reference code: #1234 has been saved.", message);
    assertTrue(message.contains("reference code"));
  }

  @Test
  public void cancellingLoadedAssignmentShouldFreeOriginalSeat()
  {
    Passenger originalPassenger = new Passenger(2001, "Jane", "Doe");
    Booking originalBooking = new Booking(customer, outbound,
        List.of(originalPassenger));
    Seat seat = firstSeat(outbound, EconomyClass.class);
    new SeatAssignment(99, originalPassenger, seat, outbound);

    assertFalse(outbound.getAvailableSeats().contains(seat));

    Passenger loadedPassenger = new Passenger(originalPassenger.getPassengerId(),
        "Jane", "Doe");
    new Booking(originalBooking.getBookingId(), customer, outbound,
        List.of(loadedPassenger));
    SeatAssignment loadedAssignment = new SeatAssignment(outbound, seat,
        loadedPassenger);
    loadedPassenger.addSeatAssignment(loadedAssignment);
    outbound.markSeatOccupied(seat);

    loadedAssignment.release();

    assertTrue(outbound.getAvailableSeats().contains(seat));
  }

  @Test
  public void connectedRoundTripBookingShouldMatchSummaryTotal()
  {
    Flight returnFirst = createFlight(7, "VIA701", paris, rome, viaAir, 60,
        returnFlight.getDepartureTime().plusHours(1));
    Flight returnSecond = createFlight(8, "VIA702", rome, berlin, viaAir, 70,
        returnFlight.getDepartureTime().plusHours(4));
    ConnectingFlight returnConnection =
        new ConnectingFlight(returnFirst, returnSecond);

    FlightSceneViewModel flightSceneViewModel = createFlightSceneViewModel();
    flightSceneViewModel.setSelectedFlight(connectingFlight);
    flightSceneViewModel.setSelectedReturnFlight(returnConnection);
    PassengerDetailsViewModel passengerDetails =
        flightSceneViewModel.getPassengerDetailsViewModel();
    passengerDetails.prepare();

    PassengerDetailsViewModel.PassengerForm form =
        passengerDetails.getPassengerForms().get(0);
    form.seatClassProperty(1).set(new BusinessClass());

    chooseSeat(passengerDetails, 1, 0,
        firstSeat(connectingFlight.getFirstSegment(), EconomyClass.class));
    chooseSeat(passengerDetails, 1, 1,
        firstSeat(connectingFlight.getSecondSegment(), BusinessClass.class));
    chooseSeat(passengerDetails, 1, 2,
        firstSeat(returnFirst, EconomyClass.class));
    chooseSeat(passengerDetails, 1, 3,
        firstSeat(returnSecond, EconomyClass.class));

    double expectedTotal = 75 + (95 * 2.5) + 60 + 70
        + PassengerDetailsViewModel.CARRY_ON_UNIT_PRICE;
    assertEquals(expectedTotal, passengerDetails.totalFareProperty().get(),
        0.001);

    Booking booking = passengerDetails.confirmBooking();

    assertEquals(expectedTotal, booking.getTotalPrice(), 0.001);
    assertTrue(booking.getBookingSummary().contains(
        "Total price: EUR " + String.format("%.2f", expectedTotal)));
  }

  @Test
  public void bookingDtoShouldKeepReturnFlightAndAllSeatAssignments()
  {
    Flight returnFirst = createFlight(7, "VIA701", paris, rome, viaAir, 60,
        returnFlight.getDepartureTime().plusHours(1));
    Flight returnSecond = createFlight(8, "VIA702", rome, berlin, viaAir, 70,
        returnFlight.getDepartureTime().plusHours(4));
    ConnectingFlight returnConnection =
        new ConnectingFlight(returnFirst, returnSecond);
    Passenger passenger = new Passenger(2002, "Jane", "Doe");
    Booking booking = new Booking(3000, customer, connectingFlight,
        List.of(passenger));
    booking.setReturnFlight(returnConnection);

    new SeatAssignment(1, passenger,
        firstSeat(connectingFlight.getFirstSegment(), EconomyClass.class),
        connectingFlight.getFirstSegment());
    new SeatAssignment(2, passenger,
        firstSeat(connectingFlight.getSecondSegment(), EconomyClass.class),
        connectingFlight.getSecondSegment());
    new SeatAssignment(3, passenger,
        firstSeat(returnFirst, EconomyClass.class), returnFirst);
    new SeatAssignment(4, passenger,
        firstSeat(returnSecond, EconomyClass.class), returnSecond);

    BookingDto dto = DtoMapper.toDto(booking);
    Booking mappedBooking = DtoMapper.fromDto(dto);

    assertNotNull(dto.returnFlight);
    assertNotNull(mappedBooking.getReturnFlight());
    assertEquals(4, mappedBooking.getPassengers().get(0)
        .getSeatAssignments().size());
  }

  @Test
  public void bookingDtoShouldKeepCurrentUserOwnerAccess()
  {
    Booking booking = new Booking(3001, customer, outbound,
        List.of(new Passenger(2005, "Jane", "Doe")));
    booking.setOwnedByCurrentUser(false);

    BookingDto dto = DtoMapper.toDto(booking);
    Booking mappedBooking = DtoMapper.fromDto(dto);

    assertFalse(dto.ownedByCurrentUser);
    assertFalse(mappedBooking.isOwnedByCurrentUser());
  }

  @Test
  public void adminBookingTripTypeShouldUseReturnFlightOnly()
  {
    BookingAdminViewModel bookingAdminViewModel =
        new BookingAdminViewModel(model);
    Booking oneWay = new Booking(4000, customer, outbound,
        List.of(new Passenger(2003, "One", "Way")));
    Booking roundTrip = new Booking(4001, customer, outbound,
        List.of(new Passenger(2004, "Round", "Trip")));
    roundTrip.setReturnFlight(returnFlight);

    assertEquals("One-way", bookingAdminViewModel.getTripType(oneWay));
    assertEquals("Round trip", bookingAdminViewModel.getTripType(roundTrip));
    assertEquals("#4000", bookingAdminViewModel.getBookingCode(oneWay));
  }

  private FlightSceneViewModel createFlightSceneViewModel()
  {
    return new FlightSceneViewModel(model);
  }

  private Flight createFlight(int id, String flightNumber, City departure,
      City arrival, Carrier carrier, double basePrice,
      LocalDateTime departureTime)
  {
    Plane plane = createPlane(carrier);
    return new Flight(id, flightNumber, departureTime,
        departureTime.plusHours(2), basePrice, carrier, plane, departure,
        arrival);
  }

  private Plane createPlane(Carrier carrier)
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

  private List<Flight> findMatchingFlights(SearchCriteria criteria)
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

  private Seat firstSeat(Flight flight,
      Class<? extends SeatClass> seatClassType)
  {
    return flight.getPlane().getSeats().stream()
        .filter(seat -> seatClassType.isInstance(seat.getSeatClass()))
        .findFirst()
        .orElseThrow();
  }

  private List<Seat> economySeats(Flight flight)
  {
    return flight.getPlane().getSeats().stream()
        .filter(seat -> seat.getSeatClass() instanceof EconomyClass)
        .toList();
  }

  private void chooseSeat(PassengerDetailsViewModel passengerDetails,
      int passengerNumber, int segmentIndex, Seat seat)
  {
    SeatMapViewModel seatMapViewModel = new SeatMapViewModel(passengerDetails);
    seatMapViewModel.startSelection(passengerNumber, segmentIndex);
    seatMapViewModel.temporarySelectionProperty().set(seat);
    seatMapViewModel.confirmSelection();
  }

  private CapturedBooking verifyCreatedBooking(Flight expectedOutbound,
      Flight expectedReturn)
  {
    assertEquals(1, model.createBookingCalls);
    assertSame(expectedOutbound, model.createdFlight);
    assertSame(expectedReturn, model.createdReturnFlight);
    return new CapturedBooking(model.createdPassengers,
        model.createdOutboundSeats, model.createdReturnSeats);
  }

  private record CapturedBooking(List<Passenger> passengers,
      List<Seat> outboundSeats, List<Seat> returnSeats)
  {
  }

  private class FakeModel implements Model
  {
    private final List<Flight> allFlights;
    private final List<City> cities;
    private final List<Carrier> carriers;
    private final List<LuggageType> luggageTypes;
    private User loggedInUser;
    private int createBookingCalls;
    private Flight createdFlight;
    private Flight createdReturnFlight;
    private List<Passenger> createdPassengers = Collections.emptyList();
    private List<Seat> createdOutboundSeats = Collections.emptyList();
    private List<Seat> createdReturnSeats;
    private Booking createdBooking;
    private int nextSeatAssignmentId = 1;

    private FakeModel(List<Flight> allFlights, List<City> cities,
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
      if (loggedInUser instanceof Customer customer)
      {
        return new ArrayList<>(customer.viewBookings());
      }
      return Collections.emptyList();
    }

    @Override public Booking addBookingToCurrentUserById(int bookingId)
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
