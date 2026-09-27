package org.example.subscription.dgateway;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.example.subscription.dgateway.payloads.DGatewayCollectRequest;
import org.example.subscription.dgateway.payloads.DGatewayPaymentStatusDTO;

@ApplicationScoped
public class DGatewayService {

    private final Map<String, PaymentRecord> payments = new ConcurrentHashMap<>();
    private final Set<String> processedWebhookKeys = ConcurrentHashMap.newKeySet();

    @Inject
    DGatewayClient dGatewayClient;

    @ConfigProperty(name = "dgateway.mock-when-unconfigured", defaultValue = "true")
    boolean mockWhenUnconfigured;

    @ConfigProperty(name = "dgateway.default-currency", defaultValue = "UGX")
    String defaultCurrency;

    @ConfigProperty(name = "dgateway.mock-auto-success-seconds", defaultValue = "12")
    int mockAutoSuccessSeconds;

    public Map<String, Object> providersInfo() {
        return Map.of(
                "dgatewayConfigured", dGatewayClient.isConfigured(),
                "mtnConfigured", dGatewayClient.isConfigured(),
                "airtelConfigured", dGatewayClient.isConfigured(),
                "mockWhenUnconfigured", mockWhenUnconfigured,
                "defaultCurrency", defaultCurrency,
                "gateway", "dgateway",
                "hint", "Configure DGateway API key under Administration → Mobile Money Settings. MTN/Airtel are routed by DGateway.");
    }

    public DGatewayPaymentStatusDTO initiate(DGatewayCollectRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Payment request is required");
        }
        double amount = request.amount == null ? 0 : request.amount;
        if (amount <= 0) {
            throw new IllegalArgumentException("Enter a valid amount greater than zero");
        }
        String currency = (request.currency != null && !request.currency.isBlank())
                ? request.currency.trim().toUpperCase(Locale.ROOT)
                : defaultCurrency;
        boolean card = "USD".equals(currency) || "card".equalsIgnoreCase(nullToEmpty(request.method));
        String phone = normalizePhone(request.phoneNumber);
        if (!card && phone.length() < 9) {
            throw new IllegalArgumentException("Enter a valid mobile money phone number");
        }

        if (!dGatewayClient.isConfigured()) {
            if (!mockWhenUnconfigured) {
                throw new IllegalStateException("DGateway is not configured");
            }
            String mockRef = "mock_" + System.currentTimeMillis();
            PaymentRecord record = new PaymentRecord(mockRef, "dgateway", phone, amount, currency, true, "PENDING",
                    "Demo mode: pretend an approval request was sent. Status will complete shortly.");
            payments.put(mockRef, record);
            return toDto(record);
        }

        try {
            JsonObject envelope = dGatewayClient.collect(amount, currency, card ? null : phone,
                    request.description != null ? request.description : "Facility subscription payment");
            JsonObject data = envelope.containsKey("data") && !envelope.isNull("data")
                    ? envelope.getJsonObject("data")
                    : envelope;
            String reference = data.getString("reference", "");
            if (reference.isBlank()) {
                throw new IllegalStateException("DGateway did not return a payment reference");
            }
            PaymentRecord record = new PaymentRecord(
                    reference,
                    data.getString("provider", "dgateway"),
                    data.containsKey("phone_number") ? data.getString("phone_number", phone) : phone,
                    amount,
                    currency,
                    false,
                    mapStatus(data.getString("status", "pending")),
                    card
                            ? "Complete card payment to finish."
                            : ("Check your phone and approve the prompt on " + phone + "."));
            if (data.containsKey("provider_ref") && !data.isNull("provider_ref")) {
                record.providerRef = data.getString("provider_ref", null);
            }
            if (data.containsKey("client_secret") && !data.isNull("client_secret")) {
                record.clientSecret = data.getString("client_secret", null);
            }
            if (data.containsKey("stripe_publishable_key") && !data.isNull("stripe_publishable_key")) {
                record.stripePublishableKey = data.getString("stripe_publishable_key", null);
            }
            payments.put(reference, record);
            return toDto(record);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage() != null ? e.getMessage() : "Could not start DGateway payment", e);
        }
    }

    public DGatewayPaymentStatusDTO getStatus(String referenceId) {
        PaymentRecord record = payments.get(referenceId);
        if (record == null) {
            DGatewayPaymentStatusDTO missing = new DGatewayPaymentStatusDTO();
            missing.referenceId = referenceId;
            missing.status = "UNKNOWN";
            missing.message = "Payment reference not found";
            return missing;
        }
        if (isTerminal(record.status)) {
            return toDto(record);
        }
        if (record.mock) {
            long elapsed = Duration.between(record.createdAt, Instant.now()).getSeconds();
            if (elapsed >= mockAutoSuccessSeconds) {
                record.status = "SUCCESSFUL";
                record.message = "Demo payment completed successfully.";
            }
            return toDto(record);
        }
        try {
            JsonObject envelope = dGatewayClient.verifyStatus(referenceId);
            JsonObject data = envelope.containsKey("data") && !envelope.isNull("data")
                    ? envelope.getJsonObject("data")
                    : envelope;
            String remote = mapStatus(data.getString("status", "pending"));
            record.status = remote;
            if (data.containsKey("failure_reason") && !data.isNull("failure_reason")) {
                record.failureReason = data.getString("failure_reason", "");
            }
            if ("SUCCESSFUL".equals(remote)) {
                record.message = "Payment completed. Thank you.";
            } else if ("FAILED".equals(remote)) {
                record.message = notBlank(record.failureReason)
                        ? record.failureReason
                        : "Payment failed or was cancelled on the phone.";
            } else {
                record.message = "Check your phone and approve the prompt…";
            }
        } catch (Exception e) {
            record.message = "Could not refresh status: " + e.getMessage();
        }
        return toDto(record);
    }

    public void applyWebhook(String rawBody, String signatureHeader) {
        String secret = dGatewayClient.resolveWebhookSecret();
        if (notBlank(secret)) {
            if (!notBlank(signatureHeader) || !verifyHmacSha256(rawBody, secret, signatureHeader)) {
                throw new SecurityException("Invalid DGateway webhook signature");
            }
        }
        try (jakarta.json.JsonReader reader = jakarta.json.Json.createReader(new java.io.StringReader(rawBody))) {
            JsonObject root = reader.readObject();
            String event = root.getString("event", "transaction.updated");
            JsonObject data = root.containsKey("data") && !root.isNull("data")
                    ? root.getJsonObject("data")
                    : root;
            String reference = data.getString("reference", root.getString("reference", ""));
            if (!notBlank(reference)) {
                return;
            }
            String idempotencyKey = event + ":" + reference + ":" + data.getString("status", "");
            if (!processedWebhookKeys.add(idempotencyKey)) {
                return;
            }
            PaymentRecord record = payments.get(reference);
            if (record == null) {
                record = new PaymentRecord(reference, data.getString("provider", "dgateway"),
                        data.getString("phone_number", ""),
                        data.containsKey("amount") ? data.getJsonNumber("amount").doubleValue() : 0,
                        data.getString("currency", defaultCurrency),
                        false,
                        mapStatus(data.getString("status", "pending")),
                        "Updated via webhook");
                payments.put(reference, record);
            } else {
                record.status = mapStatus(data.getString("status", record.status));
                if ("SUCCESSFUL".equals(record.status)) {
                    record.message = "Payment completed. Thank you.";
                } else if ("FAILED".equals(record.status)) {
                    record.failureReason = data.getString("failure_reason", record.failureReason);
                    record.message = notBlank(record.failureReason) ? record.failureReason : "Payment failed.";
                }
            }
            if (data.containsKey("provider_ref") && !data.isNull("provider_ref")) {
                record.providerRef = data.getString("provider_ref", record.providerRef);
            }
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid webhook body: " + e.getMessage(), e);
        }
    }

    public static boolean verifyHmacSha256(String rawBody, String secret, String signatureHeader) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            String expected = toHex(digest);
            String provided = signatureHeader.trim();
            if (provided.startsWith("sha256=")) {
                provided = provided.substring("sha256=".length()).trim();
            }
            return MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8),
                    provided.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }

    /** Keep leading 0 local format or 256… — DGateway accepts both. */
    static String normalizePhone(String raw) {
        if (raw == null) {
            return "";
        }
        String digits = raw.replaceAll("\\D", "");
        if (digits.startsWith("0") && digits.length() == 10) {
            return digits;
        }
        if (digits.startsWith("256")) {
            return digits;
        }
        if (digits.length() == 9) {
            return "0" + digits;
        }
        return digits;
    }

    private static String mapStatus(String status) {
        if (status == null) {
            return "PENDING";
        }
        String s = status.trim().toUpperCase(Locale.ROOT);
        if (s.contains("COMPLETE") || s.contains("SUCCESS")) {
            return "SUCCESSFUL";
        }
        if (s.contains("FAIL") || s.contains("CANCEL") || s.contains("REJECT")) {
            return "FAILED";
        }
        if (s.contains("TIMEOUT") || s.contains("EXPIRED")) {
            return "TIMEOUT";
        }
        return "PENDING";
    }

    private static boolean isTerminal(String status) {
        return "SUCCESSFUL".equals(status) || "FAILED".equals(status) || "TIMEOUT".equals(status);
    }

    private static DGatewayPaymentStatusDTO toDto(PaymentRecord record) {
        DGatewayPaymentStatusDTO dto = new DGatewayPaymentStatusDTO();
        dto.referenceId = record.referenceId;
        dto.provider = record.provider;
        dto.phoneNumber = record.phoneNumber;
        dto.amount = record.amount;
        dto.currency = record.currency;
        dto.status = record.status;
        dto.message = record.message;
        dto.mock = record.mock;
        dto.providerRef = record.providerRef;
        dto.clientSecret = record.clientSecret;
        dto.stripePublishableKey = record.stripePublishableKey;
        dto.failureReason = record.failureReason;
        return dto;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    static final class PaymentRecord {
        final String referenceId;
        String provider;
        String phoneNumber;
        final double amount;
        final String currency;
        final boolean mock;
        String status;
        String message;
        String providerRef;
        String clientSecret;
        String stripePublishableKey;
        String failureReason;
        final Instant createdAt = Instant.now();

        PaymentRecord(String referenceId, String provider, String phoneNumber, double amount, String currency,
                      boolean mock, String status, String message) {
            this.referenceId = referenceId;
            this.provider = provider;
            this.phoneNumber = phoneNumber;
            this.amount = amount;
            this.currency = currency;
            this.mock = mock;
            this.status = status;
            this.message = message;
        }
    }
}