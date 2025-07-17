package org.example.demo1.Repository;

import org.example.demo1.Config.DatabaseConnectorService;
import org.example.demo1.model.*;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    //methods:
    //1. get all orders
        public static final String VIEW_ALL_ORDERS_QUERY="SELECT * FROM orders";
        //2. get order by id
        public static final String VIEW_ORDER_BY_ID_QUERY="SELECT * FROM orders where orderId=?";
        //3. add order
        public static final String ADD_ORDER_QUERY="INSERT INTO orders(customerId, totalCost) values (?, ?)";
    public static final String GET_PRODUCT_PRICE_QUERY="SELECT price FROM products where productId=?";
    public static final String INSERT_INTO_ORDERPRODUCT_QUERY="INSERT into orderProduct(orderId, productId, quantity) values (?,?,?) ";



    //1. get all orders

    // 1. Get all customers
    public List<Order> getAllOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();

        try (Connection conn = DatabaseConnectorService.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(VIEW_ALL_ORDERS_QUERY);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                Order order = new Order();
                order.setCustomerId(rs.getInt("customerId"));
                order.setOrderId(rs.getInt("orderId"));
                orders.add(order);
            }
        }

        return orders;
    }

    //2. get order by id
    public Order getOrderById(int id) throws SQLException {
        try (Connection conn = DatabaseConnectorService.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(VIEW_ORDER_BY_ID_QUERY)) {

            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order();
                    order.setCustomerId(rs.getInt("customerId"));
                    order.setOrderId(rs.getInt("orderId"));

                    return order;
                }
            }
        }
        return null;
    }



    public void createOrder(OrderRequest request) {
        BigDecimal totalPrice = BigDecimal.ZERO;

        try (Connection conn = DatabaseConnectorService.getInstance().getConnection()) {
            conn.setAutoCommit(false);

            // Calculate total price
            for (ProductOrder po : request.getProducts()) {
                BigDecimal itemTotal = getProductPrice(po.getProductId(), conn).multiply(BigDecimal.valueOf(po.getQuantity()));
                totalPrice = totalPrice.add(itemTotal);
            }

            // Insert into orders table
            int orderId;
            try (PreparedStatement stmt = conn.prepareStatement(ADD_ORDER_QUERY, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, request.getCustomerId());
                stmt.setBigDecimal(2, totalPrice);
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        orderId = rs.getInt(1);
                    } else {
                        conn.rollback();
                        throw new SQLException("Failed to get orderId.");
                    }
                }
            }

            // Insert into orderProduct table
            try (PreparedStatement opStmt = conn.prepareStatement(INSERT_INTO_ORDERPRODUCT_QUERY)) {
                for (ProductOrder po : request.getProducts()) {
                    opStmt.setInt(1, orderId);
                    opStmt.setInt(2, po.getProductId());
                    opStmt.setInt(3, po.getQuantity());
                    opStmt.addBatch();
                }
                opStmt.executeBatch();
            }

            conn.commit();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create order: " + e.getMessage(), e);
        }
    }


    //helper method to get product price
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

}


