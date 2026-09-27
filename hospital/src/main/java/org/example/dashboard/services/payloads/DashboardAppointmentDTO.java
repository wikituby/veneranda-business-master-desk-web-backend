package org.example.dashboard.services.payloads;

public class DashboardAppointmentDTO {
    public String patientName;
    public String service;
    public String time;
    public String status;
    public String priority;

    public DashboardAppointmentDTO() {
    }

    public DashboardAppointmentDTO(String patientName, String service, String time, String status, String priority) {
        this.patientName = patientName;
        this.service = service;
        this.time = time;
        this.status = status;
        this.priority = priority;
    }
}
