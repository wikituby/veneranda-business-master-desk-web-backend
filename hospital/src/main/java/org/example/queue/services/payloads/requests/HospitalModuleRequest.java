package org.example.queue.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class HospitalModuleRequest {

    @Schema(example = "RECEPTION")
    public String code;

    @Schema(example = "Reception")
    public String name;

    public String description;

    public Boolean active;

    public Integer sortOrder;

    public String routeKey;

    /** Comma-separated allowed roles (blank/null => all roles). */
    public String allowedRoles;
}
