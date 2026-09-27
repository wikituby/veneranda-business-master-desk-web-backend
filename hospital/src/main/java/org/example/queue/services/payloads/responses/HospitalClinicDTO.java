package org.example.queue.services.payloads.responses;

import org.example.queue.domains.HospitalClinic;

public class HospitalClinicDTO {

    public Long id;
    public Long hospitalModuleId;
    public String hospitalModuleName;
    public String name;
    public boolean active;
    public String allowedRoles;

    public HospitalClinicDTO() {
    }

    public HospitalClinicDTO(HospitalClinic clinic) {
        this.id = clinic.id;
        this.hospitalModuleId = clinic.hospitalModule != null ? clinic.hospitalModule.id : null;
        this.hospitalModuleName = clinic.hospitalModule != null ? clinic.hospitalModule.name : null;
        this.name = clinic.name;
        this.active = clinic.active;
        this.allowedRoles = clinic.allowedRoles;
    }
}
