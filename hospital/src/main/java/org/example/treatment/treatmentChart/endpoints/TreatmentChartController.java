package org.example.treatment.treatmentChart.endpoints;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.treatment.treatmentChart.services.TreatmentChartService;
import org.example.treatment.treatmentChart.services.payloads.requests.TreatmentChartRequest;
import org.example.treatment.treatmentChart.services.payloads.responses.TreatmentChartDTO;

@Path("Client-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Client Management Module", description = "Client Management")
public class TreatmentChartController {

    @Inject
    TreatmentChartService treatmentChartService;

    @POST
    @Path("create-treatment-chart/{id}")
    @Transactional
    @Operation(summary = "create-treatment-chart", description = "create-treatment-chart")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = TreatmentChartDTO.class)))
    public Response createTreatmentChart(@PathParam("id") Long treatmentRequestedId, TreatmentChartRequest request) {
        return treatmentChartService.createTreatmentChart(treatmentRequestedId, request);
    }

    @PUT
    @Path("update-treatment-chart/{id}")
    @Transactional
    @Operation(summary = "update-treatment-chart", description = "update-treatment-chart")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = TreatmentChartDTO.class)))
    public Response updateTreatmentChart(@PathParam("id") Long id, TreatmentChartRequest request) {
        return treatmentChartService.updateTreatmentChart(id, request);
    }

    @GET
    @Path("get-treatment-chart-by-request-id/{id}")
    @Operation(summary = "get-treatment-chart-by-request-id", description = "get-treatment-chart-by-request-id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = TreatmentChartDTO.class)))
    public Response getTreatmentChartByRequestId(@PathParam("id") Long treatmentRequestedId) {
        return treatmentChartService.getTreatmentChartByRequestId(treatmentRequestedId);
    }

    @GET
    @Path("get-treatment-charts-by-request-id/{id}/all")
    @Operation(summary = "get-treatment-charts-by-request-id", description = "Get all treatment chart entries for a treatment request")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = TreatmentChartDTO.class)))
    public Response getTreatmentChartsByRequestId(@PathParam("id") Long treatmentRequestedId) {
        return treatmentChartService.getTreatmentChartsByRequestId(treatmentRequestedId);
    }

    @GET
    @Path("get-treatment-charts-by-visit-id/{id}")
    @Operation(summary = "get-treatment-charts-by-visit-id", description = "Get all treatment charts for a visit")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = TreatmentChartDTO.class)))
    public Response getTreatmentChartsByVisitId(@PathParam("id") Long visitId) {
        return treatmentChartService.getTreatmentChartsByVisitId(visitId);
    }

    @DELETE
    @Path("delete-treatment-chart/{id}")
    @Transactional
    @Operation(summary = "delete-treatment-chart", description = "Delete a treatment chart by ID")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteTreatmentChart(@PathParam("id") Long id) {
        return treatmentChartService.deleteTreatmentChart(id);
    }

    @POST
    @Path("delete-treatment-chart/{id}")
    @Transactional
    @Operation(summary = "delete-treatment-chart", description = "Delete a treatment chart by ID")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteTreatmentChartPost(@PathParam("id") Long id) {
        return treatmentChartService.deleteTreatmentChart(id);
    }
}

























