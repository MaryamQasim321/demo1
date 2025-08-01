package org.example.demo1.resources;

import org.example.demo1.logging.LogUtils;
import org.example.demo1.repository.DAOFactory;
import org.example.demo1.repository.CustomerDAO;
import org.example.demo1.model.Customer;
import org.example.demo1.security.JWTRequired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.sql.SQLException;
import java.util.List;

@Path("/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerResource {
    private final CustomerDAO customerDAO = DAOFactory.getCustomerDAO();
    private static final Logger logger = LoggerFactory.getLogger(CustomerResource.class);

    @GET
    @JWTRequired
    public Response getAllCustomers() {
        MDC.put("operation", "getAllCustomers");
        try {
            List<Customer> customers = customerDAO.getAllCustomers();
            logger.info(LogUtils.success("Retrieved all customers"));
            return Response.ok(customers).build();
        } catch (SQLException e) {
            logger.error(LogUtils.error("Failed to retrieve all customers", e), e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Database error").build();
        } finally {
            MDC.clear();
        }
    }

    @GET
    @Path("/{id}")
    @JWTRequired
    public Response getCustomerById(@PathParam("id") int id) {
        MDC.put("operation", "getCustomerById");
        MDC.put("customerId", String.valueOf(id));
        try {
            Customer customer = customerDAO.getCustomerById(id);
            if (customer != null) {
                logger.info(LogUtils.success("Retrieved customer with id " + id));
                return Response.ok(customer).build();
            } else {
                logger.warn(LogUtils.warn("Customer not found with id " + id));
                return Response.status(Status.NOT_FOUND).entity("Customer not found").build();
            }
        } catch (SQLException e) {
            logger.error(LogUtils.error("Error retrieving customer by ID", e), e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Database error").build();
        } finally {
            MDC.clear();
        }
    }

    @POST
    @JWTRequired
    public Response createNewCustomer(Customer customer) {
        MDC.put("operation", "createNewCustomer");
        MDC.put("customerName", customer.getName());
        try {
            customerDAO.createNewCustomer(
                    customer.getCustomerId(),
                    customer.getName(),
                    customer.getContact(),
                    customer.getEmail()
            );
            logger.info(LogUtils.success("Customer created successfully"));
            return Response.ok("Customer created successfully.").build();
        } catch (SQLException e) {
            logger.error(LogUtils.error("Failed to create customer", e), e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Database error").build();
        } finally {
            MDC.clear();
        }
    }

    @PUT
    @Path("/{id}")
    @JWTRequired
    public Response updateCustomer(@PathParam("id") int id, Customer customer) {
        MDC.put("operation", "updateCustomer");
        MDC.put("customerId", String.valueOf(id));
        try {
            customerDAO.updateCustomer(
                    id,
                    customer.getName(),
                    customer.getContact(),
                    customer.getEmail()
            );
            logger.info(LogUtils.success("Customer updated successfully"));
            return Response.ok("Customer updated successfully.").build();
        } catch (SQLException e) {
            logger.error(LogUtils.error("Failed to update customer", e), e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Database error").build();
        } finally {
            MDC.clear();
        }
    }

    @DELETE
    @Path("/{id}")
    @JWTRequired
    public Response deleteCustomer(@PathParam("id") int id) {
        MDC.put("operation", "deleteCustomer");
        MDC.put("customerId", String.valueOf(id));
        try {
            customerDAO.deleteCustomer(id);
            logger.info(LogUtils.success("Customer deleted successfully"));
            return Response.ok("Customer deleted successfully.").build();
        } catch (SQLException e) {
            logger.error(LogUtils.error("Failed to delete customer", e), e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Database error").build();
        } finally {
            MDC.clear();
        }
    }
}
