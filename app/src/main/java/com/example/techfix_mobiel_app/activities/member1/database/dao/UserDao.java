package com.example.techfix_mobiel_app.activities.member1.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.techfix_mobiel_app.activities.member1.database.entities.UserEntity;

@Dao
public interface UserDao {

    @Insert
    void registerUser(UserEntity user);

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    UserEntity loginUser(String email, String password);
}