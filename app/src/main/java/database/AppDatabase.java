package com.example.techfix_mobiel_app.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.techfix_mobiel_app.database.dao.InventoryDao;
import com.example.techfix_mobiel_app.database.entities.SparePartEntity;

@Database(entities = {SparePartEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract InventoryDao inventoryDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "techfix_db")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}