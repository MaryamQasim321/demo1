package org.example.demo1.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
        // 1. Load from environment variables
        String jdbcUrl = System.getenv("DB_URL");
        String username = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        if (jdbcUrl == null || username == null || password == null) {
            throw new IllegalStateException("One or more database environment variables are missing.");
        }

        // 2. Create DatabaseConfig object
        DatabaseConfig dbConfig = new DatabaseConfig(jdbcUrl, username, password);

        // 3. Serialize (marshal) config object to JSON
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(dbConfig);
        System.out.println("Serialized DB Config:\n" + json);

        // 4. Deserialize (unmarshal) JSON string back into an object
        DatabaseConfig deserializedConfig = gson.fromJson(json, DatabaseConfig.class);

        // 5. Use deserialized object to configure Hikari
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(deserializedConfig.getJdbcUrl());
        config.setUsername(deserializedConfig.getUsername());
        config.setPassword(deserializedConfig.getPassword());
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

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
