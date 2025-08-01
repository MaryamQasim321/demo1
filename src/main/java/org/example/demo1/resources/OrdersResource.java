package org.example.demo1.resources;

import org.example.demo1.config.DatabaseConnectorService;
import org.example.demo1.logging.LogUtils;
import org.example.demo1.model.Order;
import org.example.demo1.model.OrderRequest;
import org.example.demo1.model.StockChangeInfo;
import org.example.demo1.repository.DAOFactory;
import org.example.demo1.repository.OrderDAO;
import org.example.demo1.security.JWTRequired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrdersResource {

    private static final Logger logger = LoggerFactory.getLogger(OrdersResource.class);
    private final OrderDAO orderDAO = DAOFactory.getOrderDAO();

    @GET
    @JWTRequired
    public Response getAllOrders() {
        try {
            logger.info(LogUtils.success("Fetching all orders"));
            List<Order> orders = orderDAO.getAllOrders();
            return Response.ok(orders).build();
        } catch (SQLException e) {
            logger.error(LogUtils.error("Error fetching all orders", e));
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Database error").build();
        }
    }

    @GET
    @Path("/{id}")
    @JWTRequired
    public Response getOrderById(@PathParam("id") int id) {
        try {
            logger.info(LogUtils.success("Fetching order by ID: " + id));
            Order order = orderDAO.getOrderById(id);
            if (order != null) {
                logger.info(LogUtils.success("Order found: ID = " + id));
                return Response.ok(order).build();
            } else {
                logger.warn(LogUtils.warn("Order not found: ID = " + id));
                return Response.status(Status.NOT_FOUND).entity("Order not found").build();
            }
        } catch (SQLException e) {
            logger.error(LogUtils.error("Error fetching order by ID: " + id, e));
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Database error").build();
        }
    }
    @POST
    @Path("/create")
    @JWTRequired
    public Response createOrder(OrderRequest request) {
        try {
            int newOrderId = generateOrderId();
            logger.info(LogUtils.success("Creating order with ID: " + newOrderId));

            List<StockChangeInfo> stockChanges = orderDAO.createOrder(newOrderId, request);
            logger.info(LogUtils.success("Order created successfully: ID = " + newOrderId));

            return Response.status(Response.Status.CREATED)
                    .entity(stockChanges)
                    .build();

        } catch (Exception e) {
            logger.error(LogUtils.error("Failed to create order", e));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to create order: " + e.getMessage())
                    .build();
        }
    }
    @DELETE
    @Path("/{id}")
    @JWTRequired
    public Response deleteOrder(@PathParam("id") int id) {
        try {
            logger.info(LogUtils.success("Deleting order ID: " + id));
            List<StockChangeInfo> stockChanges = orderDAO.deleteOrderById(id);
            logger.info(LogUtils.success("Order deleted successfully: ID = " + id));
            return Response.ok(stockChanges).build();
        } catch (RuntimeException e) {
            logger.error(LogUtils.error("Failed to delete order ID: " + id, e));
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to delete order").build();
        }
    }
    @GET
    @Path("/sample-order-check")
    @JWTRequired
    public Response testOrderDAO() {
        try {
            boolean hasOrders = orderDAO.getAllOrders().size() > 0;
            logger.info(LogUtils.success("OrderDAO connectivity check passed"));
            return Response.ok("OrderDAO connected. Orders exist: " + hasOrders).build();
        } catch (SQLException e) {
            logger.error(LogUtils.error("Error during order DAO check", e));
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error accessing orders").build();
        }
    }
    @PUT
    @Path("/{id}")
    @JWTRequired
    public Response modifyOrder(@PathParam("id") int orderId, OrderRequest updatedRequest) {
        try {
            logger.info(LogUtils.success("Modifying order ID: " + orderId));
            Order existingOrder = orderDAO.getOrderById(orderId);
            if (existingOrder == null) {
                logger.warn(LogUtils.warn("Order not found for modification: ID = " + orderId));
                return Response.status(Status.NOT_FOUND).entity("Order not found").build();
            }
            orderDAO.modifyOrder(orderId, updatedRequest);
            logger.info(LogUtils.success("Order modified successfully: ID = " + orderId));
            return Response.ok("Order modified successfully.").build();

        } catch (RuntimeException | SQLException e) {
            logger.error(LogUtils.error("Failed to modify order ID: " + orderId, e));
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to modify order").build();
        }
    }
    private int generateOrderId() throws SQLException {
        String query = "SELECT MAX(orderId) FROM orders";
        try (Connection conn = DatabaseConnectorService.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int maxId = rs.getInt(1);
                return maxId + 1;
            } else {
                return 1;
            }
        }
    }
    @GET
    @Path("/retry-demo")
    @JWTRequired
    public Response retryDemo() {
        logger.info(LogUtils.success("Starting retry demo..."));
        String result = orderDAO.demoRetryForIntermittentFailure();
        return Response.ok(result).build();
    }
}