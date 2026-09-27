package org.example.inventory.stock.services.payloads.responses.dtos;

import org.example.inventory.stock.domains.StockConfidenceLevel;
import org.example.inventory.stock.domains.StockVerificationReason;
import org.example.inventory.stock.domains.VerificationStatus;

public class VerificationCatalogDto {
    public Long id;
    public String code;
    public String name;
    public String description;
    public Boolean active;
    public Integer displayOrder;
    public Boolean requiresDescription;
    public Boolean requiresApproval;
    public Integer minimumScore;
    public Integer maximumScore;

    public static VerificationCatalogDto fromStatus(VerificationStatus s) {
        VerificationCatalogDto dto = new VerificationCatalogDto();
        dto.id = s.id;
        dto.code = s.code;
        dto.name = s.name;
        dto.description = s.description;
        dto.active = s.active;
        dto.displayOrder = s.displayOrder;
        return dto;
    }

    public static VerificationCatalogDto fromReason(StockVerificationReason r) {
        VerificationCatalogDto dto = new VerificationCatalogDto();
        dto.id = r.id;
        dto.code = r.code;
        dto.name = r.name;
        dto.description = r.description;
        dto.active = r.active;
        dto.displayOrder = r.displayOrder;
        dto.requiresDescription = r.requiresDescription;
        dto.requiresApproval = r.requiresApproval;
        return dto;
    }

    public static VerificationCatalogDto fromConfidence(StockConfidenceLevel c) {
        VerificationCatalogDto dto = new VerificationCatalogDto();
        dto.id = c.id;
        dto.code = c.code;
        dto.name = c.name;
        dto.description = c.description;
        dto.active = c.active;
        dto.displayOrder = c.displayOrder;
        dto.minimumScore = c.minimumScore;
        dto.maximumScore = c.maximumScore;
        return dto;
    }
}