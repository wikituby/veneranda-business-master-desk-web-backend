package org.example.user.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.user.domains.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.security.password.Password;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.interfaces.BCryptPassword;
import org.wildfly.security.password.util.ModularCrypt;
import org.example.statics.RoleEnums;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);

    public User login(String usernameOrEmail, String password) {
        return find("username = ?1 OR email = ?1", usernameOrEmail)
                .firstResultOptional()
                .filter(user -> verifyPassword(password, user.getPassword()))
                .orElse(null);
    }

    public Boolean verifyPassword(String plainTextPwd, String encryptedPwd) {
        try {
            Password rawPassword = ModularCrypt.decode(encryptedPwd);
            PasswordFactory factory = PasswordFactory.getInstance(BCryptPassword.ALGORITHM_BCRYPT);
            BCryptPassword restored = (BCryptPassword) factory.translate(rawPassword);
            return factory.verify(restored, plainTextPwd.toCharArray());

        } catch (InvalidKeySpecException | NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("Invalid key: {}", e.getMessage());
            return false;
        }
    }

    public Optional<User> findByEmailOptional(String email) {
        return find("email", email).singleResultOptional();
    }

    public Optional<User> findByUsernameOptional(String username) {
        return find("username", username).singleResultOptional();
    }

    public Boolean usernameExists(String username) {
        return find("username", username).count() > 0;
    }

    public User getUserByEmail(String email) {
        return find("email", email).firstResult();
    }

    public List<User> getAllAdmins() {
        return list("role", RoleEnums.admin.label);
    }

    public List<User> getAllCustomers() {
        return list("role", RoleEnums.customer.label);
    }

    public List<User> getAllAgents() {
        return list("role", RoleEnums.agent.label);
    }
}






