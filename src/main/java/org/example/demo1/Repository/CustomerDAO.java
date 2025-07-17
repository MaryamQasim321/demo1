package org.example.demo1.Repository;

import org.example.demo1.Config.DatabaseConnectorService;
import org.example.demo1.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {
    // APIs needed:
    // 1. Get all customers
    // 2. Get specific customer by ID
    // 3. Create new customer
    // 4. Update existing customer
    // 5. Remove customer

    private static final String SELECT_ALL_CUSTOMERS_QUERY = "SELECT * FROM Customers";
    private static final String SELECT_CUSTOMER_BY_ID_QUERY = "SELECT * FROM Customers WHERE customerId=?";
    private static final String CREATE_CUSTOMER_QUERY = "INSERT INTO Customers(customerId, name, contact, email) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_CUSTOMER_QUERY = "UPDATE Customers SET name=?, contact=?, email=? WHERE customerId=?";
    private static final String DELETE_CUSTOMER_QUERY = "DELETE FROM Customers WHERE customerId=?";

    // 1. Get all customers
    public List<Customer> getAllCustomers() throws SQLException {
        List<Customer> customers = new ArrayList<>();

        try (Connection conn = DatabaseConnectorService.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(SELECT_ALL_CUSTOMERS_QUERY);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                Customer customer = new Customer();
                customer.setCustomerId(rs.getInt("customerId"));
                customer.setName(rs.getString("name"));
                customer.setContact(rs.getString("contact"));
                customer.setEmail(rs.getString("email"));
                customers.add(customer);
            }
        }

        return customers;
    }

    // 2. Get specific customer by ID
    public Customer getCustomerById(int id) throws SQLException {
        try (Connection conn = DatabaseConnectorService.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(SELECT_CUSTOMER_BY_ID_QUERY)) {

            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Customer customer = new Customer();
                    customer.setCustomerId(rs.getInt("customerId"));
                    customer.setName(rs.getString("name"));
                    customer.setContact(rs.getString("contact"));
                    customer.setEmail(rs.getString("email"));
                    return customer;
                }
            }
        }
        return null;
    }

    // 3. Create new customer
    public void createNewCustomer(int id, String name, String contact, String email) throws SQLException {
        try (Connection conn = DatabaseConnectorService.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(CREATE_CUSTOMER_QUERY)) {

            statement.setInt(1, id);
            statement.setString(2, name);
            statement.setString(3, contact);
            statement.setString(4, email);
            statement.executeUpdate();
            System.out.println("Customer inserted");
        }
    }

    // 4. Update existing customer
    public void updateCustomer(int id, String name, String contact, String email) throws SQLException {
        try (Connection conn = DatabaseConnectorService.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(UPDATE_CUSTOMER_QUERY)) {

            statement.setString(1, name);
            statement.setString(2, contact);
            statement.setString(3, email);
            statement.setInt(4, id);
            statement.executeUpdate();
            System.out.println("Customer updated");
        }
    }

    // 5. Remove customer
    public void deleteCustomer(int id) throws SQLException {
        try (Connection conn = DatabaseConnectorService.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(DELETE_CUSTOMER_QUERY)) {

            statement.setInt(1, id);
            statement.executeUpdate();
            System.out.println("Customer deleted");
        }
    }
}
