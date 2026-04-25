package org.example.client.services.payloads.requests;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PatientUpdateRequest {
    ///////////////////// patient data//////////////////////////

    @Schema(example = "Tumwesigye")
    public String patientFirstName;

    @Schema(example = "Cryton")
    public String patientSecondName;

    @Schema(example = "Bugogo")
    public String patientAddress;

    @Schema(example = "256784411848")
    public String patientContact;

    @Schema(example = "Male")
    public String patientGender;

    @Schema(example = "peasant")
    public String occupation;


    @Schema(example = "28")
    public BigDecimal patientAge;

    @Schema(example = "1994/02/24")
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate patientDateOfBirth;  // Matches the "dd/MM/yyyy" format in JSON


    ///////////////////// Next of keen data//////////////////////////

    @Schema(example = "Tumwesigye Benjamin")
    public String nextOfKinName;

    @Schema(example = "256702225307")
    public String nextOfKinContact;

    @Schema(example = "Brother")
    public String relationship;

    @Schema(example = "Kampala")
    public String nextOfKinAddress;

    @Schema(example = "52")
    public Long patientGroupId;


    @Schema(example = "admin")
    public String userRole;

}






