package org.example.treatment.services.payloads.requests;
import jakarta.persistence.Column;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

public class TreatmentRequestedRequest {

    @Schema(example = "1")
    public BigDecimal quantity;

    @Schema(example = "1")
    public Long itemId;

    /** Pharmacy stock batch to dispense from (stock checked and deducted on this batch). */
    @Schema(example = "1")
    public Long stockBatchId;

    @Schema(example = "1")
    public Long visitId;

    @Schema(example = "1")
    public BigDecimal durationValue;

    @Schema(example = "duration unit as day fraction: minute=0.00069444, hour=0.04166667, day=1, week=7, month=30")
    public BigDecimal durationUnit;

    @Schema(example = "2")
    public BigDecimal amountPerFrequencyValue;

    @Schema(example = "mg")
    public String amountPerFrequencyUnit;

    @Schema(example = "3")
    public BigDecimal frequencyValue;

    @Schema(example = "3")
    public BigDecimal totalUnits;

    @Schema(example = "frequency unit as day fraction: minute=0.00069444, hour=0.04166667, day=1, week=7, month=30")
    public BigDecimal frequencyUnit;


    @Schema(example = "after meals")
    public String instructions;

    @Schema(example = "Oral")
    public String route;

    /** Selected unit selling model for this treatment line. */
    @Schema(example = "1")
    public Long unitSellingModelId;

    /** Optional: link this treatment to a consultation diagnosis. */
    @Schema(example = "1")
    public Long diagnosisId;

}
