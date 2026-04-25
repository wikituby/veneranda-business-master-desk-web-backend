package org.example.user.services;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class AgentUserRequest {

    @Schema(example = "benja")
    public String username;

    @Schema(example = "benja@gmail.com")
    public String email;

    @Schema(example = "123")
    public String password;

    @Schema(example = "AGENT")
    public String role;


}
