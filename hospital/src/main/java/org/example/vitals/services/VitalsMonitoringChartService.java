package org.example.vitals.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.visit.domains.PatientVisit;
import org.example.user.domains.User;
import org.example.vitals.domains.VitalsMonitoringChart;
import org.example.vitals.domains.repositories.VitalsMonitoringChartRepository;
import org.example.vitals.services.payloads.requests.VitalsMonitoringChartRequest;
import org.example.vitals.services.payloads.responses.VitalsMonitoringChartDTO;

import java.time.LocalDateTime;

@ApplicationScoped
public class VitalsMonitoringChartService {

    @Inject
    VitalsMonitoringChartRepository vitalsMonitoringChartRepository;

    public static final String NOT_FOUND = "Not found!";

    @Transactional
    public VitalsMonitoringChartDTO newVitalsMonitoringChart (VitalsMonitoringChartRequest request){

        PatientVisit patientVisit = PatientVisit.findById(request.visitId);
        User user = User.findById(request.userId);

        if (patientVisit == null ) {
            throw new IllegalArgumentException("patient visit not found");  // Handle patient not found
        }
        if (user == null ) {
            throw new IllegalArgumentException("user not found");  // Handle patient not found
        }

        VitalsMonitoringChart vitalsMonitoringChart = new VitalsMonitoringChart();
        vitalsMonitoringChart.visit = patientVisit;
        vitalsMonitoringChart.initials = user.username;
        vitalsMonitoringChart.dateTime = LocalDateTime.now();
        vitalsMonitoringChart.temperature = request.temperature;
        vitalsMonitoringChart.bloodPressure = request.bloodPressure;
        vitalsMonitoringChart.respiratoryRate = request.respiratoryRate;
        vitalsMonitoringChart.map = request.map;
        vitalsMonitoringChart.spO2 = request.spO2;
        vitalsMonitoringChart.pulseRate = request.pulseRate;


        vitalsMonitoringChartRepository.persist(vitalsMonitoringChart);

        return new VitalsMonitoringChartDTO(vitalsMonitoringChart);
    }
}






