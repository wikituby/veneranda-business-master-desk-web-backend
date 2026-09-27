package org.example.finance.incomes.services.payloads.responses;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.example.finance.incomes.domains.IncomeSource;
import java.time.LocalDate;

public class IncomeSourceDto {
    public Long id;
    public String sourceName;
    public String description;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate dateCreated;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate dateUpdated;

    public IncomeSourceDto(IncomeSource source) {
        if (source == null) {
            return;
        }
        this.id = source.id;
        this.sourceName = source.sourceName;
        this.description = source.description;
        this.dateCreated = source.dateCreated;
        this.dateUpdated = source.dateUpdated;
    }
}
