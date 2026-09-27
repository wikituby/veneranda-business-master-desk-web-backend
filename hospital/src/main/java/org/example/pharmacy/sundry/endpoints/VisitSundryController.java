package org.example.pharmacy.sundry.endpoints;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.pharmacy.sundry.services.VisitSundryService;
import org.example.pharmacy.sundry.services.payloads.requests.VisitSundryRequest;
import org.example.pharmacy.sundry.services.payloads.responses.VisitSundryDTO;

import java.util.List;

@Path("Client-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Client Management Module", description = "Client Management")
public class VisitSundryController {

    @Inject
    VisitSundryService visitSundryService;

    @POST
    @Path("create-visit-sundry/{visitId}")
    @Transactional
    @Operation(summary = "Record pharmacy sundry used on a visit and deduct stock")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = VisitSundryDTO.class)))
    public Response createVisitSundry(@PathParam("visitId") Long visitId, VisitSundryRequest request) {
        return visitSundryService.addVisitSundry(visitId, request);
    }

    @GET
    @Path("get-visit-sundries-by-visit-id/{visitId}")
    @Operation(summary = "List sundries used on a patient visit")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = VisitSundryDTO.class)))
    public Response getVisitSundriesByVisitId(@PathParam("visitId") Long visitId) {
        List<VisitSundryDTO> rows = visitSundryService.getVisitSundriesByVisitId(visitId);
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, rows)).build();
    }

    @DELETE
    @Path("delete-visit-sundry/{id}")
    @Transactional
    @Operation(summary = "Remove a visit sundry and restore stock")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteVisitSundry(@PathParam("id") Long id) {
        return visitSundryService.deleteVisitSundry(id);
    }
}
