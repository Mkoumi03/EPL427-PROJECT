package com.example.airqualitychecker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.airqualitychecker.db.AppDatabase
import com.example.airqualitychecker.db.AirQualityRecord
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class HistoryActivity : AppCompatActivity() {
    private lateinit var lineChart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        lineChart = findViewById(R.id.lineChart)

        loadData()
    }

    private fun loadData() {
        val db = AppDatabase.getDatabase(this)
        val dao = db.airQualityDao()

        CoroutineScope(Dispatchers.Main).launch {
            val records = dao.getAll()
            displayChart(records)
        }
    }

    private fun displayChart(records: List<AirQualityRecord>) {
        val entries = ArrayList<Entry>()

        // X = time index, Y = AQI value
        for ((index, record) in records.withIndex()) {
            entries.add(Entry(index.toFloat(), record.aqi.toFloat()))
        }

        val dataSet = LineDataSet(entries, "AQI Over Time")
        dataSet.color = android.graphics.Color.BLUE
        dataSet.valueTextColor = android.graphics.Color.BLACK
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.axisRight.isEnabled = false
        lineChart.description.text = "AQI Readings History"
        lineChart.invalidate() // refresh
    }
}