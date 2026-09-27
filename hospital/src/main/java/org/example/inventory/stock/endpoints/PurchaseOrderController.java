package org.example.inventory.stock.endpoints;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.stock.services.PurchaseOrderService;
import org.example.inventory.stock.services.payloads.requests.PurchaseOrderRequest;

@Path("/Business-item")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PurchaseOrderController {

    @Inject
    PurchaseOrderService purchaseOrderService;

    @POST
    @Path("purchase-orders")
    @Transactional
    public Response create(PurchaseOrderRequest request) {
        return purchaseOrderService.create(request);
    }

    @GET
    @Path("purchase-orders")
    public Response list() {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, purchaseOrderService.list())).build();
    }

    @GET
    @Path("purchase-orders/{id}")
    public Response get(@PathParam("id") Long id) {
        return purchaseOrderService.get(id);
    }

    @PUT
    @Path("purchase-orders/{id}")
    @Transactional
    public Response update(@PathParam("id") Long id, PurchaseOrderRequest request) {
        return purchaseOrderService.update(id, request);
    }
}
