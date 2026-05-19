package server.database;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Utility class to initialize the PostgreSQL database from the flights.sql file.
 * Run this class's main method to automatically build the schema and populate the
 * database with the default users, flights, and cities.
 */
public class InitDB {
    public static void main(String[] args) {
        System.out.println("Reading flights.sql...");
        try {
            // Read the SQL file from the .idea/etc/ directory
            String sql = Files.readString(Paths.get(".idea/etc/flights.sql"));

            System.out.println("Connecting to the database...");
            // Use the centralized database connection
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement()) {

                System.out.println("Executing SQL script to initialize the database. This may take a moment...");
                stmt.execute(sql);

                System.out.println("✅ Database successfully initialized from flights.sql!");
                System.out.println("You can now start the ServerMain and log in as 'boss' (Admin) or 'j.doe@gmail.com' (Customer).");
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize the database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
