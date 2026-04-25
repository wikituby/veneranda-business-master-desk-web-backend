package org.example.auth.services.payloads;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class UserAuthRequest {

    @Schema(example = "md@vmd.com")
    public String email;

    @Schema(example = "123")
    public String password;


    public UserAuthRequest() {
    }

    public UserAuthRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}






