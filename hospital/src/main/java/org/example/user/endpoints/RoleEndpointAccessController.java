package org.example.user.endpoints;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.configuration.security.EndpointAccessCatalog;
import org.example.configuration.security.EndpointAccessService;
import org.example.user.domains.RoleEndpointAccess;
import org.example.user.domains.User;
import org.example.user.domains.repositories.RoleEndpointAccessRepository;
import org.example.user.domains.repositories.UserRepository;
import org.example.user.services.payLoads.requests.RoleEndpointAccessRequest;
import org.example.user.services.payLoads.responses.dtos.RoleEndpointAccessDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Path("user-management/role-endpoint-access")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "User Management Module", description = "Role endpoint access")
public class RoleEndpointAccessController {

    @Inject
    EndpointAccessService endpointAccessService;

    @Inject
    RoleEndpointAccessRepository roleEndpointAccessRepository;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    UserRepository userRepository;

    @GET
    @Path("catalog")
    @Operation(summary = "List protectable API endpoints")
    public Response catalog() {
        requireAccessManager();
        List<EndpointAccessCatalog.EndpointDefinition> defs = EndpointAccessCatalog.allDefinitions();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, defs)).build();
    }

    @GET
    @Operation(summary = "List role endpoint access rows")
    public Response listAll() {
        requireAccessManager();
        List<RoleEndpointAccessDTO> rows = roleEndpointAccessRepository.findAllOrdered().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, rows)).build();
    }

    @GET
    @Path("{role}")
    @Operation(summary = "Get endpoint access for a role")
    public Response getByRole(@PathParam("role") String role) {
        requireAccessManager();
        return roleEndpointAccessRepository.findByRoleNormalized(role)
                .map(row -> Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, toDto(row))).build())
                .orElse(Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,
                        new RoleEndpointAccessDTO())).build());
    }

    @PUT
    @Path("{role}")
    @Transactional
    @Operation(summary = "Save endpoint access for a role")
    public Response save(@PathParam("role") String role, RoleEndpointAccessRequest request) {
        requireAccessManager();
        List<String> keys = resolveKeys(request);
        RoleEndpointAccess saved = endpointAccessService.upsertRoleEndpoints(role, keys);
        return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label, toDto(saved))).build();
    }

    private List<String> resolveKeys(RoleEndpointAccessRequest request) {
        if (request == null) {
            return List.of();
        }
        if (request.endpointKeys != null && !request.endpointKeys.isEmpty()) {
            return request.endpointKeys;
        }
        return new ArrayList<>(EndpointAccessCatalog.parseEndpointKeysCsv(request.allowedEndpointKeys));
    }

    private RoleEndpointAccessDTO toDto(RoleEndpointAccess row) {
        List<String> keys = EndpointAccessCatalog.parseEndpointKeysCsv(row.allowedEndpointKeys);
        return new RoleEndpointAccessDTO(row, keys);
    }

    private void requireAccessManager() {
        if (securityIdentity == null || securityIdentity.isAnonymous()) {
            throw new WebApplicationException("Authentication required", 401);
        }
        String principal = securityIdentity.getPrincipal() != null
                ? securityIdentity.getPrincipal().getName()
                : "";
        User user = userRepository.findByEmailOptional(principal)
                .or(() -> userRepository.find("username", principal).firstResultOptional())
                .orElse(null);
        if (user == null || !endpointAccessService.isPrivilegedRole(user.role)) {
            throw new WebApplicationException("Admin access required", 403);
        }
    }
}
