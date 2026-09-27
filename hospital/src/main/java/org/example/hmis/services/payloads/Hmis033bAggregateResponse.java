package org.example.hmis.services.payloads;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Hmis033bAggregateResponse {

    public Period period = new Period();
    public FacilityHeader facility = new FacilityHeader();
    public OpdSummary opd = new OpdSummary();
    public ReferralSummary referrals = new ReferralSummary();
    public List<DiseaseLine> diseases = new ArrayList<>();
    public MalariaSummary malaria = new MalariaSummary();
    public List<TracerStockLine> tracerStock = new ArrayList<>();
    public List<String> notes = new ArrayList<>();

    public static class Period {
        public LocalDate from;
        public LocalDate to;
        public Integer weekNumber;
        public LocalDate reportDate;
    }

    public static class FacilityHeader {
        public String name;
        public String code;
        public String level;
        public String district;
        public String hsd;
        public String subCounty;
        public String parish;
        public String address;
    }

    public static class OpdSummary {
        public int newAttendance;
        public int reattendance;
        public int totalAttendance;
    }

    public static class ReferralSummary {
        public int referralsOut;
        public int referralsIn;
    }

    public static class DiseaseLine {
        public String code;
        public String label;
        public int casesThisWeek;
        public int deathsThisWeek;
        public int testedCases;
        public int positiveCases;
    }

    public static class MalariaSummary {
        public int suspectedFever;
        public int testedRdt;
        public int rdtPositive;
        public int rdtPositiveTreated;
        public int testedMicroscopy;
        public int microscopyPositive;
        public int microscopyPositiveTreated;
        public int notTestedTreated;
    }

    public static class TracerStockLine {
        public String tracerCode;
        public String tracerName;
        public Long stockItemId;
        public Long shopItemId;
        public String stockItemName;
        public String shopItemName;
        public double balance;
        public int dispensedInPeriod;
    }
}