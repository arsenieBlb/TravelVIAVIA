package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection
{
  private static final String URL = "jdbc:postgresql://localhost:5432/postgres";
  private static final String USER = "postgres";

  private static final String PASSWORD = "viamakesmepay";//Put yours if it doesn't work


  // connects to the PostgreSQL database
  public static Connection getConnection() throws SQLException
  {
    return DriverManager.getConnection(URL, USER, PASSWORD);
  }
}