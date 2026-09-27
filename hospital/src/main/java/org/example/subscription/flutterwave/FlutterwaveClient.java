package org.example.subscription.flutterwave;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.example.subscription.flutterwave.domains.FlutterwaveSettings;

/**
 * Flutterwave v4 — OAuth client credentials + customers / payment-methods / charges.
 * Sandbox base: https://developersandbox-api.flutterwave.com
 */
@ApplicationScoped
public class FlutterwaveClient {

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    private final AtomicReference<CachedToken> tokenCache = new AtomicReference<>();

    @Inject
    FlutterwaveSettingsService settingsService;

    @ConfigProperty(name = "flutterwave.api-url", defaultValue = "https://developersandbox-api.flutterwave.com")
    String apiUrlProp;

    @ConfigProperty(name = "flutterwave.token-url",
            defaultValue = "https://idp.flutterwave.com/realms/flutterwave/protocol/openid-connect/token")
    String tokenUrlProp;

    @ConfigProperty(name = "flutterwave.client-id")
    Optional<String> clientIdProp;

    @ConfigProperty(name = "flutterwave.secret-key")
    Optional<String> secretKeyProp;

    @ConfigProperty(name = "flutterwave.webhook-secret-hash")
    Optional<String> webhookHashProp;

    @ConfigProperty(name = "flutterwave.default-email", defaultValue = "billing@facility.local")
    String defaultEmailProp;

    public boolean isConfigured() {
        return notBlank(resolveClientId()) && notBlank(resolveSecretKey());
    }

    public JsonObject createCustomer(String email, String phoneNational, String firstName) throws Exception {
        JsonObjectBuilder phone = Json.createObjectBuilder()
                .add("country_code", "256")
                .add("number", stripLeadingZero(phoneNational));
        JsonObjectBuilder name = Json.createObjectBuilder()
                .add("first", notBlank(firstName) ? firstName : "Facility")
                .add("last", "Customer");
        JsonObject body = Json.createObjectBuilder()
                .add("email", notBlank(email) ? email.trim() : resolveDefaultEmail())
                .add("phone", phone)
                .add("name", name)
                .build();
        return postJson("/customers", body);
    }

    /** Search existing customer by email. Returns null if none. */
    public String findCustomerIdByEmail(String email) throws Exception {
        String useEmail = notBlank(email) ? email.trim() : resolveDefaultEmail();
        JsonObject body = Json.createObjectBuilder().add("email", useEmail).build();
        JsonObject envelope = postJson("/customers/search", body);
        if (!envelope.containsKey("data") || envelope.isNull("data")) {
            return null;
        }
        var data = envelope.get("data");
        if (data.getValueType() == jakarta.json.JsonValue.ValueType.ARRAY) {
            var arr = envelope.getJsonArray("data");
            if (arr.isEmpty()) {
                return null;
            }
            return arr.getJsonObject(0).getString("id", null);
        }
        if (data.getValueType() == jakarta.json.JsonValue.ValueType.OBJECT) {
            return envelope.getJsonObject("data").getString("id", null);
        }
        return null;
    }

    /**
     * Create customer, or reuse existing when Flutterwave says email already exists.
     * Same payer can make unlimited payments.
     */
    public String resolveCustomerId(String email, String phoneNational, String firstName) throws Exception {
        String useEmail = notBlank(email) ? email.trim() : resolveDefaultEmail();
        try {
            JsonObject created = createCustomer(useEmail, phoneNational, firstName);
            if (created.containsKey("data") && !created.isNull("data")) {
                return created.getJsonObject("data").getString("id");
            }
        } catch (IllegalStateException ex) {
            String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
            if (!(msg.contains("already") || msg.contains("exist") || msg.contains("duplicate"))) {
                throw ex;
            }
        }
        String existing = findCustomerIdByEmail(useEmail);
        if (!notBlank(existing)) {
            throw new IllegalStateException("Flutterwave customer already exists but could not be looked up for " + useEmail);
        }
        return existing;
    }

    public JsonObject createMobileMoneyMethod(String network, String phoneNational) throws Exception {
        JsonObject body = Json.createObjectBuilder()
                .add("type", "mobile_money")
                .add("mobile_money", Json.createObjectBuilder()
                        .add("country_code", "256")
                        .add("network", network.toUpperCase())
                        .add("phone_number", stripLeadingZero(phoneNational))
                        .build())
                .build();
        return postJson("/payment-methods", body);
    }

    public JsonObject createCharge(String customerId, String paymentMethodId, double amount, String currency, String reference)
            throws Exception {
        JsonObject body = Json.createObjectBuilder()
                .add("currency", currency)
                .add("customer_id", customerId)
                .add("payment_method_id", paymentMethodId)
                .add("amount", amount >= 1 ? (long) Math.rint(amount) : amount)
                .add("reference", reference)
                .build();
        return postJson("/charges", body);
    }

    public JsonObject getCharge(String chargeId) throws Exception {
        return getJson("/charges/" + URLEncoder.encode(chargeId, StandardCharsets.UTF_8));
    }

    public String resolveWebhookSecretHash() {
        FlutterwaveSettings s = db();
        if (s != null && notBlank(s.webhookSecretHash)) {
            return s.webhookSecretHash.trim();
        }
        return value(webhookHashProp);
    }

    public String resolveDefaultEmail() {
        FlutterwaveSettings s = db();
        if (s != null && notBlank(s.defaultEmail)) {
            return s.defaultEmail.trim();
        }
        return defaultEmailProp;
    }

    private String accessToken() throws Exception {
        CachedToken cached = tokenCache.get();
        if (cached != null && cached.expiresAtMs > System.currentTimeMillis() + 30_000) {
            return cached.token;
        }
        ensureConfigured();
        String form = "client_id=" + URLEncoder.encode(resolveClientId(), StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(resolveSecretKey(), StandardCharsets.UTF_8)
                + "&grant_type=client_credentials";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(resolveTokenUrl()))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Flutterwave token failed (" + response.statusCode() + "): " + response.body());
        }
        try (JsonReader reader = Json.createReader(new StringReader(response.body()))) {
            JsonObject json = reader.readObject();
            String token = json.getString("access_token");
            long expiresIn = json.containsKey("expires_in") ? json.getJsonNumber("expires_in").longValue() : 600;
            tokenCache.set(new CachedToken(token, System.currentTimeMillis() + expiresIn * 1000));
            return token;
        }
    }

    private JsonObject postJson(String path, JsonObject body) throws Exception {
        String token = accessToken();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(trimSlash(resolveApiUrl()) + path))
                .timeout(Duration.ofSeconds(40))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("X-Trace-Id", UUID.randomUUID().toString())
                .header("X-Idempotency-Key", UUID.randomUUID().toString())
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build();
        return send(request);
    }

    private JsonObject getJson(String path) throws Exception {
        String token = accessToken();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(trimSlash(resolveApiUrl()) + path))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .header("X-Trace-Id", UUID.randomUUID().toString())
                .GET()
                .build();
        return send(request);
    }

    private JsonObject send(HttpRequest request) throws Exception {
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        String raw = response.body() == null ? "" : response.body();
        JsonObject json;
        try (JsonReader reader = Json.createReader(new StringReader(raw.isBlank() ? "{}" : raw))) {
            json = reader.readObject();
        }
        String status = json.containsKey("status") ? json.getString("status", "") : "";
        if (response.statusCode() < 200 || response.statusCode() >= 300 || "failed".equalsIgnoreCase(status)) {
            throw new IllegalStateException(extractError(json, raw));
        }
        return json;
    }

    private void ensureConfigured() {
        if (!isConfigured()) {
            throw new IllegalStateException("Flutterwave Client ID / Secret is not configured.");
        }
    }

    private FlutterwaveSettings db() {
        try {
            return settingsService.findSettings();
        } catch (Exception e) {
            return null;
        }
    }

    private String resolveApiUrl() {
        FlutterwaveSettings s = db();
        if (s != null && notBlank(s.apiUrl)) {
            return s.apiUrl.trim();
        }
        return apiUrlProp;
    }

    private String resolveTokenUrl() {
        FlutterwaveSettings s = db();
        if (s != null && notBlank(s.tokenUrl)) {
            return s.tokenUrl.trim();
        }
        return tokenUrlProp;
    }

    private String resolveClientId() {
        FlutterwaveSettings s = db();
        if (s != null && notBlank(s.clientId)) {
            return s.clientId.trim();
        }
        return value(clientIdProp);
    }

    private String resolveSecretKey() {
        FlutterwaveSettings s = db();
        if (s != null && notBlank(s.secretKey)) {
            return s.secretKey.trim();
        }
        return value(secretKeyProp);
    }

    private static String extractError(JsonObject json, String raw) {
        try {
            if (json.containsKey("error") && !json.isNull("error")) {
                JsonObject err = json.getJsonObject("error");
                return err.getString("message", raw);
            }
            if (json.containsKey("message") && !json.isNull("message")) {
                return json.getString("message");
            }
        } catch (Exception ignored) {
        }
        return notBlank(raw) ? raw : "Flutterwave request failed";
    }

    private static String stripLeadingZero(String phone) {
        String digits = phone == null ? "" : phone.replaceAll("\\D", "");
        if (digits.startsWith("256") && digits.length() > 3) {
            return digits.substring(3);
        }
        if (digits.startsWith("0") && digits.length() > 1) {
            return digits.substring(1);
        }
        return digits;
    }

    private static String trimSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static String value(Optional<String> opt) {
        return opt.orElse("").trim();
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static final class CachedToken {
        final String token;
        final long expiresAtMs;

        CachedToken(String token, long expiresAtMs) {
            this.token = token;
            this.expiresAtMs = expiresAtMs;
        }
    }
}