package com.example.airqualitychecker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.airqualitychecker.databinding.ActivityMapBinding
import com.example.airqualitychecker.db.AppDatabase
import com.example.airqualitychecker.db.AirQualityRecord
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
       /** mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))*/

        val db = AppDatabase.getDatabase(this)
        val dao = db.airQualityDao()

        CoroutineScope(Dispatchers.Main).launch {
            val records = dao.getAll()

            for (record in records) {
                val position = LatLng(record.latitude, record.longitude)

                // Choose marker color based on AQI
                val markerColor = when {
                    record.aqi <= 50 -> BitmapDescriptorFactory.HUE_GREEN
                    record.aqi <= 100 -> BitmapDescriptorFactory.HUE_ORANGE
                    else -> BitmapDescriptorFactory.HUE_RED
                }

                val markerOptions = MarkerOptions()
                    .position(position)
                    .title("AQI: ${record.aqi}")
                    .snippet("PM2.5: ${record.pm25}, PM10: ${record.pm10}")
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor))

                googleMap.addMarker(markerOptions)
            }

            if (records.isNotEmpty()) {
                // Center map on the latest recorded location
                val latest = records.last()
                val latestPosition = LatLng(latest.latitude, latest.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latestPosition, 10f))
            }
        }
    }
}