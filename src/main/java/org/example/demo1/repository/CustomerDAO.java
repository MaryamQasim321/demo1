package org.example.demo1.repository;

import org.example.demo1.config.DatabaseConnectorService;
import org.example.demo1.logging.LogUtils;
import org.example.demo1.model.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {
    private static final Logger logger = LoggerFactory.getLogger(CustomerDAO.class);
    private static final String PREFIX = "CustomerDAO";

    private static final String SELECT_ALL_CUSTOMERS_QUERY = "SELECT * FROM Customers";
    private static final String SELECT_CUSTOMER_BY_ID_QUERY = "SELECT * FROM Customers WHERE customerId=?";
    private static final String CREATE_CUSTOMER_QUERY = "INSERT INTO Customers(customerId, name, contact, email) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_CUSTOMER_QUERY = "UPDATE Customers SET name=?, contact=?, email=? WHERE customerId=?";
    private static final String DELETE_CUSTOMER_QUERY = "DELETE FROM Customers WHERE customerId=?";

    // 1. Get all customers
    public List<Customer> getAllCustomers() throws SQLException {
        logger.info(LogUtils.info(LogUtils.prefix(PREFIX) + "Fetching all customers"));

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

            logger.info(LogUtils.success(LogUtils.prefix(PREFIX) + "Retrieved " + customers.size() + " customers"));

        } catch (SQLException e) {
            logger.error(LogUtils.error(LogUtils.prefix(PREFIX) + "Error fetching all customers", e), e);
            throw e;
        }

        return customers;
    }

    // 2. Get specific customer by ID
    public Customer getCustomerById(int id) throws SQLException {
        logger.info(LogUtils.info(LogUtils.prefix(PREFIX) + "Fetching customer with ID: " + id));

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

                    logger.info(LogUtils.success(LogUtils.prefix(PREFIX) + "Customer found: " + customer.getCustomerId()));
                    return customer;
                } else {
                    logger.warn(LogUtils.warn(LogUtils.prefix(PREFIX) + "No customer found with ID: " + id));
                }
            }

        } catch (SQLException e) {
            logger.error(LogUtils.error(LogUtils.prefix(PREFIX) + "Error fetching customer with ID: " + id, e), e);
            throw e;
        }

        return null;
    }

    // 3. Create new customer
    public void createNewCustomer(int id, String name, String contact, String email) throws SQLException {
        logger.info(LogUtils.info(LogUtils.prefix(PREFIX) + "Creating customer: id=" + id + ", name=" + name));

        try (Connection conn = DatabaseConnectorService.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(CREATE_CUSTOMER_QUERY)) {

            statement.setInt(1, id);
            statement.setString(2, name);
            statement.setString(3, contact);
            statement.setString(4, email);
            statement.executeUpdate();

            logger.info(LogUtils.success(LogUtils.prefix(PREFIX) + "Customer inserted: id=" + id));

        } catch (SQLException e) {
            logger.error(LogUtils.error(LogUtils.prefix(PREFIX) + "Error inserting customer: id=" + id, e), e);
            throw e;
        }
    }

    // 4. Update existing customer
    public void updateCustomer(int id, String name, String contact, String email) throws SQLException {
        logger.info(LogUtils.info(LogUtils.prefix(PREFIX) + "Updating customer: id=" + id));

        try (Connection conn = DatabaseConnectorService.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(UPDATE_CUSTOMER_QUERY)) {

            statement.setString(1, name);
            statement.setString(2, contact);
            statement.setString(3, email);
            statement.setInt(4, id);
            statement.executeUpdate();

            logger.info(LogUtils.success(LogUtils.prefix(PREFIX) + "Customer updated: id=" + id));

        } catch (SQLException e) {
            logger.error(LogUtils.error(LogUtils.prefix(PREFIX) + "Error updating customer: id=" + id, e), e);
            throw e;
        }
    }

    // 5. Remove customer
    public void deleteCustomer(int id) throws SQLException {
        logger.info(LogUtils.info(LogUtils.prefix(PREFIX) + "Deleting customer: id=" + id));

        try (Connection conn = DatabaseConnectorService.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(DELETE_CUSTOMER_QUERY)) {

            statement.setInt(1, id);
            statement.executeUpdate();

            logger.info(LogUtils.success(LogUtils.prefix(PREFIX) + "Customer deleted: id=" + id));

        } catch (SQLException e) {
            logger.error(LogUtils.error(LogUtils.prefix(PREFIX) + "Error deleting customer: id=" + id, e), e);
            throw e;
        }
    }
}
