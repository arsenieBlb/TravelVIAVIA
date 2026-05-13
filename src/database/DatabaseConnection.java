package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection
{
  private static final String URL = "jdbc:postgresql://localhost:5432/travelviavia";
  private static final String USER = "postgres";
<<<<<<< Updated upstream
  private static final String PASSWORD = "arsenie";//Put yours if it doesn't work
=======
  private static final String PASSWORD = "viamakesmepay";//Put yours if it doesn't work
>>>>>>> Stashed changes

  // connects to the PostgreSQL database
  public static Connection getConnection() throws SQLException
  {
    return DriverManager.getConnection(URL, USER, PASSWORD);
  }
}