package org.example.treatment.treatmentChart.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.treatment.treatmentChart.domains.TreatmentChart;
import org.example.treatment.treatmentChart.domains.repositories.TreatmentChartRepository;
import org.example.treatment.treatmentChart.services.payloads.requests.TreatmentChartRequest;
import org.example.treatment.treatmentChart.services.payloads.responses.TreatmentChartDTO;
import org.example.treatment.domains.TreatmentRequested;
import org.example.treatment.domains.repositories.TreatmentRequestedRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class TreatmentChartService {

    @Inject
    TreatmentChartRepository treatmentChartRepository;

    @Inject
    TreatmentRequestedRepository treatmentRequestedRepository;

    @Transactional
    public Response createTreatmentChart(Long treatmentRequestedId, TreatmentChartRequest request) {
        TreatmentRequested treatmentRequested = treatmentRequestedRepository.findById(treatmentRequestedId);
        if (treatmentRequested == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Treatment request not found for ID: " + treatmentRequestedId))
                    .build();
        }

        LocalDate dateGiven = request.dateGiven != null ? request.dateGiven : LocalDate.now();
        LocalTime timeGiven = request.timeGiven != null ? request.timeGiven : LocalTime.now();

        TreatmentChart chart = new TreatmentChart();
        chart.treatmentRequested = treatmentRequested;
        chart.dateGiven = dateGiven;
        chart.timeGiven = timeGiven;
        chart.givenBy = request.givenBy;
        chart.route = request.route != null ? request.route : treatmentRequested.route;
        chart.dosageValue = request.dosageValue != null ? request.dosageValue : treatmentRequested.amountPerFrequencyValue;
        chart.dosageUnit = request.dosageUnit != null ? request.dosageUnit : treatmentRequested.amountPerFrequencyUnit;
        chart.frequencyValue = request.frequencyValue != null ? request.frequencyValue : treatmentRequested.frequencyValue;
        chart.frequencyUnit = request.frequencyUnit != null ? request.frequencyUnit : treatmentRequested.frequencyUnit;
        chart.timeBetweenGivenToNextDosage = request.timeBetweenGivenToNextDosage;
        chart.dateForNextDosage = request.dateForNextDosage;
        chart.timeForNextDosage = request.timeForNextDosage;
        chart.overallTotalDosages = getTotalDosages(treatmentRequested.totalUnits);
        chart.totalDosagesGiven = request.totalDosagesGiven != null ? request.totalDosagesGiven : 0;
        chart.unitsUsed = request.unitsUsed != null ? request.unitsUsed : 0;
        chart.totalDosagesRemaining = calculateRemaining(chart.overallTotalDosages, chart.totalDosagesGiven);
        chart.status = request.status != null ? request.status : "Pending";

        treatmentChartRepository.persist(chart);

        return Response.status(Response.Status.CREATED)
                .entity(new ResponseMessage("Treatment chart created successfully", new TreatmentChartDTO(chart)))
                .build();
    }

    @Transactional
    public Response updateTreatmentChart(Long id, TreatmentChartRequest request) {
        TreatmentChart chart = treatmentChartRepository.findById(id);
        if (chart == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Treatment chart not found for ID: " + id))
                    .build();
        }

        chart.dateGiven = request.dateGiven != null ? request.dateGiven : LocalDate.now();
        chart.timeGiven = request.timeGiven != null ? request.timeGiven : LocalTime.now();
        chart.givenBy = request.givenBy != null ? request.givenBy : chart.givenBy;
        chart.route = request.route != null ? request.route : chart.route;
        chart.dosageValue = request.dosageValue != null ? request.dosageValue : chart.dosageValue;
        chart.dosageUnit = request.dosageUnit != null ? request.dosageUnit : chart.dosageUnit;
        chart.frequencyValue = request.frequencyValue != null ? request.frequencyValue : chart.frequencyValue;
        chart.frequencyUnit = request.frequencyUnit != null ? request.frequencyUnit : chart.frequencyUnit;
        chart.timeBetweenGivenToNextDosage = request.timeBetweenGivenToNextDosage != null ? request.timeBetweenGivenToNextDosage : chart.timeBetweenGivenToNextDosage;
        chart.dateForNextDosage = request.dateForNextDosage != null ? request.dateForNextDosage : chart.dateForNextDosage;
        chart.timeForNextDosage = request.timeForNextDosage != null ? request.timeForNextDosage : chart.timeForNextDosage;
        chart.status = request.status != null ? request.status : chart.status;

        if (request.totalDosagesGiven != null) {
            chart.totalDosagesGiven = request.totalDosagesGiven;
        }
        if (request.unitsUsed != null) {
            chart.unitsUsed = request.unitsUsed;
        }
        chart.totalDosagesRemaining = calculateRemaining(chart.overallTotalDosages, chart.totalDosagesGiven);

        treatmentChartRepository.persist(chart);

        return Response.ok(new ResponseMessage("Treatment chart updated successfully", new TreatmentChartDTO(chart))).build();
    }

    @Transactional
    public Response getTreatmentChartByRequestId(Long treatmentRequestedId) {
        TreatmentChart chart = TreatmentChart.find("treatmentRequested.id", treatmentRequestedId).firstResult();
        if (chart == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Treatment chart not found for treatment request ID: " + treatmentRequestedId))
                    .build();
        }
        return Response.ok(new ResponseMessage("treatment chart fetched successfully", new TreatmentChartDTO(chart))).build();
    }

    public Response getTreatmentChartsByVisitId(Long visitId) {
        List<TreatmentChart> charts = TreatmentChart.find("treatmentRequested.visit.id", visitId).list();
        List<TreatmentChartDTO> dtos = charts.stream().map(TreatmentChartDTO::new).collect(Collectors.toList());
        return Response.ok(new ResponseMessage("Treatment charts fetched successfully", dtos)).build();
    }

    @Transactional
    public Response deleteTreatmentChart(Long id) {
        TreatmentChart chart = treatmentChartRepository.findById(id);
        if (chart == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Treatment chart not found for ID: " + id))
                    .build();
        }
        treatmentChartRepository.delete(chart);
        return Response.ok(new ResponseMessage("Treatment chart deleted successfully", null)).build();
    }

    private Integer getTotalDosages(BigDecimal totalUnits) {
        if (totalUnits == null) {
            return 0;
        }
        return totalUnits.setScale(0, RoundingMode.CEILING).intValue();
    }

    private Integer calculateRemaining(Integer overallTotalDosages, Integer totalDosagesGiven) {
        int total = overallTotalDosages != null ? overallTotalDosages : 0;
        int given = totalDosagesGiven != null ? totalDosagesGiven : 0;
        return Math.max(total - given, 0);
    }
}


