package org.example.procedure.procedure.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class ProcedureCategoryRequest {
    
    @Schema(example = "artesunate")
    public String name;

    @Schema(example = "1")
    public Long parentId;
}










