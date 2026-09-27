package org.example.messages.services.payloads.responses;

import org.example.inventory.item.domain.Item;
import org.example.procedure.procedure.services.ProcedureCategoryUtils;
import org.example.procedure.procedureRequested.domains.ProcedureRequested;
import org.example.queue.services.payloads.responses.PatientQueueEntryDTO;
import org.example.treatment.treatmentChart.domains.TreatmentChart;

/**
 * Real-time event pushed over {@code /messages/ws} for chat and system alerts.
 */
public class MessagePushEvent {
    public String type;
    public Long conversationId;
    public MessageDTO message;
    public ConversationDTO conversation;
    public String title;
    public String body;
    public String route;
    public PatientQueueEntryDTO queueEntry;
    public StockAlertDTO stockAlert;
    public TreatmentDoseDueDTO treatmentDoseDue;
    public ProcedureRequestPushDTO procedureRequest;

    public MessagePushEvent() {
    }

    public MessagePushEvent(String type, Long conversationId, MessageDTO message, ConversationDTO conversation) {
        this.type = type;
        this.conversationId = conversationId;
        this.message = message;
        this.conversation = conversation;
    }

    public static MessagePushEvent newMessage(MessageDTO message, ConversationDTO conversation) {
        return new MessagePushEvent("NEW_MESSAGE", message != null ? message.conversationId : null, message, conversation);
    }

    public static MessagePushEvent messagesRead(Long conversationId, ConversationDTO conversation) {
        return new MessagePushEvent("MESSAGES_READ", conversationId, null, conversation);
    }

    public static MessagePushEvent queuePatient(PatientQueueEntryDTO queueEntry) {
        MessagePushEvent event = new MessagePushEvent();
        event.type = "QUEUE_PATIENT";
        event.queueEntry = queueEntry;
        event.route = "/overview/queue-management";
        if (queueEntry != null) {
            String patientName = patientDisplayName(queueEntry);
            String queuePrefix = queueEntry.queueNumber != null && !queueEntry.queueNumber.isBlank()
                    ? "#" + queueEntry.queueNumber + " "
                    : "";
            String fromPart = queueEntry.fromModuleName != null && !queueEntry.fromModuleName.isBlank()
                    ? " from " + queueEntry.fromModuleName
                    : "";
            event.title = queueEntry.emergency ? "Emergency patient queued" : "Patient added to queue";
            event.body = queuePrefix + patientName + " → " + queueEntry.toModuleName + fromPart;
        } else {
            event.title = "Patient added to queue";
            event.body = "A patient was queued to your department";
        }
        return event;
    }

    public static MessagePushEvent stockAlert(Item item) {
        MessagePushEvent event = new MessagePushEvent();
        event.type = "STOCK_ALERT";
        event.stockAlert = new StockAlertDTO(item);
        event.route = "/overview/reorder-levels";
        if (item != null) {
            String name = item.title != null && !item.title.isBlank() ? item.title : "Item";
            String qtyPart = event.stockAlert.stockAtHand;
            String reorderPart = item.reOrderLevel != null ? String.valueOf(item.reOrderLevel) : "?";
            event.title = "Low stock alert";
            event.body = name + " — Stock " + qtyPart + " (reorder at " + reorderPart + ")";
        } else {
            event.title = "Low stock alert";
            event.body = "An item is below reorder level";
        }
        return event;
    }

    public static MessagePushEvent treatmentDoseDue(TreatmentChart chart) {
        MessagePushEvent event = new MessagePushEvent();
        event.type = "TREATMENT_DOSE_DUE";
        event.treatmentDoseDue = new TreatmentDoseDueDTO(chart);
        event.route = "/patient-visit";
        if (chart != null && chart.treatmentRequested != null) {
            String treatmentName = chart.treatmentRequested.itemName != null
                    ? chart.treatmentRequested.itemName
                    : "Treatment";
            String patientName = chart.treatmentRequested.visit != null
                    && chart.treatmentRequested.visit.patientName != null
                    && !chart.treatmentRequested.visit.patientName.isBlank()
                    ? chart.treatmentRequested.visit.patientName
                    : "Patient";
            String timePart = event.treatmentDoseDue != null && event.treatmentDoseDue.nextDoseTime != null
                    ? event.treatmentDoseDue.nextDoseTime
                    : "now";
            event.title = "Next dose due";
            event.body = patientName + " — " + treatmentName + " due at " + timePart;
        } else {
            event.title = "Next dose due";
            event.body = "A treatment dose is due now";
        }
        return event;
    }

    public static MessagePushEvent procedureRequested(ProcedureRequested procedureRequested) {
        MessagePushEvent event = new MessagePushEvent();
        event.type = "PROCEDURE_REQUESTED";
        event.procedureRequest = new ProcedureRequestPushDTO(procedureRequested);
        event.route = resolveProcedureRoute(procedureRequested);
        if (procedureRequested != null) {
            String name = procedureRequested.procedureRequestedName != null
                    ? procedureRequested.procedureRequestedName
                    : "Procedure";
            String patient = procedureRequested.patientName != null && !procedureRequested.patientName.isBlank()
                    ? procedureRequested.patientName
                    : "Patient";
            event.title = "New procedure request";
            event.body = patient + " — " + name;
        } else {
            event.title = "New procedure request";
            event.body = "A new procedure was requested";
        }
        return event;
    }

    private static String resolveProcedureRoute(ProcedureRequested procedureRequested) {
        if (procedureRequested == null) {
            return "/overview";
        }
        String leaf = procedureRequested.procedure != null && procedureRequested.procedure.category != null
                ? procedureRequested.procedure.category.name
                : null;
        String parent = procedureRequested.category;
        if (parent == null && procedureRequested.procedure != null) {
            parent = ProcedureCategoryUtils.resolveParentCategoryName(procedureRequested.procedure);
        }
        String haystack = ((leaf != null ? leaf : "") + " " + (parent != null ? parent : "")).toLowerCase();
        if (haystack.contains("ultrasound") || haystack.contains("imaging") || haystack.contains("scan")) {
            return "/overview/scan-v2";
        }
        if (haystack.contains("dental")) {
            return "/overview/dental-v2";
        }
        if (haystack.contains("maternity") || haystack.contains("mch")) {
            return "/overview/mch-v2";
        }
        if (haystack.contains("lab") || haystack.contains("test")) {
            return "/overview/lab-v2";
        }
        return "/overview";
    }

    private static String patientDisplayName(PatientQueueEntryDTO dto) {
        String surname = dto.patientSurname != null ? dto.patientSurname.trim() : "";
        String other = dto.patientOtherNames != null ? dto.patientOtherNames.trim() : "";
        String combined = (surname + " " + other).trim();
        return combined.isBlank() ? "Patient" : combined;
    }
}
