package com.example.airqualitychecker.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.airqualitychecker.api.RetrofitClient
import com.example.airqualitychecker.api.AirQualityResponse
import com.example.airqualitychecker.db.AppDatabase
import com.example.airqualitychecker.db.AirQualityRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.android.gms.location.LocationServices
import android.location.Location
import kotlinx.coroutines.tasks.await
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

class AirQualityWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return@withContext Result.failure()
                }

                val locationTask = fusedLocationClient.lastLocation
                val location = locationTask.await()

                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    val apiKey = "9804bd8efb358403eda969128c5c1626"

                    val response = RetrofitClient.api.getAirQuality(lat, lon, apiKey).execute()

                    if (response.isSuccessful) {
                        val data = response.body()
                        val aqi = data?.list?.get(0)?.main?.aqi ?: 0
                        val pm25 = data?.list?.get(0)?.components?.pm2_5 ?: 0f
                        val pm10 = data?.list?.get(0)?.components?.pm10 ?: 0f

                        val db = AppDatabase.getDatabase(context)
                        val record = AirQualityRecord(
                            timestamp = System.currentTimeMillis(),
                            latitude = lat,
                            longitude = lon,
                            aqi = aqi,
                            pm25 = pm25,
                            pm10 = pm10
                        )
                        db.airQualityDao().insert(record)

                        if (aqi > 100) {
                            NotificationHelper.sendHighAqiNotification(context, aqi)
                        }

                        Result.success()
                    } else {
                        Result.retry()
                    }
                } else {
                    Result.retry() // Location was null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Result.retry()
            }

             catch (e: Exception) {
                e.printStackTrace()
                Result.retry()
            }
        }
    }
}