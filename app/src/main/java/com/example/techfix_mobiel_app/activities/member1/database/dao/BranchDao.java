package com.example.techfix_mobiel_app.activities.member1.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.techfix_mobiel_app.activities.member1.database.entities.BranchEntity;

import java.util.List;

@Dao
public interface BranchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBranch(BranchEntity branch);

    @Query("SELECT * FROM branches")
    List<BranchEntity> getAllBranches();

    @Query("SELECT * FROM branches WHERE id = :branchId LIMIT 1")
    BranchEntity getBranchById(int branchId);
}