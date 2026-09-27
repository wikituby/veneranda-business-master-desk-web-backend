package org.example.user.services.payLoads.requests;

public class UpdateRequest {

    public String username;

    public String email;

    public String contact;

    public String qualification;

    public String profilePic;

    public String role;

    /** Comma-separated secondary roles (optional). Primary role remains in {@link #role}. */
    public String secondaryRoles;

    public String status;

    /** Comma-separated hospital module ids, e.g. "1,3,5" */
    public String assignedModuleIds;

    /** Comma-separated hospital clinic ids, e.g. "2,4" */
    public String assignedClinicIds;

    /** Comma-separated overview page keys, e.g. "all-patients,queue-management" */
    public String allowedPageRoutes;

    /** Comma-separated page.component keys to hide, e.g. "otc-dispensary.sold-summary-footer" */
    public String hiddenPageComponents;

    /** When set, password is reset/updated (admin reset or profile change). */
    public String password;

}






