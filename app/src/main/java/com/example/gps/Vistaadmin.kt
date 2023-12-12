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

class Vistaadmin : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMyLocationClickListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var database: DatabaseReference
    private lateinit var user : FirebaseUser
    private val locationUpdateHandler = Handler()
    //private val ubicaciones: MutableList<LatLng> = mutableListOf()
    private val locationUpdateRunnable = object : Runnable {
        override fun run() {
            //uplatlong()
            locationUpdateHandler.postDelayed(this, INTERVALO_ACTUALIZACION) // establecer el próximo retraso

        }
    }

    companion object {
        const val REQUEST_CODE_LOCATION = 0
        const val INTERVALO_ACTUALIZACION = 50_000L   //60_000L 1 min
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        createMarker()
        createPolylines()
        enableLocation()
        map.setOnMyLocationClickListener(this)
        //uplatlong()
    }


    private fun createPolylines(){

        val layer = GeoJsonLayer(map, R.raw.polylines, this)

        // Añade la capa al mapa
        layer.addLayerToMap()
        val defaultLineStringStyle = layer.defaultLineStringStyle
        defaultLineStringStyle.color = Color.BLUE
        layer.setOnFeatureClickListener { feature ->
            Log.i("GeoJsonClick", "Feature clicked: ${feature.getProperty("Yachay Bounds")}")
            Toast.makeText(this, "Yachay Bounds", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createMarker() {
        val favoritePlace = LatLng(0.403531, -78.172762)
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(favoritePlace, 15f),
            4000,
            null
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vistaadmin)
        createMapFragment()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        database = Firebase.database.reference
        auth = Firebase.auth
        user = auth.currentUser!!
        onStart()
        onResume()
        mostrarUltimoPunto()
    }

    private fun createMapFragment() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }



    private fun isPermissionsGranted() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED



    private fun enableLocation() {
        if (!::map.isInitialized) return
        if (isPermissionsGranted()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            map.isMyLocationEnabled = true
        } else {
            requestLocationPermission()
        }
    }

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

    override fun onStart() {
        super.onStart()
        val serviceIntent = Intent(this, LocationUpdateService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    override fun onStop() {
        super.onStop()
        val serviceIntent = Intent(this, LocationUpdateService::class.java)
        stopService(serviceIntent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            REQUEST_CODE_LOCATION -> if(grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                map.isMyLocationEnabled = true
            }else{
                Toast.makeText(this, "Para activar la localización ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }
    override fun onResumeFragments() {
        super.onResumeFragments()
        if (!::map.isInitialized) return
        if(!isPermissionsGranted()){
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            map.isMyLocationEnabled = false
            Toast.makeText(this, "Para activar la localización ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMyLocationClick(p0: Location) {
        Toast.makeText(this, "Estás en ${p0.latitude}, ${p0.longitude}", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        onPause()// detener actualizaciones cuando la actividad se destruye
    }

    private fun mostrarUltimoPunto() {
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