package org.example.user.services.payLoads.responses.dtos;

import org.example.user.domains.User;

public class UserDTO {
    public Long id;
    public String username;
    public String email;
    public String role;
    public String qualification;
    public String registrationNumber;
    public String status;
    public String profilePic;
    public String contact;

    public UserDTO(User user) {
        if (user != null) {
            this.id = user.id;
            this.username = user.username;
            this.email = user.email;
            this.role = user.role;
            this.qualification = user.qualification;
            this.registrationNumber = user.registrationNumber;
            this.status = user.status;
            this.profilePic = user.profilePic;
            this.contact = user.contact;
        }
    }
}






