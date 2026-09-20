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
import java.util.Locale

object LocationHelper {
    @Volatile
    private var lastKnownLocation: Location? = null
    @Volatile
    private var lastKnownCity: String = "Cupertino"

    @SuppressLint("MissingPermission")
    fun getCurrentLocationAndCity(context: Context): Pair<Location?, String> {
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasCoarse && !hasFine) {
            return Pair(null, "Cupertino")
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
        } catch (e: Exception) {
            // Graceful fallback to legacy LocationManager if Google Play Services is unavailable
            try {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val providers = locationManager.getProviders(true)
                for (provider in providers) {
                    val l = locationManager.getLastKnownLocation(provider) ?: continue
                    if (lastKnownLocation == null || l.accuracy < lastKnownLocation!!.accuracy) {
                        lastKnownLocation = l
                        resolveCity(context, l)
                    }
                }
            } catch (fallbackEx: Exception) {
                fallbackEx.printStackTrace()
            }
        }

        return Pair(lastKnownLocation, lastKnownCity)
    }

    private fun resolveCity(context: Context, loc: Location) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                lastKnownCity = address.locality ?: address.subAdminArea ?: address.adminArea ?: "Cupertino"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
