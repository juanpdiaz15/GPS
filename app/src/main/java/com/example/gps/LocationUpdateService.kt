
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class LocationUpdateService : Service() {

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            val locationResult = null
            locationResult ?: return
            // Aquí manejas la actualización de la ubicación
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startLocationUpdates()
        return START_STICKY
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        try {
            if (locationRequest != null) {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    null /* Looper */
                )
            }
        } catch (unlikely: SecurityException) {
            // Log o manejo de excepción
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    companion object {
        const val CHANNEL_ID = "channel_01" // Asegúrate de crear este canal en tu aplicación
    }
}
