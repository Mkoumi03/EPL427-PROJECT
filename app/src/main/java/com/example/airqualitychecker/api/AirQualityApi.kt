package com.example.airqualitychecker.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface AirQualityApi {
    @GET("data/2.5/air_pollution")
    fun getAirQuality(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String
    ): Call<AirQualityResponse>

}