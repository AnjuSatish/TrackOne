package com.example.trackone

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LocationActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private lateinit var toolbar: Toolbar
    private lateinit var dashboardIcon: ImageView
    private lateinit var mapFragment: MapView
    private lateinit var selectedUserId: String
    private lateinit var polyline: Polyline




    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        selectedUserId = intent.getStringExtra("userId") ?: ""
        bottomNavigationView = findViewById(R.id.bottomNavigation)
        toolbar = findViewById(R.id.toolbar)
        dashboardIcon = findViewById(R.id.dashboardIcon)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_locations -> {
                    val intent = Intent(this@LocationActivity, LocationActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_users_list -> {
                    val intent = Intent(this@LocationActivity, usersListActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        dashboardIcon.setOnClickListener { view ->
            showDashboardPopupMenu(view)
        }
        // Initialize the fused location provider client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        showCurrentLocation()
    }


        private fun showDashboardPopupMenu(view: View) {
            val popupMenu = PopupMenu(this, view)
            popupMenu.inflate(R.menu.dashboard_menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_mapview -> {
                        val intent = Intent(this@LocationActivity, LocationActivity::class.java)
                        startActivity(intent)
                        true
                    }

                    R.id.menu_weatherview -> {
                        val intent = Intent(this@LocationActivity, WeatherMapViewActivity::class.java)
                        startActivity(intent)
                        true
                    }

                    else -> false
                }
            }

            popupMenu.show()

    }


    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Check location permission and request if not granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            googleMap.isMyLocationEnabled = true
            googleMap.setOnMyLocationChangeListener { location: Location? ->
                // Update location in Firebase
                location?.let { updateLocationInFirebase(it) }

            }
            showCurrentLocation()
        }

    }

    private fun updateLocationInFirebase(location: Location) {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val latLng = LatLng(location.latitude, location.longitude)

        val pathOptions = PolylineOptions().apply {
            color(Color.RED)
            width(5f)
            add(latLng)
        }
        googleMap.addPolyline(pathOptions)

        if (currentUser != null) {
            val userId = currentUser.uid
            val databaseRef = FirebaseDatabase.getInstance().reference
            val locationRef = databaseRef.child("users").child(userId).child("location")
            locationRef.setValue(location)
                .addOnSuccessListener {
                    // Location updated successfully
                }
                .addOnFailureListener { error ->
                    // Failed to update location
                }

        } else {
            // User is not authenticated, handle accordingly
        }
        // Replace with your own logic to get the user ID
            }


    private fun showCurrentLocation() {
        // Get the last known location
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
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                // Get the current location coordinates
                val currentLatLng = LatLng(location.latitude, location.longitude)

                // Add a marker at the current location
                googleMap.addMarker(
                    MarkerOptions()
                        .position(currentLatLng)
                        .title("Current Location")
                )

                // Move the camera to the current location
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
            }
        }
    }
}