package org.example.messages.services.payloads.responses;

import org.example.procedure.procedureRequested.domains.ProcedureRequested;
import org.example.procedure.procedure.services.ProcedureCategoryUtils;

/**
 * Lightweight procedure-request payload for live department boards.
 */
public class ProcedureRequestPushDTO {
    public Long id;
    public Long visitId;
    public String procedureRequestedName;
    public String category;
    public String procedureCategoryName;
    public String categoryParentName;
    public String status;
    public String patientName;

    public ProcedureRequestPushDTO() {
    }

    public ProcedureRequestPushDTO(ProcedureRequested entity) {
        if (entity == null) {
            return;
        }
        this.id = entity.id;
        this.visitId = entity.visit != null ? entity.visit.id : null;
        this.procedureRequestedName = entity.procedureRequestedName;
        this.category = entity.category;
        if (entity.procedure != null && entity.procedure.category != null) {
            this.procedureCategoryName = entity.procedure.category.name;
        }
        this.categoryParentName = ProcedureCategoryUtils.resolveParentCategoryName(entity.procedure);
        if (this.categoryParentName == null && ProcedureCategoryUtils.isMchOrMaternityParentCategory(entity.category)) {
            this.categoryParentName = entity.category;
        }
        this.status = entity.status;
        this.patientName = entity.patientName;
    }
}
