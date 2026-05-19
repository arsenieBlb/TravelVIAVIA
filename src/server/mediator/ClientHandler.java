package server.mediator;

import com.google.gson.Gson;
import client.model.Booking;
import client.model.City;
import client.model.ConnectingFlight;
import client.model.Flight;
import client.model.Model;
import client.model.Passenger;
import client.model.SearchCriteria;
import client.model.Seat;
import client.model.User;
import server.logging.ServerLogger;
import server.network.NetworkPackage;
import server.network.RequestType;
import server.network.dto.AddBookingByIdRequest;
import server.network.dto.BookingDto;
import server.network.dto.BookingRequest;
import server.network.dto.DtoMapper;
import server.network.dto.FlightDto;
import server.network.dto.IdRequest;
import server.network.dto.LoginRequest;
import server.network.dto.RegisterRequest;
import server.network.dto.SearchFlightsRequest;
import server.network.dto.UserDto;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Objects;

public class ClientHandler implements Runnable, PropertyChangeListener
{
  private final int clientId;
  private final Socket socket;
  private final Server server;
  private final Model model;
  private final Gson gson;
  private final ServerLogger logger;
  private BufferedReader in;
  private PrintWriter out;
  private boolean running;

  public ClientHandler(int clientId, Socket socket, Server server,
      Model model)
  {
    this.clientId = clientId;
    this.socket = socket;
    this.server = server;
    this.model = Objects.requireNonNull(model, "model");
    this.gson = new Gson();
    this.logger = ServerLogger.getInstance();
    this.model.addPropertyChangeListener(this);
  }

  @Override public void run()
  {
    running = true;
    try
    {
      in = createSocketReader(socket);
      out = createSocketWriter(socket);

      String requestLine;
      while (running && (requestLine = in.readLine()) != null)
      {
        NetworkPackage request;
        try
        {
          request = gson.fromJson(requestLine, NetworkPackage.class);
          if (request == null || request.type == null)
          {
            throw new IllegalArgumentException("Invalid request.");
          }
        }
        catch (RuntimeException e)
        {
          logger.logTraffic(clientAddress(), "ERR",
              "clientHandler client=" + clientLabel()
                  + " message=" + quote("Invalid request data."));
          continue;
        }

        logger.logTraffic(clientAddress(), "RX", describeRequest(request));
        NetworkPackage reply;
        try
        {
          Object response = processRequest(request);
          reply = NetworkPackage.reply(request.requestId, request.type,
              response == null ? null : gson.toJson(response));
          logger.logTraffic(clientAddress(), "TX",
              describeResponse(request, reply, response));
        }
        catch (Exception e)
        {
          reply = NetworkPackage.error(request.requestId, request.type,
              e.getMessage());
          logger.logTraffic(clientAddress(), "TX",
              describeErrorResponse(request, e));
        }
        sendMessage(reply);
      }
    }
    catch (IOException e)
    {
      logger.logTraffic(clientAddress(), "ERR",
          "clientHandler client=" + clientLabel()
              + " message=" + quote(e.getMessage()));
    }
    finally
    {
      close();
      server.removeClient(this);
    }
  }

  public int getClientId()
  {
    return clientId;
  }

  public String getRemoteAddress()
  {
    return socket.getRemoteSocketAddress() == null ? "unknown"
        : socket.getRemoteSocketAddress().toString();
  }

  public String getClientAddress()
  {
    return clientAddress();
  }

  public void sendMessage(NetworkPackage networkPackage)
  {
    synchronized (this)
    {
      if (out != null)
      {
        out.println(gson.toJson(networkPackage));
      }
    }
  }

  @Override public void propertyChange(PropertyChangeEvent event)
  {
    if ("currentUser".equals(event.getPropertyName()))
    {
      sendPropertyChange(event);
    }
  }

  private void sendPropertyChange(PropertyChangeEvent event)
  {
    String propertyName = event.getPropertyName();
    NetworkPackage networkPackage = new NetworkPackage(0,
        RequestType.BROADCAST_PROPERTY_CHANGE, gson.toJson(propertyName));
    sendMessage(networkPackage);
    logger.logTraffic(clientAddress(), "TX",
        "broadcast " + RequestType.BROADCAST_PROPERTY_CHANGE
            + " client=" + clientLabel()
            + " payload={property=" + quote(propertyName)
            + ", change=" + eventChangeSummary(event) + "}");
  }

  private Object processRequest(NetworkPackage request)
  {
    return switch (request.type)
    {
      case RequestType.LOGIN -> login(request);
      case RequestType.REGISTER -> register(request);
      case RequestType.LOGOUT -> logout();
      case RequestType.GET_LOGGED_IN_USER -> DtoMapper.toDto(
          model.getLoggedInUser());
      case RequestType.IS_LOGGED_IN -> model.isLoggedIn();
      case RequestType.SEARCH_FLIGHTS -> searchFlights(request);
      case RequestType.GET_FLIGHT_DETAILS -> getFlightDetails(request);
      case RequestType.CREATE_BOOKING -> createBooking(request);
      case RequestType.CANCEL_BOOKING -> cancelBooking(request);
      case RequestType.GET_ALL_BOOKINGS -> DtoMapper.bookingDtos(
          model.getAllBookings());
      case RequestType.GET_USER_BOOKINGS -> DtoMapper.bookingDtos(
          model.getUserBookings());
      case RequestType.ADD_BOOKING_TO_CURRENT_USER_BY_ID ->
          addBookingToCurrentUserById(request);
      case RequestType.REMOVE_BOOKING_FROM_CURRENT_USER ->
          removeBookingFromCurrentUser(request);
      case RequestType.ADD_FLIGHT -> addFlight(request);
      case RequestType.REMOVE_FLIGHT -> removeFlight(request);
      case RequestType.EDIT_FLIGHT -> editFlight(request);
      case RequestType.GET_LUGGAGE_TYPES -> DtoMapper.luggageTypeDtos(
          model.getLuggageTypes());
      case RequestType.GET_ALL_CITIES -> DtoMapper.cityDtos(
          model.getAllCities());
      case RequestType.GET_CITIES -> DtoMapper.cityDtos(model.getCities());
      case RequestType.GET_PLANES -> DtoMapper.planeDtos(model.getPlanes());
      case RequestType.GET_CARRIERS -> DtoMapper.carrierDtos(
          model.getCarriers());
      case RequestType.GET_ALL_FLIGHTS -> DtoMapper.flightDtos(
          model.getAllFlights());
      default -> throw new IllegalArgumentException(
          "Unknown request type: " + request.type);
    };
  }

  private boolean login(NetworkPackage request)
  {
    LoginRequest loginRequest = gson.fromJson(request.contentJson,
        LoginRequest.class);
    boolean result = model.login(loginRequest.email, loginRequest.password);
    logger.log(clientLabel(), RequestType.LOGIN, result ? "OK" : "ERROR",
        "email=" + loginRequest.email + ", result=" + result);
    return result;
  }

  private boolean register(NetworkPackage request)
  {
    RegisterRequest registerRequest = gson.fromJson(request.contentJson,
        RegisterRequest.class);
    boolean result = model.register(registerRequest.firstName,
        registerRequest.lastName, registerRequest.email,
        registerRequest.password);
    logger.log(clientLabel(), RequestType.REGISTER, result ? "OK" : "ERROR",
        "email=" + registerRequest.email + ", result=" + result);
    return result;
  }

  private Object logout()
  {
    String user = currentUserLabel();
    model.logout();
    logger.log(clientLabel(), RequestType.LOGOUT, "OK",
        "logged out " + user);
    return null;
  }

  private Object searchFlights(NetworkPackage request)
  {
    SearchFlightsRequest searchRequest = gson.fromJson(request.contentJson,
        SearchFlightsRequest.class);
    SearchCriteria criteria = DtoMapper.toCriteria(searchRequest,
        model.getAllCities());
    return DtoMapper.flightDtos(model.searchFlights(criteria));
  }

  private Object getFlightDetails(NetworkPackage request)
  {
    IdRequest idRequest = gson.fromJson(request.contentJson, IdRequest.class);
    return DtoMapper.toDto(model.getFlightDetails(idRequest.id));
  }

  private Object createBooking(NetworkPackage request)
  {
    BookingRequest bookingRequest = gson.fromJson(request.contentJson,
        BookingRequest.class);
    Flight flight = resolveFlightFromModel(bookingRequest.flight);
    int bookingCountBefore = model.getUserBookings().size();
    int availableSeatsBefore = countAvailableSeats(flight);
    List<Passenger> passengers = DtoMapper.passengersFromDtos(
        bookingRequest.passengers);
    List<Seat> selectedSeats = DtoMapper.seatsFromDtos(
        bookingRequest.selectedSeats);
    Flight returnFlight = bookingRequest.returnFlight == null ? null
        : resolveFlightFromModel(bookingRequest.returnFlight);
    List<Seat> returnSeats = DtoMapper.seatsFromDtos(
        bookingRequest.returnSeats);
    Booking booking = model.createBooking(flight, passengers, selectedSeats,
        returnFlight, returnSeats);
    String changeSummary = "{count=" + bookingCountBefore + "->"
        + (bookingCountBefore + 1)
        + ", seatsAvailable=" + availableSeatsBefore + "->"
        + countAvailableSeats(flight)
        + ", added=" + bookingLogValue(booking) + "}";
    logger.log(clientLabel(), RequestType.CREATE_BOOKING, "OK",
        "user=" + currentUserLabel() + ", change=" + changeSummary);
    server.broadcastPropertyChange("bookings", changeSummary);
    return DtoMapper.toDto(booking);
  }

  private Object cancelBooking(NetworkPackage request)
  {
    IdRequest idRequest = gson.fromJson(request.contentJson, IdRequest.class);
    List<Booking> bookingsBefore = model.getUserBookings();
    int bookingCountBefore = bookingsBefore.size();
    logger.log(clientLabel(), RequestType.CANCEL_BOOKING, "OK",
        "destructive request user=" + currentUserLabel()
            + ", bookingId=" + idRequest.id);
    Booking booking = findBookingById(bookingsBefore, idRequest.id);
    if (booking == null)
    {
      throw new IllegalArgumentException(
          "Booking " + idRequest.id + " was not found for "
              + currentUserLabel());
    }
    model.cancelBooking(booking);
    String changeSummary = "{count=" + bookingCountBefore + "->"
        + Math.max(0, bookingCountBefore - 1)
        + ", removed=" + bookingLogValue(booking) + "}";
    logger.log(clientLabel(), RequestType.CANCEL_BOOKING, "OK",
        "user=" + currentUserLabel() + ", change=" + changeSummary);
    server.broadcastPropertyChange("bookings", changeSummary);
    return null;
  }

  private Object addBookingToCurrentUserById(NetworkPackage request)
  {
    AddBookingByIdRequest addRequest = gson.fromJson(request.contentJson,
        AddBookingByIdRequest.class);
    List<Booking> bookingsBefore = model.getUserBookings();
    int bookingCountBefore = bookingsBefore.size();
    boolean alreadyLinked = containsBooking(bookingsBefore,
        addRequest.bookingId);
    Booking booking = model.addBookingToCurrentUserById(addRequest.bookingId);
    int bookingCountAfter = alreadyLinked ? bookingCountBefore
        : bookingCountBefore + 1;
    String changeSummary = "{count=" + bookingCountBefore + "->"
        + bookingCountAfter + ", linked=" + bookingLogValue(booking) + "}";
    logger.log(clientLabel(), RequestType.ADD_BOOKING_TO_CURRENT_USER_BY_ID,
        "OK", "user=" + currentUserLabel() + ", change=" + changeSummary);
    server.broadcastPropertyChange("bookings", changeSummary);
    return DtoMapper.toDto(booking);
  }

  private Object removeBookingFromCurrentUser(NetworkPackage request)
  {
    IdRequest idRequest = gson.fromJson(request.contentJson, IdRequest.class);
    List<Booking> bookingsBefore = model.getUserBookings();
    int bookingCountBefore = bookingsBefore.size();
    Booking booking = findBookingById(bookingsBefore, idRequest.id);
    if (booking == null)
    {
      throw new IllegalArgumentException(
          "Booking " + idRequest.id + " was not found for "
              + currentUserLabel());
    }
    model.removeBookingFromCurrentUser(idRequest.id);
    String changeSummary = "{count=" + bookingCountBefore + "->"
        + Math.max(0, bookingCountBefore - 1)
        + ", unlinked=" + bookingLogValue(booking) + "}";
    logger.log(clientLabel(), RequestType.REMOVE_BOOKING_FROM_CURRENT_USER,
        "OK", "user=" + currentUserLabel() + ", change=" + changeSummary);
    server.broadcastPropertyChange("bookings", changeSummary);
    return null;
  }

  private Object addFlight(NetworkPackage request)
  {
    FlightDto flightDto = gson.fromJson(request.contentJson, FlightDto.class);
    Flight flight = DtoMapper.fromDto(flightDto);
    int flightCountBefore = model.getAllFlights().size();
    model.addFlight(flight);
    String changeSummary = "{count=" + flightCountBefore + "->"
        + model.getAllFlights().size() + ", added=" + flightLogValue(flight)
        + "}";
    logger.log(clientLabel(), RequestType.ADD_FLIGHT, "OK",
        "user=" + currentUserLabel() + ", change=" + changeSummary);
    server.broadcastPropertyChange("allFlights", changeSummary);
    return null;
  }

  private Object removeFlight(NetworkPackage request)
  {
    IdRequest idRequest = gson.fromJson(request.contentJson, IdRequest.class);
    int flightCountBefore = model.getAllFlights().size();
    logger.log(clientLabel(), RequestType.REMOVE_FLIGHT, "OK",
        "destructive request user=" + currentUserLabel()
            + ", flightId=" + idRequest.id);
    Flight flight = findFlightById(model.getAllFlights(), idRequest.id);
    if (flight == null)
    {
      throw new IllegalArgumentException(
              "Flight " + idRequest.id + " was not found.");
    }
    model.removeFlight(flight);
    String changeSummary = "{count=" + flightCountBefore + "->"
        + model.getAllFlights().size() + ", removed="
        + flightLogValue(flight) + "}";
    logger.log(clientLabel(), RequestType.REMOVE_FLIGHT, "OK",
        "user=" + currentUserLabel() + ", change=" + changeSummary);
    server.broadcastPropertyChange("allFlights", changeSummary);
    return null;
  }

  private Object editFlight(NetworkPackage request)
  {
    FlightDto flightDto = gson.fromJson(request.contentJson, FlightDto.class);
    logger.log(clientLabel(), RequestType.EDIT_FLIGHT, "OK",
        "user=" + currentUserLabel() + ", flightId=" + flightDto.flightId);

    Flight existingFlight = findFlightById(model.getAllFlights(), flightDto.flightId);
    if (existingFlight == null)
    {
      throw new IllegalArgumentException(
          "Flight " + flightDto.flightId + " was not found.");
    }

    Flight updatedFlight = DtoMapper.flightFromDto(flightDto,
        model.getCarriers(), model.getPlanes(), model.getAllCities());
    model.editFlight(updatedFlight);

    logger.log(clientLabel(), RequestType.EDIT_FLIGHT, "OK",
        "user=" + currentUserLabel() + ", updated=" + flightLogValue(updatedFlight));
    server.broadcastPropertyChange("allFlights", "{edited=" + flightDto.flightId + "}");
    return null;
  }

  private boolean containsBooking(List<Booking> bookings, int bookingId)
  {
    return findBookingById(bookings, bookingId) != null;
  }

  private Booking findBookingById(List<Booking> bookings, int bookingId)
  {
    for (Booking booking : bookings)
    {
      if (booking.getBookingId() == bookingId)
      {
        return booking;
      }
    }
    return null;
  }

  private Flight findFlightById(List<Flight> flights, int flightId)
  {
    for (Flight flight : flights)
    {
      if (flight.getFlightId() == flightId)
      {
        return flight;
      }
    }
    return null;
  }

  private String eventChangeSummary(PropertyChangeEvent event)
  {
    return "{old=" + changeValue(event.getOldValue())
        + ", new=" + changeValue(event.getNewValue()) + "}";
  }

  private String changeValue(Object value)
  {
    if (value == null)
    {
      return "null";
    }
    if (value instanceof User user)
    {
      return "{userId=" + user.getUserId()
          + ", email=" + quote(user.getEmail())
          + ", type=" + quote(user.getClass().getSimpleName()) + "}";
    }
    if (value instanceof Booking booking)
    {
      return bookingLogValue(booking);
    }
    if (value instanceof Flight flight)
    {
      return flightLogValue(flight);
    }
    if (value instanceof List<?> list)
    {
      return "{size=" + list.size() + "}";
    }
    return quote(String.valueOf(value));
  }

  private String bookingLogValue(Booking booking)
  {
    if (booking == null)
    {
      return "null";
    }
    return "{bookingId=" + booking.getBookingId()
        + ", flight=" + flightLogValue(booking.getFlight())
        + ", passengers=" + booking.getPassengers().size()
        + ", totalPrice=" + booking.getTotalPrice()
        + ", cancelled=" + booking.isCancelled() + "}";
  }

  private String flightLogValue(Flight flight)
  {
    if (flight == null)
    {
      return "null";
    }
    String departure = flight.getDepartureCity() == null ? "?"
        : flight.getDepartureCity().getCityName();
    String arrival = flight.getArrivalCity() == null ? "?"
        : flight.getArrivalCity().getCityName();
    return "{flightId=" + flight.getFlightId()
        + ", flightNumber=" + quote(flight.getFlightNumber())
        + ", route=" + quote(departure + " -> " + arrival)
        + ", departureTime=" + quote(String.valueOf(flight.getDepartureTime()))
        + "}";
  }

  private int countAvailableSeats(Flight flight)
  {
    if (flight == null)
    {
      return 0;
    }
    if (flight instanceof ConnectingFlight connectingFlight)
    {
      return connectingFlight.getFirstSegment().getAvailableSeats().size()
          + connectingFlight.getSecondSegment().getAvailableSeats().size();
    }
    return flight.getAvailableSeats().size();
  }

  private Flight resolveFlightFromModel(FlightDto flightDto)
  {
    if (flightDto == null)
    {
      throw new IllegalArgumentException("Flight is required.");
    }
    if (flightDto.connecting && flightDto.firstSegment != null
        && flightDto.secondSegment != null)
    {
      Flight firstSegment = findFlightById(model.getAllFlights(),
          flightDto.firstSegment.flightId);
      Flight secondSegment = findFlightById(model.getAllFlights(),
          flightDto.secondSegment.flightId);
      if (firstSegment != null && secondSegment != null)
      {
        return new ConnectingFlight(firstSegment, secondSegment);
      }
    }

    Flight loadedFlight = findFlightById(model.getAllFlights(),
        flightDto.flightId);
    return loadedFlight == null ? DtoMapper.fromDto(flightDto) : loadedFlight;
  }

  private BufferedReader createSocketReader(Socket socket) throws IOException
  {
    return new BufferedReader(new InputStreamReader(socket.getInputStream()));
  }

  private PrintWriter createSocketWriter(Socket socket) throws IOException
  {
    return new PrintWriter(socket.getOutputStream(), true);
  }

  private String describeRequest(NetworkPackage request)
  {
    String action = request == null ? "INVALID" : request.type;
    int requestId = request == null ? 0 : request.requestId;
    return "request " + action
        + " #" + requestId
        + " client=" + clientLabel()
        + " user=" + quote(currentUserLabel())
        + " payload=" + describeRequestPayload(request);
  }

  private String describeResponse(NetworkPackage request,
      NetworkPackage reply, Object response)
  {
    String action = request == null ? "INVALID" : request.type;
    int requestId = request == null ? 0 : request.requestId;
    boolean success = reply != null && !reply.hasError();
    return "response " + action
        + " #" + requestId
        + " success=" + success
        + " message=" + quote(successMessage(action, response))
        + " payload=" + describeResponsePayload(action, response);
  }

  private String describeErrorResponse(NetworkPackage request,
      Exception exception)
  {
    String action = request == null ? "INVALID" : request.type;
    int requestId = request == null ? 0 : request.requestId;
    String message = exception == null ? "Unknown server error."
        : exception.getMessage();
    return "response " + action
        + " #" + requestId
        + " success=false"
        + " message=" + quote(message)
        + " payload={}";
  }

  private String describeRequestPayload(NetworkPackage request)
  {
    if (request == null || request.contentJson == null
        || request.contentJson.isBlank())
    {
      return "{}";
    }

    try
    {
      return switch (request.type)
      {
        case RequestType.LOGIN -> loginPayload(request);
        case RequestType.REGISTER -> registerPayload(request);
        case RequestType.SEARCH_FLIGHTS -> searchPayload(request);
        case RequestType.GET_FLIGHT_DETAILS, RequestType.CANCEL_BOOKING,
             RequestType.REMOVE_BOOKING_FROM_CURRENT_USER,
             RequestType.REMOVE_FLIGHT -> idPayload(request);
        case RequestType.CREATE_BOOKING -> bookingPayload(request);
        case RequestType.ADD_BOOKING_TO_CURRENT_USER_BY_ID ->
            addBookingByIdPayload(request);
        case RequestType.ADD_FLIGHT -> flightPayload(gson.fromJson(
            request.contentJson, FlightDto.class));
        default -> "{}";
      };
    }
    catch (Exception e)
    {
      return "{unreadablePayload=" + quote(e.getMessage()) + "}";
    }
  }

  private String loginPayload(NetworkPackage request)
  {
    LoginRequest loginRequest = gson.fromJson(request.contentJson,
        LoginRequest.class);
    return "{email=" + quote(loginRequest.email)
        + ", password=" + quote("<redacted>") + "}";
  }

  private String registerPayload(NetworkPackage request)
  {
    RegisterRequest registerRequest = gson.fromJson(request.contentJson,
        RegisterRequest.class);
    return "{firstName=" + quote(registerRequest.firstName)
        + ", lastName=" + quote(registerRequest.lastName)
        + ", email=" + quote(registerRequest.email)
        + ", password=" + quote("<redacted>") + "}";
  }

  private String searchPayload(NetworkPackage request)
  {
    SearchFlightsRequest searchRequest = gson.fromJson(request.contentJson,
        SearchFlightsRequest.class);
    return "{departureCityId=" + searchRequest.departureCityId
        + ", arrivalCityId=" + searchRequest.arrivalCityId
        + ", departureDate=" + quote(searchRequest.departureDate)
        + ", passengerCount=" + searchRequest.passengerCount
        + ", seatClass=" + quote(searchRequest.seatClass) + "}";
  }

  private String idPayload(NetworkPackage request)
  {
    IdRequest idRequest = gson.fromJson(request.contentJson, IdRequest.class);
    return "{id=" + idRequest.id + "}";
  }

  private String bookingPayload(NetworkPackage request)
  {
    BookingRequest bookingRequest = gson.fromJson(request.contentJson,
        BookingRequest.class);
    return "{flight=" + flightPayload(bookingRequest.flight)
        + ", passengers=" + sizeOf(bookingRequest.passengers)
        + ", selectedSeats=" + sizeOf(bookingRequest.selectedSeats)
        + ", returnFlight=" + flightPayload(bookingRequest.returnFlight)
        + ", returnSeats=" + sizeOf(bookingRequest.returnSeats) + "}";
  }

  private String addBookingByIdPayload(NetworkPackage request)
  {
    AddBookingByIdRequest addRequest = gson.fromJson(request.contentJson,
        AddBookingByIdRequest.class);
    return "{bookingId=" + addRequest.bookingId + "}";
  }

  private String describeResponsePayload(String action, Object response)
  {
    if (response == null)
    {
      return "{}";
    }
    if (response instanceof Boolean result)
    {
      return "{result=" + result + "}";
    }
    if (response instanceof UserDto userDto)
    {
      return "{userId=" + userDto.userId
          + ", email=" + quote(userDto.email)
          + ", type=" + quote(userDto.type)
          + ", loggedIn=" + userDto.loggedIn + "}";
    }
    if (response instanceof BookingDto bookingDto)
    {
      return "{bookingId=" + bookingDto.bookingId
          + ", totalPrice=" + bookingDto.totalPrice
          + ", passengerCount=" + sizeOf(bookingDto.passengers)
          + ", flight=" + flightPayload(bookingDto.flight) + "}";
    }
    if (response instanceof FlightDto flightDto)
    {
      return flightPayload(flightDto);
    }
    if (response instanceof List<?> list)
    {
      return "{" + listName(action) + "=" + list.size() + "}";
    }
    return "{value=" + quote(String.valueOf(response)) + "}";
  }

  private String successMessage(String action, Object response)
  {
    return switch (action)
    {
      case RequestType.LOGIN -> Boolean.TRUE.equals(response)
          ? "Login accepted." : "Login rejected.";
      case RequestType.REGISTER -> Boolean.TRUE.equals(response)
          ? "Customer registered." : "Registration rejected.";
      case RequestType.LOGOUT -> "User logged out.";
      case RequestType.GET_LOGGED_IN_USER -> response == null
          ? "No user is logged in." : "Current user loaded.";
      case RequestType.IS_LOGGED_IN -> "Login state checked.";
      case RequestType.SEARCH_FLIGHTS -> "Found " + count(response)
          + " flight(s).";
      case RequestType.GET_FLIGHT_DETAILS -> "Flight details loaded.";
      case RequestType.CREATE_BOOKING -> response instanceof BookingDto booking
          ? "Booking #" + booking.bookingId + " created."
          : "Booking created.";
      case RequestType.CANCEL_BOOKING -> "Booking cancelled.";
      case RequestType.REMOVE_BOOKING_FROM_CURRENT_USER ->
          "Booking removed from current user.";
      case RequestType.GET_ALL_BOOKINGS -> "Loaded " + count(response)
          + " booking(s).";
      case RequestType.GET_USER_BOOKINGS -> "Loaded " + count(response)
          + " user booking(s).";
      case RequestType.ADD_BOOKING_TO_CURRENT_USER_BY_ID ->
          "Booking linked to current user.";
      case RequestType.ADD_FLIGHT -> "Flight added.";
      case RequestType.REMOVE_FLIGHT -> "Flight removed.";
      case RequestType.GET_LUGGAGE_TYPES -> "Loaded " + count(response)
          + " luggage type(s).";
      case RequestType.GET_ALL_CITIES, RequestType.GET_CITIES -> "Loaded "
          + count(response) + " city/cities.";
      case RequestType.GET_PLANES -> "Loaded " + count(response)
          + " plane(s).";
      case RequestType.GET_CARRIERS -> "Loaded " + count(response)
          + " carrier(s).";
      case RequestType.GET_ALL_FLIGHTS -> "Loaded " + count(response)
          + " flight(s).";
      default -> "Request completed.";
    };
  }

  private String listName(String action)
  {
    return switch (action)
    {
      case RequestType.SEARCH_FLIGHTS, RequestType.GET_ALL_FLIGHTS -> "flights";
      case RequestType.GET_ALL_BOOKINGS, RequestType.GET_USER_BOOKINGS ->
          "bookings";
      case RequestType.GET_LUGGAGE_TYPES -> "luggageTypes";
      case RequestType.GET_ALL_CITIES, RequestType.GET_CITIES -> "cities";
      case RequestType.GET_PLANES -> "planes";
      case RequestType.GET_CARRIERS -> "carriers";
      default -> "items";
    };
  }

  private String flightPayload(FlightDto flightDto)
  {
    if (flightDto == null)
    {
      return "{}";
    }
    String departure = flightDto.departureCity == null ? "?"
        : flightDto.departureCity.cityName;
    String arrival = flightDto.arrivalCity == null ? "?"
        : flightDto.arrivalCity.cityName;
    String carrier = flightDto.carrier == null ? "?"
        : flightDto.carrier.name;
    return "{flightId=" + flightDto.flightId
        + ", flightNumber=" + quote(flightDto.flightNumber)
        + ", route=" + quote(departure + " -> " + arrival)
        + ", carrier=" + quote(carrier)
        + ", departureTime=" + quote(flightDto.departureTime) + "}";
  }

  private int count(Object value)
  {
    if (value instanceof List<?> list)
    {
      return list.size();
    }
    return value == null ? 0 : 1;
  }

  private int sizeOf(List<?> list)
  {
    return list == null ? 0 : list.size();
  }

  private String quote(String value)
  {
    if (value == null)
    {
      return "\"\"";
    }
    return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
  }

  private String clientAddress()
  {
    if (socket.getInetAddress() == null)
    {
      return "unknown";
    }
    return socket.getInetAddress().getHostAddress();
  }

  private String clientLabel()
  {
    return String.valueOf(clientId);
  }

  private String currentUserLabel()
  {
    User user = model.getLoggedInUser();
    return user == null ? "anonymous" : user.getEmail();
  }

  private void close()
  {
    running = false;
    model.removePropertyChangeListener(this);
    try
    {
      socket.close();
    }
    catch (IOException ignored)
    {
    }
  }
}



