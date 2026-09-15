package com.example.techfix_mobiel_app.activities.member4.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.techfix_mobiel_app.activities.member4.database.entities.InventoryEntity;

import java.util.List;

@Dao
public interface InventoryDao {

    @Query("SELECT * FROM inventory_items")
    List<InventoryEntity> getAllInventory();

    @Query("SELECT * FROM inventory_items WHERE branchId = :branchId")
    List<InventoryEntity> getInventoryByBranch(int branchId);

    @Insert
    void insertInventory(InventoryEntity item);

    @Update
    void update(InventoryEntity item);

    @Delete
    void deleteInventory(InventoryEntity item);
}