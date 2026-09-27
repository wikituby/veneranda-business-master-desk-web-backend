package org.example.hmis.configuration;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import org.example.consultations.domains.DiagnosisType;
import org.example.hmis.domains.HmisTracerItem;
import org.jboss.logging.Logger;

@ApplicationScoped
public class HmisDiagnosisCatalogInitializer {

    private static final Logger LOG = Logger.getLogger(HmisDiagnosisCatalogInitializer.class);

    @Transactional
    void onStart(@Observes StartupEvent event) {
        seedDiagnosisTypes();
        seedTracerItems();
    }

    private void seedDiagnosisTypes() {
        List<String[]> seeds = List.of(
                new String[] {"Malaria (confirmed)", "MA.", "malaria,plasmodium"},
                new String[] {"Dysentery", "DY.", "dysentery,bloody stool"},
                new String[] {"Severe Acute Respiratory Infection", "SA.", "sari,pneumonia,respiratory"},
                new String[] {"Acute Flaccid Paralysis", "AF.", "afp,flaccid paralysis"},
                new String[] {"Adverse Events Following Immunization", "AE.", "aefi"},
                new String[] {"Animal Bites (suspected rabies)", "AB.", "animal bite,rabies"},
                new String[] {"Bacterial Meningitis", "MG.", "meningitis"},
                new String[] {"Cholera", "CH.", "cholera"},
                new String[] {"Guinea Worm", "GW.", "guinea worm"},
                new String[] {"Measles", "ME.", "measles"},
                new String[] {"Neonatal tetanus", "NT.", "tetanus"},
                new String[] {"Plague", "PL.", "plague"},
                new String[] {"Typhoid Fever", "TF.", "typhoid"},
                new String[] {"Hepatitis B", "HB.", "hepatitis b"},
                new String[] {"Rifampicin resistant TB", "DR.", "tuberculosis,mdr tb"},
                new String[] {"Yellow Fever", "YF.", "yellow fever"},
                new String[] {"Other Viral Hemorrhagic Fevers", "VF.", "ebola,marburg"},
                new String[] {"Covid-19", "CV.", "covid,coronavirus"},
                new String[] {"Leprosy", "LP.", "leprosy"},
                new String[] {"Anthrax", "AX.", "anthrax"});
        int created = 0;
        for (String[] seed : seeds) {
            DiagnosisType existing = DiagnosisType.find("lower(title) = ?1", seed[0].toLowerCase()).firstResult();
            if (existing != null) {
                if (existing.hmisCode == null || existing.hmisCode.isBlank()) {
                    existing.hmisCode = seed[1];
                    existing.matchKeywords = seed[2];
                    existing.persist();
                }
                continue;
            }
            DiagnosisType row = new DiagnosisType();
            row.title = seed[0];
            row.hmisCode = seed[1];
            row.matchKeywords = seed[2];
            row.creationDate = LocalDate.now();
            row.updateDate = LocalDate.now();
            row.persist();
            created++;
        }
        if (created > 0) {
            LOG.infof("HMIS diagnosis catalog: seeded %d types", created);
        }
    }

    private void seedTracerItems() {
        List<String[]> seeds = List.of(
                new String[] {"AL", "Artemether/Lumefantrine (ACT)", "1"},
                new String[] {"RDT", "Malaria RDT kits", "2"},
                new String[] {"QUIN", "Quinine", "3"},
                new String[] {"ORS", "Oral Rehydration Salts", "4"},
                new String[] {"ZINC", "Zinc tablets", "5"});
        int created = 0;
        for (String[] seed : seeds) {
            if (HmisTracerItem.count("hmisTracerCode = ?1", seed[0]) > 0) {
                continue;
            }
            HmisTracerItem row = new HmisTracerItem();
            row.hmisTracerCode = seed[0];
            row.tracerName = seed[1];
            row.sortOrder = Integer.parseInt(seed[2]);
            row.active = true;
            row.persist();
            created++;
        }
        if (created > 0) {
            LOG.infof("HMIS tracer catalog: seeded %d items", created);
        }
    }
}