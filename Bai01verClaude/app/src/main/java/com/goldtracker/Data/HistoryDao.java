package com.goldtracker.Data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface HistoryDao {

    @Insert
    void insert(History history);

    @Query("SELECT * FROM history ORDER BY time DESC")
    List<History> getAll();
}