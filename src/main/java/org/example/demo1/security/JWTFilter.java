package org.example.demo1.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.example.demo1.logging.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@JWTRequired
@Priority(Priorities.AUTHENTICATION)
public class JWTFilter implements ContainerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JWTFilter.class);
    private static final String SECRET = System.getenv("JWT_SECRET");

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String authHeader = requestContext.getHeaderString("Authorization");

        MDC.put("operation", "JWTFilter");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn(LogUtils.warn("Missing or invalid Authorization header"));
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity("Authorization header must be provided").build());
            MDC.clear();
            return;
        }

        String token = authHeader.substring("Bearer".length()).trim();

        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            MDC.put("user", username);
            logger.info(LogUtils.success("JWT validated for user: " + username));

            // You could also store claims in requestContext for downstream use
            requestContext.setProperty("username", username);

        } catch (Exception e) {
            logger.error(LogUtils.error("Invalid or expired token", e), e);
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity("Invalid or expired token").build());
        } finally {
            MDC.clear();
        }
    }
}
