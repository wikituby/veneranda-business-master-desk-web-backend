package org.example.subscription.dgateway;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.example.subscription.dgateway.domains.DGatewaySettings;

@ApplicationScoped
public class DGatewayClient {

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    @Inject
    DGatewaySettingsService settingsService;

    @ConfigProperty(name = "dgateway.api-url", defaultValue = "https://dgatewayapi.desispay.com")
    String apiUrlProp;

    @ConfigProperty(name = "dgateway.api-key")
    Optional<String> apiKeyProp;

    @ConfigProperty(name = "dgateway.webhook-secret")
    Optional<String> webhookSecretProp;

    @ConfigProperty(name = "dgateway.webhook-callback-url")
    Optional<String> webhookCallbackUrlProp;

    public boolean isConfigured() {
        return notBlank(resolveApiKey());
    }

    public JsonObject collect(double amount, String currency, String phoneNumber, String description) throws Exception {
        JsonObjectBuilder body = Json.createObjectBuilder()
                .add("amount", amount >= 1 ? (long) Math.rint(amount) : amount)
                .add("currency", currency);
        if (notBlank(phoneNumber)) {
            body.add("phone_number", phoneNumber.trim());
        }
        if (notBlank(description)) {
            body.add("description", truncate(description.trim(), 500));
        }
        String callback = resolveCallbackUrl();
        if (notBlank(callback)) {
            body.add("callback_url", callback);
        }
        return postJson("/v1/payments/collect", body.build());
    }

    public JsonObject verifyStatus(String reference) throws Exception {
        JsonObject body = Json.createObjectBuilder()
                .add("reference", reference)
                .build();
        return postJson("/v1/webhooks/verify", body);
    }

    public JsonObject getTransaction(String reference) throws Exception {
        return getJson("/v1/payments/transactions/" + reference);
    }

    public String resolveWebhookSecret() {
        DGatewaySettings s = db();
        if (s != null && notBlank(s.webhookSecret)) {
            return s.webhookSecret.trim();
        }
        return value(webhookSecretProp);
    }

    private JsonObject postJson(String path, JsonObject body) throws Exception {
        ensureConfigured();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(trimSlash(resolveApiUrl()) + path))
                .timeout(Duration.ofSeconds(30))
                .header("X-Api-Key", resolveApiKey())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build();
        return send(request);
    }

    private JsonObject getJson(String path) throws Exception {
        ensureConfigured();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(trimSlash(resolveApiUrl()) + path))
                .timeout(Duration.ofSeconds(30))
                .header("X-Api-Key", resolveApiKey())
                .header("Accept", "application/json")
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
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String message = extractError(json, raw);
            throw new IllegalStateException(message);
        }
        if (json.containsKey("error") && !json.isNull("error")) {
            throw new IllegalStateException(extractError(json, raw));
        }
        return json;
    }

    private void ensureConfigured() {
        if (!isConfigured()) {
            throw new IllegalStateException("DGateway API key is not configured. Add it under Mobile Money / DGateway Settings.");
        }
    }

    private DGatewaySettings db() {
        try {
            return settingsService.findSettings();
        } catch (Exception e) {
            return null;
        }
    }

    private String resolveApiUrl() {
        DGatewaySettings s = db();
        if (s != null && notBlank(s.apiUrl)) {
            return s.apiUrl.trim();
        }
        return apiUrlProp;
    }

    private String resolveApiKey() {
        DGatewaySettings s = db();
        if (s != null && notBlank(s.apiKey)) {
            return s.apiKey.trim();
        }
        return value(apiKeyProp);
    }

    private String resolveCallbackUrl() {
        DGatewaySettings s = db();
        if (s != null && notBlank(s.webhookCallbackUrl)) {
            return s.webhookCallbackUrl.trim();
        }
        return value(webhookCallbackUrlProp);
    }

    private static String extractError(JsonObject json, String raw) {
        try {
            if (json.containsKey("error") && !json.isNull("error")) {
                JsonObject err = json.getJsonObject("error");
                String code = err.getString("code", "");
                String message = err.getString("message", raw);
                return notBlank(code) ? (code + ": " + message) : message;
            }
            if (json.containsKey("message") && !json.isNull("message")) {
                return json.getString("message");
            }
        } catch (Exception ignored) {
        }
        return notBlank(raw) ? raw : "DGateway request failed";
    }

    private static String truncate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max);
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
}