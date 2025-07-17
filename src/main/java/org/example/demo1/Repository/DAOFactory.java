package org.example.demo1.Repository;

public class DAOFactory {
    public static ProductDAO getProductDAO(){
        return new ProductDAO();
    }
    public static CustomerDAO getCustomerDAO(){
        return new CustomerDAO();
    }




}
