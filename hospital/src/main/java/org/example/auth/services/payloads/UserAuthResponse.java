package org.example.auth.services.payloads;

import org.example.user.domains.User;

public class UserAuthResponse {
    public User user;

    public String jwt;

    public String userType;

    public UserAuthResponse() {
    }

    public UserAuthResponse(User user, String jwt, String userType) {
        this.user = user;
        this.jwt = jwt;
        this.userType = userType;
    }
}







