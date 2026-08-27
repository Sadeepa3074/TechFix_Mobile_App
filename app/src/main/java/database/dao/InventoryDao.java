package com.example.techfix_mobiel_app.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

import com.example.techfix_mobiel_app.database.entities.SparePartEntity;

@Dao
public interface InventoryDao {
    @Insert
    void insertPart(SparePartEntity part);

    @Update
    void updatePart(SparePartEntity part);

    @Delete
    void deletePart(SparePartEntity part);

    @Query("SELECT * FROM spare_parts WHERE branchName = :branch")
    List<SparePartEntity> getPartsByBranch(String branch);

    @Query("SELECT * FROM spare_parts")
    List<SparePartEntity> getAllParts();
}