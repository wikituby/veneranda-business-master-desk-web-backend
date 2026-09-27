package org.example.queue.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.client.domains.Patient;
import org.example.client.domains.PatientGroup;
import org.example.configuration.handler.ResponseMessage;
import org.example.queue.domains.HospitalClinic;
import org.example.queue.domains.HospitalModule;
import org.example.queue.domains.PatientQueueEntry;
import org.example.queue.domains.repositories.HospitalClinicRepository;
import org.example.queue.domains.repositories.HospitalModuleRepository;
import org.example.queue.domains.repositories.PatientQueueEntryRepository;
import org.example.queue.services.payloads.requests.HospitalClinicRequest;
import org.example.queue.services.payloads.requests.HospitalModuleRequest;
import org.example.queue.services.payloads.requests.PatientQueueRequest;
import org.example.queue.services.payloads.responses.HospitalClinicDTO;
import org.example.queue.services.payloads.responses.HospitalModuleDTO;
import org.example.queue.services.payloads.responses.PatientQueueEntryDTO;
import org.example.subscription.services.SubscriptionService;
import org.example.messages.services.AppNotificationPushService;
import org.example.visit.domains.PatientVisit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class HospitalQueueService {

    private static final List<String> ACTIVE_STATUSES = List.of("WAITING", "CALLED", "SERVING");
    /** Shown in queue lists; only DISCHARGED removes a patient from the queue. */
    private static final List<String> VISIBLE_QUEUE_STATUSES = List.of(
            "WAITING", "CALLED", "SERVING", "COMPLETED", "CANCELLED");

    @Inject
    HospitalModuleRepository hospitalModuleRepository;

    @Inject
    HospitalClinicRepository hospitalClinicRepository;

    @Inject
    PatientQueueEntryRepository patientQueueEntryRepository;

    @Inject
    SubscriptionService subscriptionService;

    @Inject
    AppNotificationPushService appNotificationPushService;

    private void requireActiveSubscription() {
        subscriptionService.requireActiveFacilitySubscription();
    }

    @Transactional
    public void ensureDefaultModules() {
        if (hospitalModuleRepository.count() == 0) {
            seed("RECEPTION", "Reception", "Front desk and patient registration", 1, "reception", null);
            seed("TRIAGE", "Triage", "Initial triage and vitals", 2, "triage", null);
            seed("CONSULTATION", "Consultation", "Doctor consultation", 3, "patient-visit", null);
            seed("LAB", "Laboratory", "Laboratory services", 4, "lab", null);
            seed("PHARMACY", "Pharmacy", "Pharmacy dispensing", 5, "pharmacy", "pharmacist,pharmacy,admin,md");
            seed("SCAN", "Scan / Radiology", "Imaging and radiology", 6, "scan", null);
            seed("DENTAL", "Dental", "Dental clinic", 7, "dental", null);
            seed("MCH", "Maternal & Child Health", "MCH services", 8, "maternal-child-health-requests", null);
            seed("CASHIER", "Cashier", "Billing and payments", 9, "cashier", null);
        }
        ensurePharmacyModuleRoles();
    }

    @Transactional
    public void ensurePharmacyModuleRoles() {
        HospitalModule pharmacy = hospitalModuleRepository.find("code", "PHARMACY").firstResult();
        if (pharmacy == null) {
            return;
        }
        if (pharmacy.allowedRoles == null || pharmacy.allowedRoles.isBlank()) {
            pharmacy.allowedRoles = "pharmacist,pharmacy,admin,md";
        }
    }

    private void seed(String code, String name, String description, int order, String routeKey, String allowedRoles) {
        HospitalModule module = new HospitalModule();
        module.code = code;
        module.name = name;
        module.description = description;
        module.sortOrder = order;
        module.routeKey = routeKey;
        module.active = true;
        module.allowedRoles = allowedRoles;
        hospitalModuleRepository.persist(module);
    }

    public Response getAllModules() {
        requireActiveSubscription();
        ensureDefaultModules();
        List<HospitalModuleDTO> list = hospitalModuleRepository
                .listAll(Sort.by("sortOrder").and("name"))
                .stream()
                .map(HospitalModuleDTO::new)
                .collect(Collectors.toList());
        return Response.ok(new ResponseMessage("Hospital modules loaded", list)).build();
    }

    @Transactional
    public Response createModule(HospitalModuleRequest request) {
        requireActiveSubscription();
        if (request == null || request.name == null || request.name.isBlank()) {
            return badRequest("Module name is required");
        }
        String code = request.code != null && !request.code.isBlank()
                ? request.code.trim().toUpperCase()
                : request.name.trim().toUpperCase().replaceAll("[^A-Z0-9]+", "_");

        if (hospitalModuleRepository.find("code", code).firstResult() != null) {
            return badRequest("A module with code already exists: " + code);
        }

        HospitalModule module = new HospitalModule();
        module.code = code;
        module.name = request.name.trim();
        module.description = request.description;
        module.active = request.active == null || request.active;
        module.sortOrder = request.sortOrder;
        module.routeKey = request.routeKey;
        module.allowedRoles = normalizeRolesCsv(request.allowedRoles);
        hospitalModuleRepository.persist(module);

        return Response.ok(new ResponseMessage("Hospital module created", new HospitalModuleDTO(module))).build();
    }

    @Transactional
    public Response updateModule(Long id, HospitalModuleRequest request) {
        requireActiveSubscription();
        HospitalModule module = hospitalModuleRepository.findById(id);
        if (module == null) {
            return notFound("Hospital module not found");
        }
        if (request == null) {
            return badRequest("Update payload is required");
        }
        if (request.name != null && !request.name.isBlank()) {
            module.name = request.name.trim();
        }
        if (request.code != null && !request.code.isBlank()) {
            String code = request.code.trim().toUpperCase();
            HospitalModule existing = hospitalModuleRepository.find("code = ?1 and id != ?2", code, id).firstResult();
            if (existing != null) {
                return badRequest("Another module already uses code: " + code);
            }
            module.code = code;
        }
        if (request.description != null) {
            module.description = request.description;
        }
        if (request.active != null) {
            module.active = request.active;
        }
        if (request.sortOrder != null) {
            module.sortOrder = request.sortOrder;
        }
        if (request.routeKey != null) {
            module.routeKey = request.routeKey;
        }
        if (request.allowedRoles != null) {
            module.allowedRoles = normalizeRolesCsv(request.allowedRoles);
        }
        return Response.ok(new ResponseMessage("Hospital module updated", new HospitalModuleDTO(module))).build();
    }

    @Transactional
    public Response deleteModule(Long id) {
        requireActiveSubscription();
        HospitalModule module = hospitalModuleRepository.findById(id);
        if (module == null) {
            return notFound("Hospital module not found");
        }
        long activeQueues = patientQueueEntryRepository.count(
                "toModule.id = ?1 and status in ?2", id, ACTIVE_STATUSES);
        if (activeQueues > 0) {
            return badRequest("Cannot delete module with patients still in queue");
        }
        hospitalClinicRepository.delete("hospitalModule.id", id);
        hospitalModuleRepository.delete(module);
        return Response.ok(new ResponseMessage("Hospital module deleted", null)).build();
    }

    public Response getClinics(Long moduleId) {
        requireActiveSubscription();
        String query = moduleId != null ? "hospitalModule.id = ?1" : "1=1";
        Object[] params = moduleId != null ? new Object[]{moduleId} : new Object[]{};
        List<HospitalClinicDTO> list = (moduleId != null
                ? hospitalClinicRepository.list(query, Sort.by("name"), params)
                : hospitalClinicRepository.listAll(Sort.by("name")))
                .stream()
                .map(HospitalClinicDTO::new)
                .collect(Collectors.toList());
        return Response.ok(new ResponseMessage("Clinics loaded", list)).build();
    }

    @Transactional
    public Response createClinic(HospitalClinicRequest request) {
        requireActiveSubscription();
        if (request == null || request.hospitalModuleId == null || request.name == null || request.name.isBlank()) {
            return badRequest("Module and clinic name are required");
        }
        HospitalModule module = hospitalModuleRepository.findById(request.hospitalModuleId);
        if (module == null) {
            return notFound("Hospital module not found");
        }
        HospitalClinic clinic = new HospitalClinic();
        clinic.hospitalModule = module;
        clinic.name = request.name.trim();
        clinic.active = request.active == null || request.active;
        clinic.allowedRoles = normalizeRolesCsv(request.allowedRoles);
        hospitalClinicRepository.persist(clinic);
        return Response.ok(new ResponseMessage("Clinic created", new HospitalClinicDTO(clinic))).build();
    }

    @Transactional
    public Response updateClinic(Long id, HospitalClinicRequest request) {
        requireActiveSubscription();
        HospitalClinic clinic = hospitalClinicRepository.findById(id);
        if (clinic == null) {
            return notFound("Clinic not found");
        }
        if (request != null) {
            if (request.name != null && !request.name.isBlank()) {
                clinic.name = request.name.trim();
            }
            if (request.active != null) {
                clinic.active = request.active;
            }
            if (request.hospitalModuleId != null) {
                HospitalModule module = hospitalModuleRepository.findById(request.hospitalModuleId);
                if (module == null) {
                    return notFound("Hospital module not found");
                }
                clinic.hospitalModule = module;
            }
            if (request.allowedRoles != null) {
                clinic.allowedRoles = normalizeRolesCsv(request.allowedRoles);
            }
        }
        return Response.ok(new ResponseMessage("Clinic updated", new HospitalClinicDTO(clinic))).build();
    }

    private String normalizeRolesCsv(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = java.util.Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(String::toLowerCase)
                .distinct()
                .collect(java.util.stream.Collectors.joining(","));
        return normalized.isBlank() ? null : normalized;
    }

    @Transactional
    public Response deleteClinic(Long id) {
        requireActiveSubscription();
        HospitalClinic clinic = hospitalClinicRepository.findById(id);
        if (clinic == null) {
            return notFound("Clinic not found");
        }
        hospitalClinicRepository.delete(clinic);
        return Response.ok(new ResponseMessage("Clinic deleted", null)).build();
    }

    @Transactional
    public Response queuePatient(PatientQueueRequest request) {
        requireActiveSubscription();
        if (request == null || request.patientId == null || request.fromModuleId == null || request.toModuleId == null) {
            return badRequest("Patient, from module, and to module are required");
        }
        Patient patient = Patient.findById(request.patientId);
        if (patient == null) {
            return notFound("Patient not found");
        }
        HospitalModule fromModule = hospitalModuleRepository.findById(request.fromModuleId);
        HospitalModule toModule = hospitalModuleRepository.findById(request.toModuleId);
        if (fromModule == null || toModule == null) {
            return notFound("From or to module not found");
        }

        PatientQueueEntry entry = new PatientQueueEntry();
        entry.patient = patient;
        if (request.patientVisitId != null) {
            PatientVisit visit = PatientVisit.findById(request.patientVisitId);
            entry.patientVisit = visit;
        }
        entry.fromModule = fromModule;
        entry.toModule = toModule;
        if (request.clinicId != null) {
            HospitalClinic clinic = hospitalClinicRepository.findById(request.clinicId);
            entry.clinic = clinic;
        }
        entry.note = request.note;
        entry.visitDefaultScheme = request.visitDefaultScheme;
        if (request.patientGroupId != null) {
            PatientGroup group = PatientGroup.findById(request.patientGroupId);
            entry.patientGroup = group;
            if ((entry.visitDefaultScheme == null || entry.visitDefaultScheme.isBlank()) && group != null) {
                entry.visitDefaultScheme = group.groupName;
            }
        }
        entry.emergency = Boolean.TRUE.equals(request.emergency);
        entry.revisit = Boolean.TRUE.equals(request.revisit);
        entry.queueNumber = generateQueueNumber(toModule);
        entry.queuePosition = nextQueuePosition(toModule.id);
        entry.status = "WAITING";
        entry.queuedAt = LocalDateTime.now();
        entry.queuedBy = request.queuedBy;

        patientQueueEntryRepository.persist(entry);
        appNotificationPushService.pushQueuePatient(entry);
        return Response.ok(new ResponseMessage("Patient queued successfully", new PatientQueueEntryDTO(entry))).build();
    }

    @Transactional
    public Response dischargePatientFromQueue(org.example.queue.services.payloads.requests.DischargePatientQueueRequest request) {
        requireActiveSubscription();
        if (request == null || request.patientId == null) {
            return badRequest("Patient is required");
        }
        Patient patient = Patient.findById(request.patientId);
        if (patient == null) {
            return notFound("Patient not found");
        }

        StringBuilder query = new StringBuilder("patient.id = ?1 and status in ?2");
        List<Object> params = new ArrayList<>();
        params.add(request.patientId);
        params.add(ACTIVE_STATUSES);

        List<PatientQueueEntry> entries = patientQueueEntryRepository.list(query.toString(), params.toArray());
        if (entries.isEmpty()) {
            return Response.ok(new ResponseMessage("Patient is not in an active queue", null)).build();
        }

        LocalDateTime now = LocalDateTime.now();
        String note = request.note != null ? request.note.trim() : "";
        String dischargedBy = request.dischargedBy != null ? request.dischargedBy.trim() : "";
        List<PatientQueueEntryDTO> discharged = new ArrayList<>();
        for (PatientQueueEntry entry : entries) {
            entry.status = "DISCHARGED";
            entry.completedAt = now;
            StringBuilder entryNote = new StringBuilder();
            String existing = entry.note != null ? entry.note.trim() : "";
            if (!existing.isBlank()) {
                entryNote.append(existing);
            }
            if (!note.isBlank()) {
                if (entryNote.length() > 0) {
                    entryNote.append(" | ");
                }
                entryNote.append("Discharge: ").append(note);
            }
            if (!dischargedBy.isBlank()) {
                if (entryNote.length() > 0) {
                    entryNote.append(" | ");
                }
                entryNote.append("By: ").append(dischargedBy);
            }
            if (entryNote.length() > 0) {
                entry.note = entryNote.toString();
            }
            discharged.add(new PatientQueueEntryDTO(entry));
        }
        return Response.ok(new ResponseMessage("Patient discharged from " + discharged.size() + " active queue(s).", discharged)).build();
    }

    private String generateQueueNumber(HospitalModule toModule) {
        String prefix = toModule.code != null ? toModule.code : "Q";
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        long count = patientQueueEntryRepository.count(
                "toModule.id = ?1 and queuedAt >= ?2 and queuedAt < ?3",
                toModule.id, start, end);
        return prefix + "-" + String.format("%03d", count + 1);
    }

    private int nextQueuePosition(Long toModuleId) {
        // Max position for active entries in this destination module.
        PatientQueueEntry latest = patientQueueEntryRepository.find(
                "toModule.id = ?1 and status in ?2 and queuePosition is not null order by queuePosition desc",
                toModuleId, ACTIVE_STATUSES
        ).firstResult();
        if (latest == null || latest.queuePosition == null) {
            return 1;
        }
        return latest.queuePosition + 1;
    }

    public Response getQueueEntries(Long toModuleId, String status, String role,
                                    String assignedModuleIds, String assignedClinicIds) {
        requireActiveSubscription();
        StringBuilder query = new StringBuilder("1=1");
        List<Object> params = new ArrayList<>();
        int idx = 1;

        if (toModuleId != null) {
            query.append(" and toModule.id = ?").append(idx++);
            params.add(toModuleId);
        }
        if (status != null && !status.isBlank()) {
            query.append(" and status = ?").append(idx++);
            params.add(status.trim().toUpperCase());
        } else {
            query.append(" and status in ?").append(idx++);
            params.add(VISIBLE_QUEUE_STATUSES);
        }

        List<PatientQueueEntry> raw = patientQueueEntryRepository.list(
                query.toString(), params.toArray());
        List<PatientQueueEntryDTO> list = filterAndSortQueueList(raw, role, assignedModuleIds, assignedClinicIds, "current");

        return Response.ok(new ResponseMessage("Queue entries loaded", list)).build();
    }

    public Response getHospitalDirectory() {
        requireActiveSubscription();
        List<PatientQueueEntry> entries = patientQueueEntryRepository.list(
                "status in ?1", Sort.by("queuedAt").descending(), VISIBLE_QUEUE_STATUSES);

        Map<String, Object> directory = new LinkedHashMap<>();
        Map<Long, List<PatientQueueEntryDTO>> byModule = new LinkedHashMap<>();

        for (PatientQueueEntry entry : entries) {
            Long moduleId = entry.toModule != null ? entry.toModule.id : 0L;
            byModule.computeIfAbsent(moduleId, k -> new ArrayList<>()).add(new PatientQueueEntryDTO(entry));
        }

        List<Map<String, Object>> departments = new ArrayList<>();
        for (Map.Entry<Long, List<PatientQueueEntryDTO>> e : byModule.entrySet()) {
            Map<String, Object> row = new HashMap<>();
            List<PatientQueueEntryDTO> patients = e.getValue();
            if (!patients.isEmpty()) {
                row.put("moduleId", patients.get(0).toModuleId);
                row.put("moduleName", patients.get(0).toModuleName);
                row.put("patientCount", patients.size());
                row.put("patients", patients);
            }
            departments.add(row);
        }

        directory.put("departments", departments);
        directory.put("totalPatients", entries.size());

        return Response.ok(new ResponseMessage("Hospital directory loaded", directory)).build();
    }

    public Response getLatestQueueEntries(Long limit, String view, String role,
                                          String assignedModuleIds, String assignedClinicIds) {
        requireActiveSubscription();
        int l = limit == null || limit <= 0 ? 100 : Math.min(limit.intValue(), 500);
        String v = view == null || view.isBlank() ? "current" : view.trim().toLowerCase();
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);

        List<PatientQueueEntry> raw;
        switch (v) {
            case "discharged":
                raw = patientQueueEntryRepository.list(
                        "status = ?1 and completedAt >= ?2 and completedAt < ?3",
                        "DISCHARGED", dayStart, dayEnd);
                break;
            case "referred":
                raw = patientQueueEntryRepository.list(
                        "referredAt >= ?1 and referredAt < ?2",
                        dayStart, dayEnd);
                break;
            default:
                raw = patientQueueEntryRepository.list("status in ?1", VISIBLE_QUEUE_STATUSES);
                v = "current";
                break;
        }

        List<PatientQueueEntryDTO> sorted = filterAndSortQueueList(raw, role, assignedModuleIds, assignedClinicIds, v);
        if (sorted.size() > l) {
            sorted = sorted.subList(0, l);
        }
        return Response.ok(new ResponseMessage("Queue entries loaded", sorted)).build();
    }

    @Transactional
    public Response updateQueueEntry(Long id, org.example.queue.services.payloads.requests.PatientQueueUpdateRequest request) {
        requireActiveSubscription();
        PatientQueueEntry entry = patientQueueEntryRepository.findById(id);
        if (entry == null) {
            return notFound("Queue entry not found");
        }
        if (request == null) {
            return badRequest("Update payload is required");
        }
        if (request.emergency != null) {
            entry.emergency = request.emergency;
        }
        if (request.revisit != null) {
            entry.revisit = request.revisit;
        }
        if (request.note != null) {
            entry.note = request.note;
        }
        if (request.visitDefaultScheme != null) {
            entry.visitDefaultScheme = request.visitDefaultScheme;
        }
        if (request.queuePosition != null) {
            entry.queuePosition = request.queuePosition;
        }
        if (request.clinicId != null) {
            entry.clinic = hospitalClinicRepository.findById(request.clinicId);
        }
        if (request.fromModuleId != null) {
            entry.fromModule = hospitalModuleRepository.findById(request.fromModuleId);
        }
        if (request.toModuleId != null) {
            HospitalModule newTo = hospitalModuleRepository.findById(request.toModuleId);
            if (newTo == null) {
                return notFound("Destination module not found");
            }
            if (entry.toModule == null || !request.toModuleId.equals(entry.toModule.id)) {
                entry.referredFromModule = entry.toModule != null ? entry.toModule : entry.fromModule;
                entry.referredFromClinic = entry.clinic;
                entry.referredAt = LocalDateTime.now();
                if (request.queuedBy != null && !request.queuedBy.isBlank()) {
                    entry.referredBy = request.queuedBy.trim();
                }
                entry.fromModule = entry.toModule != null ? entry.toModule : entry.fromModule;
                entry.toModule = newTo;
                entry.status = "WAITING";
                entry.queueNumber = generateQueueNumber(newTo);
                entry.queuePosition = nextQueuePosition(newTo.id);
                entry.queuedAt = LocalDateTime.now();
                entry.calledAt = null;
                entry.completedAt = null;
                appNotificationPushService.pushQueuePatient(entry);
            }
        }
        if (request.queuedBy != null && !request.queuedBy.isBlank()) {
            entry.queuedBy = request.queuedBy.trim();
        }
        if (request.status != null && !request.status.isBlank()) {
            String newStatus = request.status.trim().toUpperCase();
            entry.status = newStatus;
            if ("CALLED".equals(newStatus) || "SERVING".equals(newStatus)) {
                if (entry.calledAt == null) {
                    entry.calledAt = LocalDateTime.now();
                }
            }
            if ("COMPLETED".equals(newStatus) || "CANCELLED".equals(newStatus) || "DISCHARGED".equals(newStatus)) {
                entry.completedAt = LocalDateTime.now();
            }
        }
        if (request.patientGroupId != null) {
            entry.patientGroup = PatientGroup.findById(request.patientGroupId);
        }
        return Response.ok(new ResponseMessage("Queue entry updated", new PatientQueueEntryDTO(entry))).build();
    }

    private boolean isPrivilegedRole(String role) {
        if (role == null || role.isBlank()) {
            return false;
        }
        String r = role.trim().toLowerCase();
        return "admin".equals(r) || "md".equals(r);
    }

    private boolean isModuleAllowedForRole(HospitalModule module, String role) {
        if (module == null) {
            return false;
        }
        if (role == null || role.isBlank() || isPrivilegedRole(role)) {
            return true;
        }
        if (module.allowedRoles == null || module.allowedRoles.isBlank()) {
            return true;
        }
        String r = role.trim().toLowerCase();
        return java.util.Arrays.stream(module.allowedRoles.split(","))
                .map(String::trim)
                .anyMatch(r::equals);
    }

    private List<PatientQueueEntryDTO> filterAndSortQueueList(
            List<PatientQueueEntry> entries,
            String role,
            String assignedModuleIds,
            String assignedClinicIds,
            String view) {
        return entries.stream()
                .filter(e -> isEntryVisibleForView(e, role, assignedModuleIds, assignedClinicIds, view))
                .map(PatientQueueEntryDTO::new)
                .sorted((a, b) -> compareQueueEntries(a, b, view))
                .collect(Collectors.toList());
    }

    private boolean isEntryVisibleForView(
            PatientQueueEntry entry,
            String role,
            String assignedModuleIds,
            String assignedClinicIds,
            String view) {
        if ("referred".equals(view)) {
            return isReferralVisibleForUser(entry, role, assignedModuleIds, assignedClinicIds);
        }
        return isEntryVisibleForUser(entry, role, assignedModuleIds, assignedClinicIds);
    }

    private boolean isReferralVisibleForUser(
            PatientQueueEntry entry,
            String role,
            String assignedModuleIds,
            String assignedClinicIds) {
        if (isPrivilegedRole(role)) {
            return true;
        }
        java.util.Set<Long> moduleIds = parseIdCsv(assignedModuleIds);
        java.util.Set<Long> clinicIds = parseIdCsv(assignedClinicIds);
        HospitalModule fromMod = entry.referredFromModule;
        HospitalClinic fromClinic = entry.referredFromClinic;
        if (!clinicIds.isEmpty() || !moduleIds.isEmpty()) {
            if (fromClinic != null && clinicIds.contains(fromClinic.id)) {
                return true;
            }
            if (fromMod != null && moduleIds.contains(fromMod.id)) {
                return true;
            }
            return false;
        }
        return isModuleAllowedForRole(fromMod, role);
    }

    private boolean isPharmacyRole(String role) {
        if (role == null || role.isBlank()) {
            return false;
        }
        String r = role.trim().toLowerCase();
        return "pharmacist".equals(r) || "pharmacy".equals(r);
    }

    private Long resolvePharmacyModuleId() {
        HospitalModule pharmacy = hospitalModuleRepository.find("code", "PHARMACY").firstResult();
        return pharmacy != null ? pharmacy.id : null;
    }

    private boolean isEntryVisibleForUser(
            PatientQueueEntry entry,
            String role,
            String assignedModuleIds,
            String assignedClinicIds) {
        if (isPrivilegedRole(role)) {
            return true;
        }
        java.util.Set<Long> moduleIds = parseIdCsv(assignedModuleIds);
        java.util.Set<Long> clinicIds = parseIdCsv(assignedClinicIds);
        if (!moduleIds.isEmpty() || !clinicIds.isEmpty()) {
            if (entry.toModule != null && moduleIds.contains(entry.toModule.id)) {
                return true;
            }
            if (entry.clinic != null && clinicIds.contains(entry.clinic.id)) {
                return true;
            }
            return false;
        }
        if (isPharmacyRole(role)) {
            Long pharmacyId = resolvePharmacyModuleId();
            return entry.toModule != null && pharmacyId != null && pharmacyId.equals(entry.toModule.id);
        }
        return isModuleAllowedForRole(entry.toModule, role);
    }

    private java.util.Set<Long> parseIdCsv(String raw) {
        if (raw == null || raw.isBlank()) {
            return java.util.Collections.emptySet();
        }
        return java.util.Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(s -> {
                    try {
                        return Long.parseLong(s);
                    } catch (NumberFormatException ex) {
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /** Emergency first + FIFO for current; most recent first for discharged/referred. */
    private int compareQueueEntries(PatientQueueEntryDTO a, PatientQueueEntryDTO b, String view) {
        if ("discharged".equals(view)) {
            return compareDescLocalDateTime(a.completedAt, b.completedAt);
        }
        if ("referred".equals(view)) {
            return compareDescLocalDateTime(a.referredAt, b.referredAt);
        }
        if (a.emergency != b.emergency) {
            return Boolean.compare(b.emergency, a.emergency);
        }
        int pa = a.queuePosition != null ? a.queuePosition : Integer.MAX_VALUE;
        int pb = b.queuePosition != null ? b.queuePosition : Integer.MAX_VALUE;
        if (pa != pb) {
            return Integer.compare(pa, pb);
        }
        if (a.queuedAt == null && b.queuedAt == null) {
            return 0;
        }
        if (a.queuedAt == null) {
            return 1;
        }
        if (b.queuedAt == null) {
            return -1;
        }
        return a.queuedAt.compareTo(b.queuedAt);
    }

    private int compareDescLocalDateTime(LocalDateTime a, LocalDateTime b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return 1;
        }
        if (b == null) {
            return -1;
        }
        return b.compareTo(a);
    }

    @Transactional
    public Response updateEntryStatus(Long id, String newStatus) {
        requireActiveSubscription();
        PatientQueueEntry entry = patientQueueEntryRepository.findById(id);
        if (entry == null) {
            return notFound("Queue entry not found");
        }
        entry.status = newStatus;
        if ("CALLED".equals(newStatus) || "SERVING".equals(newStatus)) {
            if (entry.calledAt == null) {
                entry.calledAt = LocalDateTime.now();
            }
        }
        if ("COMPLETED".equals(newStatus) || "CANCELLED".equals(newStatus) || "DISCHARGED".equals(newStatus)) {
            entry.completedAt = LocalDateTime.now();
        }
        return Response.ok(new ResponseMessage("Queue entry updated", new PatientQueueEntryDTO(entry))).build();
    }

    private Response badRequest(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ResponseMessage(message, null))
                .build();
    }

    private Response notFound(String message) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(new ResponseMessage(message, null))
                .build();
    }
}
