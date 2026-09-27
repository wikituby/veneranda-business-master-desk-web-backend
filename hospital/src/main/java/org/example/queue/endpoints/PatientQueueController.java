package org.example.queue.endpoints;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.queue.services.HospitalQueueService;
import org.example.queue.services.payloads.requests.HospitalClinicRequest;
import org.example.queue.services.payloads.requests.HospitalModuleRequest;
import org.example.queue.services.payloads.requests.PatientQueueRequest;
import org.example.queue.services.payloads.requests.PatientQueueUpdateRequest;

@Path("Client-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Client Management - Queue & Business Directory", description = "Patient queue and business modules")
public class PatientQueueController {

    @Inject
    HospitalQueueService hospitalQueueService;

    @GET
    @Path("/get-all-business-modules")
    @Operation(summary = "List business modules / departments for queue routing")
    public Response getAllModules() {
        return hospitalQueueService.getAllModules();
    }

    @POST
    @Path("/create-business-module")
    @Transactional
    public Response createModule(HospitalModuleRequest request) {
        return hospitalQueueService.createModule(request);
    }

    @PUT
    @Path("/update-business-module/{id}")
    @Transactional
    public Response updateModule(@PathParam("id") Long id, HospitalModuleRequest request) {
        return hospitalQueueService.updateModule(id, request);
    }

    @DELETE
    @Path("/delete-business-module/{id}")
    @Transactional
    public Response deleteModule(@PathParam("id") Long id) {
        return hospitalQueueService.deleteModule(id);
    }

    @GET
    @Path("/get-business-clinics")
    public Response getClinics(@QueryParam("moduleId") Long moduleId) {
        return hospitalQueueService.getClinics(moduleId);
    }

    @POST
    @Path("/create-business-clinic")
    @Transactional
    public Response createClinic(HospitalClinicRequest request) {
        return hospitalQueueService.createClinic(request);
    }

    @PUT
    @Path("/update-business-clinic/{id}")
    @Transactional
    public Response updateClinic(@PathParam("id") Long id, HospitalClinicRequest request) {
        return hospitalQueueService.updateClinic(id, request);
    }

    @DELETE
    @Path("/delete-business-clinic/{id}")
    @Transactional
    public Response deleteClinic(@PathParam("id") Long id) {
        return hospitalQueueService.deleteClinic(id);
    }

    @POST
    @Path("/queue-patient")
    @Transactional
    public Response queuePatient(PatientQueueRequest request) {
        return hospitalQueueService.queuePatient(request);
    }

    @POST
    @Path("/discharge-patient-from-queue")
    @Transactional
    public Response dischargePatientFromQueue(
            org.example.queue.services.payloads.requests.DischargePatientQueueRequest request) {
        return hospitalQueueService.dischargePatientFromQueue(request);
    }

    @GET
    @Path("/get-patient-queue-entries")
    public Response getQueueEntries(
            @QueryParam("toModuleId") Long toModuleId,
            @QueryParam("status") String status,
            @QueryParam("role") String role,
            @QueryParam("assignedModuleIds") String assignedModuleIds,
            @QueryParam("assignedClinicIds") String assignedClinicIds) {
        return hospitalQueueService.getQueueEntries(toModuleId, status, role, assignedModuleIds, assignedClinicIds);
    }

    @GET
    @Path("/get-business-directory")
    public Response getHospitalDirectory() {
        return hospitalQueueService.getHospitalDirectory();
    }

    @GET
    @Path("/get-latest-patient-queue-entries")
    public Response getLatestQueueEntries(
            @QueryParam("limit") Long limit,
            @QueryParam("view") String view,
            @QueryParam("role") String role,
            @QueryParam("assignedModuleIds") String assignedModuleIds,
            @QueryParam("assignedClinicIds") String assignedClinicIds) {
        return hospitalQueueService.getLatestQueueEntries(limit, view, role, assignedModuleIds, assignedClinicIds);
    }

    @PATCH
    @Path("/update-patient-queue-entry/{id}")
    @Transactional
    public Response updateQueueEntry(@PathParam("id") Long id, PatientQueueUpdateRequest request) {
        return hospitalQueueService.updateQueueEntry(id, request);
    }

    @PATCH
    @Path("/call-patient-queue-entry/{id}")
    @Transactional
    public Response callEntry(@PathParam("id") Long id) {
        return hospitalQueueService.updateEntryStatus(id, "CALLED");
    }

    @PATCH
    @Path("/serve-patient-queue-entry/{id}")
    @Transactional
    public Response serveEntry(@PathParam("id") Long id) {
        return hospitalQueueService.updateEntryStatus(id, "SERVING");
    }

    @PATCH
    @Path("/complete-patient-queue-entry/{id}")
    @Transactional
    public Response completeEntry(@PathParam("id") Long id) {
        return hospitalQueueService.updateEntryStatus(id, "COMPLETED");
    }

    @PATCH
    @Path("/cancel-patient-queue-entry/{id}")
    @Transactional
    public Response cancelEntry(@PathParam("id") Long id) {
        return hospitalQueueService.updateEntryStatus(id, "CANCELLED");
    }

    @PATCH
    @Path("/discharge-patient-queue-entry/{id}")
    @Transactional
    public Response dischargeEntry(@PathParam("id") Long id) {
        return hospitalQueueService.updateEntryStatus(id, "DISCHARGED");
    }
}
