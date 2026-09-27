package org.example.finance.incomes.endpoints;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.finance.incomes.services.IncomeSourceService;
import org.example.finance.incomes.services.payloads.requests.IncomeSourceRequest;
import org.example.finance.incomes.services.payloads.responses.IncomeSourceDto;
import java.util.List;

@Path("financial-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "financial Management Module", description = "financial Management")
public class IncomeSourceController {

    @Inject
    IncomeSourceService incomeSourceService;

    @POST
    @Path("create-new-income-source")
    @Transactional
    public Response createIncomeSource(IncomeSourceRequest request) {
        return incomeSourceService.createIncomeSource(request);
    }

    @GET
    @Transactional
    @Path("/get-all-income-sources")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = IncomeSourceDto.class, type = SchemaType.ARRAY)))
    public Response getAllIncomeSources() {
        List<IncomeSourceDto> list = incomeSourceService.getAllIncomeSources();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, list)).build();
    }

    @PUT
    @Path("update-income-source/{id}")
    @Transactional
    public Response updateIncomeSource(@PathParam("id") Long id, IncomeSourceRequest request) {
        return incomeSourceService.updateIncomeSource(id, request);
    }

    @DELETE
    @Path("delete-income-source/{id}")
    @Transactional
    public Response deleteIncomeSource(@PathParam("id") Long id) {
        return incomeSourceService.deleteIncomeSource(id);
    }
}
