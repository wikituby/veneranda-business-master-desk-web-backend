package org.example.auth.services.payloads;

import io.smallrye.common.constraint.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class ResetPasswordRequest {

    @NotNull
    @Schema(example = "herdsontisk@gmail.com")
    public String email;
}






