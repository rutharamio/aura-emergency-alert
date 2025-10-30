package com.example.aura.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reports")
public class ReportEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public double lat;
    public double lon;
    public String comment;     // texto del usuario
    public int severity;       // 1=bajo, 2=medio, 3=alto
    public long createdAt;     // System.currentTimeMillis()

    public ReportEntity(double lat, double lon, String comment, int severity, long createdAt) {
        this.lat = lat;
        this.lon = lon;
        this.comment = comment;
        this.severity = severity;
        this.createdAt = createdAt;
    }
}

