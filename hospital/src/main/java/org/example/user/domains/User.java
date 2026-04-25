package org.example.user.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "user")
public class User extends PanacheEntity {

    @Column(unique = true,nullable = false)
    public String username;

    @Column(unique = true,nullable = false)
    public String email;

    @JsonbTransient
    @Column(unique = true,nullable = false)
    public String password;

    @Column
    public String role;

    @Column
    public String qualification;

    @Column
    public String registrationNumber;

    @Column
    public String status;

    @Column
    public String profilePic;

    @Column
    public String contact;

    public User() {
    }

    public User(String username, String email, String password, String role, String profilePic) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.profilePic = profilePic;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}






