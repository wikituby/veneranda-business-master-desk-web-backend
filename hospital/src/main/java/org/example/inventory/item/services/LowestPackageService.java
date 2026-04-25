package org.example.inventory.item.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.item.domain.LowestPackage;
import org.example.inventory.item.domain.repositories.LowestPackageRepository;
import org.example.inventory.item.services.payloads.requests.LowestPackageRequest;
import org.example.inventory.item.services.payloads.responses.dtos.LowestPackageDTO;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class LowestPackageService {

    @Inject
    LowestPackageRepository lowestPackageRepository;

    // ✅ CREATE
    @Transactional
    public Response addNewLowestPackage(LowestPackageRequest request) {
        LowestPackage existing = lowestPackageRepository.find("title", request.title).firstResult();
        if (existing != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Lowest package with title '" + request.title + "' already exists."))
                    .build();
        }

        LowestPackage pkg = new LowestPackage();
        pkg.title = request.title;
        pkg.standardAbbreviation = request.standardAbbreviation;
        pkg.description = request.description;

        lowestPackageRepository.persist(pkg);

        return Response.ok(new ResponseMessage("Lowest package created successfully", new LowestPackageDTO(pkg))).build();
    }

    // ✅ UPDATE
    @Transactional
    public Response updateLowestPackage(Long id, LowestPackageRequest request) {
        LowestPackage pkg = lowestPackageRepository.findById(id);
        if (pkg == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Lowest package not found for ID: " + id))
                    .build();
        }

        pkg.title = request.title;
        pkg.standardAbbreviation = request.standardAbbreviation;
        pkg.description = request.description;

        return Response.ok(new ResponseMessage("Lowest package updated successfully", new LowestPackageDTO(pkg))).build();
    }

    // ✅ DELETE
    @Transactional
    public Response deleteLowestPackage(Long id) {
        LowestPackage pkg = lowestPackageRepository.findById(id);
        if (pkg == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Lowest package not found for ID: " + id))
                    .build();
        }

        lowestPackageRepository.delete(pkg);
        return Response.ok(new ResponseMessage("Lowest package deleted successfully")).build();
    }

    // ✅ GET ALL
    @Transactional
    public List<LowestPackageDTO> getAllLowestPackages() {
        return lowestPackageRepository.listAll()
                .stream()
                .map(LowestPackageDTO::new)
                .collect(Collectors.toList());
    }

    // ✅ GET BY ID
    @Transactional
    public Response getLowestPackageById(Long id) {
        LowestPackage pkg = lowestPackageRepository.findById(id);
        if (pkg == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Lowest package not found for ID: " + id))
                    .build();
        }

        return Response.ok(new ResponseMessage("Lowest package retrieved successfully", new LowestPackageDTO(pkg))).build();
    }
}
