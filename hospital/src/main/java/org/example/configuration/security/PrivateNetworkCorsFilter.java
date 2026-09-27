package org.example.configuration.security;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class PrivateNetworkCorsFilter implements ContainerResponseFilter {
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        // Always allow private-network access from public HTTPS origins (Chrome PNA).
        responseContext.getHeaders().putSingle("Access-Control-Allow-Private-Network", "true");
    }
}