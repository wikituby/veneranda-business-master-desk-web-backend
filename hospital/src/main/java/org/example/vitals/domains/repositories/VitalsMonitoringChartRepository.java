package org.example.vitals.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.vitals.domains.VitalsMonitoringChart;

@ApplicationScoped
public class VitalsMonitoringChartRepository implements PanacheRepository<VitalsMonitoringChart> {
}






