package org.example.demo1.resources;

import org.example.demo1.config.DatabaseConnectorService;
import org.example.demo1.logging.LogUtils;
import org.example.demo1.repository.AdminDAO;
import org.example.demo1.security.JWTUtility;
import org.example.demo1.model.Admin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/auth")
public class AuthResource {
    private static final Logger logger = LoggerFactory.getLogger(AuthResource.class);

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(Admin credentials) {
        MDC.put("operation", "login");
        MDC.put("username", credentials.getUsername());

        try {
            logger.info(LogUtils.info("Login attempt started"));

            AdminDAO dao = new AdminDAO(DatabaseConnectorService.getInstance().getConnection());
            Admin admin = dao.findByUsername(credentials.getUsername());

            if (admin != null && admin.getPassword().equals(credentials.getPassword())) {
                String token = JWTUtility.generateToken(admin.getUsername());

                logger.info(LogUtils.success("Login successful for user: " + admin.getUsername()));
                return Response.ok("{\"token\":\"" + token + "\"}").build();
            } else {
                logger.warn(LogUtils.warn("Invalid login attempt for user: " + credentials.getUsername()));
                return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build();
            }
        } catch (Exception e) {
            logger.error(LogUtils.error("Login error", e), e);
            return Response.serverError().entity("Server error").build();
        } finally {
            MDC.clear();
        }
    }
}
