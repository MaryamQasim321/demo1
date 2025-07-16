package org.example.demo1;

public class DAOFactory {
    public static ProductDAO getProductDAO(){
        return new ProductDAO();
    }
}
