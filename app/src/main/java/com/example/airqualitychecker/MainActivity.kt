package com.example.airqualitychecker

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.example.airqualitychecker.api.AirQualityApi
import com.example.airqualitychecker.api.AirQualityResponse
import com.example.airqualitychecker.api.RetrofitClient
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.airqualitychecker.db.AppDatabase
import com.example.airqualitychecker.db.AirQualityRecord
import com.example.airqualitychecker.db.AirQualityDao
import android.content.Intent
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import android.util.Log
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import com.example.airqualitychecker.worker.AirQualityWorker
import android.os.Build

class MainActivity : AppCompatActivity() {
    private lateinit var tvLocation: TextView
    private lateinit var tvAqi: TextView
    private lateinit var btnFetch: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvLocation = findViewById(R.id.tv_location)
        tvAqi = findViewById(R.id.tv_aqi)
        btnFetch = findViewById(R.id.btn_fetch)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        btnFetch.setOnClickListener {
            getLocation()
        }

        val btnHistory = findViewById<Button>(R.id.btn_history)
        btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        val btnMap = findViewById<Button>(R.id.btn_map)

        btnMap.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }

        val workRequest = PeriodicWorkRequestBuilder<AirQualityWorker>(
            30, TimeUnit.MINUTES //
        ).build()

        WorkManager.getInstance(this).enqueue(workRequest)
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->

                    val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
                        priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
                        interval = 0
                        fastestInterval = 0
                        numUpdates = 1
                    }

                    val locationCallback = object : com.google.android.gms.location.LocationCallback() {
                        override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                            val freshLocation = locationResult.lastLocation
                            if (freshLocation != null) {
                                val lat = freshLocation.latitude
                                val lon = freshLocation.longitude
                                tvLocation.text = "Lat: $lat, Lon: $lon"

                                val apiKey = "9804bd8efb358403eda969128c5c1626"
                                RetrofitClient.api.getAirQuality(lat, lon, apiKey)
                                    .enqueue(object : retrofit2.Callback<AirQualityResponse> {
                                        override fun onResponse(call: retrofit2.Call<AirQualityResponse>, response: retrofit2.Response<AirQualityResponse>) {
                                            if (response.isSuccessful) {
                                                val data = response.body()
                                                val aqi = data?.list?.get(0)?.main?.aqi
                                                val pm25 = data?.list?.get(0)?.components?.pm2_5
                                                val pm10 = data?.list?.get(0)?.components?.pm10

                                                tvAqi.text = "Air Quality Index: $aqi\nPM2.5: $pm25\nPM10: $pm10"
                                                val db = AppDatabase.getDatabase(this@MainActivity)
                                                val dao = db.airQualityDao()

                                                val record = AirQualityRecord(
                                                    timestamp = System.currentTimeMillis(),
                                                    latitude = lat,
                                                    longitude = lon,
                                                    aqi = aqi ?: 0,
                                                    pm25 = pm25 ?: 0f,
                                                    pm10 = pm10 ?: 0f
                                                )

                                                lifecycleScope.launch {
                                                    dao.insert(record)
                                                }

                                                val writeApiKey = "4WN3Q848LXOE6B0V"
                                                val baseUrl = "https://api.thingspeak.com/update"

                                                val url = "$baseUrl?api_key=$writeApiKey&field1=${aqi ?: 0}&field2=${pm25 ?: 0}&field3=${pm10 ?: 0}&field4=${lat}&field5=${lon}"

                                                val requestQueue = Volley.newRequestQueue(this@MainActivity)

                                                val stringRequest = StringRequest(Request.Method.GET, url,
                                                    { response ->
                                                        Log.d("ThingSpeak", "Upload success: $response")
                                                    },
                                                    { error ->
                                                        Log.e("ThingSpeak", "Upload failed: ${error.message}")
                                                    }
                                                )

                                                requestQueue.add(stringRequest)
                                            } else {
                                                tvAqi.text = "API error: ${response.code()}"
                                            }
                                        }

                                        override fun onFailure(call: retrofit2.Call<AirQualityResponse>, t: Throwable) {
                                            tvAqi.text = "API failed: ${t.message}"
                                        }
                                    })
                            } else {
                                tvLocation.text = "Could not get location"
                            }

                        }
                    }

                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, android.os.Looper.getMainLooper())
            }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST &&
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            getLocation()
        }
    }


}








