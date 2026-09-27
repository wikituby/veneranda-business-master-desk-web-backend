package org.example.inventory.stock.endpoints;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.stock.domains.StockItem;
import org.example.inventory.stock.services.ProductLocalImageService;
import org.example.inventory.stock.services.payloads.requests.LocalImageCacheRequest;
import org.example.inventory.stock.services.payloads.requests.LocalImageUploadRequest;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.nio.file.Files;

import java.util.HashMap;
import java.util.Map;

@Path("/stock-item")
@Tag(name = "Inventory Management - Product local images", description = "PC-local product image folder cache.")
public class ProductLocalImageController {

    @Inject
    ProductLocalImageService productLocalImageService;


    @POST
    @Path("/local-images/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Upload product image file into the local product-images folder (stores file on disk, returns localUrl only)")
    public Response uploadLocalImageFile(@RestForm("file") FileUpload file) {
        try {
            if (file == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ResponseMessage("Image file is required.", null))
                        .build();
            }
            java.nio.file.Path uploaded = file.uploadedFile();
            if (uploaded == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ResponseMessage("Image file is required.", null))
                        .build();
            }
            byte[] bytes = Files.readAllBytes(uploaded);
            Map<String, Object> saved = productLocalImageService.saveImageBytes(
                    file.fileName(),
                    file.contentType(),
                    bytes
            );
            return Response.ok(new ResponseMessage(ActionMessages.SAVED.label, saved)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage(e.getMessage(), null))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ResponseMessage(e.getMessage() != null ? e.getMessage() : "Could not save local image.", null))
                    .build();
        }
    }

    @POST
    @Path("/local-images")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Save product image to the local product-images folder")
    public Response saveLocalImage(LocalImageUploadRequest request) {
        try {
            Map<String, Object> saved = productLocalImageService.saveBase64Image(
                    request != null ? request.fileName : null,
                    request != null ? request.contentType : null,
                    request != null ? request.base64Data : null
            );
            return Response.ok(new ResponseMessage(ActionMessages.SAVED.label, saved)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage(e.getMessage(), null))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ResponseMessage(e.getMessage() != null ? e.getMessage() : "Could not save local image.", null))
                    .build();
        }
    }

    @POST
    @Path("/local-images/cache-from-url")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Operation(summary = "Download a web image into the local product-images folder")
    public Response cacheFromWebUrl(LocalImageCacheRequest request) {
        try {
            Map<String, Object> saved = productLocalImageService.cacheFromWebUrl(
                    request != null ? request.webUrl : null
            );
            String localUrl = String.valueOf(saved.get("localUrl"));
            if (request != null && request.stockItemId != null && request.stockItemId > 0) {
                StockItem item = StockItem.findById(request.stockItemId);
                if (item != null) {
                    int slot = request.slot != null ? request.slot : 1;
                    if (slot == 2) {
                        item.imageLocal2 = localUrl;
                    } else if (slot == 3) {
                        item.imageLocal3 = localUrl;
                    } else {
                        item.imageLocal = localUrl;
                    }
                    item.persist();
                }
            }
            Map<String, Object> body = new HashMap<>(saved);
            return Response.ok(new ResponseMessage(ActionMessages.SAVED.label, body)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage(e.getMessage(), null))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ResponseMessage(e.getMessage() != null ? e.getMessage() : "Could not cache image locally.", null))
                    .build();
        }
    }

    @GET
    @Path("/local-images/{fileName}")
    @Operation(summary = "Serve a product image from the local product-images folder")
    public Response getLocalImage(@PathParam("fileName") String fileName) {
        java.nio.file.Path file = productLocalImageService.resolveExistingFile(fileName);
        if (file == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Local image not found.", null))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        try {
            String type = Files.probeContentType(file);
            if (type == null || type.isBlank()) {
                type = "application/octet-stream";
            }
            byte[] bytes = Files.readAllBytes(file);
            return Response.ok(bytes)
                    .type(type)
                    .header("Cache-Control", "public, max-age=86400")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ResponseMessage(e.getMessage() != null ? e.getMessage() : "Could not read local image.", null))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    @GET
    @Path("/local-images/exists/{fileName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Check whether a local product image file exists")
    public Response localImageExists(@PathParam("fileName") String fileName) {
        boolean exists = productLocalImageService.resolveExistingFile(fileName) != null;
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, Map.of("exists", exists))).build();
    }
}