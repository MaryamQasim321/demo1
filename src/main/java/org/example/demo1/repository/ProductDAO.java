package org.example.demo1.repository;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.example.demo1.config.DatabaseConnectorService;
import org.example.demo1.model.Product;
import org.example.demo1.logging.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ProductDAO {

    private static final Logger logger = LoggerFactory.getLogger(ProductDAO.class);
    private static final String PREFIX = LogUtils.prefix("ProductDAO");

    private static final String SELECT_ALL_PRODUCTS_QUERY = "SELECT * FROM Products";
    private static final String SELECT_PRODUCT_BY_ID_QUERY = "SELECT * FROM Products WHERE productId = ?";
    private static final String CREATE_PRODUCT_QUERY = "INSERT INTO Products(productId, name, price, stock) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_PRODUCT_QUERY = "UPDATE Products SET name = ?, price = ?, stock = ? WHERE productId = ?";
    private static final String DELETE_PRODUCT_BY_ID_QUERY = "DELETE FROM Products WHERE productId = ?";
    private static final Retry retry = Retry.of("productRetry",
            RetryConfig.custom()
                    .maxAttempts(3)
                    .waitDuration(Duration.ofSeconds(2))
                    .retryExceptions(SQLException.class)
                    .build());
    public List<Product> getAllProducts() throws SQLException {
        Supplier<List<Product>> retryingSupplier = Retry.decorateSupplier(retry, () -> {
            List<Product> products = new ArrayList<>();

            try (Connection conn = DatabaseConnectorService.getInstance().getConnection();
                 PreparedStatement statement = conn.prepareStatement(SELECT_ALL_PRODUCTS_QUERY);
                 ResultSet rs = statement.executeQuery()) {

                while (rs.next()) {
                    Product product = new Product();
                    product.setProductId(rs.getInt("productId"));
                    product.setPrice(rs.getBigDecimal("price"));
                    product.setName(rs.getString("name"));
                    product.setStock(rs.getInt("stock"));
                    products.add(product);
                }

                logger.info(LogUtils.success(PREFIX + "Fetched all products. Count: " + products.size()));
                return products;
            } catch (SQLException e) {
                logger.warn(LogUtils.warn(PREFIX + "Retrying getAllProducts due to SQL failure: " + e.getMessage()));
                try {
                    throw e; // Will trigger retry
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        try {
            return retryingSupplier.get();  // May retry internally
        } catch (Exception e) {
            logger.error(LogUtils.error(PREFIX + "Failed after retries to fetch all products", e));
            throw e instanceof SQLException ? (SQLException) e : new SQLException(e);
        }
    }

    public Product getProductById(int id) throws SQLException {
        try (Connection conn = DatabaseConnectorService.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(SELECT_PRODUCT_BY_ID_QUERY)) {

            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Product product = new Product();
                    product.setProductId(rs.getInt("productId"));
                    product.setPrice(rs.getBigDecimal("price"));
                    product.setName(rs.getString("name"));
                    product.setStock(rs.getInt("stock"));

                    logger.info(LogUtils.success(PREFIX + "Product found with ID: " + id));
                    return product;
                }
            }

            logger.warn(LogUtils.warn(PREFIX + "No product found with ID: " + id));
        } catch (Exception e) {
            logger.error(LogUtils.error(PREFIX + "Failed to fetch product with ID: " + id, e));
            throw e;
        }

        return null;
    }

    public void createNewProduct(int id, String name, int stock, BigDecimal price) throws SQLException {
        try (Connection conn = DatabaseConnectorService.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(CREATE_PRODUCT_QUERY)) {

            statement.setInt(1, id);
            statement.setString(2, name);
            statement.setBigDecimal(3, price);
            statement.setInt(4, stock);
            statement.executeUpdate();

            logger.info(LogUtils.success(PREFIX + "Inserted new product with ID: " + id));
        } catch (Exception e) {
            logger.error(LogUtils.error(PREFIX + "Failed to insert product with ID: " + id, e));
            throw e;
        }
    }

    public void updateProduct(int id, String name, int stock, BigDecimal price) throws SQLException {
        try (Connection conn = DatabaseConnectorService.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(UPDATE_PRODUCT_QUERY)) {

            statement.setString(1, name);
            statement.setBigDecimal(2, price);
            statement.setInt(3, stock);
            statement.setInt(4, id);
            statement.executeUpdate();

            logger.info(LogUtils.success(PREFIX + "Updated product with ID: " + id));
        } catch (Exception e) {
            logger.error(LogUtils.error(PREFIX + "Failed to update product with ID: " + id, e));
            throw e;
        }
    }

    public boolean deleteProductById(int id) throws SQLException {
        try (Connection conn = DatabaseConnectorService.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(DELETE_PRODUCT_BY_ID_QUERY)) {

            statement.setInt(1, id);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                logger.info(LogUtils.success(PREFIX + "Deleted product with ID: " + id));
                return true;
            } else {
                logger.warn(LogUtils.warn(PREFIX + "No product found with ID: " + id + " to delete"));
            }
        } catch (Exception e) {
            logger.error(LogUtils.error(PREFIX + "Failed to delete product with ID: " + id, e));
            throw e;
        }

        return false;
    }
}
