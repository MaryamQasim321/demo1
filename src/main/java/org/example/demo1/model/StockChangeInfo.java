package org.example.demo1.model;

public class StockChangeInfo {
    private int productId;
    private int oldStock;
    private int newStock;

    public StockChangeInfo(int productId, int oldStock, int newStock) {
        this.productId = productId;
        this.oldStock = oldStock;
        this.newStock = newStock;
    }

    // Getters
    public int getProductId() { return productId; }
    public int getOldStock() { return oldStock; }
    public int getNewStock() { return newStock; }

    @Override
    public String toString() {
        return "Product ID: " + productId + ", Old Stock: " + oldStock + ", New Stock: " + newStock;
    }
}
