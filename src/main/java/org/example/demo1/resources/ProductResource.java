package org.example.demo1.resources;
import org.example.demo1.logging.LogUtils;
import org.example.demo1.repository.DAOFactory;
import org.example.demo1.repository.ProductDAO;
import org.example.demo1.model.Product;
import org.example.demo1.security.JWTRequired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.sql.SQLException;
import java.util.List;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {
    private final ProductDAO productDAO = DAOFactory.getProductDAO();
    private static final Logger logger = LoggerFactory.getLogger(ProductResource.class);



    @GET
    @JWTRequired
    public Response getAllProducts() {
        MDC.put("operation", "getAllProducts");
        try {
            List<Product> products = productDAO.getAllProducts();
            logger.info(LogUtils.success("Retrieved all products"));
            return Response.ok(products).build();
        } catch (SQLException e) {
            logger.error(LogUtils.error("Failed to retrieve all products", e), e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Database error occurred.").build();
        } finally {
            MDC.clear();
        }
    }

    @GET
    @Path("/{id}")
    @JWTRequired
    public Response getProductById(@PathParam("id") int id) {

        MDC.put("operation", "getProductById");
        MDC.put("productId", String.valueOf(id));
        logger.info("MDC Test - productId={}, operation={}, userId={}",
                MDC.get("productId"), MDC.get("operation"), MDC.get("userId"));

        try {
            Product product = productDAO.getProductById(id);
            if (product != null) {
                logger.info(LogUtils.success("Retrieved product with id " + id));
                return Response.ok(product).build();
            } else {
                logger.warn(LogUtils.warn("Product not found with id " + id));
                return Response.status(Status.NOT_FOUND).entity("Product not found").build();
            }
        } catch (SQLException e) {
            logger.error(LogUtils.error("Error retrieving product by ID", e), e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Database error occurred.").build();
        } finally {
            MDC.clear();
        }
    }

    @POST
    @JWTRequired
    public Response createNewProduct(Product product) {
        MDC.put("productId", String.valueOf(product.getProductId()));

        MDC.put("operation", "createNewProduct");
        MDC.put("productName", product.getName());
        try {
            productDAO.createNewProduct(
                    product.getProductId(),
                    product.getName(),
                    product.getStock(),
                    product.getPrice()
            );
            logger.info(LogUtils.success("Product created successfully"));
            return Response.status(Status.CREATED).entity("Product created successfully.").build();
        } catch (SQLException e) {
            logger.error(LogUtils.error("Failed to create product", e), e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Database error occurred.").build();
        } finally {
            MDC.clear();
        }
    }

    @PUT
    @Path("/{id}")
    @JWTRequired
    public Response updateProduct(@PathParam("id") int id, Product product) {
        MDC.put("operation", "updateProduct");
        MDC.put("productId", String.valueOf(id));
        try {
            productDAO.updateProduct(
                    id,
                    product.getName(),
                    product.getStock(),
                    product.getPrice()
            );
            logger.info(LogUtils.success("Product updated successfully"));
            return Response.ok("Product updated successfully.").build();
        } catch (SQLException e) {
            logger.error(LogUtils.error("Failed to update product", e), e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Database error occurred.").build();
        } finally {
            MDC.clear();
        }
    }

    @DELETE
    @Path("/{id}")
    @JWTRequired
    public Response deleteProduct(@PathParam("id") int id) {
        MDC.put("operation", "deleteProduct");
        MDC.put("productId", String.valueOf(id));
        try {
            boolean deleted = productDAO.deleteProductById(id);
            if (deleted) {
                logger.info(LogUtils.success("Product deleted successfully"));
                return Response.ok("Product deleted successfully.").build();
            } else {
                logger.warn(LogUtils.warn("Product not found to delete"));
                return Response.status(Status.NOT_FOUND).entity("Product not found").build();
            }
        } catch (SQLException e) {
            logger.error(LogUtils.error("Failed to delete product", e), e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Database error occurred.").build();
        } finally {
            MDC.clear();
        }
    }
}