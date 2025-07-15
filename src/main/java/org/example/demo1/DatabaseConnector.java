package org.example.demo1;
import java.sql.*;
public class DatabaseConnector {
    String jdbcURL = "jdbc:mysql://localhost:3306/retailSystem";
    String username = "root";
    String password = "BSCSBatch2027";
    public DatabaseConnector() {
        try (
                Connection conn = DriverManager.getConnection(jdbcURL, username, password);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM Products")  // table name corrected
        ) {
            System.out.println("Connected!");

            while (rs.next()) {
                System.out.println(rs.getInt("productId") + " - " + rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        new DatabaseConnector();  // call the constructor
    }
}