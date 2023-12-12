package com.example.gps

import LocationUpdateService
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.maps.android.data.geojson.GeoJsonLayer

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMyLocationClickListener {

    // Declaración de variables miembro
    private lateinit var auth: FirebaseAuth
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var database: DatabaseReference
    private lateinit var user: FirebaseUser
    private val locationUpdateHandler = Handler()
    private val ubicaciones: MutableList<LatLng> = mutableListOf()
    private val locationUpdateRunnable = object : Runnable {
        override fun run() {
            // Actualizar la ubicación
            uplatlong()
            // Establecer el próximo retraso para la actualización
            locationUpdateHandler.postDelayed(this, INTERVALO_ACTUALIZACION)
        }
    }

    // Constantes
    companion object {
        const val REQUEST_CODE_LOCATION = 0
        const val INTERVALO_ACTUALIZACION = 50_000L   // Intervalo de actualización de ubicación (50 segundos)
    }

    // Método llamado cuando el mapa está listo
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        // Crear marcador y polilíneas en el mapa
        createMarker()
        createPolylines()
        // Habilitar la ubicación en el mapa
        enableLocation()
        map.setOnMyLocationClickListener(this)
        // Actualizar la ubicación al iniciar la actividad
        uplatlong()
    }

    // Método para crear las polilíneas
    private fun createPolylines() {
        // Crear una capa GeoJson y establecer un estilo de línea azul
        val layer = GeoJsonLayer(map, R.raw.polylines, this)
        layer.addLayerToMap()
        val defaultLineStringStyle = layer.defaultLineStringStyle
        defaultLineStringStyle.color = Color.BLUE
        // Manejar clics en las características GeoJson
        layer.setOnFeatureClickListener { feature ->
            Log.i("GeoJsonClick", "Feature clicked: ${feature.getProperty("Yachay Bounds")}")
            Toast.makeText(this, "Yachay Bounds", Toast.LENGTH_SHORT).show()
        }
    }

    // Método para crear un marcador en un lugar específico
    private fun createMarker() {
        val favoritePlace = LatLng(0.403531, -78.172762)
        // Animar la cámara para centrarse en el marcador
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(favoritePlace, 15f),
            4000, null
        )
    }

    // Método llamado al crear la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Crear fragmento de mapa y configurar servicios de ubicación
        createMapFragment()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        database = Firebase.database.reference
        auth = Firebase.auth
        user = auth.currentUser!!
        onStart()
        onResume()
        // Mostrar el marcador del último punto al iniciar la actividad
        mostrarMarcadorUltimoPunto()
    }

    // Método para actualizar la ubicación actual
    private fun uplatlong() {
        // Verificar permisos de ubicación
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Retornar si no se tienen los permisos necesarios
            return
        }
        // Obtener la última ubicación conocida
        fusedLocationClient.lastLocation
            .addOnSuccessListener(this) { location ->
                // Verificar si la ubicación no es nula
                if (location != null) {
                    // Log de la latitud y longitud
                    Log.e("Latitud: ${location.latitude}", "Longitud: ${location.longitude}")

                    // Obtener la clave del usuario
                    val userKey = user.email?.replace(".", "?")
                    // Crear un nuevo objeto LatLng con la ubicación actual
                    val newLocation = LatLng(location.latitude, location.longitude)
                    // Agregar la nueva ubicación a la lista
                    ubicaciones.add(newLocation)

                    // Crear una referencia a la ubicación en la base de datos
                    val newLocationRef =
                        database.child("usuarios").child(userKey ?: "").child("ubicaciones").push()
                    newLocationRef.child("first").setValue(newLocation.latitude)
                    newLocationRef.child("second").setValue(newLocation.longitude)

                    // Mostrar el marcador del último punto en el mapa
                    mostrarMarcadorUltimoPunto()
                }
            }
    }

    // Método para crear el fragmento del mapa
    private fun createMapFragment() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // Método para verificar si los permisos de ubicación están concedidos
    private fun isPermissionsGranted() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    // Método para habilitar la capa de ubicación en el mapa
    private fun enableLocation() {
        // Verificar si el mapa está inicializado
        if (!::map.isInitialized) return
        // Verificar si los permisos de ubicación están concedidos
        if (isPermissionsGranted()) {
            // Verificar si se tienen los permisos necesarios y habilitar la capa de ubicación
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            map.isMyLocationEnabled = true
        } else {
            // Solicitar permisos de ubicación si no están concedidos
            requestLocationPermission()
        }
    }

    // Método para solicitar permisos de ubicación al usuario
    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this, "Ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION
            )
        }
    }

    // Método llamado cuando la actividad inicia
    override fun onStart() {
        super.onStart()
        // Iniciar el servicio de actualización de ubicación en primer plano
        val serviceIntent = Intent(this, LocationUpdateService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    // Método llamado cuando la actividad se detiene
    override fun onStop() {
        super.onStop()
        // Detener el servicio de actualización de ubicación
        val serviceIntent = Intent(this, LocationUpdateService::class.java)
        stopService(serviceIntent)
    }

    // Método llamado cuando la actividad se reanuda
    override fun onResume() {
        super.onResume()
        // Iniciar la actualización de ubicación en un intervalo definido
        locationUpdateHandler.postDelayed(locationUpdateRunnable, INTERVALO_ACTUALIZACION)
    }

    // Método llamado cuando la actividad se pausa
    override fun onPause() {
        super.onPause()
        // Detener la actualización de ubicación
        locationUpdateHandler.removeCallbacks(locationUpdateRunnable)
    }

    // Método llamado cuando se obtiene una respuesta a la solicitud de permisos
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_LOCATION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                // Habilitar la capa de ubicación si los permisos son concedidos
                map.isMyLocationEnabled = true
            } else {
                // Mostrar un mensaje si los permisos no son concedidos
                Toast.makeText(
                    this,
                    "Para activar la localización ve a ajustes y acepta los permisos",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
            }
        }
    }

    // Método llamado cuando se reanudan los fragmentos de la actividad
    override fun onResumeFragments() {
        super.onResumeFragments()
        // Verificar si el mapa está inicializado y si los permisos no están concedidos
        if (!::map.isInitialized) return
        if (!isPermissionsGranted()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            // Deshabilitar la capa de ubicación y mostrar un mensaje
            map.isMyLocationEnabled = false
            Toast.makeText(
                this,
                "Para activar la localización ve a ajustes y acepta los permisos",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Método llamado cuando se hace clic en la ubicación del usuario en el mapa
    override fun onMyLocationClick(p0: Location) {
        Toast.makeText(this, "Estás en ${p0.latitude}, ${p0.longitude}", Toast.LENGTH_SHORT).show()
    }

    // Método llamado cuando la actividad se destruye
    override fun onDestroy() {
        super.onDestroy()
        // Detener las actualizaciones cuando la actividad se destruye
        onPause()
    }

    // Método para mostrar el marcador del último punto de todos los usuarios
    private fun mostrarMarcadorUltimoPunto() {
        val usuariosRef = database.child("usuarios")

        usuariosRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (usuarioSnapshot in snapshot.children) {
                    val email = usuarioSnapshot.key
                    val ubicacionesRef = usuarioSnapshot.child("ubicaciones")

                    if (ubicacionesRef.exists()) {
                        // Obtener el último punto de ubicación del usuario
                        val ultimoPuntoSnapshot = ubicacionesRef.children.last()
                        val latitud = ultimoPuntoSnapshot.child("first").value as? Double
                        val longitud = ultimoPuntoSnapshot.child("second").value as? Double

                        // Verificar si se encontraron latitud y longitud
                        if (latitud != null && longitud != null) {
                            // Agregar marcador para el último punto del usuario
                            val ultimoPunto = LatLng(latitud, longitud)
                            map.addMarker(
                                MarkerOptions().position(ultimoPunto).title("Último Punto - $email")
                            )
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar errores si es necesario
            }
        })
    }
}
