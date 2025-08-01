package org.example.demo1.logging;

import org.slf4j.MDC;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.util.UUID;

@Provider
public class MDCLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);

        String userId = requestContext.getHeaderString("X-User-Id");
        if (userId != null) {
            MDC.put("userId", userId);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        MDC.clear(); // Clear to avoid memory leaks
    }
}
