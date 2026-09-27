package org.example.dashboard.endpoints;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.dashboard.services.DashboardService;
import org.example.dashboard.services.payloads.DashboardSummaryDTO;

import java.time.LocalDate;

@Path("Client-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Dashboard", description = "Aggregated dashboard metrics")
public class DashboardController {

    @Inject
    DashboardService dashboardService;

    @GET
    @Path("dashboard-summary")
    @Operation(summary = "Dashboard summary", description = "Returns counts, trends, and small activity lists for the overview dashboard")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = DashboardSummaryDTO.class)))
    public Response getDashboardSummary(
            @QueryParam("from") String from,
            @QueryParam("to") String to) {
        LocalDate fromDate = parseDate(from);
        LocalDate toDate = parseDate(to);
        DashboardSummaryDTO summary = fromDate != null && toDate != null
                ? dashboardService.getSummary(fromDate, toDate)
                : dashboardService.getSummary();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, summary)).build();
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception ignored) {
            return null;
        }
    }
}
