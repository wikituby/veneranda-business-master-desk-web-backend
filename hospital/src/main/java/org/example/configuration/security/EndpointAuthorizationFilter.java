package org.example.configuration.security;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.example.configuration.handler.ResponseMessage;
import org.example.user.domains.User;
import org.example.user.domains.repositories.UserRepository;

import java.util.Optional;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class EndpointAuthorizationFilter implements ContainerRequestFilter {

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    UserRepository userRepository;

    @Inject
    EndpointAccessService endpointAccessService;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            return;
        }

        String path = requestContext.getUriInfo().getPath();
        Optional<String> endpointKey = EndpointAccessCatalog.resolveEndpointKey(
                requestContext.getMethod(),
                path
        );
        if (endpointKey.isEmpty()) {
            return;
        }

        if (securityIdentity == null || securityIdentity.isAnonymous()) {
            abort(requestContext, "Authentication required for this action.");
            return;
        }

        String principal = securityIdentity.getPrincipal() != null
                ? securityIdentity.getPrincipal().getName()
                : null;
        if (principal == null || principal.isBlank()) {
            abort(requestContext, "Authentication required for this action.");
            return;
        }

        User user = userRepository.findByEmailOptional(principal)
                .or(() -> userRepository.find("username", principal).firstResultOptional())
                .orElse(null);

        if (user == null) {
            abort(requestContext, "User not found.");
            return;
        }

        if (!endpointAccessService.canAccessEndpoint(user, endpointKey.get())) {
            abort(requestContext, "Your role is not allowed to perform this action.");
        }
    }

    private void abort(ContainerRequestContext ctx, String message) {
        ctx.abortWith(
                Response.status(Response.Status.FORBIDDEN)
                        .entity(new ResponseMessage(message, null))
                        .build()
        );
    }
}
