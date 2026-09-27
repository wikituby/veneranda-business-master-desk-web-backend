package org.example.cafeteria.client.services;

import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.Row;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.example.cafeteria.client.domains.Buyer;
import org.example.cafeteria.client.domains.BuyerGroup;
import org.example.cafeteria.client.domains.repositories.BuyerGroupRepository;
import org.example.cafeteria.client.domains.repositories.BuyerRepository;
import org.example.cafeteria.client.services.payloads.requests.BuyerParametersRequest;
import org.example.cafeteria.client.services.payloads.requests.BuyerRequest;
import org.example.cafeteria.client.services.payloads.requests.BuyerUpdateRequest;
import org.example.cafeteria.client.services.payloads.responses.FullBuyerResponse;
import org.example.cafeteria.client.services.payloads.responses.dtos.PatientDTO;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicReference;

@ApplicationScoped
public class BuyerService {

    @Inject
    BuyerRepository buyerRepository;

    @Inject
    DeletedBuyerNosService deletedBuyerNosService;

    @Inject
    BuyerGroupRepository buyerGroupRepository;

    @Inject
    Pool client;


    public static final String NOT_FOUND = "Not found!";

    @Transactional
    public Response createNewPatient(BuyerRequest request) {
        // Validate the request
        /*if (request.patientAge == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Patient Age cannot be null or empty", null))
                    .build();
        }

        if (request.patientGender == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Patient Gender cannot be null or empty", null))
                    .build();
        }*/




        // Check if a buyer with the same first and second names already exists
        Buyer existingBuyer = buyerRepository.findByFirstNameAndSecondName(
                request.patientFirstName, request.patientSecondName);

        if (existingBuyer != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ResponseMessage("A buyer with the same first and second names already exists", null))
                    .build();
        }



        BuyerGroup patientGroup = null;
        if (request.patientGroupId != null) {
            patientGroup = buyerGroupRepository.findById(request.patientGroupId);
            if (patientGroup == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseMessage("Patient group not found for ID: " + request.patientGroupId, null))
                        .build();
            }
        }

        // Create new Patient entity and set basic information
        Buyer buyer = new Buyer();
        buyer.patientGroup = patientGroup;
        buyer.patientFirstName = request.patientFirstName;
        buyer.patientSecondName = request.patientSecondName;
        buyer.patientAddress = request.patientAddress;
        buyer.patientAge = request.patientAge;
        buyer.patientContact = request.patientContact;
        buyer.patientGender = request.patientGender;

//        buyer.patientProfilePic = "https://firebasestorage.googleapis.com/v0/b/newstorageforuplodapp.appspot.com/o/images%2Fplaceholder.jpg?alt=media&token=caade802-c591-4dee-b590-a040c694553b";

        buyer.patientDateOfBirth = request.patientDateOfBirth;
        buyer.creationDate = LocalDate.now();

        // Set Next of Kin information
        buyer.nextOfKinName = request.nextOfKinName;
        buyer.nextOfKinAddress = request.nextOfKinAddress;
        buyer.nextOfKinContact = request.nextOfKinContact;
        buyer.relationship = request.relationship;

        // Determine buyer number
        int deletedPatientNumberInQue = deletedBuyerNosService.findFirstDeletedPatientNo();
        if (deletedPatientNumberInQue == 0) {
            buyer.patientNo = findMaxPatientFileNoReturnInt() + 1;
        } else {
            buyer.patientNo = deletedPatientNumberInQue;
        }

        // Generate buyer file number
        buyer.patientFileNo = "OPD" + buyer.patientNo;

        // Persist the new Patient entity
        buyerRepository.persist(buyer);

        // Remove the deleted buyer number from the queue
        if (deletedPatientNumberInQue != 0) {
            deletedBuyerNosService.deleteByDeletedPatientNumber(deletedPatientNumberInQue);
        }

        // Return a success response with the created PatientDTO
        return Response.status(Response.Status.CREATED)
                .entity(new ResponseMessage("Patient created successfully", new PatientDTO(buyer)))
                .build();
    }


    public void updateTotalAmountDue(Buyer buyer, BigDecimal totalAmountDue){

        buyer.totalAmountDue = totalAmountDue;

        buyerRepository.persist(buyer);

    }


    @Transactional
    public Response createMultiplePatients(List<BuyerRequest> requests) {
        List<PatientDTO> createdPatients = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (BuyerRequest request : requests) {
            try {

                // Check for existing buyer
                Buyer existingBuyer = buyerRepository.findByFirstNameAndSecondName(
                        request.patientFirstName, request.patientSecondName);
                if (existingBuyer != null) {
                    errors.add("Duplicate buyer: " + request.patientFirstName + " " + request.patientSecondName);
                    continue;
                }

                // Check buyer group
                BuyerGroup patientGroup = null;
                if (request.patientGroupId != null) {
                    patientGroup = buyerGroupRepository.findById(request.patientGroupId);
                    if (patientGroup == null) {
                        errors.add("Invalid group ID for buyer: " + request.patientFirstName + " " + request.patientSecondName);
                        continue;
                    }
                }

                // Create and populate Patient
                Buyer buyer = new Buyer();
                buyer.patientGroup = patientGroup;
                buyer.patientFirstName = request.patientFirstName;
                buyer.patientSecondName = request.patientSecondName;
                buyer.patientAddress = request.patientAddress;
                buyer.patientAge = request.patientAge;
                buyer.patientContact = request.patientContact;
                buyer.patientGender = request.patientGender;
                buyer.patientProfilePic = request.patientProfilePic;
                buyer.patientDateOfBirth = request.patientDateOfBirth;
                buyer.creationDate = LocalDate.now();

                // Set next of kin info
                buyer.nextOfKinName = request.nextOfKinName;
                buyer.nextOfKinAddress = request.nextOfKinAddress;
                buyer.nextOfKinContact = request.nextOfKinContact;
                buyer.relationship = request.relationship;

                // Assign buyer number
                int deletedPatientNumberInQue = deletedBuyerNosService.findFirstDeletedPatientNo();
                if (deletedPatientNumberInQue == 0) {
                    buyer.patientNo = findMaxPatientFileNoReturnInt() + 1;
                } else {
                    buyer.patientNo = deletedPatientNumberInQue;
                }

                buyer.patientFileNo = "OPD" + buyer.patientNo;

                buyerRepository.persist(buyer);

                // Remove number from deleted queue
                if (deletedPatientNumberInQue != 0) {
                    deletedBuyerNosService.deleteByDeletedPatientNumber(deletedPatientNumberInQue);
                }

                createdPatients.add(new PatientDTO(buyer));

            } catch (Exception ex) {
                errors.add("Error creating buyer: " + request.patientFirstName + " " + request.patientSecondName);
            }
        }

        if (createdPatients.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("No patients created", errors))
                    .build();
        }

        return Response.status(Response.Status.CREATED)
                .entity(new ResponseMessage("Patients created successfully", createdPatients))
                .build();
    }










    /*@Transactional
    public List<Patient> getAllPatients() {
        return patientRepository.listAll(Sort.descending("patientNo"));
    }*/

    @Transactional
    public List<PatientDTO> getAllPatients() {
        return buyerRepository.listAll(Sort.descending("patientNo"))
                .stream()
                .map(PatientDTO::new)
                .toList();
    }

    @Transactional
    public List<PatientDTO> getAllPatientsWithDebt() {
        return buyerRepository.listAll(Sort.descending("patientNo"))
                .stream()
                .filter(buyer -> buyer.getTotalBalanceDue() != null &&
                        buyer.getTotalBalanceDue().compareTo(BigDecimal.ZERO) > 0)
                .map(PatientDTO::new)
                .toList();
    }

    @Transactional
    public List<PatientDTO> getAllPatientsByGroupId(Long groupId) {
        return buyerRepository.listAll(Sort.descending("patientNo"))
                .stream()
                .filter(buyer -> buyer.patientGroup != null &&
                        buyer.patientGroup.id.equals(groupId))
                .map(PatientDTO::new)
                .toList();
    }


    public PatientDTO getPatientById(Long id) {
        return buyerRepository.findByIdOptional(id)
                .map(PatientDTO::new)  // Convert Patient entity to PatientDTO
                .orElseThrow(() -> new WebApplicationException("Patient not found", 404));
    }

    @Transactional
    public PatientDTO updatePatientById(Long id, BuyerUpdateRequest request) {
        // Find the buyer group (only if patientGroupId is provided)
        BuyerGroup patientGroup;
        if (request.patientGroupId != null) {
            patientGroup = buyerGroupRepository.findById(request.patientGroupId);
            if (patientGroup == null) {
                throw new IllegalArgumentException("Patient group not found for ID: " + request.patientGroupId);
            }
        } else {
            patientGroup = null;
        }

        return buyerRepository.findByIdOptional(id)
                .map(buyer -> {
                    // Update buyer fields
                    buyer.patientFirstName = request.patientFirstName;
                    buyer.patientSecondName = request.patientSecondName;
                    buyer.patientAddress = request.patientAddress;
                    buyer.patientContact = request.patientContact;
                    buyer.patientGender = request.patientGender;
                    buyer.patientAge = request.patientAge;
                    buyer.patientGroup = patientGroup; // Can be null
                    buyer.nextOfKinName = request.nextOfKinName;
                    buyer.nextOfKinContact = request.nextOfKinContact;
                    buyer.relationship = request.relationship;
                    buyer.nextOfKinAddress = request.nextOfKinAddress;
                    buyer.patientDateOfBirth = request.patientDateOfBirth;
                    buyer.patientLastUpdatedDate = LocalDate.now();

                    // Persist the updated buyer
                    buyerRepository.persist(buyer);

                    // Return the updated buyer as a DTO
                    return new PatientDTO(buyer);
                })
                .orElseThrow(() -> new WebApplicationException("Patient not found for ID: " + id, Integer.parseInt(NOT_FOUND)));
    }


    @Transactional
    public Object findMaxPatientNo() {
        return buyerRepository.listAll(Sort.descending("patientNo"))
                .stream()
                .findFirst()
                .orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }

   // public static final String NOT_FOUND = "Not found!";


    @Transactional
    public int findMaxPatientFileNoReturnInt() {
        return buyerRepository.listAll(Sort.descending("patientNo"))
                .stream()
                .map(buyer -> buyer.patientNo)
                .findFirst()
                .orElse(0);
    }

    @Transactional
    public Response deletePatientById(Long id) {

        Buyer buyer = buyerRepository.findById(id);

        if (buyer == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .build();
        }

        deletedBuyerNosService.saveDeletedPatientNo(buyer.patientNo);

        buyerRepository.delete(buyer);

        return Response.ok(new ResponseMessage(ActionMessages.DELETED.label)).build();
    }





    public List<FullBuyerResponse> getPatientsAdvancedFilter(BuyerParametersRequest request) {
        StringJoiner whereClause = getStringJoiner(request);

        String sql = """
        SELECT
            id,
            group_id,
            nextOfKinAddress,
            nextOfKinContact,
            nextOfKinName,
            patientAddress,
            patientAge,
            patientContact,
            patientDateOfBirth,
            patientFileNo,
            patientFirstName,
            patientGender,
            patientLastUpdatedDate,
            patientNo,
            patientProfilePic,
            patientSecondName,
            relationship,
            totalAmountDue
        FROM buyer
        %s
        ORDER BY id DESC;
        """.formatted(whereClause);

        return client.query(sql)
                .execute()
                .onItem()
                .transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem()
                .transform(this::from)
                .collect().asList()
                .await()
                .indefinitely();
    }


    private FullBuyerResponse from(Row row){

        FullBuyerResponse response = new FullBuyerResponse();
        response.id = row.getLong("id");
        response.group_id = row.getLong("group_id");
        response.nextOfKinAddress = row.getString("nextOfKinAddress");
        response.nextOfKinContact = row.getString("nextOfKinContact");
        response.nextOfKinName = row.getString("nextOfKinName");
        response.patientAddress = row.getString("patientAddress");
        response.patientAge = row.getBigDecimal("patientAge");
        response.totalAmountDue = row.getBigDecimal("totalAmountDue");
        response.patientNo = row.getInteger("patientNo");

        response.patientContact = row.getString("patientContact");
        response.patientFileNo = row.getString("patientFileNo");
        response.patientFirstName = row.getString("patientFirstName");
        response.patientGender = row.getString("patientGender");
        response.patientProfilePic = row.getString("patientProfilePic");
        response.patientSecondName = row.getString("patientSecondName");


        response.patientDateOfBirth = row.getLocalDate("patientDateOfBirth");
        response.patientLastUpdatedDate = row.getLocalDate("patientLastUpdatedDate");

        return response;
    }

    private StringJoiner getStringJoiner(BuyerParametersRequest request) {
        AtomicReference<Boolean> hasSearchCriteria = new AtomicReference<>(Boolean.FALSE);

        List<String> conditions = new ArrayList<>();
        if (request.group_id != null) {
            conditions.add("group_id = " + request.group_id);
            hasSearchCriteria.set(Boolean.TRUE);
        }


        if (request.patientAddress != null && !request.patientAddress.isEmpty()) {
            conditions.add("patientAddress = '" + request.patientAddress + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }

        if (request.patientGender != null && !request.patientGender.isEmpty()) {
            conditions.add("patientGender = '" + request.patientGender + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }

        /*if (request.datefrom != null && request.dateto != null) {
            conditions.add("dateOfPayment BETWEEN '" + request.datefrom + "' AND '" + request.dateto + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }*/

        StringJoiner whereClause = new StringJoiner(" AND ", "WHERE ", "");

        conditions.forEach(whereClause::add);

        if (Boolean.FALSE.equals(hasSearchCriteria.get())) {
            whereClause.add("1 = 1");
        }

        return whereClause;
    }









}






