package server.database;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class UpdateDB {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE flights.luggage_types SET extra_price = 15.00 WHERE name ILIKE '%Carry%'")) {
            int rows = stmt.executeUpdate();
            System.out.println("Updated " + rows + " row(s). Carry-on is now 15 euros.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
