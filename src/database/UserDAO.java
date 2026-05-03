package database;

import model.Admin;
import model.Customer;
import model.FlightSearchService;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO
{
  // checks the users table for matching email and password, then loads the right user type
  public User login(String email, String password,
      FlightSearchService flightSearchService) throws SQLException {
    try (Connection connection = DatabaseConnection.getConnection())
    {
      String sql = "SELECT u.user_id, u.email, u.password_hash, u.user_type "
          + "FROM flights.users u "
          + "WHERE u.email = ? AND u.password_hash = ?";

      PreparedStatement statement = connection.prepareStatement(sql);
      statement.setString(1, email);
      statement.setString(2, password);
      ResultSet resultSet = statement.executeQuery();

      if (resultSet.next())
      {
        int userId = resultSet.getInt("user_id");
        String userEmail = resultSet.getString("email");
        String passwordHash = resultSet.getString("password_hash");
        
        String userType = resultSet.getString("user_type");

        // loads an Admin or Customer depending on the user_type column
        if ("Admin".equalsIgnoreCase(userType)) {
          return new Admin(userId, userEmail, passwordHash,
              flightSearchService);
        } else if ("Customer".equalsIgnoreCase(userType)) {
          return loadCustomer(userId, userEmail, passwordHash,
              flightSearchService, connection);
        }
      }
    }
    return null;
  }

  // loads the customer first_name and last_name from the customer table
  private Customer loadCustomer(int userId, String email, String password,
      FlightSearchService flightSearchService, Connection connection)
      throws SQLException
  {
    String sql = "SELECT first_name, last_name FROM flights.customer "
        + "WHERE customer_id = ?";

    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setInt(1, userId);
    ResultSet resultSet = statement.executeQuery();

    if (resultSet.next()) {
      String firstName = resultSet.getString("first_name");
      String lastName = resultSet.getString("last_name");
      return new Customer(userId, email, password, firstName, lastName,
          flightSearchService);
    }

    return new Customer(userId, email, password, "Unknown", "User",
        flightSearchService);
  }
}
