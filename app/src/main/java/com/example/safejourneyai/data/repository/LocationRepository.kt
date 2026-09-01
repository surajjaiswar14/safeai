package com.example.safejourneyai.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import java.net.URL
import java.net.HttpURLConnection
import org.json.JSONObject
import kotlin.coroutines.resume

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val city: String = "Current Location",
    val state: String = ""
) {
    fun getDisplayName(): String = if (state.isNotBlank()) "$city, $state" else city
}

sealed class LocationState {
    object PermissionDenied : LocationState()
    data class Success(val location: UserLocation) : LocationState()
    data class Error(val message: String) : LocationState()
}

interface LocationRepository {
    fun hasLocationPermission(): Boolean
    suspend fun fetchCurrentLocation(): LocationState
}

class LocationRepositoryImpl(private val context: Context) : LocationRepository {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    override fun hasLocationPermission(): Boolean {
        val finePerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarsePerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return finePerm || coarsePerm
    }

    override suspend fun fetchCurrentLocation(): LocationState = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) {
            return@withContext LocationState.PermissionDenied
        }

        return@withContext suspendCancellableCoroutine<LocationState> { continuation ->
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val userLoc = reverseGeocode(location.latitude, location.longitude)
                        if (continuation.isActive) continuation.resume(LocationState.Success(userLoc))
                    } else {
                        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                            .setMaxUpdates(1)
                            .build()
                        val singleCallback = object : LocationCallback() {
                            override fun onLocationResult(result: LocationResult) {
                                val loc = result.lastLocation
                                if (loc != null) {
                                    val userLoc = reverseGeocode(loc.latitude, loc.longitude)
                                    if (continuation.isActive) continuation.resume(LocationState.Success(userLoc))
                                } else {
                                    val fallback = UserLocation(19.0760, 72.8777, "Mumbai", "Maharashtra")
                                    if (continuation.isActive) continuation.resume(LocationState.Success(fallback))
                                }
                                fusedLocationClient.removeLocationUpdates(this)
                            }
                        }
                        fusedLocationClient.requestLocationUpdates(locationRequest, singleCallback, Looper.getMainLooper())
                    }
                }.addOnFailureListener {
                    val fallback = UserLocation(19.0760, 72.8777, "Mumbai", "Maharashtra")
                    if (continuation.isActive) continuation.resume(LocationState.Success(fallback))
                }
            } catch (e: SecurityException) {
                if (continuation.isActive) continuation.resume(LocationState.PermissionDenied)
            } catch (e: Exception) {
                val fallback = UserLocation(19.0760, 72.8777, "Mumbai", "Maharashtra")
                if (continuation.isActive) continuation.resume(LocationState.Success(fallback))
            }
        }
    }

    private fun reverseGeocode(lat: Double, lng: Double): UserLocation {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                var syncCity: String? = null
                var syncState: String? = null
                val latch = java.util.concurrent.CountDownLatch(1)
                geocoder.getFromLocation(lat, lng, 1) { addresses ->
                    if (addresses.isNotEmpty()) {
                        val addr = addresses[0]
                        syncCity = addr.locality ?: addr.subAdminArea ?: addr.adminArea
                        syncState = addr.adminArea
                    }
                    latch.countDown()
                }
                latch.await(2, java.util.concurrent.TimeUnit.SECONDS)
                if (!syncCity.isNullOrBlank()) {
                    return UserLocation(lat, lng, syncCity!!, syncState ?: "")
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea
                    val state = addr.adminArea ?: ""
                    if (!city.isNullOrBlank()) {
                        return UserLocation(lat, lng, city, state)
                    }
                }
            }
        } catch (e: Exception) {
            // Fallthrough to HTTP reverse geocoding fallback
        }

        // Network reverse geocoding fallback for accuracy
        return try {
            val urlString = "https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=$lat&longitude=$lng&localityLanguage=en"
            val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
                connectTimeout = 3000
                readTimeout = 3000
            }
            if (conn.responseCode == 200) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
                val city = json.optString("city", json.optString("locality", json.optString("principalSubdivision", "Current Location")))
                val state = json.optString("principalSubdivision", "")
                val cleanCity = if (city.isNotBlank() && city != "null") city else "Current Location"
                val cleanState = if (state.isNotBlank() && state != "null" && state != cleanCity) state else ""
                UserLocation(lat, lng, cleanCity, cleanState)
            } else {
                UserLocation(lat, lng, "Current Location", "")
            }
        } catch (e: Exception) {
            UserLocation(lat, lng, "Current Location", "")
        }
    }
}
