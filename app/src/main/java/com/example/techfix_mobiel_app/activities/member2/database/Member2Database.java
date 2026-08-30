package com.example.techfix_mobiel_app.activities.member2.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.techfix_mobiel_app.activities.member2.database.dao.QuotationDao;
import com.example.techfix_mobiel_app.activities.member2.database.entities.QuotationEntity;

@Database(entities = {QuotationEntity.class}, version = 1, exportSchema = false)
public abstract class Member2Database extends RoomDatabase {

    private static volatile Member2Database INSTANCE;
    public abstract QuotationDao quotationDao();

    public static Member2Database getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (Member2Database.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            Member2Database.class,
                            "techfix_member2_db"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}