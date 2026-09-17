package com.example.techfix_mobiel_app.activities.member2.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "appointments")
public class AppointmentEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int customerId;
    public int serviceId;
    public int assignedBranchId;
    public int assignedTechId;

    public String deviceName;
    public String issueType;
    public String description; // Captures customer's custom description notes
    public String branchName;  // Captures GPS auto-assigned branch ("Colombo" or "Galle")
    public String status;
    public String paymentStatus;
    public String requestDate;

    public double price;
    public String customerName;
    public String completionPhotoPath;

    public AppointmentEntity(int customerId, int serviceId, int assignedBranchId, int assignedTechId,
                             String deviceName, String issueType, String description, String branchName,
                             String status, String paymentStatus, String requestDate, double price, String customerName) {
        this.customerId = customerId;
        this.serviceId = serviceId;
        this.assignedBranchId = assignedBranchId;
        this.assignedTechId = assignedTechId;
        this.deviceName = deviceName;
        this.issueType = issueType;
        this.description = description;
        this.branchName = branchName;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.requestDate = requestDate;
        this.price = price;
        this.customerName = customerName;
    }
}