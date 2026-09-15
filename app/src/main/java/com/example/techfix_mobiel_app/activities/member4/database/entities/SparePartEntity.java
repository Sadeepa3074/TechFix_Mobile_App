package com.example.techfix_mobiel_app.activities.member4.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "spare_parts")
public class SparePartEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String partName;
    public String category; // e.g. Phones, Laptops
    public int quantity;
    public double price;
}