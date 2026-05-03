package database;

import model.City;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CityDAO
{
  // loads all cities from the city table
  public List<City> getAllCities() throws SQLException {
    List<City> cities = new ArrayList<>();
    try (Connection connection = DatabaseConnection.getConnection())
    {
      String sql = "SELECT city_id, city_name, country FROM flights.city";
      PreparedStatement statement = connection.prepareStatement(sql);
      ResultSet resultSet = statement.executeQuery();
      
      while (resultSet.next())
      {
        int cityId = resultSet.getInt("city_id");
        String cityName = resultSet.getString("city_name");
        
        String country = resultSet.getString("country");

        cities.add(new City(cityId, cityName, country));
      }
    }
    return cities;
  }
  // finds one city by its ID
  public City getCityById(int cityId) throws SQLException
  {
    try (Connection connection = DatabaseConnection.getConnection())
    {
      String sql = "SELECT city_id, city_name, country FROM flights.city "
          + "WHERE city_id = ?";
          
      PreparedStatement statement = connection.prepareStatement(sql);
      statement.setInt(1, cityId);
      ResultSet resultSet = statement.executeQuery();

      if (resultSet.next()) {
        return new City(resultSet.getInt("city_id"), resultSet.getString("city_name"),
            resultSet.getString("country"));
      }
    }
    return null;
  }
}
