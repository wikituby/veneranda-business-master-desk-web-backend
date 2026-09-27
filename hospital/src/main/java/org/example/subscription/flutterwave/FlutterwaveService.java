package org.example.subscription.flutterwave;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.example.subscription.flutterwave.payloads.FlutterwaveCollectRequest;
import org.example.subscription.flutterwave.payloads.FlutterwavePaymentStatusDTO;

@ApplicationScoped
public class FlutterwaveService {

    private final Map<String, PaymentRecord> payments = new ConcurrentHashMap<>();
    private final Set<String> processedWebhookKeys = ConcurrentHashMap.newKeySet();

    @Inject
    FlutterwaveClient flutterwaveClient;

    @ConfigProperty(name = "flutterwave.mock-when-unconfigured", defaultValue = "true")
    boolean mockWhenUnconfigured;

    @ConfigProperty(name = "flutterwave.default-currency", defaultValue = "UGX")
    String defaultCurrency;

    @ConfigProperty(name = "flutterwave.mock-auto-success-seconds", defaultValue = "8")
    int mockAutoSuccessSeconds;

    public Map<String, Object> providersInfo() {
        return Map.of(
                "flutterwaveConfigured", flutterwaveClient.isConfigured(),
                "mockWhenUnconfigured", mockWhenUnconfigured,
                "defaultCurrency", defaultCurrency,
                "gateway", "flutterwave",
                "apiVersion", "v4",
                "hint", "Flutterwave v4 Client ID + Secret. Paste webhook secret hash into Flutterwave Dashboard → Webhooks.");
    }

    public FlutterwavePaymentStatusDTO initiate(FlutterwaveCollectRequest request) {
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
        String phone = normalizePhone(request.phoneNumber);
        if (phone.length() < 9) {
            throw new IllegalArgumentException("Enter a valid mobile money phone number");
        }
        String network = normalizeNetwork(request.network, phone);

        if (!flutterwaveClient.isConfigured()) {
            if (!mockWhenUnconfigured) {
                throw new IllegalStateException("Flutterwave is not configured");
            }
            String mockRef = "flw_mock_" + System.currentTimeMillis();
            PaymentRecord record = new PaymentRecord(mockRef, "flutterwave", phone, amount, currency, network, true,
                    "PENDING", "Demo Flutterwave: pretend PIN prompt sent. Completes shortly.");
            payments.put(mockRef, record);
            return toDto(record);
        }

        String reference = "sub_" + UUID.randomUUID().toString().replace("-", "").substring(0, 18);
        try {
            // Reuse existing Flutterwave customer by email — "already exists" is normal on repeat payments
            String customerId = flutterwaveClient.resolveCustomerId(
                    request.email, phone, "Subscriber");

            JsonObject methodEnv = flutterwaveClient.createMobileMoneyMethod(network, phone);
            JsonObject methodData = dataOf(methodEnv);
            String paymentMethodId = methodData.getString("id");

            JsonObject chargeEnv = flutterwaveClient.createCharge(
                    customerId, paymentMethodId, amount, currency, reference);
            JsonObject chargeData = dataOf(chargeEnv);
            String chargeId = chargeData.getString("id", reference);
            String status = mapStatus(chargeData.getString("status", "pending"));

            PaymentRecord record = new PaymentRecord(
                    reference,
                    "flutterwave",
                    phone,
                    amount,
                    currency,
                    network,
                    false,
                    status,
                    "Check your phone and approve the Flutterwave / " + network + " prompt.");
            record.providerRef = chargeId;
            payments.put(reference, record);
            payments.put(chargeId, record);
            return toDto(record);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage() != null ? e.getMessage() : "Could not start Flutterwave payment", e);
        }
    }

    public FlutterwavePaymentStatusDTO getStatus(String referenceId) {
        PaymentRecord record = payments.get(referenceId);
        if (record == null) {
            FlutterwavePaymentStatusDTO missing = new FlutterwavePaymentStatusDTO();
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
                record.message = "Demo Flutterwave payment completed.";
            }
            return toDto(record);
        }
        try {
            String chargeId = notBlank(record.providerRef) ? record.providerRef : referenceId;
            JsonObject envelope = flutterwaveClient.getCharge(chargeId);
            JsonObject data = dataOf(envelope);
            String remote = mapStatus(data.getString("status", "pending"));
            record.status = remote;
            if ("SUCCESSFUL".equals(remote)) {
                record.message = "Payment completed. Thank you.";
            } else if ("FAILED".equals(remote)) {
                record.failureReason = "Payment failed or was cancelled.";
                record.message = record.failureReason;
            } else {
                record.message = "Check your phone and approve the prompt…";
            }
        } catch (Exception e) {
            record.message = "Could not refresh status: " + e.getMessage();
        }
        return toDto(record);
    }

    public void applyWebhook(String rawBody, String flutterwaveSignature, String verifHash) {
        String secret = flutterwaveClient.resolveWebhookSecretHash();
        if (notBlank(secret)) {
            boolean ok = false;
            if (notBlank(flutterwaveSignature) && verifyHmacBase64(rawBody, secret, flutterwaveSignature)) {
                ok = true;
            }
            if (!ok && notBlank(verifHash) && secret.equals(verifHash.trim())) {
                ok = true;
            }
            if (!ok) {
                throw new SecurityException("Invalid Flutterwave webhook signature");
            }
        }
        try (jakarta.json.JsonReader reader = jakarta.json.Json.createReader(new java.io.StringReader(rawBody))) {
            JsonObject root = reader.readObject();
            String event = root.containsKey("type") ? root.getString("type", "charge.completed")
                    : root.getString("event", "charge.completed");
            JsonObject data = root.containsKey("data") && !root.isNull("data")
                    ? root.getJsonObject("data")
                    : root;
            String reference = data.getString("reference", "");
            String chargeId = data.getString("id", "");
            if (!notBlank(reference) && !notBlank(chargeId)) {
                return;
            }
            String idempotencyKey = event + ":" + reference + ":" + chargeId + ":" + data.getString("status", "");
            if (!processedWebhookKeys.add(idempotencyKey)) {
                return;
            }
            PaymentRecord record = null;
            if (notBlank(reference)) {
                record = payments.get(reference);
            }
            if (record == null && notBlank(chargeId)) {
                record = payments.get(chargeId);
            }
            String mapped = mapStatus(data.getString("status", "pending"));
            if (record == null) {
                record = new PaymentRecord(
                        notBlank(reference) ? reference : chargeId,
                        "flutterwave",
                        "",
                        data.containsKey("amount") ? data.getJsonNumber("amount").doubleValue() : 0,
                        data.getString("currency", defaultCurrency),
                        "MTN",
                        false,
                        mapped,
                        "Updated via Flutterwave webhook");
                record.providerRef = chargeId;
                payments.put(record.referenceId, record);
                if (notBlank(chargeId)) {
                    payments.put(chargeId, record);
                }
            } else {
                record.status = mapped;
                if ("SUCCESSFUL".equals(mapped)) {
                    record.message = "Payment completed. Thank you.";
                } else if ("FAILED".equals(mapped)) {
                    record.message = "Payment failed.";
                }
            }
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid webhook body: " + e.getMessage(), e);
        }
    }

    public static boolean verifyHmacBase64(String rawBody, String secret, String signatureHeader) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            String expected = Base64.getEncoder().encodeToString(digest);
            return MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8),
                    signatureHeader.trim().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }

    static String normalizeNetwork(String network, String phone) {
        if (network != null && !network.isBlank()) {
            String n = network.trim().toUpperCase(Locale.ROOT);
            if (n.startsWith("AIR")) {
                return "AIRTEL";
            }
            return "MTN";
        }
        String digits = phone.replaceAll("\\D", "");
        String national = digits;
        if (national.startsWith("256") && national.length() > 3) {
            national = "0" + national.substring(3);
        }
        if (national.startsWith("070") || national.startsWith("075") || national.startsWith("074") || national.startsWith("020")) {
            return "AIRTEL";
        }
        return "MTN";
    }

    static String normalizePhone(String raw) {
        if (raw == null) {
            return "";
        }
        String digits = raw.replaceAll("\\D", "");
        if (digits.startsWith("256") && digits.length() >= 12) {
            return "0" + digits.substring(3);
        }
        if (digits.startsWith("0")) {
            return digits;
        }
        if (digits.length() == 9) {
            return "0" + digits;
        }
        return digits;
    }

    private static JsonObject dataOf(JsonObject envelope) {
        if (envelope.containsKey("data") && !envelope.isNull("data")) {
            return envelope.getJsonObject("data");
        }
        return envelope;
    }

    private static String mapStatus(String status) {
        if (status == null) {
            return "PENDING";
        }
        String s = status.trim().toLowerCase(Locale.ROOT);
        if (s.contains("success") || "succeeded".equals(s) || "successful".equals(s)) {
            return "SUCCESSFUL";
        }
        if (s.contains("fail") || s.contains("cancel") || "failed".equals(s)) {
            return "FAILED";
        }
        return "PENDING";
    }

    private static boolean isTerminal(String status) {
        return "SUCCESSFUL".equals(status) || "FAILED".equals(status) || "TIMEOUT".equals(status);
    }

    private static FlutterwavePaymentStatusDTO toDto(PaymentRecord record) {
        FlutterwavePaymentStatusDTO dto = new FlutterwavePaymentStatusDTO();
        dto.referenceId = record.referenceId;
        dto.provider = record.provider;
        dto.phoneNumber = record.phoneNumber;
        dto.amount = record.amount;
        dto.currency = record.currency;
        dto.status = record.status;
        dto.message = record.message;
        dto.mock = record.mock;
        dto.providerRef = record.providerRef;
        dto.network = record.network;
        dto.failureReason = record.failureReason;
        return dto;
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    static final class PaymentRecord {
        final String referenceId;
        String provider;
        String phoneNumber;
        final double amount;
        final String currency;
        final String network;
        final boolean mock;
        String status;
        String message;
        String providerRef;
        String failureReason;
        final Instant createdAt = Instant.now();

        PaymentRecord(String referenceId, String provider, String phoneNumber, double amount, String currency,
                      String network, boolean mock, String status, String message) {
            this.referenceId = referenceId;
            this.provider = provider;
            this.phoneNumber = phoneNumber;
            this.amount = amount;
            this.currency = currency;
            this.network = network;
            this.mock = mock;
            this.status = status;
            this.message = message;
        }
    }
}