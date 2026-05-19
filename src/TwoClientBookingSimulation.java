import client.mediator.Client;
import client.model.Booking;
import client.model.ConnectingFlight;
import client.model.Flight;
import client.model.LuggageType;
import client.model.Passenger;
import client.model.PassengerLuggage;
import client.model.Seat;
import server.mediator.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class TwoClientBookingSimulation
{
  private static final String HOST = "localhost";
  private static final int RANDOM_CLIENT_COUNT = 2;
  private static final int MAX_PASSENGERS_PER_BOOKING = 3;
  private static final int MAX_LUGGAGE_QUANTITY = 3;
  private static final int MAX_BOOKING_ATTEMPTS = 3;
  private static final int MAX_REGISTRATION_ATTEMPTS = 5;
  private static final long BACKGROUND_START_DELAY_MILLIS = 1500;
  private static final String RANDOM_PASSWORD = "random1234";
  private static final String RANDOM_EMAIL_DOMAIN = "travelvia.local";
  private static final String[] RANDOM_FIRST_NAMES = {
      "Maya", "Noah", "Lina", "Oscar", "Sofia", "Theo", "Nora", "Leo"
  };
  private static final String[] RANDOM_LAST_NAMES = {
      "Andersen", "Khan", "Rossi", "Berg", "Patel", "Muller", "Silva",
      "Novak"
  };

  public static void main(String[] args) throws InterruptedException
  {
    runSimulation();
  }

  public static void startRandomClientsInBackground()
  {
    if (!Boolean.getBoolean("travelviavia.randomClients"))
    {
      return;
    }

    Thread simulationThread = new Thread(() -> {
      try
      {
        Thread.sleep(BACKGROUND_START_DELAY_MILLIS);
        runSimulation();
      }
      catch (InterruptedException e)
      {
        Thread.currentThread().interrupt();
      }
      catch (RuntimeException e)
      {
        System.out.println("Random client booking simulation stopped: "
            + e.getMessage());
      }
    }, "TravelVIAVIA-random-client-booking-simulation");
    simulationThread.setDaemon(true);
    simulationThread.start();
  }

  private static void runSimulation() throws InterruptedException
  {
    ensureServerIsRunning();

    CountDownLatch readyToBook = new CountDownLatch(RANDOM_CLIENT_COUNT);
    CountDownLatch startBooking = new CountDownLatch(1);
    Thread[] threads = new Thread[RANDOM_CLIENT_COUNT];

    for (int i = 0; i < RANDOM_CLIENT_COUNT; i++)
    {
      int clientNumber = i + 1;
      threads[i] = new Thread(
          () -> runClient(clientNumber, readyToBook, startBooking),
          "Random-client-" + clientNumber);
      threads[i].start();
    }

    if (!readyToBook.await(30, TimeUnit.SECONDS))
    {
      System.out.println("Not all clients reached the booking step in time.");
    }

    System.out.println("Random client threads are released to book now.");
    startBooking.countDown();

    for (Thread thread : threads)
    {
      thread.join();
    }

    System.out.println("Random client booking simulation finished.");
  }

  private static void runClient(int clientNumber,
      CountDownLatch readyToBook, CountDownLatch startBooking)
  {
    boolean prepared = false;
    String clientLabel = "Random-client-" + clientNumber;

    try (Client client = new Client(HOST, Server.PORT))
    {
      ClientCredentials credentials = registerRandomCustomer(client,
          clientNumber);
      if (credentials == null)
      {
        System.out.println(clientLabel + " could not register a customer.");
        return;
      }

      System.out.println(credentials.label + " registered as "
          + credentials.email + " and is logging in.");

      if (!client.login(credentials.email, credentials.password))
      {
        System.out.println(credentials.label + " login failed.");
        return;
      }

      BookingPlan plan = createRandomBookingPlan(client, clientNumber);
      if (plan == null)
      {
        System.out.println(credentials.label
            + " found no random bookable flight with enough seats.");
        return;
      }

      System.out.println(credentials.label + " prepared "
          + describePlan(plan));

      prepared = true;
      readyToBook.countDown();

      if (!startBooking.await(10, TimeUnit.SECONDS))
      {
        System.out.println(credentials.label
            + " did not receive the booking start signal.");
        return;
      }

      try
      {
        Booking booking = bookWithRetries(client, credentials.label, plan,
            clientNumber);
        if (booking != null)
        {
          System.out.println(credentials.label
              + " booked successfully: booking #"
              + booking.getBookingId() + ".");
        }
      }
      catch (RuntimeException e)
      {
        System.out.println(credentials.label + " booking failed: "
            + e.getMessage());
      }
      finally
      {
        client.logout();
      }
    }
    catch (RuntimeException | InterruptedException e)
    {
      if (e instanceof InterruptedException)
      {
        Thread.currentThread().interrupt();
      }
      System.out.println(clientLabel + " stopped: " + e.getMessage());
    }
    finally
    {
      if (!prepared)
      {
        readyToBook.countDown();
      }
    }
  }

  private static ClientCredentials registerRandomCustomer(Client client,
      int clientNumber)
  {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    for (int attempt = 1; attempt <= MAX_REGISTRATION_ATTEMPTS; attempt++)
    {
      String firstName = randomItem(RANDOM_FIRST_NAMES, random);
      String lastName = randomItem(RANDOM_LAST_NAMES, random);
      String email = randomEmail(clientNumber, random);

      if (client.register(firstName, lastName, email, RANDOM_PASSWORD))
      {
        return new ClientCredentials("Random-client-" + clientNumber, email,
            RANDOM_PASSWORD);
      }
    }
    return null;
  }

  private static String randomEmail(int clientNumber, ThreadLocalRandom random)
  {
    return "random.client." + clientNumber + "."
        + System.currentTimeMillis() + "."
        + random.nextInt(1000, 10000) + "@" + RANDOM_EMAIL_DOMAIN;
  }

  private static Booking bookWithRetries(Client client, String clientLabel,
      BookingPlan firstPlan, int clientNumber)
  {
    BookingPlan plan = firstPlan;
    for (int attempt = 1; attempt <= MAX_BOOKING_ATTEMPTS; attempt++)
    {
      try
      {
        return client.createBooking(plan.flight, plan.passengers,
            plan.selectedSeats);
      }
      catch (RuntimeException e)
      {
        System.out.println(clientLabel + " booking attempt " + attempt
            + " failed: " + e.getMessage());
        if (attempt == MAX_BOOKING_ATTEMPTS)
        {
          throw e;
        }
        plan = createRandomBookingPlan(client, clientNumber);
        if (plan == null)
        {
          throw new IllegalStateException(
              "No random retry booking plan could be prepared.", e);
        }
        System.out.println(clientLabel + " retrying with "
            + describePlan(plan));
      }
    }
    return null;
  }

  private static BookingPlan createRandomBookingPlan(Client client,
      int clientNumber)
  {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    List<Flight> candidates = new ArrayList<>();
    for (Flight flight : client.getAllFlights())
    {
      if (maxPassengersForFlight(flight) > 0)
      {
        candidates.add(flight);
      }
    }
    if (candidates.isEmpty())
    {
      return null;
    }

    Flight flight = randomItem(candidates, random);
    int passengerCount = random.nextInt(1,
        Math.min(MAX_PASSENGERS_PER_BOOKING,
            maxPassengersForFlight(flight)) + 1);
    List<LuggageType> luggageTypes = client.getLuggageTypes();
    List<Passenger> passengers = randomPassengers(clientNumber,
        passengerCount, luggageTypes, random);
    List<Seat> selectedSeats = randomSeats(flight, passengerCount, random);
    return new BookingPlan(flight, passengers, selectedSeats);
  }

  private static List<Passenger> randomPassengers(int clientNumber,
      int passengerCount, List<LuggageType> luggageTypes,
      ThreadLocalRandom random)
  {
    List<Passenger> passengers = new ArrayList<>();
    int passengerBaseId = clientNumber * 100000 + random.nextInt(1000, 9000);
    int luggageBaseId = clientNumber * 100000 + random.nextInt(100, 900);

    for (int i = 0; i < passengerCount; i++)
    {
      Passenger passenger = new Passenger(passengerBaseId + i,
          randomItem(RANDOM_FIRST_NAMES, random),
          randomItem(RANDOM_LAST_NAMES, random));
      addRandomLuggage(passenger, luggageTypes, luggageBaseId + (i * 10),
          random);
      passengers.add(passenger);
    }
    return passengers;
  }

  private static void addRandomLuggage(Passenger passenger,
      List<LuggageType> luggageTypes, int luggageBaseId,
      ThreadLocalRandom random)
  {
    if (luggageTypes == null || luggageTypes.isEmpty())
    {
      return;
    }

    boolean addedAny = false;
    for (int i = 0; i < luggageTypes.size(); i++)
    {
      int quantity = random.nextInt(0, MAX_LUGGAGE_QUANTITY + 1);
      if (quantity > 0)
      {
        passenger.addPassengerLuggage(new PassengerLuggage(luggageBaseId + i,
            quantity, luggageTypes.get(i)));
        addedAny = true;
      }
    }

    if (!addedAny && random.nextBoolean())
    {
      passenger.addPassengerLuggage(new PassengerLuggage(luggageBaseId,
          random.nextInt(1, MAX_LUGGAGE_QUANTITY + 1),
          randomItem(luggageTypes, random)));
    }
  }

  private static List<Seat> randomSeats(Flight flight, int passengerCount,
      ThreadLocalRandom random)
  {
    List<List<Seat>> availableSeatsBySegment = new ArrayList<>();
    for (Flight segment : getSegments(flight))
    {
      availableSeatsBySegment.add(new ArrayList<>(segment.getAvailableSeats()));
    }

    List<Seat> selectedSeats = new ArrayList<>();
    for (int passengerIndex = 0; passengerIndex < passengerCount;
        passengerIndex++)
    {
      for (List<Seat> availableSeats : availableSeatsBySegment)
      {
        selectedSeats.add(removeRandomSeat(availableSeats, random));
      }
    }
    return selectedSeats;
  }

  private static Seat removeRandomSeat(List<Seat> seats,
      ThreadLocalRandom random)
  {
    return seats.remove(random.nextInt(seats.size()));
  }

  private static int maxPassengersForFlight(Flight flight)
  {
    int maxPassengers = Integer.MAX_VALUE;
    for (Flight segment : getSegments(flight))
    {
      maxPassengers = Math.min(maxPassengers,
          segment.getAvailableSeats().size());
    }
    return maxPassengers == Integer.MAX_VALUE ? 0 : maxPassengers;
  }

  private static List<Flight> getSegments(Flight flight)
  {
    if (flight instanceof ConnectingFlight connectingFlight)
    {
      return List.of(connectingFlight.getFirstSegment(),
          connectingFlight.getSecondSegment());
    }
    return List.of(flight);
  }

  private static String describePlan(BookingPlan plan)
  {
    return plan.passengers.size() + " passenger(s), flight "
        + plan.flight.getFlightNumber() + " "
        + plan.flight.getDepartureCity().getCityName() + " -> "
        + plan.flight.getArrivalCity().getCityName()
        + ", seats " + describeSeats(plan)
        + ", luggage " + describeLuggage(plan.passengers);
  }

  private static String describeSeats(BookingPlan plan)
  {
    List<String> seatNames = new ArrayList<>();
    for (Seat seat : plan.selectedSeats)
    {
      seatNames.add(seat.getSeatNumber());
    }
    return String.join(", ", seatNames);
  }

  private static String describeLuggage(List<Passenger> passengers)
  {
    List<String> values = new ArrayList<>();
    for (Passenger passenger : passengers)
    {
      if (passenger.getPassengerLuggage().isEmpty())
      {
        values.add(passenger.getFullName() + ": none");
        continue;
      }

      List<String> luggageValues = new ArrayList<>();
      for (PassengerLuggage luggage : passenger.getPassengerLuggage())
      {
        luggageValues.add(luggage.getQuantity() + "x "
            + luggage.getLuggageType().getName());
      }
      values.add(passenger.getFullName() + ": "
          + String.join(", ", luggageValues));
    }
    return String.join(" | ", values);
  }

  private static <T> T randomItem(List<T> items, ThreadLocalRandom random)
  {
    return items.get(random.nextInt(items.size()));
  }

  private static String randomItem(String[] items, ThreadLocalRandom random)
  {
    return items[random.nextInt(items.length)];
  }

  private static void ensureServerIsRunning()
  {
    if (canConnectToServer())
    {
      System.out.println("Using existing TravelVIAVIA socket server.");
      return;
    }

    System.out.println("Starting local TravelVIAVIA socket server.");
    Thread serverThread = new Thread(() -> new Server().start(),
        "TravelVIAVIA-simulation-server");
    serverThread.setDaemon(true);
    serverThread.start();

    waitForServer();
  }

  private static boolean canConnectToServer()
  {
    try (Socket socket = new Socket())
    {
      socket.connect(new InetSocketAddress(HOST, Server.PORT), 500);
      return true;
    }
    catch (IOException e)
    {
      return false;
    }
  }

  private static void waitForServer()
  {
    for (int attempt = 0; attempt < 20; attempt++)
    {
      if (canConnectToServer())
      {
        return;
      }

      try
      {
        Thread.sleep(250);
      }
      catch (InterruptedException e)
      {
        Thread.currentThread().interrupt();
        throw new IllegalStateException("Interrupted while starting server.",
            e);
      }
    }

    throw new IllegalStateException("TravelVIAVIA server did not start.");
  }

  private record ClientCredentials(String label, String email, String password)
  {
  }

  private record BookingPlan(Flight flight, List<Passenger> passengers,
      List<Seat> selectedSeats)
  {
  }
}
