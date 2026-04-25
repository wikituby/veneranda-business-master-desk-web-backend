package org.example.inventory.item.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.item.domain.Department;
import org.example.inventory.item.domain.repositories.DepartmentRepository;
import org.example.inventory.item.services.payloads.requests.DepartmentRequest;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class DepartmentService {

    @Inject
    DepartmentRepository departmentRepository;

    @Transactional
    public ResponseMessage addDepartment(DepartmentRequest request) {
        Department dept = new Department();
        dept.title = request.title;
        dept.description = request.description;
        dept.creationDate = LocalDate.now();
        dept.updateDate = LocalDate.now();

        departmentRepository.persist(dept);
        return new ResponseMessage("Department added successfully");
    }

    public List<Department> getAllDepartments() {
        return departmentRepository.listAll();
    }

    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id);
    }

    @Transactional
    public ResponseMessage updateDepartment(Long id, DepartmentRequest request) {
        Department dept = departmentRepository.findById(id);
        if (dept == null) {
            return new ResponseMessage("Department not found");
        }

        dept.title = request.title;
        dept.description = request.description;
        dept.updateDate = LocalDate.now();

        return new ResponseMessage("Department updated successfully");
    }

    @Transactional
    public ResponseMessage deleteDepartment(Long id) {
        boolean deleted = departmentRepository.deleteById(id);
        return deleted ? new ResponseMessage("Department deleted successfully")
                : new ResponseMessage("Department not found");
    }
}
