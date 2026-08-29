package com.example.techfix_mobiel_app.activities.member3.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.techfix_mobiel_app.activities.member3.database.entities.RepairJobEntity;

import java.util.List;

@Dao
public interface RepairJobDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertJob(RepairJobEntity job);

    @Update
    void updateJob(RepairJobEntity job);

    @Delete
    void deleteJob(RepairJobEntity job);

    @Query("SELECT * FROM repair_jobs")
    List<RepairJobEntity> getAllJobs();
}