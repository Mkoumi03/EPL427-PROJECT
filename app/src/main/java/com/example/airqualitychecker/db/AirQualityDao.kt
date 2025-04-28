package com.example.airqualitychecker.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AirQualityDao {
    @Insert
    suspend fun insert(record: AirQualityRecord)

    @Query("SELECT * FROM AirQualityRecord ORDER BY timestamp DESC")
    suspend fun getAll(): List<AirQualityRecord>
}