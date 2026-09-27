package org.example.user.services;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.example.auth.services.payloads.RoleResponse;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.statics.RoleEnums;
import org.example.user.services.payLoads.requests.AgentUserRequest;
import org.example.user.services.payLoads.requests.UpdateAgentRole;
import org.example.user.services.payLoads.requests.UpdateProfilePicRequest;
import org.example.user.services.payLoads.requests.UpdateRequest;
import org.example.user.services.payLoads.requests.UserRequest;
import org.example.user.services.payLoads.responses.dtos.UserDTO;
import org.example.user.domains.User;
import org.example.user.domains.repositories.UserRepository;
import org.example.user.services.RoleService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;

    public static final String NOT_FOUND = "Not found!";

    public UserDTO createNewCustomerUser(UserRequest request){
        User user = new User();
        user.username = request.username;
        user.email = request.email;
        user.password = BcryptUtil.bcryptHash(request.password);
        user.role = request.role;
        user.secondaryRoles = normalizeSecondaryRoles(request.secondaryRoles, request.role);
        user.qualification = request.qualification;
        user.registrationNumber = request.registrationNumber;
        user.status = request.status != null && !request.status.isBlank() ? request.status : "active";
        user.profilePic = request.profilePic;
        user.contact = request.contact;
        // New users start with dashboard only until MD grants pages in Access Control.
        if (request.role == null
                || request.role.isBlank()
                || (!"admin".equalsIgnoreCase(request.role.trim())
                    && !"md".equalsIgnoreCase(request.role.trim()))) {
            user.allowedPageRoutes = "main-dashboard";
        }
        userRepository.persist(user);

        return new UserDTO(user);

    }

    public List<UserDTO> getAllUsersAsDtos() {
        return userRepository.listAll().stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
    }

    public List<UserDTO> createNewCustomerUserAndListAll(UserRequest request) {
        createNewCustomerUser(request);
        userRepository.getEntityManager().flush();
        return getAllUsersAsDtos();
    }

    public User getById(Long id){
        return userRepository.findByIdOptional(id)
                .orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }

    public UserDTO updateUserById(UpdateRequest request, Long id){
        User user = userRepository.findById(id);
        if (user == null) {
            throw new WebApplicationException(NOT_FOUND, 404);
        }
        if (request.username != null) {
            user.username = request.username;
        }
        if (request.email != null) {
            user.email = request.email;
        }
        if (request.contact != null) {
            user.contact = request.contact;
        }
        if (request.qualification != null) {
            user.qualification = request.qualification.trim();
        }
        if (request.profilePic != null) {
            user.profilePic = request.profilePic;
        }
        if (request.role != null && !request.role.isBlank()) {
            user.role = request.role.trim();
        }
        if (request.secondaryRoles != null) {
            user.secondaryRoles = normalizeSecondaryRoles(request.secondaryRoles, user.role);
        }
        if (request.status != null && !request.status.isBlank()) {
            user.status = request.status.trim().toLowerCase();
        }
        if (request.assignedModuleIds != null) {
            user.assignedModuleIds = normalizeIdsCsv(request.assignedModuleIds);
        }
        if (request.assignedClinicIds != null) {
            user.assignedClinicIds = normalizeIdsCsv(request.assignedClinicIds);
        }
        if (request.allowedPageRoutes != null) {
            user.allowedPageRoutes = normalizePageRoutesCsv(request.allowedPageRoutes);
        }
        if (request.hiddenPageComponents != null) {
            user.hiddenPageComponents = normalizeHiddenComponentsCsv(request.hiddenPageComponents);
        }
        if (request.password != null && !request.password.isBlank()) {
            user.password = BcryptUtil.bcryptHash(request.password.trim());
        }

        userRepository.persist(user);

        return new UserDTO(user);

    }

    public UserDTO updateRegistryItemsPerPage(Long id, Integer registryItemsPerPage) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new WebApplicationException(NOT_FOUND, 404);
        }
        if (registryItemsPerPage == null) {
            user.registryItemsPerPage = null;
        } else {
            int size = registryItemsPerPage;
            if (size != 8 && size != 15 && size != 25 && size != 50 && size != 100) {
                throw new WebApplicationException(
                        "registryItemsPerPage must be one of 8, 15, 25, 50, 100",
                        400);
            }
            user.registryItemsPerPage = size;
        }
        userRepository.persist(user);
        return new UserDTO(user);
    }

    private String normalizeIdsCsv(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .collect(Collectors.joining(","));
        return normalized.isBlank() ? null : normalized;
    }

    /** Secondary roles CSV, excluding blanks and the primary role (case-insensitive). */
    private String normalizeSecondaryRoles(String raw, String primaryRole) {
        if (raw == null) {
            return null;
        }
        String primary = primaryRole == null ? "" : primaryRole.trim().toLowerCase();
        String normalized = Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .filter(s -> primary.isBlank() || !s.equalsIgnoreCase(primary))
                .map(String::trim)
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(java.util.LinkedHashSet::new),
                        set -> String.join(",", set)
                ));
        return normalized.isBlank() ? null : normalized;
    }

    private String normalizePageRoutesCsv(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        // Explicit full-access token from Access Control panel.
        if ("*".equals(trimmed)) {
            return "*";
        }
        String normalized = Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(String::toLowerCase)
                .distinct()
                .collect(Collectors.joining(","));
        if (normalized.isBlank()) {
            return null;
        }
        if ("*".equals(normalized) || normalized.contains("*")) {
            return "*";
        }
        return normalized;
    }

    private String normalizeHiddenComponentsCsv(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .collect(Collectors.joining(","));
        return normalized.isBlank() ? null : normalized;
    }

    public UserDTO updateProfilePicById(UpdateProfilePicRequest request, Long id){
        User user = getById(id);
        user.profilePic = request.profilePic;
        userRepository.persist(user);

        return new UserDTO(user);
    }

    public List<User> getAllUsers(){
        return userRepository.listAll();
    }



    public Response deleteUserById(Long id) {
        User user = userRepository.findById(id);

        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .build();
        }

        userRepository.delete(user);

        return Response.ok(new ResponseMessage(ActionMessages.DELETED.label)).build();
    }

    public void deleteAllUsers(){
        userRepository.deleteAll();

    }



  //////////////////////////////////////agent section //////////////////////////////////



    public User createNewAgentUser(AgentUserRequest request){
        User user = new User();
        user.username = request.username;
        user.email = request.email;
        user.role = RoleEnums.valueOf(request.role).label;
        user.password = BcryptUtil.bcryptHash(request.password);

        userRepository.persist(user);

        return user;

    }


    public User updateAgentRole(Long id , UpdateAgentRole request){
        return userRepository.findByIdOptional(id)
                .map(user -> {
                    user.role = RoleEnums.valueOf(request.role).name();
                    userRepository.persist(user);

                    return user;
                }).orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }


    @Inject
    RoleService roleService;

    ////////////////////////////////////// role section //////////////////////////////////

    public RoleResponse getAllRoles() {
        return roleService.getAllRoles();
    }












































    /*public User createNewUser(UserRequest request){
        User user = new User();
        user.username = request.username;
        user.email = request.email;
        user.password = BcryptUtil.bcryptHash(request.password);
        user.role = RoleEnums.valueOf(request.role).label;
        userRepository.persist(user);

        return user;
    }

    public User getById(Long id){
        return userRepository.findByIdOptional(id)
                .orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }

    public User update(Long id, UpdateRequest request){
        return userRepository.findByIdOptional(id)
                .map(user -> {
                    user.email = request.email;
                    user.username = request.username;

                    user.persist();

                    return user;
                }).orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }

    public User updatePassword(Long id, UpdatePasswordRequest request){
        return userRepository.findByIdOptional(id)
                .map(user -> Optional.ofNullable(userRepository.login(user.username, request.oldPassword))
                        .map(confirmedUser -> validateUser(user,request))
                        .orElseThrow(() -> new WebApplicationException("Invalid credentials",409))
                )
                .orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }

    public User validateUser(User user, UpdatePasswordRequest request){
        if (Boolean.FALSE.equals(request.oldPassword.equals(request.newPassword))){
            user.password = BcryptUtil.bcryptHash(request.newPassword);
            user.persist();

            return user;
        }
        throw new WebApplicationException("Your new password must be unique",409);
    }

    public User updateRole(Long id , RoleRequest request){
        return userRepository.findByIdOptional(id)
                .map(user -> {
                    user.role = RoleEnums.valueOf(request.role).name();
                    user.persist();

                    return user;
                }).orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }

    public RoleResponse roles(){
        return new RoleResponse(Arrays.stream(RoleEnums.values())
                .map(Enum::name)
                .collect(Collectors.toSet()));
    }
    public List<User> getAllAgents() {
        return userRepository.listAll();
    }*/
}










