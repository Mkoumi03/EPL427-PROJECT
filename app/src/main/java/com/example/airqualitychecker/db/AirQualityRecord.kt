package com.example.airqualitychecker.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AirQualityRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val aqi: Int,
    val pm25: Float,
    val pm10: Float
)