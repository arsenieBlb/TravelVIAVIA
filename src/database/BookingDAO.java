package database;

import model.Booking;
import model.Customer;
import model.Flight;
import model.LuggageType;
import model.Passenger;
import model.PassengerLuggage;
import model.SeatAssignment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
      String sql = "SELECT booking_id, flight_id, created_by_customer_id, "
          + "passenger_count, total_price FROM flights.booking";
      PreparedStatement statement = connection.prepareStatement(sql);
      ResultSet resultSet = statement.executeQuery();

      while (resultSet.next())
      {
        int bookingId = resultSet.getInt("booking_id");
        int flightId = resultSet.getInt("flight_id");
        int customerId = resultSet.getInt("created_by_customer_id");

        Flight flight = findFlightById(flights, flightId);
        Customer customer = findCustomerById(customers, customerId);

        if (flight != null && customer != null) {
          // loads passengers that belong to this booking
          List<Passenger> passengers = loadPassengers(bookingId, luggageTypes,
              connection);
          
          if (!passengers.isEmpty()) {
            Booking booking = new Booking(bookingId, LocalDateTime.now(),
                customer, flight, passengers);
            bookings.add(booking);
          }
        }
      }
    }

    return bookings;
  }

  // loads all passengers for a specific booking and attaches their luggage
  private List<Passenger> loadPassengers(int bookingId,
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

        // inserts the booking record
        String bookingSql = "INSERT INTO flights.booking "
            + "(booking_id, flight_id, created_by_customer_id, "
            + "passenger_count, total_price) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement bookingStatement = connection.prepareStatement(bookingSql);
        bookingStatement.setInt(1, booking.getBookingId());
        bookingStatement.setInt(2, booking.getFlight().getFlightId());
        bookingStatement.setInt(3, booking.getCustomer().getUserId());
        bookingStatement.setInt(4, booking.getPassengers().size());
        bookingStatement.setDouble(5, booking.getTotalPrice());
        bookingStatement.executeUpdate();
        
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

    SeatAssignment seatAssignment = passenger.getSeatAssignment();
    if (seatAssignment != null)
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
}
