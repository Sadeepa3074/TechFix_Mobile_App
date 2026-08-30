package com.example.techfix_mobiel_app.activities.member2.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.techfix_mobiel_app.activities.member2.database.entities.QuotationEntity;

import java.util.List;

@Dao
public interface QuotationDao {

    @Insert
    void insertQuotation(QuotationEntity quotation);

    @Query("SELECT * FROM quotations")
    List<QuotationEntity> getAllQuotations();
}