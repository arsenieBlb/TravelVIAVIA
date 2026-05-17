package server.database;

import client.model.Carrier;
import client.model.City;
import client.model.Flight;
import client.model.Plane;
import client.model.Seat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FlightDAO {
  // loads all flights from the flight table and connects them to carriers,
  // planes, and cities
  public List<Flight> getAllFlights(List<Carrier> carriers, List<Plane> planes,
      List<City> cities) throws SQLException {
    List<Flight> flights = new ArrayList<>();

    try (Connection connection = DatabaseConnection.getConnection()) {
      String sql = "SELECT flight_id, carrier_id, plane_id, "
          + "departure_city_id, arrival_city_id, "
          + "departure_time, arrival_time, base_price, flight_status "
          + "FROM flights.flight "
          + "WHERE flight_status = 'Available'";

      PreparedStatement statement = connection.prepareStatement(sql);
      ResultSet resultSet = statement.executeQuery();

      while (resultSet.next()) {
        int flightId = resultSet.getInt("flight_id");
        int carrierId = resultSet.getInt("carrier_id");
        int planeId = resultSet.getInt("plane_id");
        int departureCityId = resultSet.getInt("departure_city_id");
        int arrivalCityId = resultSet.getInt("arrival_city_id");
        Timestamp depTimestamp = resultSet.getTimestamp("departure_time");

        Timestamp arrTimestamp = resultSet.getTimestamp("arrival_time");
        double basePrice = resultSet.getDouble("base_price");

        LocalDateTime departureTime = depTimestamp.toLocalDateTime();
        LocalDateTime arrivalTime = arrTimestamp.toLocalDateTime();

        // finds the matching carrier, plane, and cities for this flight
        Carrier carrier = findCarrierById(carriers, carrierId);
        Plane plane = findPlaneById(planes, planeId);
        City departureCity = findCityById(cities, departureCityId);
        City arrivalCity = findCityById(cities, arrivalCityId);

        if (carrier != null && plane != null && departureCity != null
            && arrivalCity != null) {
          String flightNumber = "FL-" + flightId;

          Flight flight = new Flight(flightId, flightNumber, departureTime,
              arrivalTime, basePrice, carrier, plane, departureCity,
              arrivalCity);

          flights.add(flight);
        }
      }

      loadOccupiedSeats(flights, connection);
    }

    return flights;
  }

  // marks already booked seats as unavailable for the seat picker
  private void loadOccupiedSeats(List<Flight> flights, Connection connection)
      throws SQLException {
    String sql = "SELECT flight_id, seat_id FROM flights.flight_seat "
        + "WHERE is_occupied = TRUE";
    PreparedStatement statement = connection.prepareStatement(sql);
    ResultSet resultSet = statement.executeQuery();

    while (resultSet.next()) {
      Flight flight = findFlightById(flights, resultSet.getInt("flight_id"));
      if (flight == null) {
        continue;
      }

      Seat seat = findSeatById(flight.getPlane().getSeats(),
          resultSet.getInt("seat_id"));
      if (seat != null) {
        flight.markSeatOccupied(seat);
      }
    }
  }

  // finds a carrier by ID in the provided list
  private Carrier findCarrierById(List<Carrier> carriers, int id) {
    for (Carrier c : carriers) {
      if (c.getCarrierId() == id) {
        return c;
      }
    }
    return null;
  }

  private Flight findFlightById(List<Flight> flights, int id) {
    for (Flight flight : flights) {
      if (flight.getFlightId() == id) {
        return flight;
      }
    }
    return null;
  }

  // finds a plane by ID in the provided list
  private Plane findPlaneById(List<Plane> planes, int id) {
    for (Plane p : planes) {
      if (p.getPlaneId() == id) {
        return p;
      }
    }
    return null;
  }

  private Seat findSeatById(List<Seat> seats, int id) {
    for (Seat seat : seats) {
      if (seat.getSeatId() == id) {
        return seat;
      }
    }
    return null;
  }

  // finds a city by ID in the provided list
  private City findCityById(List<City> cities, int id) {
    for (City c : cities) {
      if (c.getCityId() == id) {
        return c;
      }
    }
    return null;
  }

  public void saveFlight(Flight flight) throws SQLException {
    String sql = "INSERT INTO flights.flight (flight_id, carrier_id, plane_id, " +
        "departure_city_id, arrival_city_id, departure_time, arrival_time, " +
        "base_price, flight_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement stmt = connection.prepareStatement(sql)) {

      stmt.setInt(1, flight.getFlightId());
      stmt.setInt(2, flight.getCarrier().getCarrierId());
      stmt.setInt(3, flight.getPlane().getPlaneId());
      stmt.setInt(4, flight.getDepartureCity().getCityId());
      stmt.setInt(5, flight.getArrivalCity().getCityId());
      stmt.setTimestamp(6, Timestamp.valueOf(flight.getDepartureTime()));
      stmt.setTimestamp(7, Timestamp.valueOf(flight.getArrivalTime()));
      stmt.setDouble(8, flight.getBasePrice());
      stmt.setString(9, "Available");

      stmt.executeUpdate();
    }
  }

  public void removeFlight(int flightId) throws SQLException {
    String sql = "UPDATE flights.flight SET flight_status = ? "
        + "WHERE flight_id = ?";

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement stmt = connection.prepareStatement(sql)) {
      stmt.setString(1, "Unavailable");
      stmt.setInt(2, flightId);
      if (stmt.executeUpdate() == 0) {
        throw new SQLException("Flight " + flightId + " was not found.");
      }
    }
  }

  public void updateFlight(Flight flight) throws SQLException {
    String sql = "UPDATE flights.flight SET carrier_id = ?, plane_id = ?, "
        + "departure_city_id = ?, arrival_city_id = ?, "
        + "departure_time = ?, arrival_time = ?, base_price = ? "
        + "WHERE flight_id = ?";

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement stmt = connection.prepareStatement(sql)) {
      stmt.setInt(1, flight.getCarrier().getCarrierId());
      stmt.setInt(2, flight.getPlane().getPlaneId());
      stmt.setInt(3, flight.getDepartureCity().getCityId());
      stmt.setInt(4, flight.getArrivalCity().getCityId());
      stmt.setTimestamp(5, Timestamp.valueOf(flight.getDepartureTime()));
      stmt.setTimestamp(6, Timestamp.valueOf(flight.getArrivalTime()));
      stmt.setDouble(7, flight.getBasePrice());
      stmt.setInt(8, flight.getFlightId());

      if (stmt.executeUpdate() == 0) {
        throw new SQLException("Flight " + flight.getFlightId() + " was not found.");
      }
    }
  }
}


