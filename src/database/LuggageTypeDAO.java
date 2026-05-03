package database;

import model.LuggageType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LuggageTypeDAO
{
  // loads all luggage types from the luggage_type table
  public List<LuggageType> getAllLuggageTypes() throws SQLException
  {
    List<LuggageType> luggageTypes = new ArrayList<>();
    
    try (Connection connection = DatabaseConnection.getConnection()) {
      String sql = "SELECT luggage_type_id, name, description, extra_price "
          + "FROM flights.luggage_type";
      PreparedStatement statement = connection.prepareStatement(sql);
      ResultSet resultSet = statement.executeQuery();
      while (resultSet.next())
      {
        int id = resultSet.getInt("luggage_type_id");
        String name = resultSet.getString("name");
        String description = resultSet.getString("description");
        double extraPrice = resultSet.getDouble("extra_price");

        luggageTypes.add(new LuggageType(id, name, description, extraPrice));
      }
    }

    return luggageTypes;
  }
  // finds one luggage type by its ID
  public LuggageType getLuggageTypeById(int luggageTypeId) throws SQLException
  {
    try (Connection connection = DatabaseConnection.getConnection())
    {
      String sql = "SELECT luggage_type_id, name, description, extra_price "
          + "FROM flights.luggage_type WHERE luggage_type_id = ?";
          
      PreparedStatement statement = connection.prepareStatement(sql);
      statement.setInt(1, luggageTypeId);
      ResultSet resultSet = statement.executeQuery();

      if (resultSet.next()) {
        return new LuggageType(resultSet.getInt("luggage_type_id"),
            resultSet.getString("name"), resultSet.getString("description"),
            resultSet.getDouble("extra_price"));
      }
    }
    return null;
  }
}
