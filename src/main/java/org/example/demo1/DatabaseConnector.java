package org.example.demo1;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
public class DatabaseConnector {

    private static DatabaseConnector instance;
    private final HikariDataSource dataSource;

    private DatabaseConnector() {
        HikariConfig config = new HikariConfig();
        String jdbcUrl = System.getenv("JDBC_URL");
        String username = System.getenv("DB_USERNAME");
        String password = System.getenv("DB_PASSWORD");

        if (jdbcUrl == null || username == null || password == null) {
            throw new RuntimeException("Missing environment variables: JDBC_URL, DB_USERNAME, or DB_PASSWORD");
        }

        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);

        this.dataSource = new HikariDataSource(config);
    }
    public static synchronized DatabaseConnector getInstance() {
        if (instance == null) {
            instance = new DatabaseConnector();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}