package org.example.admissions.services.payloads.requests;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class InPatientTreatmentRequest {

    @Schema(example = "Ceftriaxone, ")
    public String medicine;

    @Schema(example = "2g BD 3/7, ")
    public String dose;

    @Schema(example = "8 hourly, ")
    public String frequency;

    @Schema(example = "IM/IV, ")
    public String route;

    @Schema(example = "Dr.Judith, ")
    public String administeredByInitials;

    @Schema(example = "1")
    public Long visitId;



}






