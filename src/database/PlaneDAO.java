package database;

import model.Carrier;
import model.Plane;
import model.PlaneStatus;
import model.PlaneType;
import model.Seat;
import model.SeatClass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaneDAO
{
  // loads all plane types from the database, maps section type to seat counts
  public List<PlaneType> getAllPlaneTypes() throws SQLException {
    List<PlaneType> planeTypes = new ArrayList<>();

    try (Connection connection = DatabaseConnection.getConnection())
    {
      String sql = "SELECT pt.id, pt.name, pt.model, pt.num_of_columns, "
          + "business.num_of_seats AS business_seats, "
          + "economy.num_of_seats AS economy_seats "
          + "FROM flights.plane_type pt "
          + "LEFT JOIN flights.section business "
          + "ON pt.business_section = business.id "
          + "LEFT JOIN flights.section economy "
          + "ON pt.economy_section = economy.id";

      PreparedStatement statement = connection.prepareStatement(sql);
      ResultSet resultSet = statement.executeQuery();

      while (resultSet.next()) {
        int id = resultSet.getInt("id");
        String name = resultSet.getString("name");
        String model = resultSet.getString("model");
        int numberOfColumns = resultSet.getInt("num_of_columns");
        int businessSeats = resultSet.getInt("business_seats");
        if (resultSet.wasNull()) {
          businessSeats = 0;
        }
        int economySeats = resultSet.getInt("economy_seats");
        if (resultSet.wasNull()) {
          economySeats = 0;
        }

        planeTypes.add(
            new PlaneType(id, name, model, numberOfColumns, economySeats,
                businessSeats));
      }
    }

    return planeTypes;
  }

  // loads all planes with their type and carrier, and attaches the seats
  public List<Plane> getAllPlanes(List<PlaneType> planeTypes,
      List<Carrier> carriers) throws SQLException
  {
    List<Plane> planes = new ArrayList<>();

    try (Connection connection = DatabaseConnection.getConnection()) {
      String sql = "SELECT id, plane_type, carrier_id FROM flights.plane";
      PreparedStatement statement = connection.prepareStatement(sql);
      ResultSet resultSet = statement.executeQuery();

      while (resultSet.next())
      {
        int planeId = resultSet.getInt("id");
        int planeTypeId = resultSet.getInt("plane_type");
        int carrierId = resultSet.getInt("carrier_id");

        PlaneType planeType = findPlaneTypeById(planeTypes, planeTypeId);
        Carrier carrier = findCarrierById(carriers, carrierId);

        if (planeType != null && carrier != null) {
          Plane plane = new Plane(planeId, "REG-" + planeId,
              PlaneStatus.Active, planeType, carrier);

          // loads the seats that belong to this plane's section
          loadSeatsForPlane(plane, connection);
          
          planes.add(plane);
        }
      }
    }

    return planes;
  }
  // loads all seats from the seat table and adds them to the plane
  private void loadSeatsForPlane(Plane plane, Connection connection)
      throws SQLException
  {
    String sql = "SELECT s.id, s.seat_label, sec.type "
        + "FROM flights.seat s "
        + "JOIN flights.section sec ON s.section_id = sec.id";

    PreparedStatement statement = connection.prepareStatement(sql);
    ResultSet resultSet = statement.executeQuery();

    while (resultSet.next()) {
      int seatId = resultSet.getInt("id");
      String seatLabel = resultSet.getString("seat_label");
      String sectionType = resultSet.getString("type");

      SeatClass seatClass = "Business".equalsIgnoreCase(sectionType)
          ? SeatClass.Business : SeatClass.Economy;

      int rowNumber = extractRowNumber(seatLabel);

      try {
        Seat seat = new Seat(seatId, seatLabel, rowNumber, seatClass);
        plane.addSeat(seat);
      } catch (IllegalArgumentException e) {
        // seat capacity reached for this plane type, skip remaining seats
      }
    }
  }

  // pulls out the row number from a seat label like "10A" -> 10
  private int extractRowNumber(String seatLabel)
  {
    StringBuilder digits = new StringBuilder();
    for (char c : seatLabel.toCharArray()) {
      if (Character.isDigit(c)) {
        digits.append(c);
      } else {
        break;
      }
    }
    return digits.length() > 0 ? Integer.parseInt(digits.toString()) : 1;
  }
  // finds a plane type by ID in the provided list
  private PlaneType findPlaneTypeById(List<PlaneType> planeTypes, int id)
  {
    for (PlaneType pt : planeTypes) {
      if (pt.getPlaneTypeId() == id) {
        return pt;
      }
    }
    return null;
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
}
