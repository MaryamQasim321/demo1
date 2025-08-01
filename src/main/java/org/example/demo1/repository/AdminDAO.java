package org.example.demo1.repository;

import org.example.demo1.logging.LogUtils;
import org.example.demo1.model.Admin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdminDAO {
    private static final Logger logger = LoggerFactory.getLogger(AdminDAO.class);
    private final Connection connection;

    public AdminDAO(Connection connection) {
        this.connection = connection;
    }

    public Admin findByUsername(String username) {
        String context = "AdminDAO";

        logger.info(LogUtils.success(context + ": Looking up admin by username: " + username));

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

                logger.info(LogUtils.success(context + ": Admin found: id=" + admin.getId() + ", username=" + admin.getUsername()));
                return admin;
            } else {
                logger.warn(LogUtils.warn(context + ": No admin found with username: " + username));
            }
        } catch (Exception e) {
            logger.error(LogUtils.error(context + ": Error while fetching admin by username: " + username, e), e);
        }

        return null;
    }
}
