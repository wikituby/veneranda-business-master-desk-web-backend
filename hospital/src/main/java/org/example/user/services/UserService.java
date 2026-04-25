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
        user.qualification = request.qualification;
        user.registrationNumber = request.registrationNumber;
        user.status = request.status;
        user.profilePic = request.profilePic;
        user.contact = request.contact;
        userRepository.persist(user);

        return new UserDTO(user);

    }

    public User getById(Long id){
        return userRepository.findByIdOptional(id)
                .orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }

    public UserDTO updateUserById(UpdateRequest request, Long id){
        User user = userRepository.findById(id);
        user.username = request.username;
        user.email = request.email;
        user.contact = request.contact;
        user.profilePic = request.profilePic;

        userRepository.persist(user);

        return new UserDTO(user);

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


    ////////////////////////////////////// role section //////////////////////////////////


    public RoleResponse getAllRoles(){
        return new RoleResponse(Arrays.stream(RoleEnums.values())
                .map(Enum::name)
                .collect(Collectors.toSet()));

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










