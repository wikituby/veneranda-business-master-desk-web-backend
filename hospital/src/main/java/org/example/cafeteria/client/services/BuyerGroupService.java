package org.example.cafeteria.client.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.example.cafeteria.client.domains.BuyerGroup;
import org.example.cafeteria.client.domains.repositories.BuyerGroupRepository;
import org.example.cafeteria.client.services.payloads.requests.BuyerGroupRequest;
import org.example.cafeteria.client.services.payloads.requests.BuyerGroupUpdateRequest;
import org.example.cafeteria.client.services.payloads.responses.dtos.PatientGroupDTO;
import org.example.configuration.handler.ResponseMessage;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class BuyerGroupService {

    @Inject
    BuyerGroupRepository buyerGroupRepository;

    private static final String NOT_FOUND = "Not found!";


    @Transactional
    public Response createNewPatientGroup(BuyerGroupRequest request){

        BuyerGroup existingPatientGroup = buyerGroupRepository.findByNormalizedGroupName(request.groupName);

        if (existingPatientGroup != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ResponseMessage("A group with the same or similar name already exists.", null))
                    .build();
        }

        BuyerGroup existingShortFormGroup = buyerGroupRepository.findByNormalizedShortForm(request.groupNameShortForm);

        if (existingShortFormGroup != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ResponseMessage("A group with the same or similar short name already exists.", null))
                    .build();
        }


        BuyerGroup patientGroup = new BuyerGroup();
        patientGroup.groupName = request.groupName;
        patientGroup.groupEmail = request.groupEmail;
        patientGroup.groupContact = request.groupContact;
        patientGroup.groupAddress = request.groupAddress;
        patientGroup.description = request.description;
        patientGroup.groupNameShortForm = request.groupNameShortForm;
        patientGroup.patientGroupCreationDate = LocalDate.now();

        buyerGroupRepository.persist(patientGroup);

        //return new PatientGroupDTO(patientGroup);

        return Response.status(Response.Status.CREATED)
                .entity(new ResponseMessage("Patient Group created successfully", new PatientGroupDTO(patientGroup)))
                .build();

    }



    @Transactional
    public List<PatientGroupDTO> getAllPatientGroups() {
        return buyerGroupRepository.listAll(Sort.descending("id"))
                .stream()
                .map(PatientGroupDTO::new)
                .toList();
    }

    public PatientGroupDTO updatePatientGroupById(Long id, BuyerGroupUpdateRequest request) {
        return buyerGroupRepository.findByIdOptional(id)
                .map(patientGroup -> {

                    patientGroup.groupName = request.groupName;
                    patientGroup.groupNameShortForm = request.groupNameShortForm;
                    patientGroup.groupAddress = request.groupAddress;
                    patientGroup.groupEmail = request.groupEmail;
                    patientGroup.groupContact = request.groupContact;
                    patientGroup.patientGroupLastUpdatedDate = LocalDate.now();

                    buyerGroupRepository.persist(patientGroup);

                    return new PatientGroupDTO(patientGroup);
                }).orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }

    public PatientGroupDTO getPatientGroupById(Long id) {
        return buyerGroupRepository.findByIdOptional(id)
                .map(PatientGroupDTO::new)  // Convert Patient entity to PatientDTO
                .orElseThrow(() -> new WebApplicationException("Patient Group not found", 404));
    }



    @Transactional
    public Response deleteGroupById(Long id){
        BuyerGroup patientGroup = buyerGroupRepository.findById(id);
        if (patientGroup == null) {
            //return Response.status(Response.Status.NOT_FOUND).build();
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Group not found", null))
                    .build();
        }
        buyerGroupRepository.delete(patientGroup);
        return Response.ok(new ResponseMessage("Group Deleted successfully")).build();
    }







}






