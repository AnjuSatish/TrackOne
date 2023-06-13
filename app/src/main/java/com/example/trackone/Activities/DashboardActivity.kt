package com.example.trackone.Activities

import WeatherResponse
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.trackone.R
import com.example.trackone.userDatas.weatherMapApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
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

class DashboardActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var dashboardButton: ImageButton
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager
    private lateinit var weatherTextView: TextView
    private lateinit var userTv: TextView
    private val path: MutableList<LatLng> = ArrayList()



    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        bottomNavigationView = findViewById(R.id.bottomNavigation)
        dashboardButton = findViewById(R.id.dashboardButton)
        weatherTextView=findViewById(R.id.weatherTextView)
        userTv=findViewById(R.id.userTv)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
        val userName = intent.getStringExtra("name")
        userTv.text = userName

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        fusedLocationClient=LocationServices.getFusedLocationProviderClient(this)
        // Set bottom navigation item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_locations -> {
                    val intent = Intent(this@DashboardActivity, DashboardActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_users_list -> {
                    val intent = Intent(this@DashboardActivity, usersListActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        dashboardButton.setOnClickListener { view ->
            showDashboardPopupMenu(view)
        }
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Enable the My Location button and permission check
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        )else {
            mMap.isMyLocationEnabled = true
            googleMap.setOnMyLocationChangeListener { location: Location? ->
                location?.let { updateLocationInFirebase(it) }

            }
            mMap.uiSettings.isMyLocationButtonEnabled = true
        }

        getCurrentLocationAndUpdateFirebase()
    }

    private fun updateLocationInFirebase(location: Location) {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val latLng = LatLng(location.latitude, location.longitude)
        if (currentUser != null) {
            val userId = currentUser.uid
            val databaseRef = FirebaseDatabase.getInstance().reference
            val locationRef = databaseRef.child("users").child(userId).child("location")
            locationRef.setValue(location)
                .addOnSuccessListener {
                }
                .addOnFailureListener { error ->
                   Toast.makeText(this,"Failed to update Location",Toast.LENGTH_SHORT).show()
                }

        } else {
            Toast.makeText(this,"Failed",Toast.LENGTH_SHORT).show()

        }
            }

    fun fetchWeatherAndUpdateDatabase(latitude: Double?, longitude: Double?) {
        val apiKey = "6a4c8c10b92ddc1804f329692259fd33"
        val baseUrl = "https://api.openweathermap.org/"

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(weatherMapApi::class.java)

        val call = service.getCurrentWeather(latitude!!, longitude!!, apiKey)
        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    val temperature = weatherResponse?.main?.temp
                    val description = weatherResponse?.weather?.get(0)?.description
                    runOnUiThread {
                        // Assuming you have a TextView for temperature
                        weatherTextView.text =  "Temperature: $temperature°C\nWeather: $description"

                    }
                } else {

                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {

            }
        })
    }

    private fun showDashboardPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.dashboard_menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_mapview -> {
                    val intent = Intent(this@DashboardActivity, DashboardActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_weatherview -> {
                    weatherTextView.visibility=View.VISIBLE
                    getWeatherDataAndUpdateFirebase()
                   // getCurrentLocationAndFetchWeather()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun getWeatherDataAndUpdateFirebase() {
        val database = FirebaseDatabase.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val userRef = database.getReference("users/$userId/location")
        userRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val latitude = dataSnapshot.child("latitude").getValue(Double::class.java)
                val longitude = dataSnapshot.child("longitude").getValue(Double::class.java)

                fetchWeatherAndUpdateDatabase(latitude, longitude)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })
    }


    private fun getCurrentLocationAndUpdateFirebase() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val LOCATION_PERMISSION_REQUEST_CODE = 1
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.addMarker(
                        MarkerOptions().position(currentLatLng).title("Current Location")
                    )
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18f))
                    if (path.size > 1) {
                        mMap.addPolyline(PolylineOptions().addAll(path).color(Color.BLUE))
                    }
                }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val REQUEST_CODE_LOCATION_PERMISSION = 1
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocationAndUpdateFirebase()
            } else {
                Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show()
            }
        }
    }



}



