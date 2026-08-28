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
    public void initUser(@Observes StartupEvent ev){

        boolean adminExists = userRepository.usernameExists("vmd") ||
                             userRepository.findByEmailOptional("md@vmd.com").isPresent();

        if (!adminExists){
            User adminUser = new User();
            adminUser.username = "vmd";
            adminUser.profilePic = "";
            adminUser.role = "md";
            adminUser.email = "md@vmd.com";
            adminUser.password = BcryptUtil.bcryptHash("password@vmd.com");
            adminUser.persist();
        }

        seedAdjustmentTypesIfMissing();
        seedDemoActivationTokenIfMissing();
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
