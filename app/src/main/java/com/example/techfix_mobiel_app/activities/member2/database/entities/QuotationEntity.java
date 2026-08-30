package com.example.techfix_mobiel_app.activities.member2.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "quotations")
public class QuotationEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String itemCategory;
    private String itemDescription;
    private int quantity;
    private String status; // e.g., "Pending", "Approved", "Rejected"
    private double estimatedPrice;

    public QuotationEntity(String itemCategory, String itemDescription, int quantity, String status, double estimatedPrice) {
        this.itemCategory = itemCategory;
        this.itemDescription = itemDescription;
        this.quantity = quantity;
        this.status = status;
        this.estimatedPrice = estimatedPrice;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getItemCategory() { return itemCategory; }
    public String getItemDescription() { return itemDescription; }
    public int getQuantity() { return quantity; }
    public String getStatus() { return status; }
    public double getEstimatedPrice() { return estimatedPrice; }
}