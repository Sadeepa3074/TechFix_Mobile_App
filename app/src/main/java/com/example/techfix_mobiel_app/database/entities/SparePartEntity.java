package com.example.techfix_mobiel_app.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "spare_parts")
public class SparePartEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String partName;
    public String branchName; // "Colombo" or "Galle"
    public int stockQuantity;
    public double unitPrice;

    public SparePartEntity(String partName, String branchName, int stockQuantity, double unitPrice) {
        this.partName = partName;
        this.branchName = branchName;
        this.stockQuantity = stockQuantity;
        this.unitPrice = unitPrice;
    }
}