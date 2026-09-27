package org.example.user.services.payLoads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;


public class UserRequest {

    @Schema(example = "Cryton")
    public String username;

    @Schema(example = "herdsontisk@gmail.com")
    public String email;

    @Schema(example = "Hope@9199")
    public String password;

    @Schema(example = "admin")
    public String role;

    @Schema(example = "lab,sono", description = "Comma-separated secondary roles (optional)")
    public String secondaryRoles;

    @Schema(example = "MBChB")
    public String qualification;

    @Schema(example = "MED12345")
    public String registrationNumber;

    @Schema(example = "active")
    public String status;

    @Schema(example = "256784411848")
    public String contact;

    @Schema(example = "https://firebasestorage.googleapis.com/v0/b/newstorageforuplodapp.appspot.com/o/images%2F1%20(3).jpg?alt=media&token=7a7ac099-cccd-4982-a93b-2912af397211")
    public String profilePic;


}






