package org.example.queue.services.payloads.requests;

public class HospitalClinicRequest {

    public Long hospitalModuleId;

    public String name;

    public Boolean active;

    /** Comma-separated allowed roles (blank/null => all roles). */
    public String allowedRoles;
}
