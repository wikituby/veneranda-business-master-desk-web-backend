package org.example.user.services;

import org.eclipse.microprofile.openapi.annotations.media.Schema;


public class UserRequest {

    @Schema(example = "Cryton")
    public String username;

    @Schema(example = "herdsontisk@gmail.com")
    public String email;

    @Schema(example = "Hope@9199")
    public String password;

    @Schema(example = "Hope@9199")
    public String contact;

    @Schema(example = "https://firebasestorage.googleapis.com/v0/b/newstorageforuplodapp.appspot.com/o/images%2F1%20(3).jpg?alt=media&token=7a7ac099-cccd-4982-a93b-2912af397211")
    public String profilePic;


}
