package com.example.techfix_mobiel_app.activities.member1.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.techfix_mobiel_app.activities.member1.database.entities.UserEntity;

import java.util.List;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void registerUser(UserEntity user);

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    UserEntity loginUser(String email, String password);

    @Query("SELECT * FROM users WHERE role = 'Technician'")
    List<UserEntity> getAllTechnicians();

    @Query("SELECT * FROM users WHERE role = 'Technician' AND branchId = :branchId")
    List<UserEntity> getTechniciansByBranch(int branchId);

    @Delete
    void deleteUser(UserEntity user);

    @Query("SELECT Username FROM users WHERE id = :userId")
    String getUserNameById(int userId);

}