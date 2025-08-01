package org.example.demo1.repository;

public class DAOFactory {
    public static ProductDAO getProductDAO() {

        return new ProductDAO();
    }

    public static CustomerDAO getCustomerDAO() {
        return new CustomerDAO();
    }

    public static OrderDAO getOrderDAO() {
        return new OrderDAO();
    }



}
