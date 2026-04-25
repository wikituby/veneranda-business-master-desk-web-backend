package org.example.visit.services.paloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

public class PatientVisitUpdateRequest {

    @Schema(example = "admission")
    public String visitType;

    @Schema(example = "Review")
    public String visitReason;

    @Schema(example = "28")
    public BigDecimal patientAge;

    @Schema(example = "Bugogo")
    public String patientAddress;

    @Schema(example = "256784411848")
    public String patientContact;

    @Schema(example = "peasant")
    public String occupation;

    @Schema(example = "Tumwesigye Benjamin")
    public String nextOfKinName;

    @Schema(example = "256702225307")
    public String nextOfKinContact;

    @Schema(example = "Brother")
    public String relationship;

    @Schema(example = "Kampala")
    public String nextOfKinAddress;
}
