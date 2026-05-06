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
  public Customer getCustomerById(int userId,
      FlightSearchService flightSearchService) throws SQLException
  {
    try (Connection connection = DatabaseConnection.getConnection())
    {
      String sql = "SELECT u.user_id, u.email, u.password_hash "
          + "FROM flights.users u "
          + "JOIN flights.customer c ON u.user_id = c.customer_id "
          + "WHERE u.user_id = ?";

      PreparedStatement statement = connection.prepareStatement(sql);
      statement.setInt(1, userId);
      ResultSet resultSet = statement.executeQuery();

      if (resultSet.next())
      {
        return loadCustomer(resultSet.getInt("user_id"),
            resultSet.getString("email"), resultSet.getString("password_hash"),
            flightSearchService, connection);
      }
    }
    return null;
  }

  public Customer getFirstCustomer(FlightSearchService flightSearchService)
      throws SQLException
  {
    try (Connection connection = DatabaseConnection.getConnection())
    {
      String sql = "SELECT u.user_id, u.email, u.password_hash "
          + "FROM flights.users u "
          + "JOIN flights.customer c ON u.user_id = c.customer_id "
          + "ORDER BY u.user_id LIMIT 1";

      PreparedStatement statement = connection.prepareStatement(sql);
      ResultSet resultSet = statement.executeQuery();

      if (resultSet.next())
      {
        return loadCustomer(resultSet.getInt("user_id"),
            resultSet.getString("email"), resultSet.getString("password_hash"),
            flightSearchService, connection);
      }
    }
    return null;
  }

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

  public boolean registerCustomer(String firstName, String lastName, String email, String password) throws SQLException {
    try (Connection connection = DatabaseConnection.getConnection()) {
      connection.setAutoCommit(false);
      try {
        String maxIdSql = "SELECT COALESCE(MAX(user_id), 0) FROM flights.users";
        PreparedStatement maxIdStmt = connection.prepareStatement(maxIdSql);
        ResultSet rs = maxIdStmt.executeQuery();
        int newUserId = 1;
        if (rs.next()) {
           newUserId = rs.getInt(1) + 1;
        }

        String insertUserSql = "INSERT INTO flights.users (user_id, email, password_hash, user_type) VALUES (?, ?, ?, 'Customer')";
        PreparedStatement insertUserStmt = connection.prepareStatement(insertUserSql);
        insertUserStmt.setInt(1, newUserId);
        insertUserStmt.setString(2, email);
        insertUserStmt.setString(3, password);
        insertUserStmt.executeUpdate();

        String insertCustomerSql = "INSERT INTO flights.customer (customer_id, first_name, last_name) VALUES (?, ?, ?)";
        PreparedStatement insertCustomerStmt = connection.prepareStatement(insertCustomerSql);
        insertCustomerStmt.setInt(1, newUserId);
        insertCustomerStmt.setString(2, firstName);
        insertCustomerStmt.setString(3, lastName);
        insertCustomerStmt.executeUpdate();

        connection.commit();
        return true;
      } catch (SQLException e) {
        connection.rollback();
        return false;
      } finally {
        connection.setAutoCommit(true);
      }
    }
  }
}
