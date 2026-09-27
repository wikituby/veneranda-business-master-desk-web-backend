package org.example.user.services.payLoads.responses.dtos;

import org.example.user.domains.RoleEndpointAccess;

import java.util.List;

public class RoleEndpointAccessDTO {
    public String role;
    public String allowedEndpointKeys;
    public List<String> endpointKeys;

    public RoleEndpointAccessDTO() {
    }

    public RoleEndpointAccessDTO(RoleEndpointAccess entity, List<String> keys) {
        this.role = entity != null ? entity.role : null;
        this.allowedEndpointKeys = entity != null ? entity.allowedEndpointKeys : null;
        this.endpointKeys = keys;
    }
}
