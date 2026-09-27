package org.example.user.services.payLoads.requests;

import java.util.List;

public class RoleEndpointAccessRequest {
    public List<String> endpointKeys;
    public String allowedEndpointKeys;
}
