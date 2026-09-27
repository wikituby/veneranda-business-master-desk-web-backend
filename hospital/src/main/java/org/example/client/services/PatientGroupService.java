package org.example.client.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

import jakarta.ws.rs.core.Response;
import org.example.client.domains.PatientGroup;
import org.example.client.domains.repositories.PatientGroupRepository;
import org.example.client.services.payloads.responses.dtos.PatientGroupDTO;
import org.example.client.services.payloads.requests.PatientGroupRequest;
import org.example.client.services.payloads.requests.PatientGroupUpdateRequest;
import org.example.configuration.handler.ResponseMessage;
import org.example.configuration.security.AuthenticatedUserResolver;
import org.example.subscription.services.SpecialPrivilegeService;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class PatientGroupService {

    @Inject
    PatientGroupRepository patientGroupRepository;

    @Inject
    SpecialPrivilegeService specialPrivilegeService;

    @Inject
    AuthenticatedUserResolver authenticatedUserResolver;

    private static final String NOT_FOUND = "Not found!";


    @Transactional
    public Response createNewPatientGroup(PatientGroupRequest request){

        PatientGroup existingPatientGroup = patientGroupRepository.findByNormalizedGroupName(request.groupName);

        if (existingPatientGroup != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ResponseMessage("A group with the same or similar name already exists.", null))
                    .build();
        }

        PatientGroup existingShortFormGroup = patientGroupRepository.findByNormalizedShortForm(request.groupNameShortForm);

        if (existingShortFormGroup != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ResponseMessage("A group with the same or similar short name already exists.", null))
                    .build();
        }


        PatientGroup patientGroup = new PatientGroup();
        patientGroup.groupName = request.groupName;
        patientGroup.groupEmail = request.groupEmail;
        patientGroup.groupContact = request.groupContact;
        patientGroup.groupAddress = request.groupAddress;
        patientGroup.description = request.description;
        patientGroup.groupNameShortForm = request.groupNameShortForm;
        patientGroup.patientGroupCreationDate = LocalDate.now();
        applyCreditFlagOnCreate(patientGroup, request.allowsCreditDespiteUnpaidBalance);

        patientGroupRepository.persist(patientGroup);

        return Response.status(Response.Status.CREATED)
                .entity(new ResponseMessage("Patient Group created successfully", new PatientGroupDTO(patientGroup)))
                .build();

    }

    private void applyCreditFlagOnCreate(PatientGroup patientGroup, Boolean requested) {
        if (requested == null) {
            patientGroup.allowsCreditDespiteUnpaidBalance =
                    PatientGroupCreditUtil.allowsCreditDespiteUnpaidBalance(patientGroup);
            return;
        }
        if (Boolean.TRUE.equals(requested)
                && !specialPrivilegeService.hasPrivilege(
                        authenticatedUserResolver.requireCurrentUser(),
                        SpecialPrivilegeService.Privilege.MANAGE_CREDIT_GROUP_RIGHTS)) {
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity(new ResponseMessage(
                                    "You need admin approval to grant a group credit / debt rights", null))
                            .build()
            );
        }
        patientGroup.allowsCreditDespiteUnpaidBalance = requested;
    }



    @Transactional
    public List<PatientGroupDTO> getAllPatientGroups() {
        return patientGroupRepository.listAll(Sort.descending("id"))
                .stream()
                .map(PatientGroupDTO::new)
                .toList();
    }

    public PatientGroupDTO updatePatientGroupById(Long id, PatientGroupUpdateRequest request) {
        return patientGroupRepository.findByIdOptional(id)
                .map(patientGroup -> {

                    patientGroup.groupName = request.groupName;
                    patientGroup.groupNameShortForm = request.groupNameShortForm;
                    patientGroup.groupAddress = request.groupAddress;
                    patientGroup.groupEmail = request.groupEmail;
                    patientGroup.groupContact = request.groupContact;
                    patientGroup.description = request.description;
                    patientGroup.patientGroupLastUpdatedDate = LocalDate.now();
                    if (request.allowsCreditDespiteUnpaidBalance != null) {
                        boolean current = PatientGroupCreditUtil.allowsCreditDespiteUnpaidBalance(patientGroup);
                        boolean next = Boolean.TRUE.equals(request.allowsCreditDespiteUnpaidBalance);
                        if (current != next
                                && !specialPrivilegeService.hasPrivilege(
                                        authenticatedUserResolver.requireCurrentUser(),
                                        SpecialPrivilegeService.Privilege.MANAGE_CREDIT_GROUP_RIGHTS)) {
                            throw new WebApplicationException(
                                    Response.status(Response.Status.BAD_REQUEST)
                                            .entity(new ResponseMessage(
                                                    "You need admin approval to change group credit / debt rights", null))
                                            .build()
                            );
                        }
                        patientGroup.allowsCreditDespiteUnpaidBalance = next;
                    }

                    patientGroupRepository.persist(patientGroup);

                    return new PatientGroupDTO(patientGroup);
                }).orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }

    public PatientGroupDTO getPatientGroupById(Long id) {
        return patientGroupRepository.findByIdOptional(id)
                .map(PatientGroupDTO::new)  // Convert Patient entity to PatientDTO
                .orElseThrow(() -> new WebApplicationException("Patient Group not found", 404));
    }



    @Transactional
    public Response deleteGroupById(Long id){
        PatientGroup patientGroup = patientGroupRepository.findById(id);
        if (patientGroup == null) {
            //return Response.status(Response.Status.NOT_FOUND).build();
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Group not found", null))
                    .build();
        }
        patientGroupRepository.delete(patientGroup);
        return Response.ok(new ResponseMessage("Group Deleted successfully")).build();
    }







}






