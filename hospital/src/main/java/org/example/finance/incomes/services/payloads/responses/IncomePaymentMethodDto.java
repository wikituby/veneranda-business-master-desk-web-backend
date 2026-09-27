package org.example.finance.incomes.services.payloads.responses;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.example.finance.incomes.domains.IncomePaymentMethod;
import java.time.LocalDate;

public class IncomePaymentMethodDto {
    public Long id;
    public String methodName;
    public String description;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate dateCreated;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate dateUpdated;

    public IncomePaymentMethodDto(IncomePaymentMethod method) {
        if (method == null) {
            return;
        }
        this.id = method.id;
        this.methodName = method.methodName;
        this.description = method.description;
        this.dateCreated = method.dateCreated;
        this.dateUpdated = method.dateUpdated;
    }
}
