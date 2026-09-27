package org.example.hmis.endpoints;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.hmis.services.Hmis033bAggregationService;
import org.example.hmis.services.Hmis033bPdfService;
import org.example.hmis.services.Hmis105PdfService;
import org.example.hmis.services.HmisTracerItemService;
import org.example.hmis.services.payloads.Hmis033bAggregateResponse;
import org.example.hmis.services.payloads.HmisTracerItemUpdateRequest;

@Path("hmis")
@Tag(name = "HMIS Reporting", description = "Uganda HMIS aggregate exports")
public class HmisController {

    @Inject Hmis033bAggregationService hmis033bAggregationService;
    @Inject Hmis033bPdfService hmis033bPdfService;
    @Inject Hmis105PdfService hmis105PdfService;
    @Inject HmisTracerItemService hmisTracerItemService;

    @GET
    @Path("033b/aggregate")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Operation(summary = "HMIS 033b weekly aggregate JSON")
    public Response aggregate033b(
            @QueryParam("from") String from,
            @QueryParam("to") String to,
            @QueryParam("datefrom") String datefrom,
            @QueryParam("dateto") String dateto) {
        try {
            LocalDate fromDate = parseDate(from != null ? from : datefrom);
            LocalDate toDate = parseDate(to != null ? to : dateto);
            if (fromDate == null || toDate == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ResponseMessage("Query params from and to (YYYY-MM-DD) are required", null))
                        .build();
            }
            Hmis033bAggregateResponse data = hmis033bAggregationService.aggregate(fromDate, toDate);
            return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, data)).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage(ex.getMessage(), null))
                    .build();
        } catch (DateTimeParseException ex) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Invalid date format. Use YYYY-MM-DD.", null))
                    .build();
        }
    }

    @GET
    @Path("033b/pdf")
    @Produces("application/pdf")
    @Transactional
    @Operation(summary = "HMIS 033b weekly report PDF (official template overlay)")
    public Response pdf033b(
            @QueryParam("from") String from,
            @QueryParam("to") String to,
            @QueryParam("datefrom") String datefrom,
            @QueryParam("dateto") String dateto) {
        try {
            LocalDate fromDate = parseDate(from != null ? from : datefrom);
            LocalDate toDate = parseDate(to != null ? to : dateto);
            if (fromDate == null || toDate == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ResponseMessage("Query params from and to (YYYY-MM-DD) are required", null))
                        .build();
            }
            byte[] pdf = hmis033bPdfService.generate(fromDate, toDate);
            String filename = "hmis_033b_" + fromDate + "_" + toDate + ".pdf";
            return Response.ok(pdf)
                    .header("Content-Disposition", "attachment; filename=" + filename)
                    .type("application/pdf")
                    .build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage(ex.getMessage(), null))
                    .build();
        } catch (DateTimeParseException ex) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Invalid date format. Use YYYY-MM-DD.", null))
                    .build();
        } catch (RuntimeException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ResponseMessage(ex.getMessage(), null))
                    .build();
        }
    }

    @GET
    @Path("105/pdf")
    @Produces("application/pdf")
    @Transactional
    @Operation(summary = "HMIS 105 monthly outpatient report PDF (official template overlay)")
    public Response pdf105(
            @QueryParam("from") String from,
            @QueryParam("to") String to,
            @QueryParam("datefrom") String datefrom,
            @QueryParam("dateto") String dateto) {
        try {
            LocalDate fromDate = parseDate(from != null ? from : datefrom);
            LocalDate toDate = parseDate(to != null ? to : dateto);
            if (fromDate == null || toDate == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ResponseMessage("Query params from and to (YYYY-MM-DD) are required", null))
                        .build();
            }
            byte[] pdf = hmis105PdfService.generate(fromDate, toDate);
            String filename = "hmis_105_" + fromDate.getYear() + "-" + String.format("%02d", fromDate.getMonthValue()) + ".pdf";
            return Response.ok(pdf)
                    .header("Content-Disposition", "attachment; filename=" + filename)
                    .type("application/pdf")
                    .build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage(ex.getMessage(), null))
                    .build();
        } catch (DateTimeParseException ex) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Invalid date format. Use YYYY-MM-DD.", null))
                    .build();
        } catch (RuntimeException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ResponseMessage(ex.getMessage(), null))
                    .build();
        }
    }

    @GET
    @Path("tracer-items")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Operation(summary = "List HMIS tracer medicine mappings with inventory details")
    public Response listTracerItems(
            @QueryParam("from") String from,
            @QueryParam("to") String to) {
        LocalDate fromDate = parseDate(from);
        LocalDate toDate = parseDate(to);
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,
                hmisTracerItemService.listAll(fromDate, toDate))).build();
    }

    @PUT
    @Path("tracer-items/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Operation(summary = "Update HMIS tracer stock item mapping")
    public Response updateTracerItem(@PathParam("id") Long id, HmisTracerItemUpdateRequest request) {
        return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label, hmisTracerItemService.update(id, request))).build();
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value.trim());
    }
}