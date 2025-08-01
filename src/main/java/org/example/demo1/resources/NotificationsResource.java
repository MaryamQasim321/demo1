package org.example.demo1.resources;

import org.example.demo1.services.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/notifications")
public class NotificationsResource {

    private static final Logger logger = LoggerFactory.getLogger(NotificationsResource.class);
    public static NotificationService notificationService = new NotificationService();

    @POST
    @Path("/send")
    @Produces(MediaType.TEXT_PLAIN)
    public Response send(@QueryParam("to") String to,
                         @QueryParam("message") @DefaultValue("Default message") String message) {
        logger.info("Received request to send notification. To: {}, Message: {}", to, message);

        if (to == null || to.trim().isEmpty()) {
            logger.warn("Missing 'to' query parameter in notification request.");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Missing 'to' query parameter").build();
        }

        try {
            String result = notificationService.sendNotifications(to, message);
            logger.info("Notification sent successfully. Result: {}", result);
            return Response.ok(result).build();
        } catch (Exception e) {
            logger.error("Error while sending notification", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to send notification").build();
        }
    }
}
