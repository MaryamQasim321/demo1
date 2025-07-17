package org.example.demo1.Config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnectorService {

    private static DatabaseConnectorService instance;
    private HikariDataSource dataSource;

    private DatabaseConnectorService() {
        configureDataSource();
    }

    public static synchronized DatabaseConnectorService getInstance() {
        if (instance == null) {
            instance = new DatabaseConnectorService();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        Connection connection = dataSource.getConnection();
        if (connection == null || connection.isClosed()) {
            throw new SQLException("Failed to obtain a valid connection from the pool.");
        }
        return connection;
    }

    private void configureDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/retailSystem");
        config.setUsername("root"); // or your DB user
        config.setPassword("BSCSBatch2027"); // your DB password
        config.setDriverClassName("com.mysql.cj.jdbc.Driver"); // ✅ VERY IMPORTANT

        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(10000);
        config.setLeakDetectionThreshold(15000);
        config.setValidationTimeout(5000);
        config.setInitializationFailTimeout(-1);

        dataSource = new HikariDataSource(config);
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
