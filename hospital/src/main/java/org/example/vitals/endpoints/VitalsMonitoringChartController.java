package org.example.vitals.endpoints;

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
import org.example.configuration.handler.ResponseMessage;
import org.example.statics.StatusTypes;
import org.example.vitals.services.payloads.responses.VitalsMonitoringChartDTO;
import org.example.vitals.services.payloads.requests.VitalsMonitoringChartRequest;
import org.example.vitals.services.VitalsMonitoringChartService;

@Path("Patient-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Patient Management Module", description = "Patient Management")

public class VitalsMonitoringChartController {

    @Inject
    VitalsMonitoringChartService vitalsMonitoringChartService;

    @POST
    @Path("create-new-VitalsMonitoring")
    @Transactional
    @Operation(summary = "new VitalsMonitoringChart for inpatients", description = "new-VitalsMonitoringChart for inpatients")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = VitalsMonitoringChartDTO.class)))
    public Response createVitalsMonitoringChart(VitalsMonitoringChartRequest request){
        return Response.ok(new ResponseMessage(StatusTypes.CREATED.label,vitalsMonitoringChartService.newVitalsMonitoringChart(request) )).build();
    }
}






