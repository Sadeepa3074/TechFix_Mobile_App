package com.example.techfix_mobiel_app.activities.member4.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.techfix_mobiel_app.activities.member4.database.dao.InventoryDao;
import com.example.techfix_mobiel_app.activities.member4.database.entities.SparePartEntity;

@Database(entities = {SparePartEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract InventoryDao inventoryDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "techfix_database"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}