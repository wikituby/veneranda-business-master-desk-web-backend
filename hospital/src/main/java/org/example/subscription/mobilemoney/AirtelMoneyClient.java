package org.example.subscription.mobilemoney;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
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
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.example.subscription.mobilemoney.domains.MobileMoneySettings;

/**
 * Airtel Money Africa Open API — Collections (USSD push for PIN).
 * Credentials: DB settings (admin UI) override application.properties / env.
 */
@ApplicationScoped
public class AirtelMoneyClient {

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    private final AtomicReference<CachedToken> tokenCache = new AtomicReference<>();

    @Inject
    MobileMoneySettingsService settingsService;

    @ConfigProperty(name = "mobile-money.airtel.base-url", defaultValue = "https://openapiuat.airtel.africa")
    String baseUrlProp;

    @ConfigProperty(name = "mobile-money.airtel.client-id")
    Optional<String> clientIdProp;

    @ConfigProperty(name = "mobile-money.airtel.client-secret")
    Optional<String> clientSecretProp;

    @ConfigProperty(name = "mobile-money.airtel.country", defaultValue = "UG")
    String countryProp;

    @ConfigProperty(name = "mobile-money.airtel.currency", defaultValue = "UGX")
    String currencyProp;

    public boolean isConfigured() {
        return notBlank(resolveClientId()) && notBlank(resolveClientSecret());
    }

    public String initiatePayment(String referenceId, String msisdnLocal, double amount, String currency, String note)
            throws Exception {
        String token = fetchAccessToken();
        String useCurrency = notBlank(currency) ? currency : resolveCurrency();
        String msisdn = stripCountryCode(msisdnLocal);
        String country = resolveCountry();

        JsonObject body = Json.createObjectBuilder()
                .add("reference", truncate(notBlank(note) ? note : ("SUB-" + referenceId), 50))
                .add("subscriber", Json.createObjectBuilder()
                        .add("country", country)
                        .add("currency", useCurrency)
                        .add("msisdn", msisdn)
                        .build())
                .add("transaction", Json.createObjectBuilder()
                        .add("amount", amount >= 1 ? (long) Math.rint(amount) : amount)
                        .add("country", country)
                        .add("currency", useCurrency)
                        .add("id", referenceId)
                        .build())
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(trimSlash(resolveBaseUrl()) + "/merchant/v1/payments/"))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .header("X-Country", country)
                .header("X-Currency", useCurrency)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Airtel payment failed (" + response.statusCode() + "): " + response.body());
        }
        try (JsonReader reader = Json.createReader(new StringReader(response.body()))) {
            JsonObject json = reader.readObject();
            if (json.containsKey("data") && !json.isNull("data")) {
                JsonObject data = json.getJsonObject("data");
                if (data.containsKey("transaction") && !data.isNull("transaction")) {
                    JsonObject txn = data.getJsonObject("transaction");
                    if (txn.containsKey("id") && !txn.isNull("id")) {
                        return String.valueOf(txn.get("id")).replace("\"", "");
                    }
                }
            }
        }
        return referenceId;
    }

    public String enquireStatus(String transactionId) throws Exception {
        String token = fetchAccessToken();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(trimSlash(resolveBaseUrl()) + "/standard/v1/payments/"
                        + URLEncoder.encode(transactionId, StandardCharsets.UTF_8)))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "*/*")
                .header("X-Country", resolveCountry())
                .header("X-Currency", resolveCurrency())
                .GET()
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Airtel enquiry failed (" + response.statusCode() + "): " + response.body());
        }
        try (JsonReader reader = Json.createReader(new StringReader(response.body()))) {
            JsonObject json = reader.readObject();
            if (json.containsKey("data") && !json.isNull("data")) {
                JsonObject data = json.getJsonObject("data");
                if (data.containsKey("transaction") && !data.isNull("transaction")) {
                    JsonObject txn = data.getJsonObject("transaction");
                    String status = txn.getString("status", txn.getString("transaction_status", "UNKNOWN"));
                    return mapAirtelStatus(status);
                }
            }
            return "UNKNOWN";
        }
    }

    private String fetchAccessToken() throws Exception {
        CachedToken cached = tokenCache.get();
        if (cached != null && cached.expiresAtMs > System.currentTimeMillis() + 30_000) {
            return cached.token;
        }
        String form = "client_id=" + URLEncoder.encode(resolveClientId(), StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(resolveClientSecret(), StandardCharsets.UTF_8)
                + "&grant_type=client_credentials";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(trimSlash(resolveBaseUrl()) + "/auth/oauth2/token"))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "*/*")
                .POST(HttpRequest.BodyPublishers.ofString(form, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Airtel token failed (" + response.statusCode() + "): " + response.body());
        }
        try (JsonReader reader = Json.createReader(new StringReader(response.body()))) {
            JsonObject json = reader.readObject();
            String token = json.getString("access_token");
            long expiresIn = json.containsKey("expires_in") ? json.getJsonNumber("expires_in").longValue() : 300;
            tokenCache.set(new CachedToken(token, System.currentTimeMillis() + expiresIn * 1000));
            return token;
        }
    }

    private MobileMoneySettings db() {
        try {
            return settingsService.findSettings();
        } catch (Exception e) {
            return null;
        }
    }

    private String resolveBaseUrl() {
        MobileMoneySettings s = db();
        if (s != null && notBlank(s.airtelBaseUrl)) {
            return s.airtelBaseUrl.trim();
        }
        return baseUrlProp;
    }

    private String resolveClientId() {
        MobileMoneySettings s = db();
        if (s != null && notBlank(s.airtelClientId)) {
            return s.airtelClientId.trim();
        }
        return value(clientIdProp);
    }

    private String resolveClientSecret() {
        MobileMoneySettings s = db();
        if (s != null && notBlank(s.airtelClientSecret)) {
            return s.airtelClientSecret.trim();
        }
        return value(clientSecretProp);
    }

    private String resolveCountry() {
        MobileMoneySettings s = db();
        if (s != null && notBlank(s.airtelCountry)) {
            return s.airtelCountry.trim();
        }
        return countryProp;
    }

    private String resolveCurrency() {
        MobileMoneySettings s = db();
        if (s != null && notBlank(s.airtelCurrency)) {
            return s.airtelCurrency.trim();
        }
        return currencyProp;
    }

    private static String mapAirtelStatus(String status) {
        if (status == null) {
            return "UNKNOWN";
        }
        String s = status.trim().toUpperCase();
        if (s.contains("SUCCESS") || "TS".equals(s)) {
            return "SUCCESSFUL";
        }
        if (s.contains("FAIL") || s.contains("ERROR") || "TF".equals(s)) {
            return "FAILED";
        }
        if (s.contains("PEND") || "TP".equals(s) || "TIP".equals(s)) {
            return "PENDING";
        }
        return s;
    }

    private static String stripCountryCode(String msisdn) {
        String digits = msisdn == null ? "" : msisdn.replaceAll("\\D", "");
        if (digits.startsWith("256") && digits.length() > 9) {
            return digits.substring(3);
        }
        if (digits.startsWith("0") && digits.length() > 1) {
            return digits.substring(1);
        }
        return digits;
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

    private static boolean notBlank(Optional<String> opt) {
        return opt.isPresent() && !opt.get().isBlank();
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
