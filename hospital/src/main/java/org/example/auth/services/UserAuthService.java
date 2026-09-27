package org.example.auth.services;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.example.auth.services.payloads.*;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.configuration.security.JwtUtils;
import org.example.user.domains.User;
import org.example.user.domains.repositories.UserRepository;
import org.example.messages.EmailService;
import org.example.statics.UserTypes;
import org.jboss.logging.Logger;


import java.util.Optional;

import static org.example.user.services.UserService.NOT_FOUND;

@ApplicationScoped

public class UserAuthService {

    @Inject
    UserRepository userRepository;

    @Inject
    EmailService emailService;

    @Inject
    JwtUtils jwtUtils;

    private static final String BEARER = "Bearer ";


    public UserAuthResponse login(UserAuthRequest request) {
        return Optional.ofNullable(userRepository.login(request.email, request.password))
                .map(user -> {
                    if (isInactiveAccount(user.status)) {
                        throw new WebApplicationException(
                                "Your account is inactive. Please contact an administrator.",
                                403
                        );
                    }
                    String jwtToken = jwtUtils.generateJwtToken(user);
                    return new UserAuthResponse(user, jwtToken, UserTypes.AGENT.label);
                })
                .orElseThrow(() -> new WebApplicationException("user Not Found", 404));
    }

    private boolean isInactiveAccount(String status) {
        return status != null && "inactive".equalsIgnoreCase(status.trim());
    }

    @Transactional
    public Response sendResetPassword(ResetPasswordRequest request){
        return userRepository.findByEmailOptional(request.email)
                .map(user -> {
                    String generatedPassword = generateRandomPassword(5);

                    emailService.sendPasswordResetLink(user, generatedPassword)
                            .subscribe()
                            .with(voidItem -> Logger.getLogger(UserAuthService.class).info("Email sent"));

                    user.password = BcryptUtil.bcryptHash(generatedPassword);


                    userRepository.persist(user);


                    return Response.ok(new ResponseMessage(ActionMessages.SEND.label)).build();
                        }
                )
                .orElseThrow(() -> new WebApplicationException("User Not found",404));
    }


    public String generateRandomPassword(int length) {
        // Define characters that can be used in the password
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        // Generate a random password of the specified length
        for (int i = 0; i < length; i++) {
            int randomIndex = (int) (Math.random() * characters.length());
            password.append(characters.charAt(randomIndex));
        }

        return password.toString();
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
            userRepository.persist(user);

            return user;
        }
        throw new WebApplicationException("Your new password must be unique",409);
    }

    public Response updatePassword(String token, ForcePasswordUpdateRequest request){
        if (Boolean.TRUE.equals(jwtUtils.validateResetToken(token))){
            return userRepository.findByEmailOptional(jwtUtils.getJwt().getSubject())
                    .map(user -> {
                        user.password = BcryptUtil.bcryptHash(request.newPassword);
                        userRepository.persist(user);

                        return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label)).build();
                    })
                    .orElseThrow(() -> new WebApplicationException("Something went wrong Contact Admin"));
        }
        throw new WebApplicationException("Invalid token",403);
    }







}






