package org.example.demo1.Controller;

import org.example.demo1.Repository.DAOFactory;
import org.example.demo1.Repository.ProductDAO;
import org.example.demo1.model.Product;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;
@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {
    private final ProductDAO productDAO = DAOFactory.getProductDAO();



    public ProductResource() {
        System.out.println("ProductResource initialized!");
    }


    //apis:
    //get api to get all products
    @GET
    public Response getAllProducts(){
            try{
                List<Product> products=productDAO.getAllProducts();
                return Response.ok(products).build();

            } catch (SQLException e) {
                return Response.status(500).entity("Database error").build();
            }
    }

    @GET
    @Path("/{id}")
    public Response getProductById(@PathParam("id") int id){
        try{
           Product product=productDAO.getProductById(id);
           if(product!=null) {
               return Response.ok(product).build();
           }
           else{
               return Response.status(404).entity("Product not found").build();
           }
        } catch (SQLException e) {
            return Response.status(500).entity("Database error").build();
        }
    }

    @POST
    public Response createNewProduct(Product product) {
        try {
            productDAO.createNewProduct(
                    product.getProductId(),
                    product.getName(),
                    product.getStock(),
                    product.getPrice()
            );
            return Response.ok("Product created successfully.").build();
        }
        catch (RuntimeException e){
            return Response.status(500).entity("Database error").build();
        } catch (SQLException e) {
            return Response.status(500).entity("Database error").build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateProduct(@PathParam("id") int id, Product product){

        try{
            productDAO.updateProduct(
                   id,
                    product.getName(),
                    product.getStock(),
                    product.getPrice()
            );
            return Response.ok("Product updated successfully.").build();

        } catch (SQLException e) {
            return Response.status(500).entity("Database error").build();
        }


    }






}
