package database;

import model.Carrier;
import model.City;
import model.Flight;
import model.Plane;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FlightDAO
{
  // loads all flights from the flight table and connects them to carriers, planes, and cities
  public List<Flight> getAllFlights(List<Carrier> carriers, List<Plane> planes,
      List<City> cities) throws SQLException {
    List<Flight> flights = new ArrayList<>();

    try (Connection connection = DatabaseConnection.getConnection())
    {
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
            && arrivalCity != null)
        {
          String flightNumber = "FL-" + flightId;

          Flight flight = new Flight(flightId, flightNumber, departureTime,
              arrivalTime, basePrice, carrier, plane, departureCity,
              arrivalCity);

          flights.add(flight);
        }
      }
    }

    return flights;
  }
  // finds a carrier by ID in the provided list
  private Carrier findCarrierById(List<Carrier> carriers, int id)
  {
    for (Carrier c : carriers) {
      if (c.getCarrierId() == id) {
        return c;
      }
    }
    return null;
  }

  // finds a plane by ID in the provided list
  private Plane findPlaneById(List<Plane> planes, int id) {
    for (Plane p : planes)
    {
      if (p.getPlaneId() == id) {
        return p;
      }
    }
    return null;
  }

  // finds a city by ID in the provided list
  private City findCityById(List<City> cities, int id)
  {
    for (City c : cities) {
      if (c.getCityId() == id) {
        return c;
      }
    }
    return null;
  }
}
