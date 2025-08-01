package org.example.demo1.repository;

import org.example.demo1.config.DatabaseConnectorService;
import org.example.demo1.logging.LogUtils;
import org.example.demo1.model.*;

import io.github.resilience4j.retry.*;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class OrderDAO {
    private static final Logger logger = LoggerFactory.getLogger(OrderDAO.class);

    // SQL queries
    public static final String VIEW_ALL_ORDERS_QUERY = "SELECT * FROM orders";
    public static final String VIEW_ORDER_BY_ID_QUERY = "SELECT * FROM orders WHERE orderId=?";
    public static final String ADD_ORDER_QUERY = "INSERT INTO orders(orderId, customerId, totalCost) VALUES (?, ?, ?)";
    public static final String GET_PRODUCT_PRICE_QUERY = "SELECT price FROM products WHERE productId=?";
    public static final String INSERT_INTO_ORDERPRODUCT_QUERY = "INSERT INTO orderProduct(orderId, productId, quantity) VALUES (?, ?, ?)";
    public static final String UPDATE_PRODUCT_STOCK_QUERY = "UPDATE products SET stock = stock - ? WHERE productId = ?";
    public static final String INCREMENT_PRODUCT_STOCK_QUERY = "UPDATE products SET stock = stock + ? WHERE productId = ?";
    public static final String GET_ORDER_PRODUCTS_QUERY = "SELECT productId, quantity FROM orderProduct WHERE orderId = ?";

    // 1. Get all orders
    public List<Order> getAllOrders() throws SQLException {
        String logPrefix = LogUtils.prefix("OrderDAO");
        logger.info("{}Fetching all orders", logPrefix);

        List<Order> orders = new ArrayList<>();

        try (Connection conn = DatabaseConnectorService.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(VIEW_ALL_ORDERS_QUERY);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("orderId"));
                order.setCustomerId(rs.getInt("customerId"));
                order.setTotalCost(rs.getFloat("totalCost"));
                order.setOrderDate(rs.getTimestamp("orderDate"));
                orders.add(order);
            }

            logger.info("{}Fetched {} orders", logPrefix, orders.size());

        } catch (SQLException e) {
            logger.error("{}Error fetching all orders", logPrefix, e);
            throw e;
        }

        return orders;
    }

    // 2. Get order by ID
    public Order getOrderById(int id) throws SQLException {
        String logPrefix = LogUtils.prefix("OrderDAO");
        logger.info("{}Fetching order with ID: {}", logPrefix, id);

        try (Connection conn = DatabaseConnectorService.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(VIEW_ORDER_BY_ID_QUERY)) {

            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getInt("orderId"));
                    order.setCustomerId(rs.getInt("customerId"));
                    order.setTotalCost(rs.getFloat("totalCost"));
                    order.setOrderDate(rs.getTimestamp("orderDate"));
                    logger.info("{}Order found: {}", logPrefix, order.getOrderId());
                    return order;
                } else {
                    logger.warn("{}Order ID {} not found", logPrefix, id);
                }
            }

        } catch (SQLException e) {
            logger.error("{}Error fetching order with ID: {}", logPrefix, id, e);
            throw e;
        }

        return null;
    }

    // 3. Create new order and decrease stock
    public List<StockChangeInfo> createOrder(int orderId, OrderRequest request) {
        String logPrefix = LogUtils.prefix("OrderDAO");
        logger.info("{}Creating order: orderId={}, customerId={}", logPrefix, orderId, request.getCustomerId());

        BigDecimal totalPrice = BigDecimal.ZERO;
        List<StockChangeInfo> stockChanges = new ArrayList<>();

        try (Connection conn = DatabaseConnectorService.getInstance().getConnection()) {
            conn.setAutoCommit(false);

            if (!customerExists(request.getCustomerId(), conn)) {
                throw new SQLException("Customer ID " + request.getCustomerId() + " does not exist.");
            }

            for (ProductOrder po : request.getProducts()) {
                BigDecimal itemTotal = getProductPrice(po.getProductId(), conn)
                        .multiply(BigDecimal.valueOf(po.getQuantity()));
                totalPrice = totalPrice.add(itemTotal);
            }

            try (PreparedStatement stmt = conn.prepareStatement(ADD_ORDER_QUERY)) {
                stmt.setInt(1, orderId);
                stmt.setInt(2, request.getCustomerId());
                stmt.setBigDecimal(3, totalPrice);
                stmt.executeUpdate();
            }

            try (PreparedStatement opStmt = conn.prepareStatement(INSERT_INTO_ORDERPRODUCT_QUERY);
                 PreparedStatement stockStmt = conn.prepareStatement(UPDATE_PRODUCT_STOCK_QUERY);
                 PreparedStatement getStockStmt = conn.prepareStatement("SELECT stock FROM products WHERE productId = ?")) {

                for (ProductOrder po : request.getProducts()) {
                    int productId = po.getProductId();
                    int quantity = po.getQuantity();

                    getStockStmt.setInt(1, productId);
                    int oldStock;
                    try (ResultSet rs = getStockStmt.executeQuery()) {
                        if (rs.next()) {
                            oldStock = rs.getInt("stock");
                        } else {
                            throw new SQLException("Product ID " + productId + " not found.");
                        }
                    }

                    if (oldStock < quantity) {
                        throw new SQLException("Insufficient stock for product ID " + productId);
                    }

                    int newStock = oldStock - quantity;
                    stockChanges.add(new StockChangeInfo(productId, oldStock, newStock));

                    opStmt.setInt(1, orderId);
                    opStmt.setInt(2, productId);
                    opStmt.setInt(3, quantity);
                    opStmt.addBatch();

                    stockStmt.setInt(1, quantity);
                    stockStmt.setInt(2, productId);
                    stockStmt.addBatch();
                }

                opStmt.executeBatch();
                stockStmt.executeBatch();
            }

            conn.commit();
            logger.info("{}Order created: orderId={}, totalPrice={}", logPrefix, orderId, totalPrice);
            return stockChanges;

        } catch (SQLException e) {
            logger.error("{}Failed to create order: orderId={}", logPrefix, orderId, e);
            throw new RuntimeException("Failed to create order: " + e.getMessage(), e);
        }
    }

    private boolean customerExists(int customerId, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM customers WHERE customerId = ?")) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // 4. Delete order and restore stock
    public List<StockChangeInfo> deleteOrderById(int orderId) {
        String logPrefix = LogUtils.prefix("OrderDAO");
        logger.info("{}Deleting order: orderId={}", logPrefix, orderId);

        List<StockChangeInfo> stockChanges = new ArrayList<>();

        try (Connection conn = DatabaseConnectorService.getInstance().getConnection()) {
            conn.setAutoCommit(false);

            List<ProductOrder> orderedProducts = new ArrayList<>();
            try (PreparedStatement getItems = conn.prepareStatement(GET_ORDER_PRODUCTS_QUERY)) {
                getItems.setInt(1, orderId);
                try (ResultSet rs = getItems.executeQuery()) {
                    while (rs.next()) {
                        ProductOrder po = new ProductOrder();
                        po.setProductId(rs.getInt("productId"));
                        po.setQuantity(rs.getInt("quantity"));
                        orderedProducts.add(po);
                    }
                }
            }

            try (PreparedStatement getStockStmt = conn.prepareStatement("SELECT stock FROM products WHERE productId = ?");
                 PreparedStatement updateStockStmt = conn.prepareStatement(INCREMENT_PRODUCT_STOCK_QUERY)) {

                for (ProductOrder po : orderedProducts) {
                    int productId = po.getProductId();
                    int quantity = po.getQuantity();
                    int oldStock = 0;

                    getStockStmt.setInt(1, productId);
                    try (ResultSet rs = getStockStmt.executeQuery()) {
                        if (rs.next()) {
                            oldStock = rs.getInt("stock");
                        }
                    }

                    int newStock = oldStock + quantity;
                    stockChanges.add(new StockChangeInfo(productId, oldStock, newStock));

                    updateStockStmt.setInt(1, quantity);
                    updateStockStmt.setInt(2, productId);
                    updateStockStmt.addBatch();
                }

                updateStockStmt.executeBatch();
            }

            try (PreparedStatement deleteOrderProduct = conn.prepareStatement("DELETE FROM orderProduct WHERE orderId = ?")) {
                deleteOrderProduct.setInt(1, orderId);
                deleteOrderProduct.executeUpdate();
            }

            try (PreparedStatement deleteOrder = conn.prepareStatement("DELETE FROM orders WHERE orderId = ?")) {
                deleteOrder.setInt(1, orderId);
                deleteOrder.executeUpdate();
            }

            conn.commit();
            logger.info("{}Order deleted: orderId={}", logPrefix, orderId);
            return stockChanges;

        } catch (SQLException e) {
            logger.error("{}Failed to delete order: orderId={}", logPrefix, orderId, e);
            throw new RuntimeException("Failed to delete order: " + e.getMessage(), e);
        }
    }

    // Helper: Get product price
    private BigDecimal getProductPrice(int productId, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(GET_PRODUCT_PRICE_QUERY)) {
            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("price");
                } else {
                    throw new SQLException("Product with ID " + productId + " not found.");
                }
            }
        }
    }

    public void modifyOrder(int orderId, OrderRequest updatedRequest) {
        String logPrefix = LogUtils.prefix("OrderDAO");
        logger.info("{}Modifying order: orderId={}", logPrefix, orderId);

        try (Connection conn = DatabaseConnectorService.getInstance().getConnection()) {
            conn.setAutoCommit(false);

            List<ProductOrder> existingProducts = new ArrayList<>();
            try (PreparedStatement getItems = conn.prepareStatement(GET_ORDER_PRODUCTS_QUERY)) {
                getItems.setInt(1, orderId);
                try (ResultSet rs = getItems.executeQuery()) {
                    while (rs.next()) {
                        ProductOrder po = new ProductOrder();
                        po.setProductId(rs.getInt("productId"));
                        po.setQuantity(rs.getInt("quantity"));
                        existingProducts.add(po);
                    }
                }
            }

            try (PreparedStatement restoreStock = conn.prepareStatement(INCREMENT_PRODUCT_STOCK_QUERY)) {
                for (ProductOrder po : existingProducts) {
                    restoreStock.setInt(1, po.getQuantity());
                    restoreStock.setInt(2, po.getProductId());
                    restoreStock.addBatch();
                }
                restoreStock.executeBatch();
            }

            try (PreparedStatement deleteOrderProduct = conn.prepareStatement("DELETE FROM orderProduct WHERE orderId = ?")) {
                deleteOrderProduct.setInt(1, orderId);
                deleteOrderProduct.executeUpdate();
            }

            BigDecimal totalCost = BigDecimal.ZERO;
            try (PreparedStatement insertOrderProduct = conn.prepareStatement(INSERT_INTO_ORDERPRODUCT_QUERY);
                 PreparedStatement updateStock = conn.prepareStatement(UPDATE_PRODUCT_STOCK_QUERY)) {

                for (ProductOrder po : updatedRequest.getProducts()) {
                    BigDecimal price = getProductPrice(po.getProductId(), conn);
                    totalCost = totalCost.add(price.multiply(BigDecimal.valueOf(po.getQuantity())));

                    insertOrderProduct.setInt(1, orderId);
                    insertOrderProduct.setInt(2, po.getProductId());
                    insertOrderProduct.setInt(3, po.getQuantity());
                    insertOrderProduct.addBatch();

                    updateStock.setInt(1, po.getQuantity());
                    updateStock.setInt(2, po.getProductId());
                    updateStock.addBatch();
                }

                insertOrderProduct.executeBatch();
                updateStock.executeBatch();
            }

            try (PreparedStatement updateOrder = conn.prepareStatement("UPDATE orders SET customerId = ?, totalCost = ? WHERE orderId = ?")) {
                updateOrder.setInt(1, updatedRequest.getCustomerId());
                updateOrder.setBigDecimal(2, totalCost);
                updateOrder.setInt(3, orderId);
                updateOrder.executeUpdate();
            }

            conn.commit();
            logger.info("{}Order modified: orderId={}, new totalCost={}", logPrefix, orderId, totalCost);

        } catch (SQLException e) {
            logger.error("{}Failed to modify order: orderId={}", logPrefix, orderId, e);
            throw new RuntimeException("Failed to modify order: " + e.getMessage(), e);
        }
    }
    // ✅ Demo method with intermittent failure + retry logic
    public String demoRetryForIntermittentFailure() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(2))
                .retryExceptions(RuntimeException.class)
                .build();
        Retry retry = Retry.of("orderRetry", config);
        Supplier<String> riskySupplier = Retry.decorateSupplier(retry, () -> {
            if (Math.random() < 0.7) {
                throw new RuntimeException("Simulated intermittent failure.");
            }
            return "✅ Success after retry!";
        });
        return Try.ofSupplier(riskySupplier)
                .recover(throwable -> "❌ Fallback result: " + throwable.getMessage())
                .get();
    }
}