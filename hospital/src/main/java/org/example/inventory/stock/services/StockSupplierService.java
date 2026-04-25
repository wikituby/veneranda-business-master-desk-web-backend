package org.example.inventory.stock.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.stock.domains.StockSupplier;
import org.example.inventory.stock.domains.repositories.StockSupplierRepository;
import org.example.inventory.stock.services.payloads.requests.StockSupplierRequest;
import org.example.inventory.stock.services.payloads.responses.dtos.StockSupplierDTO;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class StockSupplierService {

    @Inject
    StockSupplierRepository stockSupplierRepository;

    // ✅ CREATE
    @Transactional
    public Response addNewStockSupplier(StockSupplierRequest request) {
        StockSupplier existing = stockSupplierRepository.find("supplierName", request.supplierName).firstResult();
        if (existing != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Supplier with name '" + request.supplierName + "' already exists."))
                    .build();
        }

        StockSupplier supplier = new StockSupplier();
        supplier.supplierName = request.supplierName;
        supplier.abbreviation = request.abbreviation;
        supplier.contact = request.contact;
        supplier.emailAddress = request.emailAddress;
        supplier.physicalAddress = request.physicalAddress;
        supplier.webSiteAddress = request.webSiteAddress;
        supplier.description = request.description;
        supplier.creationDateTime = LocalDateTime.now();

        stockSupplierRepository.persist(supplier);

        return Response.ok(new ResponseMessage("Supplier created successfully", new StockSupplierDTO(supplier))).build();
    }

    // ✅ UPDATE
    @Transactional
    public Response updateStockSupplier(Long id, StockSupplierRequest request) {
        StockSupplier supplier = stockSupplierRepository.findById(id);
        if (supplier == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Supplier not found for ID: " + id))
                    .build();
        }

        supplier.supplierName = request.supplierName;
        supplier.abbreviation = request.abbreviation;
        supplier.contact = request.contact;
        supplier.emailAddress = request.emailAddress;
        supplier.physicalAddress = request.physicalAddress;
        supplier.webSiteAddress = request.webSiteAddress;
        supplier.description = request.description;
        supplier.upDateTime = LocalDateTime.now();
        

        return Response.ok(new ResponseMessage("Supplier updated successfully", new StockSupplierDTO(supplier))).build();
    }

    // ✅ DELETE
    @Transactional
    public Response deleteStockSupplier(Long id) {
        StockSupplier supplier = stockSupplierRepository.findById(id);
        if (supplier == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Supplier not found for ID: " + id))
                    .build();
        }

        stockSupplierRepository.delete(supplier);
        return Response.ok(new ResponseMessage("Supplier deleted successfully")).build();
    }

    // ✅ GET ALL
    public List<StockSupplierDTO> getAllStockSuppliers() {
        return stockSupplierRepository.listAll()
                .stream()
                .sorted(Comparator.comparing((StockSupplier s) -> s.id).reversed())
                .map(StockSupplierDTO::new)
                .collect(Collectors.toList());
    }

    // ✅ GET BY ID
    public Response getStockSupplierById(Long id) {
        StockSupplier supplier = stockSupplierRepository.findById(id);
        if (supplier == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Supplier not found for ID: " + id))
                    .build();
        }

        return Response.ok(new ResponseMessage("Supplier retrieved successfully", new StockSupplierDTO(supplier))).build();
    }
}
