package org.example.demo1.Tests;

import org.example.demo1.repository.ProductDAO;
import org.example.demo1.model.Product;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductResourceTests {

    private static ProductDAO productDAO;

    @BeforeAll
    public static void setup() throws SQLException {
        productDAO = new ProductDAO();
        // Clean up before test
        Product existing = productDAO.getProductById(109);
        if (existing != null) {
            productDAO.deleteProductById(109);
        }
    }

    @Test
    @Order(1)
    public void testCreateProduct() {
        assertDoesNotThrow(() -> productDAO.createNewProduct(
                109,
                "Water bottle",
                80,
                BigDecimal.valueOf(90.00)
        ));
    }

    @Test
    @Order(2)
    public void GetAllProductsTest() throws SQLException {
        List<Product> products = productDAO.getAllProducts();
        assertNotNull(products, "List is null (query failed)");
        System.out.println("Number of products found: " + products.size());
        assertTrue(products.size() > 0, "No products found in DB");
    }

    @Test
    @Order(3)
    public void GetAllProductByIdTest() throws SQLException {
        Product product = productDAO.getProductById(109);
        assertNotNull(product);
        assertEquals(109, product.getProductId());
        assertEquals("Water bottle", product.getName());
        assertEquals(80, product.getStock());
        assertEquals(0, product.getPrice().compareTo(BigDecimal.valueOf(90.00)));
    }

    @Test
    @Order(4)
    public void testUpdateProduct() throws SQLException {
        // Update the product
        assertDoesNotThrow(() -> productDAO.updateProduct(109, "Updated Bottle", 100, BigDecimal.valueOf(120.00)));

        // Fetch updated product
        Product updated = productDAO.getProductById(109);
        assertNotNull(updated);
        assertEquals("Updated Bottle", updated.getName());
        assertEquals(100, updated.getStock());
        assertEquals(0, updated.getPrice().compareTo(BigDecimal.valueOf(120.00)));
    }

    @Test
    @Order(5)
    public void deleteProductByIdTest() throws SQLException {
        // Act
        assertDoesNotThrow(() -> productDAO.deleteProductById(109));

        // Assert
        Product deleted = productDAO.getProductById(109);
        assertNull(deleted, "Product should be null after deletion");
    }

}
