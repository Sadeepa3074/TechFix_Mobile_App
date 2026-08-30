package com.example.techfix_mobiel_app.activities.member1.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.techfix_mobiel_app.activities.member1.database.dao.UserDao;
import com.example.techfix_mobiel_app.activities.member1.database.entities.UserEntity;

@Database(entities = {UserEntity.class}, version = 1, exportSchema = false)
public abstract class Member1Database extends RoomDatabase {

    private static volatile Member1Database INSTANCE;
    public abstract UserDao userDao();

    public static Member1Database getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (Member1Database.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            Member1Database.class,
                            "techfix_member1_db"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}