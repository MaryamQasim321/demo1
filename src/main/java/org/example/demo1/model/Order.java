package org.example.demo1.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

public class Order {
    private int orderId;
    private int customerId;
    private float totalCost;

    @JsonIgnore // 👈 hides raw orderDate from JSON
    private Timestamp orderDate;

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public float getTotalCost() { return totalCost; }
    public void setTotalCost(float totalCost) { this.totalCost = totalCost; }

    public Timestamp getOrderDate() { return orderDate; }
    public void setOrderDate(Timestamp orderDate) { this.orderDate = orderDate; }

    // ✅ Only this will be returned in JSON
    @JsonProperty("orderDate")
    public String getFormattedOrderDate() {
        if (orderDate == null) return null;
        return orderDate.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
