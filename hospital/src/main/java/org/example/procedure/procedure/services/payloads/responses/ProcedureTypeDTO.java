package org.example.procedure.procedure.services.payloads.responses;

import org.example.procedure.procedure.domains.ProcedureCategory;
import org.example.procedure.procedure.domains.ProcedureType;

public class ProcedureTypeDTO {

    public Long id;
    public String procedureType;
    public String typeDescription;


    public ProcedureTypeDTO(ProcedureType type) {
        this.id = type.id;
        this.procedureType = type.procedureType;
        this.typeDescription = type.typeDescription;


    }

}
