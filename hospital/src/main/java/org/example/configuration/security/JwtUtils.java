package org.example.configuration.security;

import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtSignatureException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.user.domains.User;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class JwtUtils {

    private static final Logger LOG = LoggerFactory.getLogger(JwtUtils.class);
    private static final Duration DEFAULT_TOKEN_EXPIRATION = Duration.ofHours(8);
    private static final Duration RESET_TOKEN_EXPIRATION = Duration.ofMinutes(15);

    private final String jwtSecret = "z$C&F)J@NcRfUjXn2r5u8x/A?D*G-KaPdSgVkYp3s6v9y$B&E)" +
            "H+MbQeThWmZq4t7w!z%C*F-JaNcRfUjXn2r5u8x/A?D(G+KbPeSgVkYp3s6v9y$B&E)H@McQfTjWnZ";

    private final String resetSecret = "cvb%@wDfcRfUjXn2r5u8x/A?D*G-KaPdSgVkYp3s6v9y$B&E)" +
            "H+MbQeThWmZq4t7w!z%C*F-JaNcRfUjXn2r5u8x/A?D(G+KbPeSgVkYp3s6v9y$B&E)qWerTGGd7";

    @Inject
    JWTParser parser;

    @Inject
    JsonWebToken jwt;

    public String generateJwtToken(User user) {
        if (user == null || user.email == null || user.role == null) {
            throw new IllegalArgumentException("Invalid user data for JWT generation");
        }

        Set<String> roleGroups = new HashSet<>(user.allRoleNames());
        if (roleGroups.isEmpty()) {
            roleGroups.add(user.role);
        }

        return Jwt.subject(user.email)
                .issuedAt(Instant.now())
                .groups(roleGroups)
                .expiresIn(DEFAULT_TOKEN_EXPIRATION)
                .upn(user.username)
                .issuer("hospital-server")
                .signWithSecret(jwtSecret);
    }

    public Set<String> getRoles() {
        try {
            return jwt.getGroups();
        } catch (Exception e) {
            LOG.warn("Failed to extract roles from JWT", e);
            return Set.of();
        }
    }

    public String generateResetToken(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }

        return Jwt.subject(email)
                .expiresIn(RESET_TOKEN_EXPIRATION)
                .signWithSecret(resetSecret);
    }

    public boolean validateResetToken(String resetToken) {
        return validateToken(resetToken, resetSecret, "Reset");
    }

    public JsonWebToken getJwt() {
        return jwt;
    }

    public String getUserNameFromJwtToken(String token) {
        try {
            jwt = parser.verify(token, jwtSecret);
            String subject = jwt.getSubject();

            if (subject == null || subject.isBlank()) {
                throw new WebApplicationException("Invalid subject in JWT", 401);
            }

            return subject;
        } catch (ParseException e) {
            LOG.error("JWT parsing failed", e);
            throw new WebApplicationException("Invalid JWT token", 401);
        } catch (Exception e) {
            LOG.error("JWT validation failed", e);
            throw new WebApplicationException("Authentication failed", 401);
        }
    }

    /** Preferred login key: {@code upn} (username), falling back to JWT subject (email). */
    public String getLoginFromJwtToken(String token) {
        try {
            JsonWebToken parsed = parser.verify(token, jwtSecret);
            String upn = parsed.getClaim("upn");
            if (upn != null && !upn.isBlank()) {
                return upn.trim();
            }
            String subject = parsed.getSubject();
            if (subject != null && !subject.isBlank()) {
                return subject.trim();
            }
            throw new WebApplicationException("Invalid JWT claims", 401);
        } catch (ParseException e) {
            LOG.error("JWT parsing failed", e);
            throw new WebApplicationException("Invalid JWT token", 401);
        } catch (Exception e) {
            LOG.error("JWT login resolution failed", e);
            throw new WebApplicationException("Authentication failed", 401);
        }
    }

    public boolean validateJwtToken(String authToken) {
        return validateToken(authToken, jwtSecret, "Access");
    }

    private boolean validateToken(String token, String secret, String tokenType) {
        if (token == null || token.isBlank()) {
            LOG.warn("{} token is empty", tokenType);
            return false;
        }

        try {
            jwt = parser.verify(token, secret);

            // Get expiration time (returns long primitive)
            long expirationTime = jwt.getExpirationTime();
            long currentTime = Instant.now().getEpochSecond();

            // Check if token is expired
            if (expirationTime <= 0 || currentTime > expirationTime) {
                LOG.warn("{} token expired", tokenType);
                return false;
            }

            // Check required claims
            if (jwt.getSubject() == null || jwt.getSubject().isBlank()) {
                LOG.warn("{} token missing subject", tokenType);
                return false;
            }

            return true;
        } catch (JwtSignatureException e) {
            LOG.warn("Invalid {} token signature", tokenType, e);
        } catch (IllegalArgumentException e) {
            LOG.warn("{} token claims empty", tokenType, e);
        } catch (ParseException e) {
            LOG.warn("{} token parsing failed", tokenType, e);
        } catch (Exception e) {
            LOG.error("{} token validation failed", tokenType, e);
        }

        return false;
    }
}





