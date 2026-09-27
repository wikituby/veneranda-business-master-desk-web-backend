package org.example.queue.services.payloads.responses;

import org.example.queue.domains.HospitalModule;

public class HospitalModuleDTO {

    public Long id;
    public String code;
    public String name;
    public String description;
    public boolean active;
    public Integer sortOrder;
    public String routeKey;
    public String allowedRoles;

    public HospitalModuleDTO() {
    }

    public HospitalModuleDTO(HospitalModule module) {
        this.id = module.id;
        this.code = module.code;
        this.name = module.name;
        this.description = module.description;
        this.active = module.active;
        this.sortOrder = module.sortOrder;
        this.routeKey = module.routeKey;
        this.allowedRoles = module.allowedRoles;
    }
}
