package org.example.subscription.endpoints;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.configuration.security.AuthenticatedUserResolver;
import org.example.statics.StatusTypes;
import org.example.subscription.services.FacilityBrandingService;
import org.example.subscription.services.payloads.BusinessSettingsUpdateRequest;
import org.example.subscription.services.payloads.FacilityBrandingDTO;
import org.example.user.domains.User;

@Path("business-settings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Business Settings", description = "Facility branding for reports and invoices")
public class BusinessSettingsController {

    @Inject
    FacilityBrandingService facilityBrandingService;

    @Inject
    AuthenticatedUserResolver authenticatedUserResolver;

    @GET
    @Path("public")
    @Operation(summary = "Get public facility branding (no authentication required)")
    public Response getPublicSettings() {
        FacilityBrandingDTO dto = facilityBrandingService.getPublicSettingsDto();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, dto)).build();
    }

    @GET
    @Operation(summary = "Get business settings and report branding lines for the current facility")
    public Response getSettings() {
        User user = authenticatedUserResolver.requireCurrentUser();
        FacilityBrandingDTO dto = facilityBrandingService.getSettingsDtoForUser(user.id);
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, dto)).build();
    }

    @PUT
    @Transactional
    @Operation(summary = "Update business settings (admin / MD only)")
    public Response updateSettings(BusinessSettingsUpdateRequest request) {
        User user = authenticatedUserResolver.requireCurrentUser();
        FacilityBrandingDTO dto = facilityBrandingService.updateSettingsForUser(user.id, request);
        return Response.ok(new ResponseMessage(StatusTypes.UPDATED_SUCCESSFULLY.label, dto)).build();
    }
}
