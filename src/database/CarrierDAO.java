package database;

import model.Carrier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CarrierDAO {
  // loads all carriers from the carrier table
  public List<Carrier> getAllCarriers() throws SQLException {
    List<Carrier> carriers = new ArrayList<>();

    try (Connection connection = DatabaseConnection.getConnection())
    {
      String sql = "SELECT carrier_id, carrier_name FROM flights.carrier";
      PreparedStatement statement = connection.prepareStatement(sql);
      ResultSet resultSet = statement.executeQuery();
      
      while (resultSet.next()) {
        int carrierId = resultSet.getInt("carrier_id");
        
        String carrierName = resultSet.getString("carrier_name");

        carriers.add(new Carrier(carrierId, carrierName));
      }
    }

    return carriers;
  }

  // finds one carrier by its ID
  public Carrier getCarrierById(int carrierId) throws SQLException
  {
    try (Connection connection = DatabaseConnection.getConnection()) {
      String sql = "SELECT carrier_id, carrier_name FROM flights.carrier "
          + "WHERE carrier_id = ?";
          
      PreparedStatement statement = connection.prepareStatement(sql);
      statement.setInt(1, carrierId);
      ResultSet resultSet = statement.executeQuery();

      if (resultSet.next()) {
        return new Carrier(resultSet.getInt("carrier_id"),
            resultSet.getString("carrier_name"));
      }
    }
    return null;
  }
}
