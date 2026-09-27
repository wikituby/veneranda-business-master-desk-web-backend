package org.example.configuration.security;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.example.user.domains.User;
import org.example.user.domains.repositories.UserRepository;

/**
 * Resolves the logged-in {@link User} when auth uses {@link io.quarkus.security.runtime.QuarkusPrincipal}
 * instead of an injected request-scoped {@link JsonWebToken}.
 */
@ApplicationScoped
public class AuthenticatedUserResolver {

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    UserRepository userRepository;

    @Inject
    JwtUtils jwtUtils;

    @Inject
    HttpHeaders httpHeaders;

    public User requireCurrentUser() {
        String login = resolveLogin();
        if (login == null) {
            throw new WebApplicationException("User not authenticated", Response.Status.UNAUTHORIZED);
        }
        return userRepository.findByUsernameOrEmailOptional(login)
                .orElseThrow(() -> new WebApplicationException("User not found", Response.Status.NOT_FOUND));
    }

    private String resolveLogin() {
        if (securityIdentity != null && !securityIdentity.isAnonymous()
                && securityIdentity.getPrincipal() != null) {
            String name = securityIdentity.getPrincipal().getName();
            if (name != null && !name.isBlank()) {
                return name.trim();
            }
        }
        String auth = httpHeaders != null ? httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION) : null;
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7).trim();
            if (!token.isEmpty() && jwtUtils.validateJwtToken(token)) {
                return jwtUtils.getLoginFromJwtToken(token);
            }
        }
        return null;
    }
}
