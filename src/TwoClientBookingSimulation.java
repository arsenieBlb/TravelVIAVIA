import clientmediator.Client;
import model.Booking;
import model.Flight;
import model.Passenger;
import model.Seat;
import servermediator.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TwoClientBookingSimulation
{
  private static final String HOST = "localhost";
  private static final ClientCredentials[] CLIENTS = {
      new ClientCredentials("Client-1", "j.doe@gmail.com", "1234b",
          "John", "Doe"),
      new ClientCredentials("Client-2", "alice.smith@outlook.com", "1234c",
          "Alice", "Smith")
  };

  public static void main(String[] args) throws InterruptedException
  {
    ensureServerIsRunning();

    CountDownLatch readyToBook = new CountDownLatch(CLIENTS.length);
    CountDownLatch startBooking = new CountDownLatch(1);
    Thread[] threads = new Thread[CLIENTS.length];

    for (int i = 0; i < CLIENTS.length; i++)
    {
      int clientNumber = i + 1;
      ClientCredentials credentials = CLIENTS[i];
      threads[i] = new Thread(
          () -> runClient(credentials, clientNumber, readyToBook,
              startBooking),
          credentials.label);
      threads[i].start();
    }

    if (!readyToBook.await(30, TimeUnit.SECONDS))
    {
      System.out.println("Not all clients reached the booking step in time.");
    }

    System.out.println("Both client threads are released to book now.");
    startBooking.countDown();

    for (Thread thread : threads)
    {
      thread.join();
    }

    System.out.println("Two-client booking simulation finished.");
  }

  private static void runClient(ClientCredentials credentials, int clientNumber,
      CountDownLatch readyToBook, CountDownLatch startBooking)
  {
    boolean prepared = false;

    try (Client client = new Client(HOST, Server.PORT))
    {
      System.out.println(credentials.label + " logging in as "
          + credentials.email);

      if (!client.login(credentials.email, credentials.password))
      {
        System.out.println(credentials.label + " login failed.");
        return;
      }

      Flight flight = findBookableFlight(client.getAllFlights());
      if (flight == null)
      {
        System.out.println(credentials.label + " found no flight with seats.");
        return;
      }

      Seat seat = flight.getAvailableSeats().get(0);
      Passenger passenger = new Passenger(9000 + clientNumber,
          credentials.firstName, credentials.lastName);

      System.out.println(credentials.label + " prepared flight "
          + flight.getFlightNumber() + " "
          + flight.getDepartureCity().getCityName() + " -> "
          + flight.getArrivalCity().getCityName() + " seat "
          + seat.getSeatNumber());

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
        Booking booking = client.createBooking(flight, List.of(passenger),
            List.of(seat));
        System.out.println(credentials.label + " booked successfully: booking #"
            + booking.getBookingId() + ", seat " + seat.getSeatNumber());
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
      System.out.println(credentials.label + " stopped: " + e.getMessage());
    }
    finally
    {
      if (!prepared)
      {
        readyToBook.countDown();
      }
    }
  }

  private static Flight findBookableFlight(List<Flight> flights)
  {
    for (Flight flight : flights)
    {
      if (!flight.getAvailableSeats().isEmpty())
      {
        return flight;
      }
    }
    return null;
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

  private record ClientCredentials(String label, String email, String password,
      String firstName, String lastName)
  {
  }
}
