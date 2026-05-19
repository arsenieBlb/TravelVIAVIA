package client.mediator;

import com.google.gson.Gson;
import client.model.Booking;
import client.model.Carrier;
import client.model.City;
import client.model.Flight;
import client.model.LuggageType;
import client.model.Passenger;
import client.model.Plane;
import client.model.SearchCriteria;
import client.model.Seat;
import client.model.User;
import server.network.NetworkPackage;
import server.network.RequestType;
import server.network.dto.AddBookingByIdRequest;
import server.network.dto.BookingDto;
import server.network.dto.BookingRequest;
import server.network.dto.CarrierDto;
import server.network.dto.CityDto;
import server.network.dto.DtoMapper;
import server.network.dto.FlightDto;
import server.network.dto.IdRequest;
import server.network.dto.LoginRequest;
import server.network.dto.LuggageTypeDto;
import server.network.dto.PlaneDto;
import server.network.dto.RegisterRequest;
import server.network.dto.SearchFlightsRequest;
import server.network.dto.UserDto;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class Client implements ServerModel, AutoCloseable
{
  private static final int REQUEST_TIMEOUT_SECONDS = 30;

  private final Socket socket;
  private final BufferedReader in;
  private final PrintWriter out;
  private final Gson gson;
  private final PropertyChangeSupport support;
  private final AtomicInteger nextRequestId;
  private final Map<Integer, CompletableFuture<NetworkPackage>> pendingReplies;
  private User loggedInUser;
  private boolean loggedInUserLoaded;
  private List<LuggageType> luggageTypesCache;
  private List<City> allCitiesCache;
  private List<Plane> planesCache;
  private List<Carrier> carriersCache;

  public Client(String host, int port)
  {
    try
    {
      this.socket = connectWithRetry(host, port);
      this.in = new BufferedReader(
          new InputStreamReader(socket.getInputStream()));
      this.out = new PrintWriter(socket.getOutputStream(), true);
      this.gson = new Gson();
      this.support = new PropertyChangeSupport(this);
      this.nextRequestId = new AtomicInteger(1);
      this.pendingReplies = new ConcurrentHashMap<>();
      this.loggedInUser = null;
      this.loggedInUserLoaded = false;
      this.luggageTypesCache = null;
      this.allCitiesCache = null;
      this.planesCache = null;
      this.carriersCache = null;

      Thread listenerThread = new Thread(this::listenFromServer,
          "TravelVIAVIA-client-listener");
      listenerThread.setDaemon(true);
      listenerThread.start();
      System.out.println("CLIENT connected to server " + host + ":" + port);
    }
    catch (IOException e)
    {
      throw new IllegalStateException(
          "Could not connect to TravelVIAVIA server at " + host + ":" + port
              + ". Start server.mediator.ServerMain first.", e);
    }
  }

  private static Socket connectWithRetry(String host, int port)
      throws IOException
  {
    IOException lastException = null;
    for (int attempt = 0; attempt < 20; attempt++)
    {
      try
      {
        return new Socket(host, port);
      }
      catch (IOException e)
      {
        lastException = e;
        try
        {
          Thread.sleep(250);
        }
        catch (InterruptedException interruptedException)
        {
          Thread.currentThread().interrupt();
          IOException interrupted =
              new IOException("Interrupted while connecting to server.",
                  interruptedException);
          interrupted.addSuppressed(e);
          throw interrupted;
        }
      }
    }
    throw lastException;
  }

  public void sendToServer(NetworkPackage networkPackage)
  {
    synchronized (out)
    {
      out.println(gson.toJson(networkPackage));
    }
  }

  private void listenFromServer()
  {
    try
    {
      String line;
      while ((line = in.readLine()) != null)
      {
        NetworkPackage networkPackage = gson.fromJson(line,
            NetworkPackage.class);
        CompletableFuture<NetworkPackage> pending =
            pendingReplies.remove(networkPackage.requestId);
        if (pending != null)
        {
          pending.complete(networkPackage);
        }
        else
        {
          handleBroadcast(networkPackage);
        }
      }
    }
    catch (IOException e)
    {
      completePendingWithError(e);
    }
    finally
    {
      completePendingWithError(new IOException("Server connection closed."));
    }
  }

  private void handleBroadcast(NetworkPackage networkPackage)
  {
    if (RequestType.BROADCAST_PROPERTY_CHANGE.equals(networkPackage.type))
    {
      String propertyName = gson.fromJson(networkPackage.contentJson,
          String.class);
      System.out.println("CLIENT broadcast received: " + propertyName);
      if ("currentUser".equals(propertyName))
      {
        loggedInUserLoaded = false;
        return;
      }
      support.firePropertyChange(propertyName, null, "server");
    }
  }

  private void completePendingWithError(Exception exception)
  {
    for (CompletableFuture<NetworkPackage> future : pendingReplies.values())
    {
      future.completeExceptionally(exception);
    }
    pendingReplies.clear();
  }

  private NetworkPackage request(String type, Object content)
  {
    int requestId = nextRequestId.getAndIncrement();
    String contentJson = content == null ? null : gson.toJson(content);
    NetworkPackage request = new NetworkPackage(requestId, type, contentJson);
    CompletableFuture<NetworkPackage> replyFuture = new CompletableFuture<>();
    pendingReplies.put(requestId, replyFuture);
    sendToServer(request);

    try
    {
      NetworkPackage reply = replyFuture.get(REQUEST_TIMEOUT_SECONDS,
          TimeUnit.SECONDS);
      if (reply.hasError())
      {
        throw new IllegalStateException(reply.errorMessage);
      }
      return reply;
    }
    catch (InterruptedException e)
    {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted while waiting for server.",
          e);
    }
    catch (ExecutionException | TimeoutException e)
    {
      throw new IllegalStateException(
          "No valid reply from server for request " + type + ".", e);
    }
    finally
    {
      pendingReplies.remove(requestId);
    }
  }

  private <T> T requestObject(String type, Object content,
      Class<T> responseType)
  {
    NetworkPackage reply = request(type, content);
    if (reply.contentJson == null || reply.contentJson.isBlank())
    {
      return null;
    }
    return gson.fromJson(reply.contentJson, responseType);
  }

  private <T> List<T> requestList(String type, Object content,
      Class<T[]> responseArrayType)
  {
    T[] response = requestObject(type, content, responseArrayType);
    if (response == null)
    {
      return new ArrayList<>();
    }
    return new ArrayList<>(Arrays.asList(response));
  }

  @Override public List<Flight> searchFlights(SearchCriteria criteria)
  {
    SearchFlightsRequest request = DtoMapper.toDto(criteria);
    List<FlightDto> dtos = requestList(RequestType.SEARCH_FLIGHTS, request,
        FlightDto[].class);
    return DtoMapper.flightsFromDtos(dtos);
  }

  @Override public Flight getFlightDetails(int flightId)
  {
    FlightDto dto = requestObject(RequestType.GET_FLIGHT_DETAILS,
        new IdRequest(flightId), FlightDto.class);
    return DtoMapper.fromDto(dto);
  }

  @Override public boolean login(String email, String password)
  {
    User oldUser = loggedInUserLoaded ? loggedInUser : null;
    Boolean result = requestObject(RequestType.LOGIN,
        new LoginRequest(email, password), Boolean.class);
    boolean success = Boolean.TRUE.equals(result);
    if (success)
    {
      loggedInUserLoaded = false;
      User newUser = getLoggedInUser();
      support.firePropertyChange("currentUser", oldUser, newUser);
    }
    else
    {
      loggedInUser = null;
      loggedInUserLoaded = true;
      support.firePropertyChange("currentUser", oldUser, null);
    }
    return success;
  }

  @Override public boolean register(String firstName, String lastName,
      String email, String password)
  {
    Boolean result = requestObject(RequestType.REGISTER,
        new RegisterRequest(firstName, lastName, email, password),
        Boolean.class);
    return Boolean.TRUE.equals(result);
  }

  @Override public void logout()
  {
    User oldUser = getLoggedInUser();
    request(RequestType.LOGOUT, null);
    loggedInUser = null;
    loggedInUserLoaded = true;
    support.firePropertyChange("currentUser", oldUser, null);
  }

  @Override public User getLoggedInUser()
  {
    if (loggedInUserLoaded)
    {
      return loggedInUser;
    }
    UserDto dto = requestObject(RequestType.GET_LOGGED_IN_USER, null,
        UserDto.class);
    loggedInUser = DtoMapper.fromDto(dto);
    loggedInUserLoaded = true;
    return loggedInUser;
  }

  @Override public Booking createBooking(Flight flight,
      List<Passenger> passengers)
  {
    return createBooking(flight, passengers, new ArrayList<>());
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
    BookingRequest request = new BookingRequest(DtoMapper.toDto(flight),
        DtoMapper.passengerDtos(passengers), DtoMapper.seatDtos(selectedSeats));
    request.returnFlight = DtoMapper.toDto(returnFlight);
    request.returnSeats = DtoMapper.seatDtos(returnSeats);
    BookingDto dto = requestObject(RequestType.CREATE_BOOKING, request,
        BookingDto.class);
    Booking booking = DtoMapper.fromDto(dto);
    support.firePropertyChange("bookings", null, booking);
    return booking;
  }

  @Override public void cancelBooking(Booking booking)
  {
    if (booking == null)
    {
      return;
    }
    request(RequestType.CANCEL_BOOKING, new IdRequest(booking.getBookingId()));
    support.firePropertyChange("bookings", booking, null);
  }

  @Override public List<Booking> getAllBookings()
  {
    List<BookingDto> dtos = requestList(RequestType.GET_ALL_BOOKINGS, null,
        BookingDto[].class);
    return DtoMapper.bookingsFromDtos(dtos);
  }

  @Override public List<Booking> getUserBookings()
  {
    List<BookingDto> dtos = requestList(RequestType.GET_USER_BOOKINGS, null,
        BookingDto[].class);
    return DtoMapper.bookingsFromDtos(dtos);
  }

  @Override public Booking addBookingToCurrentUserById(int bookingId,
      String passengerLastName)
  {
    BookingDto dto = requestObject(RequestType.ADD_BOOKING_TO_CURRENT_USER_BY_ID,
        new AddBookingByIdRequest(bookingId, passengerLastName),
        BookingDto.class);
    Booking booking = DtoMapper.fromDto(dto);
    support.firePropertyChange("bookings", null, booking);
    return booking;
  }

  @Override public void addFlight(Flight flight)
  {
    request(RequestType.ADD_FLIGHT, DtoMapper.toDto(flight));
    support.firePropertyChange("allFlights", null, getAllFlights());
  }

  @Override public void removeFlight(Flight flight)
  {
    if (flight == null)
    {
      return;
    }
    request(RequestType.REMOVE_FLIGHT, new IdRequest(flight.getFlightId()));
    support.firePropertyChange("allFlights", flight, null);
  }

  @Override public void editFlight(Flight flight)
  {
    if (flight == null)
    {
      return;
    }
    request(RequestType.EDIT_FLIGHT, DtoMapper.toDto(flight));
    support.firePropertyChange("allFlights", null, getAllFlights());
  }

  @Override public List<LuggageType> getLuggageTypes()
  {
    if (luggageTypesCache == null)
    {
      List<LuggageTypeDto> dtos = requestList(RequestType.GET_LUGGAGE_TYPES,
          null, LuggageTypeDto[].class);
      luggageTypesCache = DtoMapper.luggageTypesFromDtos(dtos);
    }
    return new ArrayList<>(luggageTypesCache);
  }

  @Override public List<City> getAllCities()
  {
    if (allCitiesCache == null)
    {
      List<CityDto> dtos = requestList(RequestType.GET_ALL_CITIES, null,
          CityDto[].class);
      allCitiesCache = DtoMapper.citiesFromDtos(dtos);
    }
    return new ArrayList<>(allCitiesCache);
  }

  @Override public List<City> getCities()
  {
    return getAllCities();
  }

  @Override public List<Plane> getPlanes()
  {
    if (planesCache == null)
    {
      List<PlaneDto> dtos = requestList(RequestType.GET_PLANES, null,
          PlaneDto[].class);
      planesCache = DtoMapper.planesFromDtos(dtos);
    }
    return new ArrayList<>(planesCache);
  }

  @Override public List<Carrier> getCarriers()
  {
    if (carriersCache == null)
    {
      List<CarrierDto> dtos = requestList(RequestType.GET_CARRIERS, null,
          CarrierDto[].class);
      carriersCache = DtoMapper.carriersFromDtos(dtos);
    }
    return new ArrayList<>(carriersCache);
  }

  @Override public List<Flight> getAllFlights()
  {
    List<FlightDto> dtos = requestList(RequestType.GET_ALL_FLIGHTS, null,
        FlightDto[].class);
    return DtoMapper.flightsFromDtos(dtos);
  }

  @Override public boolean isLoggedIn()
  {
    Boolean result = requestObject(RequestType.IS_LOGGED_IN, null,
        Boolean.class);
    return Boolean.TRUE.equals(result);
  }

  @Override public void addPropertyChangeListener(
      PropertyChangeListener listener)
  {
    support.addPropertyChangeListener(listener);
  }

  @Override public void removePropertyChangeListener(
      PropertyChangeListener listener)
  {
    support.removePropertyChangeListener(listener);
  }

  @Override public void close()
  {
    try
    {
      socket.close();
    }
    catch (IOException ignored)
    {
    }
    completePendingWithError(new IOException("Client connection closed."));
  }
}


