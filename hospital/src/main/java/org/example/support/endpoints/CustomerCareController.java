package org.example.support.endpoints;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.support.services.CustomerCareService;
import org.example.support.services.payloads.CustomerCareSubmitRequest;

@Path("support")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Support", description = "Customer care and help desk requests")
public class CustomerCareController {

    @Inject
    CustomerCareService customerCareService;

    @POST
    @Authenticated
    @Path("/customer-care")
    @Operation(summary = "Submit a message to customer care")
    public Response submitCustomerCare(CustomerCareSubmitRequest request) {
        return customerCareService.submit(request);
    }
}
