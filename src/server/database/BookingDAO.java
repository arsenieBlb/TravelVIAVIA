package server.database;

import client.model.Booking;
import client.model.Customer;
import client.model.Flight;
import client.model.LuggageType;
import client.model.Passenger;
import client.model.PassengerLuggage;
import client.model.SeatAssignment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO
{
  // loads all bookings from the database and connects them to flights and customers
  public List<Booking> getAllBookings(List<Flight> flights,
      List<Customer> customers, List<LuggageType> luggageTypes)
      throws SQLException
  {
    List<Booking> bookings = new ArrayList<>();

    try (Connection connection = DatabaseConnection.getConnection()) {
      String sql = "SELECT booking_id, flight_id, "
          + optionalBookingColumn(connection, "", "second_flight_id") + ", "
          + optionalBookingColumn(connection, "", "return_flight_id") + ", "
          + optionalBookingColumn(connection, "", "second_return_flight_id")
          + ", created_by_customer_id, "
          + "passenger_count, total_price FROM flights.booking "
          + "ORDER BY booking_id";
      PreparedStatement statement = connection.prepareStatement(sql);
      ResultSet resultSet = statement.executeQuery();

      while (resultSet.next())
      {
        try {
          int bookingId = resultSet.getInt("booking_id");
          int flightId = resultSet.getInt("flight_id");
          int customerId = resultSet.getInt("created_by_customer_id");

          Flight flight = findFlightById(flights, flightId);

          int secondFlightId = resultSet.getInt("second_flight_id");
          if (!resultSet.wasNull()) {
              Flight secondFlight = findFlightById(flights, secondFlightId);
              if (secondFlight != null) {
                  flight = new client.model.ConnectingFlight(flight, secondFlight);
              }
          }

          Customer customer = findCustomerById(customers, customerId);

          if (flight != null && customer != null) {
            // loads passengers that belong to this booking
            List<Passenger> passengers = loadPassengers(bookingId, flight, luggageTypes,
                connection);
            
            if (!passengers.isEmpty()) {
              Booking booking = new Booking(bookingId, LocalDateTime.now(),
                  customer, flight, passengers);

              // load return flight
              int returnFlightId = resultSet.getInt("return_flight_id");
              if (!resultSet.wasNull()) {
                  Flight returnFlight = findFlightById(flights, returnFlightId);
                  int secondReturnId = resultSet.getInt("second_return_flight_id");
                  if (!resultSet.wasNull()) {
                      Flight secondReturn = findFlightById(flights, secondReturnId);
                      if (secondReturn != null && returnFlight != null) {
                          returnFlight = new client.model.ConnectingFlight(returnFlight, secondReturn);
                      }
                  }
                  if (returnFlight != null) {
                      booking.setReturnFlight(returnFlight);
                  }
              }

              loadAllSeatAssignmentsForBooking(booking, connection);

              bookings.add(booking);
            }
          }
        } catch (Exception e) {
            System.err.println("Warning: Skipping corrupted booking. Reason: " + e.getMessage());
        }
      }
    }

    return bookings;
  }

  public List<Booking> getBookingsForCustomer(Customer customer,
      List<Flight> flights, List<LuggageType> luggageTypes)
      throws SQLException
  {
    List<Booking> bookings = new ArrayList<>();

    try (Connection connection = DatabaseConnection.getConnection())
    {
      String sql = "SELECT b.booking_id, b.flight_id, "
          + optionalBookingColumn(connection, "b", "second_flight_id") + ", "
          + optionalBookingColumn(connection, "b", "return_flight_id") + ", "
          + optionalBookingColumn(connection, "b", "second_return_flight_id")
          + ", b.created_by_customer_id, b.passenger_count, b.total_price "
          + "FROM flights.booking b "
          + "JOIN flights.booking_customer bc "
          + "ON b.booking_id = bc.booking_id "
          + "WHERE bc.customer_id = ? "
          + "ORDER BY b.booking_id DESC";

      PreparedStatement statement = connection.prepareStatement(sql);
      statement.setInt(1, customer.getUserId());
      ResultSet resultSet = statement.executeQuery();

      while (resultSet.next())
      {
        try {
          Booking booking = createBookingFromResultSet(resultSet, customer,
              flights, luggageTypes, connection);
          if (booking != null)
          {
            bookings.add(booking);
          }
        } catch (Exception e) {
            System.err.println("Warning: Skipping corrupted booking for customer. Reason: " + e.getMessage());
        }
      }
    }
    return bookings;
  }

  public Booking getBookingById(int bookingId, Customer customer,
      List<Flight> flights, List<LuggageType> luggageTypes)
      throws SQLException
  {
    try (Connection connection = DatabaseConnection.getConnection())
    {
      String sql = "SELECT booking_id, flight_id, "
          + optionalBookingColumn(connection, "", "second_flight_id") + ", "
          + optionalBookingColumn(connection, "", "return_flight_id") + ", "
          + optionalBookingColumn(connection, "", "second_return_flight_id")
          + ", created_by_customer_id, "
          + "passenger_count, total_price "
          + "FROM flights.booking WHERE booking_id = ?";

      PreparedStatement statement = connection.prepareStatement(sql);
      statement.setInt(1, bookingId);
      ResultSet resultSet = statement.executeQuery();

      if (resultSet.next())
      {
        try {
          return createBookingFromResultSet(resultSet, customer, flights,
              luggageTypes, connection);
        } catch (Exception e) {
            System.err.println("Warning: Skipping corrupted booking lookup. Reason: " + e.getMessage());
            return null;
        }
      }
    }
    return null;
  }

  public boolean bookingExists(int bookingId) throws SQLException
  {
    try (Connection connection = DatabaseConnection.getConnection())
    {
      String sql = "SELECT 1 FROM flights.booking WHERE booking_id = ?";
      PreparedStatement statement = connection.prepareStatement(sql);
      statement.setInt(1, bookingId);
      ResultSet resultSet = statement.executeQuery();
      return resultSet.next();
    }
  }

  public boolean bookingHasPassengerLastName(int bookingId, String lastName)
      throws SQLException
  {
    if (lastName == null || lastName.isBlank())
    {
      return true;
    }

    try (Connection connection = DatabaseConnection.getConnection())
    {
      String sql = "SELECT 1 FROM flights.passenger "
          + "WHERE booking_id = ? AND LOWER(last_name) = LOWER(?)";
      PreparedStatement statement = connection.prepareStatement(sql);
      statement.setInt(1, bookingId);
      statement.setString(2, lastName.trim());
      ResultSet resultSet = statement.executeQuery();
      return resultSet.next();
    }
  }

  public void linkBookingToCustomer(int bookingId, int customerId)
      throws SQLException
  {
    try (Connection connection = DatabaseConnection.getConnection())
    {
      String sql = "INSERT INTO flights.booking_customer "
          + "(booking_id, customer_id) "
          + "SELECT ?, ? WHERE NOT EXISTS ("
          + "SELECT 1 FROM flights.booking_customer "
          + "WHERE booking_id = ? AND customer_id = ?)";
      PreparedStatement statement = connection.prepareStatement(sql);
      statement.setInt(1, bookingId);
      statement.setInt(2, customerId);
      statement.setInt(3, bookingId);
      statement.setInt(4, customerId);
      statement.executeUpdate();
    }
  }

  private Booking createBookingFromResultSet(ResultSet resultSet,
      Customer customer, List<Flight> flights,
      List<LuggageType> luggageTypes, Connection connection)
      throws SQLException
  {
    int bookingId = resultSet.getInt("booking_id");
    int flightId = resultSet.getInt("flight_id");
    Flight flight = findFlightById(flights, flightId);

    int secondFlightId = resultSet.getInt("second_flight_id");
    if (!resultSet.wasNull()) {
        Flight secondFlight = findFlightById(flights, secondFlightId);
        if (secondFlight != null) {
            flight = new client.model.ConnectingFlight(flight, secondFlight);
        }
    }

    if (flight == null)
    {
      return null;
    }

    List<Passenger> passengers = loadPassengers(bookingId, flight, luggageTypes,
        connection);
    if (passengers.isEmpty())
    {
      return null;
    }

    Booking booking = new Booking(bookingId, LocalDateTime.now(), customer, flight,
        passengers);

    // load return flight if present
    int returnFlightId = resultSet.getInt("return_flight_id");
    if (!resultSet.wasNull()) {
        Flight returnFlight = findFlightById(flights, returnFlightId);
        int secondReturnId = resultSet.getInt("second_return_flight_id");
        if (!resultSet.wasNull()) {
            Flight secondReturn = findFlightById(flights, secondReturnId);
            if (secondReturn != null && returnFlight != null) {
                returnFlight = new client.model.ConnectingFlight(returnFlight, secondReturn);
            }
        }
        if (returnFlight != null) {
            booking.setReturnFlight(returnFlight);
        }
    }

    loadAllSeatAssignmentsForBooking(booking, connection);

    return booking;
  }

  // loads all passengers for a specific booking and attaches their luggage
  private List<Passenger> loadPassengers(int bookingId, Flight flight,
      List<LuggageType> luggageTypes, Connection connection) throws SQLException
  {
    List<Passenger> passengers = new ArrayList<>();

    String sql = "SELECT passenger_id, first_name, last_name "
        + "FROM flights.passenger WHERE booking_id = ?";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setInt(1, bookingId);
    ResultSet resultSet = statement.executeQuery();

    while (resultSet.next()) {
      int passengerId = resultSet.getInt("passenger_id");
      String firstName = resultSet.getString("first_name");
      String lastName = resultSet.getString("last_name");

      Passenger passenger = new Passenger(passengerId, firstName, lastName);
      // loads luggage for this passenger
      loadPassengerLuggage(passenger, luggageTypes, connection);
      passengers.add(passenger);
    }
    return passengers;
  }

  // loads all luggage items for a specific passenger
  private void loadPassengerLuggage(Passenger passenger,
      List<LuggageType> luggageTypes, Connection connection) throws SQLException
  {
    String sql = "SELECT passenger_luggage_id, luggage_type_id, quantity "
        + "FROM flights.passenger_luggage WHERE passenger_id = ?";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setInt(1, passenger.getPassengerId());
    ResultSet resultSet = statement.executeQuery();

    while (resultSet.next())
    {
      int luggageId = resultSet.getInt("passenger_luggage_id");
      int luggageTypeId = resultSet.getInt("luggage_type_id");
      int quantity = resultSet.getInt("quantity");

      LuggageType luggageType = findLuggageTypeById(luggageTypes,
          luggageTypeId);

      if (luggageType != null) {
        PassengerLuggage luggage = new PassengerLuggage(luggageId, quantity,
            luggageType);
        passenger.addPassengerLuggage(luggage);
      }
    }
  }
  // saves a new booking into the database
  public void saveBooking(Booking booking) throws SQLException
  {
    try (Connection connection = DatabaseConnection.getConnection())
    {
      connection.setAutoCommit(false);
      try
      {
        int bookingId = getNextId(connection, "booking", "booking_id");
        booking.setBookingId(bookingId);

        ensureBookingFlightsExist(booking, connection);
        saveBookingRow(booking, connection);
        
        // inserts the booking-customer link
        String linkSql = "INSERT INTO flights.booking_customer "
            + "(booking_id, customer_id) VALUES (?, ?)";
        PreparedStatement linkStatement = connection.prepareStatement(linkSql);
        linkStatement.setInt(1, booking.getBookingId());
        linkStatement.setInt(2, booking.getCustomer().getUserId());
        linkStatement.executeUpdate();

        // inserts each passenger and their luggage
        for (Passenger passenger : booking.getPassengers()) {
          savePassenger(passenger, booking, connection);
        }
        
        connection.commit();
      }
      catch (SQLException e) {
        connection.rollback();
        System.out.println("Error saving the booking to the database");
        throw e;
      }
    }
  }

  private void ensureBookingFlightsExist(Booking booking, Connection connection)
      throws SQLException
  {
    ensureFlightExists(booking.getFlight(), connection);
    if (booking.getReturnFlight() != null)
    {
      ensureFlightExists(booking.getReturnFlight(), connection);
    }
  }

  private void ensureFlightExists(Flight flight, Connection connection)
      throws SQLException
  {
    if (flight instanceof client.model.ConnectingFlight connectingFlight)
    {
      ensureFlightExists(connectingFlight.getFirstSegment(), connection);
      ensureFlightExists(connectingFlight.getSecondSegment(), connection);
      return;
    }

    String existsSql = "SELECT 1 FROM flights.flight WHERE flight_id = ?";
    PreparedStatement existsStatement = connection.prepareStatement(existsSql);
    existsStatement.setInt(1, flight.getFlightId());
    ResultSet resultSet = existsStatement.executeQuery();
    if (resultSet.next())
    {
      return;
    }

    String insertSql = "INSERT INTO flights.flight "
        + "(flight_id, carrier_id, plane_id, departure_city_id, "
        + "arrival_city_id, departure_time, arrival_time, base_price, "
        + "flight_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    PreparedStatement insertStatement = connection.prepareStatement(insertSql);
    insertStatement.setInt(1, flight.getFlightId());
    insertStatement.setInt(2, flight.getCarrier().getCarrierId());
    insertStatement.setInt(3, flight.getPlane().getPlaneId());
    insertStatement.setInt(4, flight.getDepartureCity().getCityId());
    insertStatement.setInt(5, flight.getArrivalCity().getCityId());
    insertStatement.setTimestamp(6, Timestamp.valueOf(flight.getDepartureTime()));
    insertStatement.setTimestamp(7, Timestamp.valueOf(flight.getArrivalTime()));
    insertStatement.setDouble(8, flight.getBasePrice());
    insertStatement.setString(9, "Generated");
    insertStatement.executeUpdate();
  }

  private void saveBookingRow(Booking booking, Connection connection)
      throws SQLException
  {
    if (!hasExtendedBookingColumns(connection))
    {
      if (booking.getFlight() instanceof client.model.ConnectingFlight
          || booking.getReturnFlight() != null)
      {
        throw new SQLException("Current booking table only supports direct "
            + "one-way bookings. Run the newer flights.sql schema to save "
            + "connecting or return bookings.");
      }

      String bookingSql = "INSERT INTO flights.booking "
          + "(booking_id, flight_id, created_by_customer_id, "
          + "passenger_count, total_price) VALUES (?, ?, ?, ?, ?)";
      PreparedStatement bookingStatement =
          connection.prepareStatement(bookingSql);
      bookingStatement.setInt(1, booking.getBookingId());
      bookingStatement.setInt(2, booking.getFlight().getFlightId());
      bookingStatement.setInt(3, booking.getCustomer().getUserId());
      bookingStatement.setInt(4, booking.getPassengers().size());
      bookingStatement.setDouble(5, booking.getTotalPrice());
      bookingStatement.executeUpdate();
      return;
    }

    String bookingSql = "INSERT INTO flights.booking "
        + "(booking_id, flight_id, second_flight_id, "
        + "return_flight_id, second_return_flight_id, "
        + "created_by_customer_id, "
        + "passenger_count, total_price) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    PreparedStatement bookingStatement =
        connection.prepareStatement(bookingSql);
    bookingStatement.setInt(1, booking.getBookingId());

    if (booking.getFlight() instanceof client.model.ConnectingFlight connectingFlight) {
      bookingStatement.setInt(2, connectingFlight.getFirstSegment().getFlightId());
      bookingStatement.setInt(3, connectingFlight.getSecondSegment().getFlightId());
    } else {
      bookingStatement.setInt(2, booking.getFlight().getFlightId());
      bookingStatement.setNull(3, java.sql.Types.INTEGER);
    }

    if (booking.getReturnFlight() != null) {
      if (booking.getReturnFlight() instanceof client.model.ConnectingFlight connReturn) {
        bookingStatement.setInt(4, connReturn.getFirstSegment().getFlightId());
        bookingStatement.setInt(5, connReturn.getSecondSegment().getFlightId());
      } else {
        bookingStatement.setInt(4, booking.getReturnFlight().getFlightId());
        bookingStatement.setNull(5, java.sql.Types.INTEGER);
      }
    } else {
      bookingStatement.setNull(4, java.sql.Types.INTEGER);
      bookingStatement.setNull(5, java.sql.Types.INTEGER);
    }

    bookingStatement.setInt(6, booking.getCustomer().getUserId());
    bookingStatement.setInt(7, booking.getPassengers().size());
    bookingStatement.setDouble(8, booking.getTotalPrice());
    bookingStatement.executeUpdate();
  }

  // saves a single passenger and their luggage records
  private void savePassenger(Passenger passenger, Booking booking,
      Connection connection) throws SQLException
  {
    passenger.setPassengerId(getNextId(connection, "passenger",
        "passenger_id"));

    String sql = "INSERT INTO flights.passenger "
        + "(passenger_id, booking_id, first_name, last_name) "
        + "VALUES (?, ?, ?, ?)";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setInt(1, passenger.getPassengerId());
    statement.setInt(2, booking.getBookingId());
    statement.setString(3, passenger.getFirstName());
    statement.setString(4, passenger.getLastName());
    statement.executeUpdate();
    // saves each luggage item for this passenger
    for (PassengerLuggage luggage : passenger.getPassengerLuggage())
    {
      luggage.setPassengerLuggageId(getNextId(connection, "passenger_luggage",
          "passenger_luggage_id"));
      String luggageSql = "INSERT INTO flights.passenger_luggage "
          + "(passenger_luggage_id, passenger_id, luggage_type_id, quantity) "
          + "VALUES (?, ?, ?, ?)";
      PreparedStatement luggageStatement = connection.prepareStatement(luggageSql);
      luggageStatement.setInt(1, luggage.getPassengerLuggageId());
      luggageStatement.setInt(2, passenger.getPassengerId());
      luggageStatement.setInt(3, luggage.getLuggageType().getLuggageTypeId());
      luggageStatement.setInt(4, luggage.getQuantity());
      luggageStatement.executeUpdate();
    }

    for (SeatAssignment seatAssignment : passenger.getSeatAssignments())
    {
      saveSeatAssignment(seatAssignment, connection);
    }
  }

  private void saveSeatAssignment(SeatAssignment seatAssignment,
      Connection connection) throws SQLException
  {
    String sql = "INSERT INTO flights.flight_seat "
        + "(flight_id, seat_id, passenger_id, is_occupied) "
        + "VALUES (?, ?, ?, TRUE)";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setInt(1, seatAssignment.getFlight().getFlightId());
    statement.setInt(2, seatAssignment.getSeat().getSeatId());
    statement.setInt(3, seatAssignment.getPassenger().getPassengerId());
    statement.executeUpdate();
  }

  private void loadAllSeatAssignmentsForBooking(Booking booking, Connection connection) throws SQLException {
    String sql = "SELECT p.passenger_id, fs.flight_id, fs.seat_id "
        + "FROM flights.passenger p "
        + "JOIN flights.flight_seat fs ON p.passenger_id = fs.passenger_id "
        + "WHERE p.booking_id = ?";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setInt(1, booking.getBookingId());
    ResultSet resultSet = statement.executeQuery();

    while (resultSet.next()) {
        int passengerId = resultSet.getInt("passenger_id");
        int flightId = resultSet.getInt("flight_id");
        int seatId = resultSet.getInt("seat_id");

        Passenger passenger = null;
        for (Passenger p : booking.getPassengers()) {
            if (p.getPassengerId() == passengerId) {
                passenger = p;
                break;
            }
        }
        if (passenger == null) continue;

        Flight specificFlight = null;
        Flight[] potentialFlights = {booking.getFlight(), booking.getReturnFlight()};
        
        for (Flight f : potentialFlights) {
            if (f == null) continue;
            if (f instanceof client.model.ConnectingFlight connectingFlight) {
                if (connectingFlight.getFirstSegment().getFlightId() == flightId) {
                    specificFlight = connectingFlight.getFirstSegment();
                } else if (connectingFlight.getSecondSegment().getFlightId() == flightId) {
                    specificFlight = connectingFlight.getSecondSegment();
                }
            } else if (f.getFlightId() == flightId) {
                specificFlight = f;
            }
            if (specificFlight != null) break;
        }

        if (specificFlight != null) {
            for (client.model.Seat seat : specificFlight.getPlane().getSeats()) {
                if (seat.getSeatId() == seatId) {
                    SeatAssignment sa = new SeatAssignment(specificFlight, seat, passenger);
                    passenger.addSeatAssignment(sa);
                    specificFlight.markSeatOccupied(seat);
                    break;
                }
            }
        }
    }
  }

  private int getNextId(Connection connection, String tableName,
      String columnName) throws SQLException
  {
    String sql = "SELECT COALESCE(MAX(" + columnName + "), 0) + 1 AS next_id "
        + "FROM flights." + tableName;
    PreparedStatement statement = connection.prepareStatement(sql);
    ResultSet resultSet = statement.executeQuery();
    resultSet.next();
    return resultSet.getInt("next_id");
  }

  private String optionalBookingColumn(Connection connection, String tableAlias,
      String columnName) throws SQLException
  {
    if (hasBookingColumn(connection, columnName))
    {
      if (tableAlias == null || tableAlias.isBlank())
      {
        return columnName;
      }
      return tableAlias + "." + columnName;
    }
    return "NULL::INTEGER AS " + columnName;
  }

  private boolean hasExtendedBookingColumns(Connection connection)
      throws SQLException
  {
    return hasBookingColumn(connection, "second_flight_id")
        && hasBookingColumn(connection, "return_flight_id")
        && hasBookingColumn(connection, "second_return_flight_id");
  }

  private boolean hasBookingColumn(Connection connection, String columnName)
      throws SQLException
  {
    String sql = "SELECT 1 FROM information_schema.columns "
        + "WHERE table_schema = 'flights' "
        + "AND table_name = 'booking' "
        + "AND column_name = ?";
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setString(1, columnName);
    ResultSet resultSet = statement.executeQuery();
    return resultSet.next();
  }

  // finds a flight by ID in the provided list
  private Flight findFlightById(List<Flight> flights, int id)
  {
    for (Flight f : flights) {
      if (f.getFlightId() == id) {
        return f;
      }
    }
    return null;
  }
  
  // finds a customer by ID in the provided list
  private Customer findCustomerById(List<Customer> customers, int id) {
    for (Customer c : customers) {
      if (c.getUserId() == id) {
        return c;
      }
    }
    return null;
  }

  // finds a luggage type by ID in the provided list
  private LuggageType findLuggageTypeById(List<LuggageType> luggageTypes,
      int id)
  {
    for (LuggageType lt : luggageTypes) {
      if (lt.getLuggageTypeId() == id) {
        return lt;
      }
    }
    return null;
  }

    public void removeBooking(int bookingId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String deleteSeatsSql = "DELETE FROM flights.flight_seat WHERE passenger_id IN "
                        + "(SELECT passenger_id FROM flights.passenger WHERE booking_id = ?)";
                PreparedStatement deleteSeatsStmt = connection.prepareStatement(deleteSeatsSql);
                deleteSeatsStmt.setInt(1, bookingId);
                deleteSeatsStmt.executeUpdate();

                String deleteLuggageSql = "DELETE FROM flights.passenger_luggage WHERE passenger_id IN "
                        + "(SELECT passenger_id FROM flights.passenger WHERE booking_id = ?)";
                PreparedStatement deleteLuggageStmt = connection.prepareStatement(deleteLuggageSql);
                deleteLuggageStmt.setInt(1, bookingId);
                deleteLuggageStmt.executeUpdate();

                String deletePassengersSql = "DELETE FROM flights.passenger WHERE booking_id = ?";
                PreparedStatement deletePassengersStmt = connection.prepareStatement(deletePassengersSql);
                deletePassengersStmt.setInt(1, bookingId);
                deletePassengersStmt.executeUpdate();

                String deleteLinkSql = "DELETE FROM flights.booking_customer WHERE booking_id = ?";
                PreparedStatement deleteLinkStmt = connection.prepareStatement(deleteLinkSql);
                deleteLinkStmt.setInt(1, bookingId);
                deleteLinkStmt.executeUpdate();

                String deleteBookingSql = "DELETE FROM flights.booking WHERE booking_id = ?";
                PreparedStatement deleteBookingStmt = connection.prepareStatement(deleteBookingSql);
                deleteBookingStmt.setInt(1, bookingId);
                deleteBookingStmt.executeUpdate();

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }
}


