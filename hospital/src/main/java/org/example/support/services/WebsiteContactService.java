package org.example.support.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.support.domains.WebsiteContactMessage;
import org.example.support.services.payloads.WebsiteContactSubmitRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class WebsiteContactService {

    @Transactional
    public Response submit(WebsiteContactSubmitRequest request) {
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Request body is required", null))
                    .build();
        }

        String fullName = trim(request.fullName);
        String phone = trim(request.phone);
        String email = trim(request.email);
        String subject = trim(request.subject);
        String message = trim(request.message);

        if (fullName.isEmpty()) {
            return badRequest("Full name is required");
        }
        if (phone.isEmpty()) {
            return badRequest("Phone is required");
        }
        if (message.isEmpty()) {
            return badRequest("Message is required");
        }
        if (subject.isEmpty()) {
            subject = "General enquiry";
        }

        WebsiteContactMessage entity = new WebsiteContactMessage();
        entity.fullName = fullName;
        entity.phone = phone;
        entity.email = email.isEmpty() ? null : email;
        entity.subject = subject;
        entity.message = message;
        entity.status = "NEW";
        entity.source = "website";
        entity.persist();

        Map<String, Object> data = new HashMap<>();
        data.put("id", entity.id);
        data.put("ref", "WC-" + entity.id);
        data.put("status", entity.status);
        data.put("message", "Your message was received. Veneranda Hospital will respond as soon as possible.");

        return Response.ok(new ResponseMessage("Contact message received", data)).build();
    }

    public Response listAll() {
        List<WebsiteContactMessage> rows = WebsiteContactMessage.listAll();
        rows.sort((a, b) -> {
            if (a.createdAt == null && b.createdAt == null) {
                return Long.compare(b.id == null ? 0L : b.id, a.id == null ? 0L : a.id);
            }
            if (a.createdAt == null) {
                return 1;
            }
            if (b.createdAt == null) {
                return -1;
            }
            return b.createdAt.compareTo(a.createdAt);
        });

        List<Map<String, Object>> data = rows.stream().map(this::toDto).collect(Collectors.toList());
        return Response.ok(new ResponseMessage("Website contact messages fetched", data)).build();
    }

    @Transactional
    public Response updateStatus(Long id, String status) {
        if (id == null) {
            return badRequest("Message id is required");
        }
        WebsiteContactMessage entity = WebsiteContactMessage.findById(id);
        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Contact message not found", null))
                    .build();
        }

        String normalized = normalizeStatus(status);
        if (normalized == null) {
            return badRequest("Status must be NEW, READ, or CLOSED");
        }
        entity.status = normalized;
        return Response.ok(new ResponseMessage("Contact message updated", toDto(entity))).build();
    }

    private Map<String, Object> toDto(WebsiteContactMessage entity) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", entity.id);
        row.put("ref", "WC-" + entity.id);
        row.put("fullName", entity.fullName);
        row.put("phone", entity.phone);
        row.put("email", entity.email);
        row.put("subject", entity.subject);
        row.put("message", entity.message);
        row.put("status", entity.status);
        row.put("source", entity.source);
        row.put("createdAt", entity.createdAt);
        return row;
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        String value = status.trim().toUpperCase(Locale.ROOT);
        if ("NEW".equals(value) || "READ".equals(value) || "CLOSED".equals(value)) {
            return value;
        }
        return null;
    }

    private Response badRequest(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ResponseMessage(message, null))
                .build();
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
