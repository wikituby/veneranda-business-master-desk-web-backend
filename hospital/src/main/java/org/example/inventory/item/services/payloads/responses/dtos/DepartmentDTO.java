package org.example.inventory.item.services.payloads.responses.dtos;

import org.example.inventory.item.domain.Department;

import java.time.LocalDate;

public class DepartmentDTO {

    public Long id;
    public String title;
    public String description;
    public LocalDate creationDate;
    public LocalDate updateDate;

    public DepartmentDTO(Department dept) {
        this.id = dept.id;
        this.title = dept.title;
        this.description = dept.description;
        this.creationDate = dept.creationDate;
        this.updateDate = dept.updateDate;
    }
}
