package org.example.demo1.Tests;

import org.example.demo1.repository.CustomerDAO;
import org.example.demo1.model.Customer;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CustomerResourceTests {

    private static CustomerDAO customerDAO;

    @BeforeAll
    public static void setup() {
        customerDAO = new CustomerDAO();
    }

    @Test
    @Order(1)
    public void testCreateCustomer() {
        assertDoesNotThrow(() -> customerDAO.createNewCustomer(
                101, "Test User", "03331234567", "testuser@example.com"));
    }

    @Test
    @Order(2)
    public void testGetCustomerById() throws SQLException {
        Customer customer = customerDAO.getCustomerById(101);
        assertNotNull(customer);
        assertEquals(101, customer.getCustomerId());
        assertEquals("Test User", customer.getName());
        assertEquals("03331234567", customer.getContact());
        assertEquals("testuser@example.com", customer.getEmail());
    }

    @Test
    @Order(3)
    public void testUpdateCustomer() throws SQLException {
        customerDAO.updateCustomer(101, "Updated User", "03001234567", "updated@example.com");
        Customer updatedCustomer = customerDAO.getCustomerById(101);
        assertEquals("Updated User", updatedCustomer.getName());
        assertEquals("03001234567", updatedCustomer.getContact());
        assertEquals("updated@example.com", updatedCustomer.getEmail());
    }

    @Test
    @Order(4)
    public void testGetAllCustomers() throws SQLException {
        List<Customer> customers = customerDAO.getAllCustomers();
        assertNotNull(customers, "Returned list is null – check method logic.");

        System.out.println("Number of customers found: " + customers.size());

        assertTrue(customers.size() > 0, "Database might be empty or fetching failed.");
    }


    @Test
    @Order(5)
    public void testDeleteCustomer() throws SQLException {
        customerDAO.deleteCustomer(101);
        Customer deletedCustomer = customerDAO.getCustomerById(101);
        assertNull(deletedCustomer);
    }
}
