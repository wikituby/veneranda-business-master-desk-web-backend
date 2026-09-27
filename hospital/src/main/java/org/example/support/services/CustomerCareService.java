package org.example.support.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.configuration.security.AuthenticatedUserResolver;
import org.example.support.domains.CustomerCareRequest;
import org.example.support.services.payloads.CustomerCareSubmitRequest;
import org.example.subscription.services.FacilityBranding;
import org.example.subscription.services.FacilityBrandingService;
import org.example.user.domains.User;
import org.example.user.domains.repositories.UserRepository;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class CustomerCareService {

    @Inject
    UserRepository userRepository;

    @Inject
    FacilityBrandingService facilityBrandingService;

    @Inject
    AuthenticatedUserResolver authenticatedUserResolver;

    private User getCurrentUser() {
        return authenticatedUserResolver.requireCurrentUser();
    }

    private String resolveFacilityName(User user) {
        FacilityBranding branding;
        if (user != null && user.facilityId != null) {
            branding = facilityBrandingService.resolveForFacilityId(user.facilityId);
        } else {
            branding = facilityBrandingService.resolveDefaultBranding();
        }
        return branding != null && branding.facilityName != null
                ? branding.facilityName
                : "Health Facility";
    }

    @Transactional
    public Response submit(CustomerCareSubmitRequest request) {
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Request body is required", null))
                    .build();
        }

        String subject = request.subject != null ? request.subject.trim() : "";
        String category = request.category != null ? request.category.trim() : "";
        String message = request.message != null ? request.message.trim() : "";

        if (subject.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Subject is required", null))
                    .build();
        }
        if (message.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Message is required", null))
                    .build();
        }
        if (category.isEmpty()) {
            category = "other";
        }

        User user = getCurrentUser();
        CustomerCareRequest entity = new CustomerCareRequest();
        entity.user = user;
        entity.username = user.username;
        entity.userEmail = user.email;
        entity.subject = subject;
        entity.category = category;
        entity.message = message;
        entity.facilityName = resolveFacilityName(user);
        entity.status = "OPEN";
        entity.persist();

        Map<String, Object> data = new HashMap<>();
        data.put("id", entity.id);
        data.put("ticketRef", "CC-" + entity.id);
        data.put("status", entity.status);
        data.put("message", "Your message was received. Customer care will respond as soon as possible.");

        return Response.ok(new ResponseMessage("Customer care request submitted", data)).build();
    }
}
