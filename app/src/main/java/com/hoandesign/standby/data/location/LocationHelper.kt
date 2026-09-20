package com.hoandesign.standby.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import java.util.Locale

object LocationHelper {
    fun getCurrentLocationAndCity(context: Context): Pair<Location?, String> {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return Pair(null, "Cupertino")
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        
        var location: Location? = null
        try {
            val providers = locationManager.getProviders(true)
            for (provider in providers) {
                val l = locationManager.getLastKnownLocation(provider) ?: continue
                if (location == null || l.accuracy < location.accuracy) {
                    location = l
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val defaultCity = "Cupertino"
        var cityName = defaultCity

        location?.let {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    cityName = address.locality ?: address.subAdminArea ?: address.adminArea ?: defaultCity
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return Pair(location, cityName)
    }
}
