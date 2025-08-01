package org.example.demo1.Tests;

import org.example.demo1.repository.OrderDAO;
import org.example.demo1.model.Order;
import org.example.demo1.model.OrderRequest;
import org.example.demo1.model.ProductOrder;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

public class OrderResourceTests {

    private static OrderDAO orderDAO;
    private static int createdOrderId;

    private static final int TEST_ORDER_ID = 5000;

    @BeforeAll
    public static void setup() {
        orderDAO = new OrderDAO();
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    public void testCreateOrder() {
        // 🔄 Delete existing test order (if any)
        try {
            orderDAO.deleteOrderById(TEST_ORDER_ID);
        } catch (Exception e) {
            System.out.println("No existing test order found or failed to delete: " + e.getMessage());
        }

        // 🛒 Build test order
        OrderRequest request = new OrderRequest();
        request.setCustomerId(11); // Make sure customer with ID 11 exists

        List<ProductOrder> products = new ArrayList<>();
        ProductOrder productOrder1 = new ProductOrder();
        ProductOrder productOrder2 = new ProductOrder();

        productOrder1.setProductId(201);
        productOrder1.setQuantity(2);

        productOrder2.setProductId(202);
        productOrder2.setQuantity(1);

        products.add(productOrder1);
        products.add(productOrder2);

        request.setProducts(products);

        // ✅ Now create the order
        assertDoesNotThrow(() -> orderDAO.createOrder(TEST_ORDER_ID, request), "Order creation failed");
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    public void testGetAllOrders() throws SQLException {
        List<Order> orders = orderDAO.getAllOrders();
        assertNotNull(orders);
        assertTrue(orders.size() > 0, "No orders found in database");
        createdOrderId = orders.get(orders.size() - 1).getOrderId(); // Save for next test
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    public void testGetOrderById() throws SQLException {
        Order order = orderDAO.getOrderById(createdOrderId);
        assertNotNull(order, "Order with ID " + createdOrderId + " not found");
        assertEquals(createdOrderId, order.getOrderId());
    }

}






