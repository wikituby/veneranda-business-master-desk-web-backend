package org.example.subscription.mobilemoney.payloads;

import org.example.subscription.mobilemoney.domains.MobileMoneySettings;

public class MobileMoneySettingsDTO {
    public Long id;
    public String mtnTargetEnvironment;
    public String mtnBaseUrl;
    public String mtnSubscriptionKey;
    public String mtnApiUser;
    public String mtnApiKey;
    public boolean mtnConfigured;
    public String airtelBaseUrl;
    public String airtelClientId;
    public String airtelClientSecret;
    public String airtelCountry;
    public String airtelCurrency;
    public boolean airtelConfigured;
    public Boolean enabled;
    public String updatedAt;
    public String hint;

    public static MobileMoneySettingsDTO from(MobileMoneySettings s, boolean mtnLive, boolean airtelLive) {
        MobileMoneySettingsDTO dto = new MobileMoneySettingsDTO();
        dto.id = s.id;
        dto.mtnTargetEnvironment = s.mtnTargetEnvironment;
        dto.mtnBaseUrl = s.mtnBaseUrl;
        dto.mtnSubscriptionKey = s.mtnSubscriptionKey;
        dto.mtnApiUser = s.mtnApiUser;
        dto.mtnApiKey = s.mtnApiKey;
        dto.mtnConfigured = mtnLive;
        dto.airtelBaseUrl = s.airtelBaseUrl;
        dto.airtelClientId = s.airtelClientId;
        dto.airtelClientSecret = s.airtelClientSecret;
        dto.airtelCountry = s.airtelCountry;
        dto.airtelCurrency = s.airtelCurrency;
        dto.airtelConfigured = airtelLive;
        dto.enabled = s.enabled;
        dto.updatedAt = s.updatedAt != null ? s.updatedAt.toString() : null;
        dto.hint = "Keys are stored in the database and used at runtime (they override application.properties).";
        return dto;
    }
}
