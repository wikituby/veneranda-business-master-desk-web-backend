package org.example.subscription.mobilemoney.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "mobile_money_settings")
public class MobileMoneySettings extends PanacheEntity {

    /** sandbox | production */
    @Column(length = 40)
    public String mtnTargetEnvironment = "sandbox";

    @Column(length = 500)
    public String mtnBaseUrl = "https://sandbox.momodeveloper.mtn.com";

    @Column(length = 255)
    public String mtnSubscriptionKey;

    @Column(length = 255)
    public String mtnApiUser;

    @Column(length = 255)
    public String mtnApiKey;

    @Column(length = 500)
    public String airtelBaseUrl = "https://openapiuat.airtel.africa";

    @Column(length = 255)
    public String airtelClientId;

    @Column(length = 255)
    public String airtelClientSecret;

    @Column(length = 10)
    public String airtelCountry = "UG";

    @Column(length = 10)
    public String airtelCurrency = "UGX";

    public Boolean enabled = Boolean.TRUE;

    public LocalDateTime updatedAt;
}
