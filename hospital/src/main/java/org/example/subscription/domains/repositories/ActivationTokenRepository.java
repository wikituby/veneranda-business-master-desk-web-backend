package org.example.subscription.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.subscription.domains.ActivationToken;

import java.util.Locale;
import java.util.Optional;

@ApplicationScoped
public class ActivationTokenRepository implements PanacheRepository<ActivationToken> {

    public Optional<ActivationToken> findByToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return find("token", token.trim().toUpperCase(Locale.ROOT)).firstResultOptional();
    }

    public Optional<ActivationToken> findByJti(String jti) {
        if (jti == null || jti.isBlank()) {
            return Optional.empty();
        }
        return find("token", jti.trim()).firstResultOptional();
    }

    public Optional<ActivationToken> findByTokenHash(String tokenHash) {
        if (tokenHash == null || tokenHash.isBlank()) {
            return Optional.empty();
        }
        return find("tokenHash", tokenHash.trim()).firstResultOptional();
    }

    public boolean isUsedTokenHash(String tokenHash) {
        return findByTokenHash(tokenHash)
                .map(t -> "used".equalsIgnoreCase(t.status))
                .orElse(false);
    }
}
