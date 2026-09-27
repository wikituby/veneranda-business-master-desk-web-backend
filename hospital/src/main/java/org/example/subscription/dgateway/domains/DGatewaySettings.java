package org.example.subscription.dgateway.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "dgateway_settings")
public class DGatewaySettings extends PanacheEntity {

    @Column(length = 500)
    public String apiUrl = "https://dgatewayapi.desispay.com";

    @Column(columnDefinition = "TEXT")
    public String apiKey;

    @Column(columnDefinition = "TEXT")
    public String webhookSecret;

    @Column(length = 500)
    public String webhookCallbackUrl;

    @Column(length = 255)
    public String stripePublishableKey;

    @Column(length = 40)
    public String defaultCurrency = "UGX";

    public Boolean enabled = Boolean.TRUE;

    public LocalDateTime updatedAt;
}