package org.example.demo1.Repository;

import org.example.demo1.Config.DatabaseConnectorService;
import org.example.demo1.model.Product;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    private static final String SELECT_ALL_PRODUCTS_QUERY = "SELECT * FROM Products";
    private static final String SELECT_PRODUCT_BY_ID_QUERY = "SELECT * FROM Products WHERE productId = ?";
    private static final String CREATE_PRODUCT_QUERY = "INSERT INTO Products(productId, name, price, stock) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_PRODUCT_QUERY = "UPDATE Products SET name = ?, price = ?, stock = ? WHERE productId = ?";
    private static final String DELETE_PRODUCT_BY_ID_QUERY = "DELETE FROM Products WHERE productId = ?";  // ✅ Added

    // 1. Get all products
    public List<Product> getAllProducts() throws SQLException {
        List<Product> products = new ArrayList<>();

        try (Connection conn = DatabaseConnectorService.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(SELECT_ALL_PRODUCTS_QUERY);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                Product product = new Product();
                product.setProductId(rs.getInt("productId"));
                product.setPrice(rs.getBigDecimal("price"));
                product.setName(rs.getString("name"));
                product.setStock(rs.getInt("stock"));
                products.add(product);
            }
        }

        return products;
    }

    // 2. Get product by ID
    public Product getProductById(int id) throws SQLException {
        try (Connection conn = DatabaseConnectorService.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(SELECT_PRODUCT_BY_ID_QUERY)) {

            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Product product = new Product();
                    product.setProductId(rs.getInt("productId"));
                    product.setPrice(rs.getBigDecimal("price"));
                    product.setName(rs.getString("name"));
                    product.setStock(rs.getInt("stock"));

                    return product;
                }
            }
        }
        return null;
    }

    // 3. Create new product
    public void createNewProduct(int id, String name, int stock, BigDecimal price) throws SQLException {
        try (Connection conn = DatabaseConnectorService.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(CREATE_PRODUCT_QUERY)) {

            statement.setInt(1, id);
            statement.setString(2, name);
            statement.setBigDecimal(3, price);
            statement.setInt(4, stock);
            statement.executeUpdate();
            System.out.println("Product inserted");
        }
    }

    // 4. Update existing product
    public void updateProduct(int id, String name, int stock, BigDecimal price) throws SQLException {
        try (Connection conn = DatabaseConnectorService.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(UPDATE_PRODUCT_QUERY)) {

            statement.setString(1, name);
            statement.setBigDecimal(2, price);
            statement.setInt(3, stock);
            statement.setInt(4, id);
            statement.executeUpdate();
            System.out.println("Product updated");
        }
    }

    // ✅ 5. Delete product by ID
    public void deleteProductById(int id) throws SQLException {
        try (Connection conn = DatabaseConnectorService.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(DELETE_PRODUCT_BY_ID_QUERY)) {

            statement.setInt(1, id);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Product with ID " + id + " deleted.");
            } else {
                System.out.println("No product found with ID " + id + " to delete.");
            }
        }
    }
}
