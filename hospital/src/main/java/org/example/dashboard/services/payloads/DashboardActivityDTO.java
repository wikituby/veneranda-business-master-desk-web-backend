package org.example.dashboard.services.payloads;

public class DashboardActivityDTO {
    public String type;
    public String icon;
    public String title;
    public String description;
    public String time;
    public String color;

    public DashboardActivityDTO() {
    }

    public DashboardActivityDTO(String type, String icon, String title, String description, String time, String color) {
        this.type = type;
        this.icon = icon;
        this.title = title;
        this.description = description;
        this.time = time;
        this.color = color;
    }
}
