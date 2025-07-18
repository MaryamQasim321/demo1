package org.example.demo1.Controller;
import org.example.demo1.Config.DatabaseConnectorService;
import org.example.demo1.Repository.AdminDAO;
import org.example.demo1.Security.JWTUtility;
import org.example.demo1.model.Admin;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
@Path("/auth")
public class AuthResource {
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(Admin credentials) {
        try {
            AdminDAO dao = new AdminDAO(DatabaseConnectorService.getInstance().getConnection());
            Admin admin = dao.findByUsername(credentials.getUsername());
            if (admin != null && admin.getPassword().equals(credentials.getPassword())) {
                String token = JWTUtility.generateToken(admin.getUsername());
                return Response.ok("{\"token\":\"" + token + "\"}").build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build();
            }
        } catch (Exception e) {
            return Response.serverError().entity("Server error").build();
        }
    }
}