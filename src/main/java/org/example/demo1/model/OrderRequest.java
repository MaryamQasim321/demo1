package org.example.demo1.model;

import java.util.List;

public class OrderRequest {

    private int customerId;
    private List<ProductOrder> products;


    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public List<ProductOrder> getProducts() {
        return products;
    }

    public void setProducts(List<ProductOrder> products) {
        this.products = products;
    }
}
