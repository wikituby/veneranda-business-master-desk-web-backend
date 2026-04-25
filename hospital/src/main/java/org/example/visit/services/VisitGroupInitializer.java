package org.example.visit.services;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class VisitGroupInitializer {

    private static final Logger LOG = Logger.getLogger(VisitGroupInitializer.class);

    @Inject
    PatientVisitService patientVisitService;

    void onStart(@Observes StartupEvent ev) {
        LOG.info("Application started successfully. Visit group updates are disabled at startup for performance reasons.");

        // Uncomment the below line if you want to run in background (optional)

        new Thread(() -> {
            try {
                //patientVisitService.updateAllVisitGroupsAndFinancialsFromPatients();
                //patientVisitService.fixProcedureRequestedNames();
                LOG.info("Visit groups and procedure names updated successfully (background run).");
            } catch (Exception e) {
                LOG.error("Error during background initialization", e);
            }
        }).start();

    }
}






