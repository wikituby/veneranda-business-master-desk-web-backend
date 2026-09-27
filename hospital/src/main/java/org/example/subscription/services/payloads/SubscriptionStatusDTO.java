package org.example.subscription.services.payloads;

import java.time.LocalDateTime;

public class SubscriptionStatusDTO {
    public Long facilityId;
    public String facilityName;
    public String facilityAddress;
    public boolean active;
    public String status;
    public String subscribedModuleKeys;
    public LocalDateTime periodStart;
    public LocalDateTime periodEnd;
    public String message;
}
