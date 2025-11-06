package com.example.aura;

public class ReportData {
    public double lat;
    public double lon;
    public String comment;
    public int severity;
    public String user;

    // 🔹 Constructor vacío requerido por Firebase
    public ReportData() {}

    public ReportData(double lat, double lon, String comment, int severity, String user) {
        this.lat = lat;
        this.lon = lon;
        this.comment = comment;
        this.severity = severity;
        this.user = user;
    }
}
