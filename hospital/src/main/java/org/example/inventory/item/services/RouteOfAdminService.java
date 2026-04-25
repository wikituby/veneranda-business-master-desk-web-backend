package org.example.inventory.item.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.item.domain.RouteOfAdmin;
import org.example.inventory.item.domain.repositories.RouteOfAdminRepository;
import org.example.inventory.item.services.payloads.requests.RouteOfAdminRequest;
import org.example.inventory.item.services.payloads.responses.dtos.RouteOfAdminDTO;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class RouteOfAdminService {

    @Inject
    RouteOfAdminRepository routeOfAdminRepository;

    // ✅ CREATE
    @Transactional
    public Response addNewRouteOfAdmin(RouteOfAdminRequest request) {
        RouteOfAdmin existing = routeOfAdminRepository.find("title", request.title).firstResult();
        if (existing != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Route of administration with title '" + request.title + "' already exists."))
                    .build();
        }

        RouteOfAdmin route = new RouteOfAdmin();
        route.title = request.title;
        route.standardAbbreviation = request.standardAbbreviation;
        route.description = request.description;

        routeOfAdminRepository.persist(route);

        return Response.ok(new ResponseMessage("Route of administration created successfully", new RouteOfAdminDTO(route))).build();
    }

    // ✅ UPDATE
    @Transactional
    public Response updateRouteOfAdmin(Long id, RouteOfAdminRequest request) {
        RouteOfAdmin route = routeOfAdminRepository.findById(id);
        if (route == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Route of administration not found for ID: " + id))
                    .build();
        }

        route.title = request.title;
        route.standardAbbreviation = request.standardAbbreviation;
        route.description = request.description;

        return Response.ok(new ResponseMessage("Route of administration updated successfully", new RouteOfAdminDTO(route))).build();
    }

    // ✅ DELETE
    @Transactional
    public Response deleteRouteOfAdmin(Long id) {
        RouteOfAdmin route = routeOfAdminRepository.findById(id);
        if (route == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Route of administration not found for ID: " + id))
                    .build();
        }

        routeOfAdminRepository.delete(route);
        return Response.ok(new ResponseMessage("Route of administration deleted successfully")).build();
    }

    // ✅ GET ALL
    @Transactional
    public List<RouteOfAdminDTO> getAllRoutesOfAdmin() {
        return routeOfAdminRepository.listAll()
                .stream()
                .map(RouteOfAdminDTO::new)
                .collect(Collectors.toList());
    }

    // ✅ GET BY ID
    @Transactional
    public Response getRouteOfAdminById(Long id) {
        RouteOfAdmin route = routeOfAdminRepository.findById(id);
        if (route == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Route of administration not found for ID: " + id))
                    .build();
        }

        return Response.ok(new ResponseMessage("Route of administration retrieved successfully", new RouteOfAdminDTO(route))).build();
    }
}
