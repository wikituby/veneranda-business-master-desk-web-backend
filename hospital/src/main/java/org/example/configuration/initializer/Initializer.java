package org.example.configuration.initializer;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.inventory.stock.domains.AdjustmentType;
import org.example.inventory.stock.domains.repositories.AdjustmentTypeRepository;
import org.example.subscription.domains.ActivationToken;
import org.example.subscription.domains.repositories.ActivationTokenRepository;
import org.example.user.domains.User;
import org.example.user.domains.repositories.UserRepository;

@ApplicationScoped
public class Initializer {

    @Inject
    UserRepository userRepository;

    @Inject
    AdjustmentTypeRepository adjustmentTypeRepository;

    @Inject
    ActivationTokenRepository activationTokenRepository;

    @Transactional
    public void initUser(@Observes StartupEvent ev) {
        ensureDefaultAdminUser();
        // Only seed catalog defaults on a fresh-ish DB (idempotent helpers below).
        seedAdjustmentTypesIfMissing();
        seedDemoActivationTokenIfMissing();
    }

    /**
     * Ensure the documented local admin account exists.
     * Never overwrite password (or other credentials) on an existing user — that wiped
     * profile password changes whenever Quarkus restarted / live-reloaded.
     */
    private void ensureDefaultAdminUser() {
        final String email = "md@vmd.com";
        final String username = "vmd";
        final String plainPassword = "password@vmd.com";

        User existing = userRepository.findByEmailOptional(email).orElse(null);
        if (existing == null) {
            existing = userRepository.findByUsernameOptional(username).orElse(null);
        }
        if (existing != null) {
            boolean dirty = false;
            if (existing.email == null || existing.email.isBlank()) {
                existing.email = email;
                dirty = true;
            }
            if (existing.username == null || existing.username.isBlank()) {
                existing.username = username;
                dirty = true;
            }
            if (existing.role == null || existing.role.isBlank()) {
                existing.role = "md";
                dirty = true;
            }
            if (existing.status == null || existing.status.isBlank()) {
                existing.status = "active";
                dirty = true;
            }
            if (dirty) {
                userRepository.persist(existing);
            }
            return;
        }

        // Fresh DB: create the default admin once.
        User adminUser = new User();
        adminUser.username = username;
        adminUser.profilePic = "";
        adminUser.role = "md";
        adminUser.status = "active";
        adminUser.email = email;
        adminUser.password = BcryptUtil.bcryptHash(plainPassword);
        adminUser.persist();
    }

    private void seedAdjustmentTypesIfMissing() {
        ensureAdjustmentType("DAMAGED", "Damaged");
        ensureAdjustmentType("EXPIRED", "Expired");
        ensureAdjustmentType("RECOUNT", "Recount correction");
        ensureAdjustmentType("THEFT", "Theft / loss");
        ensureAdjustmentType("OTHER", "Other");
    }

    private void ensureAdjustmentType(String code, String name) {
        if (adjustmentTypeRepository.find("code", code).firstResult() != null) {
            return;
        }
        AdjustmentType t = new AdjustmentType();
        t.code = code;
        t.name = name;
        t.active = Boolean.TRUE;
        adjustmentTypeRepository.persist(t);
    }

    private void seedDemoActivationTokenIfMissing() {
        if (activationTokenRepository.findByToken("KMC-DEMO-2026").isPresent()) {
            return;
        }
        ActivationToken token = new ActivationToken();
        token.token = "KMC-DEMO-2026";
        token.facilityName = "Kavuma Medical Clinic";
        token.facilityAddress = "Kampala, Uganda";
        token.subscribedModuleKeys = "all";
        token.durationMonths = 12;
        token.status = "unused";
        activationTokenRepository.persist(token);
    }
}
