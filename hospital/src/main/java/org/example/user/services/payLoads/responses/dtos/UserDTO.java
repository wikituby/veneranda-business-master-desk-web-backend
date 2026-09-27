package org.example.user.services.payLoads.responses.dtos;

import org.example.user.domains.User;

public class UserDTO {
    public Long id;
    public String username;
    public String email;
    public String role;
    public String secondaryRoles;
    public String qualification;
    public String registrationNumber;
    public String status;
    public String profilePic;
    public String contact;
    public String assignedModuleIds;
    public String assignedClinicIds;
    public String allowedPageRoutes;
    public String hiddenPageComponents;
    public Integer registryItemsPerPage;

    public UserDTO(User user) {
        if (user != null) {
            this.id = user.id;
            this.username = user.username;
            this.email = user.email;
            this.role = user.role;
            this.secondaryRoles = user.secondaryRoles;
            this.qualification = user.qualification;
            this.registrationNumber = user.registrationNumber;
            this.status = user.status;
            this.profilePic = user.profilePic;
            this.contact = user.contact;
            this.assignedModuleIds = user.assignedModuleIds;
            this.assignedClinicIds = user.assignedClinicIds;
            this.allowedPageRoutes = user.allowedPageRoutes;
            this.hiddenPageComponents = user.hiddenPageComponents;
            this.registryItemsPerPage = user.registryItemsPerPage;
        }
    }
}






