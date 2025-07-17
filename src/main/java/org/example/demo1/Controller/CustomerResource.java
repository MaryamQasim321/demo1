package org.example.demo1.Controller;

import org.example.demo1.Repository.DAOFactory;
import org.example.demo1.Repository.CustomerDAO;
import org.example.demo1.model.Customer;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;

@Path("/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerResource {
    private final CustomerDAO customerDAO = DAOFactory.getCustomerDAO();

    // Get all customers
    @GET
    public Response getAllCustomers() {
        try {
            List<Customer> customers = customerDAO.getAllCustomers();
            return Response.ok(customers).build();
        } catch (SQLException e) {
            return Response.status(500).entity("Database error").build();
        }
    }

    // Get specific customer by ID
    @GET
    @Path("/{id}")
    public Response getCustomerById(@PathParam("id") int id) {
        try {
            Customer customer = customerDAO.getCustomerById(id);
            if (customer != null) {
                return Response.ok(customer).build();
            } else {
                return Response.status(404).entity("Customer not found").build();
            }
        } catch (SQLException e) {
            return Response.status(500).entity("Database error").build();
        }
    }

    // Create new customer
    @POST
    public Response createNewCustomer(Customer customer) {
        try {
            customerDAO.createNewCustomer(
                    customer.getCustomerId(),
                    customer.getName(),
                    customer.getContact(),
                    customer.getEmail()
            );
            return Response.ok("Customer created successfully.").build();
        } catch (RuntimeException e) {
            return Response.status(500).entity("Database error").build();
        } catch (SQLException e) {
            return Response.status(500).entity("Database error").build();
        }
    }

    // Update existing customer
    @PUT
    @Path("/{id}")
    public Response updateCustomer(@PathParam("id") int id, Customer customer) {
        try {
            customerDAO.updateCustomer(
                    id,
                    customer.getName(),
                    customer.getContact(),
                    customer.getEmail()
            );
            return Response.ok("Customer updated successfully.").build();
        } catch (SQLException e) {
            return Response.status(500).entity("Database error").build();
        }
    }
}
