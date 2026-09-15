package com.example.techfix_mobiel_app.activities.member4.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.techfix_mobiel_app.activities.member4.database.entities.SparePartEntity;

import java.util.List;

@Dao
public interface SparePartDao {
    @Query("SELECT * FROM spare_parts ORDER BY id DESC")
    List<SparePartEntity> getAllSpareParts();

    @Query("SELECT * FROM spare_parts WHERE id = :id")
    SparePartEntity getSparePartById(int id);

    @Insert
    void insertSparePart(SparePartEntity sparePart);

    @Update
    void updateSparePart(SparePartEntity sparePart);

    @Delete
    void deleteSparePart(SparePartEntity sparePart);
}