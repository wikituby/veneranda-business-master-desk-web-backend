package org.example.course.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class CourseUpdateRequest {

    @Schema(example = "Bachelors of Medicine and Bachelors of Surgery")
    public String title;

    @Schema(example = "Bachelors of Medicine and Bachelors of Surgery")
    public String description;

    @Schema(example = "Bachelors of Medicine and Bachelors of Surgery")
    public String details;

    @Schema(example = "https://firebasestorage.googleapis.com/v0/b/newstorageforuplodapp.appspot.com/o/images%2Fcomputer.jpg?alt=media&token=54728992-5370-4be3-91d2-05e54bac6042")
    public String image;

    @Schema(example = "Bachelors of Medicine and Bachelors of Surgery")
    public String parent;


}






