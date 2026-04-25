package org.example.inventory.item.endpoints;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.inventory.item.services.DosageFormService;
import org.example.inventory.item.services.payloads.requests.DosageFormRequest;
import org.example.inventory.item.services.payloads.responses.basicResponses.DosageFormResponse;

@Path("/Hospital-item")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Hospital Management Module", description = "Hospital Management")

public class DosageFormController {

    @Inject
    DosageFormService dosageFormService;

    @POST
    @Path("/add-new-dosage-form")
    @Transactional
    @Operation(summary = "Add a new dosage form", description = "Add a new dosage form for product variants")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = DosageFormResponse.class)))
    public Response addFormulation(DosageFormRequest request) {
        return dosageFormService.createDosageForm(request);
    }
}
