package com.example.techfix_mobiel_app.activities.member4.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "inventory_items")
public class InventoryEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String itemName;
    public String category; // "Tools", "Equipment", "Consumables"
    public int branchId;    // 1 = Colombo, 2 = Galle
    public int quantity;
    public double value;
    public String status;   // "Good", "Fair", "Needs Attention"

    public InventoryEntity() {
    }

    public InventoryEntity(String itemName, String category, int branchId, int quantity, double value, String status) {
        this.itemName = itemName;
        this.category = category;
        this.branchId = branchId;
        this.quantity = quantity;
        this.value = value;
        this.status = status;
    }
}