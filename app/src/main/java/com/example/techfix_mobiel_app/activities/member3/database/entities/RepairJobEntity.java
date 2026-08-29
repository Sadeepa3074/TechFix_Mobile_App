package com.example.techfix_mobiel_app.activities.member3.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "repair_jobs")
public class RepairJobEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String deviceName;
    public String status;
    public String photoPath;

    public RepairJobEntity(String deviceName, String status, String photoPath) {
        this.deviceName = deviceName;
        this.status = status;
        this.photoPath = photoPath;
    }
}