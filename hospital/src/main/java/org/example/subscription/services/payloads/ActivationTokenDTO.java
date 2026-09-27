package org.example.subscription.services.payloads;

import org.example.subscription.domains.ActivationToken;

import java.time.LocalDateTime;

public class ActivationTokenDTO {
    public Long id;
    public String token;
    public String facilityName;
    public String facilityAddress;
    public String subscribedModuleKeys;
    public Integer durationDays;
    public Integer durationMonths;
    public String status;
    public LocalDateTime usedAt;
    public Long usedByUserId;

    public static ActivationTokenDTO from(ActivationToken entity) {
        ActivationTokenDTO dto = new ActivationTokenDTO();
        dto.id = entity.id;
        dto.token = entity.token;
        dto.facilityName = entity.facilityName;
        dto.facilityAddress = entity.facilityAddress;
        dto.subscribedModuleKeys = entity.subscribedModuleKeys;
        dto.durationDays = entity.durationDays;
        dto.durationMonths = entity.durationMonths;
        dto.status = entity.status;
        dto.usedAt = entity.usedAt;
        dto.usedByUserId = entity.usedByUserId;
        return dto;
    }
}
