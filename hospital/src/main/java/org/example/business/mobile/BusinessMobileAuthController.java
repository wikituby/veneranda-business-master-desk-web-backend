package org.example.business.mobile;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.auth.services.UserAuthService;
import org.example.auth.services.payloads.UserAuthRequest;
import org.example.auth.services.payloads.UserAuthResponse;
import org.example.configuration.security.JwtUtils;
import org.example.user.domains.User;
import org.example.user.domains.repositories.UserRepository;
import org.example.user.services.UserService;
import org.example.user.services.payLoads.requests.UserRequest;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Flutter mother-app auth contract under /health_care/auth/*.
 */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Business Mobile Auth", description = "Flutter remote auth adapters")
public class BusinessMobileAuthController {

    @Inject
    UserAuthService userAuthService;

    @Inject
    UserService userService;

    @Inject
    UserRepository userRepository;

    @Inject
    JwtUtils jwtUtils;

    @POST
    @Path("/login")
    @Operation(summary = "Flutter login", description = "Returns token + user shape expected by the mobile app")
    public Response login(Map<String, Object> body) {
        String email = str(body == null ? null : body.get("email"));
        String password = str(body == null ? null : body.get("password"));
        if (email.isBlank() || password.isBlank()) {
            return Response.status(400).entity(Map.of("message", "Email and password are required")).build();
        }
        try {
            UserAuthResponse auth = userAuthService.login(new UserAuthRequest(email, password));
            return Response.ok(flutterAuthPayload(auth.user, auth.jwt)).build();
        } catch (WebApplicationException e) {
            int code = e.getResponse() == null ? 401 : e.getResponse().getStatus();
            String message = e.getMessage() == null ? "Invalid credentials" : e.getMessage();
            if (code == 403) {
                return Response.status(403).entity(Map.of(
                        "message", message,
                        "code", "PENDING_APPROVAL"
                )).build();
            }
            if (code == 404) {
                return Response.status(401).entity(Map.of("message", "Invalid credentials")).build();
            }
            return Response.status(code >= 400 ? code : 401).entity(Map.of("message", message)).build();
        }
    }

    @POST
    @Path("/signup")
    @Transactional
    @Operation(summary = "Flutter signup")
    public Response signup(Map<String, Object> body) {
        String email = str(body == null ? null : body.get("email")).toLowerCase();
        String password = str(body == null ? null : body.get("password"));
        String name = str(body == null ? null : body.get("name"));
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            return Response.status(400).entity(Map.of("message", "Missing fields")).build();
        }
        if (userRepository.findByEmailOptional(email).isPresent()
                || userRepository.findByUsernameOptional(name).isPresent()) {
            return Response.status(409).entity(Map.of("message", "User already exists")).build();
        }
        UserRequest request = new UserRequest();
        request.email = email;
        request.password = password;
        request.username = name;
        request.role = "STAFF";
        request.status = "active";
        userService.createNewCustomerUser(request);
        return Response.status(201).entity(Map.of(
                "message", "Signup received. Await role assignment from administrator."
        )).build();
    }

    @POST
    @Path("/reset-password")
    @Transactional
    @Operation(summary = "Flutter reset password")
    public Response resetPassword(Map<String, Object> body) {
        String email = str(body == null ? null : body.get("email")).toLowerCase();
        String newPassword = str(body == null ? null : body.get("newPassword"));
        if (email.isBlank() || newPassword.isBlank()) {
            return Response.status(400).entity(Map.of("message", "Missing fields")).build();
        }
        return userRepository.findByEmailOptional(email)
                .map(user -> {
                    user.password = BcryptUtil.bcryptHash(newPassword);
                    userRepository.persist(user);
                    return Response.ok(Map.of("message", "Password reset successful.")).build();
                })
                .orElse(Response.status(404).entity(Map.of("message", "Account not found.")).build());
    }

    @GET
    @Path("/me")
    @Operation(summary = "Current Flutter user")
    public Response me(@HeaderParam("Authorization") String authorization) {
        User user = requireUser(authorization);
        return Response.ok(Map.of("user", flutterUser(user))).build();
    }

    @POST
    @Path("/profile")
    @Transactional
    @Operation(summary = "Update Flutter profile")
    public Response updateProfilePost(@HeaderParam("Authorization") String authorization, Map<String, Object> body) {
        return updateProfile(authorization, body);
    }

    @PUT
    @Path("/profile")
    @Transactional
    @Operation(summary = "Update Flutter profile")
    public Response updateProfilePut(@HeaderParam("Authorization") String authorization, Map<String, Object> body) {
        return updateProfile(authorization, body);
    }

    private Response updateProfile(String authorization, Map<String, Object> body) {
        User user = requireUser(authorization);
        String name = str(body == null ? null : body.get("name"));
        String email = str(body == null ? null : body.get("email")).toLowerCase();
        String profilePic = str(body == null ? null : body.get("profilePic"));
        if (name.isBlank() || email.isBlank()) {
            return Response.status(400).entity(Map.of("message", "Name and email are required")).build();
        }
        user.username = name;
        user.email = email;
        if (!profilePic.isBlank()) {
            user.profilePic = profilePic;
        }
        userRepository.persist(user);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("message", "Profile updated successfully.");
        out.put("user", flutterUser(user));
        return Response.ok(out).build();
    }

    private User requireUser(String authorization) {
        String token = bearer(authorization);
        if (token == null || !jwtUtils.validateJwtToken(token)) {
            throw new WebApplicationException("Unauthorized", 401);
        }
        String login = jwtUtils.getLoginFromJwtToken(token);
        return userRepository.findByUsernameOrEmailOptional(login)
                .orElseThrow(() -> new WebApplicationException("User not found", 404));
    }

    private static Map<String, Object> flutterAuthPayload(User user, String jwt) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("token", jwt);
        out.put("jwt", jwt);
        out.put("user", flutterUser(user));
        return out;
    }

    private static Map<String, Object> flutterUser(User user) {
        Map<String, Object> u = new LinkedHashMap<>();
        if (user == null) return u;
        u.put("id", user.id == null ? null : user.id.intValue());
        u.put("name", user.username);
        u.put("email", user.email);
        u.put("role", user.role == null ? "STAFF" : user.role.toUpperCase());
        u.put("profilePic", user.profilePic);
        return u;
    }

    private static String bearer(String authorization) {
        if (authorization == null) return null;
        String v = authorization.trim();
        if (v.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return v.substring(7).trim();
        }
        return v.isEmpty() ? null : v;
    }

    private static String str(Object o) {
        return o == null ? "" : o.toString().trim();
    }
}
