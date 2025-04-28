package com.example.airqualitychecker.api

data class AirQualityResponse(
    val list: List<AQItem>
)

data class AQItem(
    val main: AQMain,
    val components: AQComponents
)

data class AQMain(
    val aqi: Int
)

data class AQComponents(
    val pm2_5: Float,
    val pm10: Float,
    val co: Float,
    val no2: Float
)