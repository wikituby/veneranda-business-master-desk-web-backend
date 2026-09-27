package org.example.subscription.flutterwave.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "flutterwave_settings")
public class FlutterwaveSettings extends PanacheEntity {

    /** Sandbox: https://developersandbox-api.flutterwave.com  Live: https://f4bexperience.flutterwave.com (per FLW v4 docs) */
    @Column(length = 500)
    public String apiUrl = "https://developersandbox-api.flutterwave.com";

    @Column(length = 100)
    public String tokenUrl = "https://idp.flutterwave.com/realms/flutterwave/protocol/openid-connect/token";

    @Column(length = 255)
    public String clientId;

    @Column(columnDefinition = "TEXT")
    public String secretKey;

    @Column(columnDefinition = "TEXT")
    public String publicKey;

    @Column(columnDefinition = "TEXT")
    public String encryptionKey;

    @Column(columnDefinition = "TEXT")
    public String webhookSecretHash;

    @Column(length = 500)
    public String webhookCallbackUrl;

    @Column(length = 255)
    public String defaultEmail = "billing@facility.local";

    @Column(length = 40)
    public String defaultCurrency = "UGX";

    public Boolean enabled = Boolean.TRUE;

    public LocalDateTime updatedAt;
}