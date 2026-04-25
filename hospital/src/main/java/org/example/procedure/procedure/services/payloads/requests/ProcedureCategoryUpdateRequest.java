package org.example.procedure.procedure.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class ProcedureCategoryUpdateRequest {
    @Schema(example = "1")
    public Long categoryId;

    @Schema(example = "Hematology")
    public Long parentCategoryId;

    @Schema(example = "1")
    public String name;

    @Schema(example = "1")
    public Long parentId;
}










