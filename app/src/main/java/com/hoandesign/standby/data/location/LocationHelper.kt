package com.hoandesign.standby.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

object LocationHelper {
    @Volatile
    private var lastKnownLocation: Location? = null
    @Volatile
    private var lastKnownCity: String = "Local Weather"

    fun hasLocationPermission(context: Context): Boolean {
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return hasCoarse || hasFine
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocationAndCity(context: Context): Pair<Location?, String> {
        if (!hasLocationPermission(context)) {
            return Pair(null, "Local Weather")
        }

        try {
            val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                if (loc != null) {
                    lastKnownLocation = loc
                    resolveCity(context, loc)
                }
            }

            if (lastKnownLocation == null) {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                    .addOnSuccessListener { loc: Location? ->
                        if (loc != null) {
                            lastKnownLocation = loc
                            resolveCity(context, loc)
                        }
                    }
            }
        } catch (_: Exception) {
            fallbackToLocationManager(context)
        }

        return Pair(lastKnownLocation, lastKnownCity)
    }

    @SuppressLint("MissingPermission")
    suspend fun getFreshLocationAndCity(context: Context): Pair<Location?, String> = withContext(Dispatchers.IO) {
        if (!hasLocationPermission(context)) {
            return@withContext Pair(null, "Local Weather")
        }

        val location = suspendCancellableCoroutine<Location?> { cont ->
            try {
                val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                fusedClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        cont.resume(loc)
                    } else {
                        fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                            .addOnSuccessListener { currentLoc -> cont.resume(currentLoc) }
                            .addOnFailureListener { cont.resume(null) }
                    }
                }.addOnFailureListener {
                    cont.resume(null)
                }
            } catch (_: Exception) {
                cont.resume(null)
            }
        } ?: fallbackToLocationManager(context)

        if (location != null) {
            lastKnownLocation = location
            val city = resolveCityDirect(context, location)
            lastKnownCity = city
            return@withContext Pair(location, city)
        }

        return@withContext Pair(lastKnownLocation, lastKnownCity)
    }

    @SuppressLint("MissingPermission")
    private fun fallbackToLocationManager(context: Context): Location? {
        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val providers = locationManager.getProviders(true)
            var bestLocation: Location? = null
            for (provider in providers) {
                val l = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                    bestLocation = l
                }
            }
            if (bestLocation != null) {
                lastKnownLocation = bestLocation
                resolveCity(context, bestLocation)
            }
            bestLocation
        } catch (_: Exception) {
            null
        }
    }

    private fun resolveCity(context: Context, loc: Location) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                lastKnownCity = address.locality ?: address.subAdminArea ?: address.adminArea ?: "Local Weather"
            }
        } catch (_: Exception) {
            // Keep current city
        }
    }

    private fun resolveCityDirect(context: Context, loc: Location): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                address.locality ?: address.subAdminArea ?: address.adminArea ?: "Local Weather"
            } else {
                "Local Weather"
            }
        } catch (_: Exception) {
            "Local Weather"
        }
    }
}
