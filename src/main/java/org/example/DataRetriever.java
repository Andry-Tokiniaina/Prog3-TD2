package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DataRetriever {

    public Dish findDishById(int id) {
        DBConnection dbConnection = new DBConnection();
        String query = "select id, name from dish where id = ?";
        try {
            Connection conn = dbConnection.getDBConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1,id);
            ResultSet rs = stmt.executeQuery();
            Dish dish = null;
            return dish;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
