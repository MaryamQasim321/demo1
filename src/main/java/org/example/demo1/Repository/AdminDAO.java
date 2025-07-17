package org.example.demo1.Repository;
import org.example.demo1.model.Admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

    public class AdminDAO {
        private final Connection connection;

        public AdminDAO(Connection connection) {
            this.connection = connection;
        }

        public Admin findByUsername(String username) {
            try {
                String query = "SELECT * FROM admin WHERE username = ?";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    Admin admin = new Admin();
                    admin.setId(rs.getInt("id"));
                    admin.setUsername(rs.getString("username"));
                    admin.setPassword(rs.getString("password"));
                    return admin;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

}
