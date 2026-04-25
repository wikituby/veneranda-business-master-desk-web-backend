package org.example.inventory.item.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.item.domain.Manufacturer;
import org.example.inventory.item.domain.repositories.ManufacturerRepository;
import org.example.inventory.item.services.payloads.requests.ManufacturerRequest;
import org.example.inventory.item.services.payloads.responses.dtos.ManufacturerDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ManufacturerService {

    @Inject
    ManufacturerRepository manufacturerRepository;

    // ✅ CREATE
    @Transactional
    public Response addNewManufacturer(ManufacturerRequest request) {
        // Map frontend fields to backend fields
        String manufacturerName = request.manufacturerName != null ? request.manufacturerName : request.name;
        String contact = request.contact != null ? request.contact : request.phone;
        String physicalAddress = request.physicalAddress != null ? request.physicalAddress : request.address;
        String emailAddress = request.emailAddress != null ? request.emailAddress : request.email;
        String countryOfOrigin = request.countryOfOrigin != null ? request.countryOfOrigin : request.country;

        // Build full address from components if needed
        if (physicalAddress == null || physicalAddress.isEmpty()) {
            if (request.address != null && !request.address.isEmpty()) {
                physicalAddress = request.address;
            }
        }

        // Validate required field
        if (manufacturerName == null || manufacturerName.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Manufacturer name is required"))
                    .build();
        }

        Manufacturer existing = manufacturerRepository.find("manufacturerName", manufacturerName).firstResult();
        if (existing != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Manufacturer with name '" + manufacturerName + "' already exists."))
                    .build();
        }

        Manufacturer manufacturer = new Manufacturer();
        manufacturer.manufacturerName = manufacturerName;
        manufacturer.abbreviation = request.abbreviation;
        manufacturer.contact = contact;
        manufacturer.emailAddress = emailAddress;
        manufacturer.physicalAddress = physicalAddress;
        manufacturer.webSiteAddress = request.webSiteAddress;
        manufacturer.countryOfOrigin = countryOfOrigin;
        manufacturer.description = request.description;
        manufacturer.creationDateTime = LocalDateTime.now();

        manufacturerRepository.persist(manufacturer);

        return Response.ok(new ResponseMessage("Manufacturer created successfully", new ManufacturerDTO(manufacturer))).build();
    }

    // ✅ UPDATE
    @Transactional
    public Response updateManufacturer(Long id, ManufacturerRequest request) {
        Manufacturer manufacturer = manufacturerRepository.findById(id);
        if (manufacturer == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Manufacturer not found for ID: " + id))
                    .build();
        }

        // Map frontend fields to backend fields
        String manufacturerName = request.manufacturerName != null ? request.manufacturerName : request.name;
        String contact = request.contact != null ? request.contact : request.phone;
        String physicalAddress = request.physicalAddress != null ? request.physicalAddress : request.address;
        String emailAddress = request.emailAddress != null ? request.emailAddress : request.email;
        String countryOfOrigin = request.countryOfOrigin != null ? request.countryOfOrigin : request.country;

        // Build full address from components if needed
        if (physicalAddress == null || physicalAddress.isEmpty()) {
            if (request.address != null && !request.address.isEmpty()) {
                physicalAddress = request.address;
            }
        }

        // Update fields (only if provided)
        if (manufacturerName != null && !manufacturerName.trim().isEmpty()) {
            manufacturer.manufacturerName = manufacturerName;
        }
        if (request.abbreviation != null) {
            manufacturer.abbreviation = request.abbreviation;
        }
        if (contact != null) {
            manufacturer.contact = contact;
        }
        if (emailAddress != null) {
            manufacturer.emailAddress = emailAddress;
        }
        if (physicalAddress != null) {
            manufacturer.physicalAddress = physicalAddress;
        }
        if (request.webSiteAddress != null) {
            manufacturer.webSiteAddress = request.webSiteAddress;
        }
        if (countryOfOrigin != null) {
            manufacturer.countryOfOrigin = countryOfOrigin;
        }
        if (request.description != null) {
            manufacturer.description = request.description;
        }
        manufacturer.upDateTime = LocalDateTime.now();

        manufacturerRepository.persist(manufacturer);

        return Response.ok(new ResponseMessage("Manufacturer updated successfully", new ManufacturerDTO(manufacturer))).build();
    }

    // ✅ DELETE
    @Transactional
    public Response deleteManufacturer(Long id) {
        Manufacturer manufacturer = manufacturerRepository.findById(id);
        if (manufacturer == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Manufacturer not found for ID: " + id))
                    .build();
        }

        manufacturerRepository.delete(manufacturer);
        return Response.ok(new ResponseMessage("Manufacturer deleted successfully")).build();
    }

    // ✅ GET ALL
    public List<ManufacturerDTO> getAllManufacturers() {
        return manufacturerRepository.listAll(Sort.descending("id"))
                .stream()
                .map(ManufacturerDTO::new)
                .collect(Collectors.toList());
    }

    // ✅ GET BY ID
    public Response getManufacturerById(Long id) {
        Manufacturer manufacturer = manufacturerRepository.findById(id);
        if (manufacturer == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Manufacturer not found for ID: " + id))
                    .build();
        }

        return Response.ok(new ResponseMessage("Manufacturer retrieved successfully", new ManufacturerDTO(manufacturer))).build();
    }
}

