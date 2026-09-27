package org.example.subscription.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

public final class TokenFingerprint {

    private TokenFingerprint() {
    }

    public static String hash(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Token is required");
        }
        String normalized = rawToken.trim();
        if (SignedActivationTokenVerifier.isSignedToken(normalized)) {
            normalized = normalized.replaceAll("\\s+", "");
        } else {
            normalized = normalized.toUpperCase(Locale.ROOT);
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(normalized.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
