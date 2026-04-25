package org.example.fileresource.service.payload;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class FileRequestBytes {

    @Schema(example = "default_image")
    public String name; // common name of the file as seen in client filesystem

    @Schema(required = true, example = "png")
    public String fileType;

    @Schema(required = true, example = "Base 64 string")
    public byte[] data; // base 64 String
}






