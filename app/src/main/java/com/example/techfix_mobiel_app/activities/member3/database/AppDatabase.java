package com.example.techfix_mobiel_app.activities.member3.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.techfix_mobiel_app.activities.member3.database.dao.RepairJobDao;
import com.example.techfix_mobiel_app.activities.member3.database.entities.RepairJobEntity;

@Database(entities = {RepairJobEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract RepairJobDao repairJobDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "techfix_repair_db"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}