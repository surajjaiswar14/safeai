package com.example.safejourneyai.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class WeatherInfo(
    val temperature: Double,
    val condition: String,
    val windSpeed: Double,
    val weatherCode: Int,
    val locationName: String = ""
) {
    fun formattedTemp(): String = "${temperature.toInt()}°C"
}

interface WeatherRepository {
    suspend fun getWeatherForLocation(lat: Double, lng: Double, locationName: String = ""): Result<WeatherInfo>
    suspend fun getWeatherForCity(cityName: String): Result<WeatherInfo>
}

class WeatherRepositoryImpl : WeatherRepository {

    override suspend fun getWeatherForLocation(lat: Double, lng: Double, locationName: String): Result<WeatherInfo> = withContext(Dispatchers.IO) {
        try {
            val urlString = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lng&current_weather=true"
            val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
            }

            if (connection.responseCode == 200) {
                val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(jsonString)
                val current = root.getJSONObject("current_weather")

                val temp = current.getDouble("temperature")
                val wind = current.getDouble("windspeed")
                val code = current.getInt("weathercode")
                val conditionStr = parseWeatherCode(code)

                Result.success(WeatherInfo(temp, conditionStr, wind, code, locationName))
            } else {
                Result.failure(Exception("Weather API returned code ${connection.responseCode}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getWeatherForCity(cityName: String): Result<WeatherInfo> = withContext(Dispatchers.IO) {
        val coordinates = getCoordinatesForCity(cityName)
        if (coordinates != null) {
            getWeatherForLocation(coordinates.first, coordinates.second, cityName)
        } else {
            Result.failure(Exception("Coordinates not found for city $cityName"))
        }
    }

    private fun getCoordinatesForCity(city: String): Pair<Double, Double>? {
        return when (city.lowercase().trim()) {
            "mumbai" -> Pair(19.0760, 72.8777)
            "delhi", "new delhi" -> Pair(28.6139, 77.2090)
            "jaipur" -> Pair(26.9124, 75.7873)
            "goa" -> Pair(15.2993, 74.1240)
            "manali" -> Pair(32.2432, 77.1892)
            "bengaluru", "bangalore" -> Pair(12.9716, 77.5946)
            "kolkata" -> Pair(22.5726, 88.3639)
            "chennai" -> Pair(13.0827, 80.2707)
            "hyderabad" -> Pair(17.3850, 78.4867)
            "pune" -> Pair(18.5204, 73.8567)
            "udaipur" -> Pair(24.5854, 73.7125)
            "kerala", "kochi" -> Pair(9.9312, 76.2673)
            "shimla" -> Pair(31.1048, 77.1734)
            "varanasi" -> Pair(25.3176, 82.9739)
            "agra" -> Pair(27.1767, 78.0081)
            else -> Pair(19.0760, 72.8777)
        }
    }

    private fun parseWeatherCode(code: Int): String {
        return when (code) {
            0 -> "Clear Sky"
            1, 2, 3 -> "Partly Cloudy"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Drizzle"
            61, 63, 65 -> "Rainy"
            71, 73, 75 -> "Snowy"
            80, 81, 82 -> "Rain Showers"
            95, 96, 99 -> "Thunderstorm"
            else -> "Clear"
        }
    }
}
