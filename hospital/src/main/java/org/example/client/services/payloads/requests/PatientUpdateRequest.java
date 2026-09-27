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

    /** Business UI alias for {@link #patientFirstName}. */
    public String clientFirstName;

    /** Business UI alias for {@link #patientSecondName}. */
    public String clientSecondName;

    @Schema(example = "Bugogo")
    public String patientAddress;

    public String clientAddress;

    @Schema(example = "256784411848")
    public String patientContact;

    public String clientContact;

    @Schema(example = "Male")
    public String patientGender;

    public String clientGender;

    @Schema(example = "peasant")
    public String occupation;

    @Schema(example = "O+")
    public String bloodGroup;

    @Schema(example = "28")
    public BigDecimal patientAge;

    public BigDecimal clientAge;

    @Schema(example = "1994/02/24")
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate patientDateOfBirth;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate clientDateOfBirth;

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

    public Long clientGroupId;

    /** All group memberships. When set, replaces membership; primary is {@link #patientGroupId} or first id. */
    @Schema(example = "[52, 61]")
    public java.util.List<Long> patientGroupIds;

    public java.util.List<Long> clientGroupIds;

    @Schema(example = "admin")
    public String userRole;

    /**
     * Maps business-app {@code client*} fields onto entity {@code patient*} fields
     * when the latter are missing.
     */
    public void normalizeClientAliases() {
        patientFirstName = firstNonBlank(patientFirstName, clientFirstName);
        patientSecondName = firstNonBlank(patientSecondName, clientSecondName);
        patientAddress = firstNonBlank(patientAddress, clientAddress);
        patientContact = firstNonBlank(patientContact, clientContact);
        patientGender = firstNonBlank(patientGender, clientGender);
        if (patientAge == null) {
            patientAge = clientAge;
        }
        if (patientDateOfBirth == null) {
            patientDateOfBirth = clientDateOfBirth;
        }
        if (patientGroupId == null) {
            patientGroupId = clientGroupId;
        }
        if ((patientGroupIds == null || patientGroupIds.isEmpty()) && clientGroupIds != null && !clientGroupIds.isEmpty()) {
            patientGroupIds = clientGroupIds;
        }
        if (patientFirstName == null) {
            patientFirstName = "";
        }
        if (patientSecondName == null) {
            patientSecondName = "";
        }
    }

    private static String firstNonBlank(String primary, String alias) {
        if (primary != null && !primary.isBlank()) {
            return primary.trim();
        }
        if (alias != null && !alias.isBlank()) {
            return alias.trim();
        }
        return primary != null ? primary : alias;
    }
}
