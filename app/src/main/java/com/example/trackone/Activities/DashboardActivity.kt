package com.example.trackone.Activities

import WeatherResponse
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.example.trackone.R
import com.example.trackone.userDatas.weatherMapApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
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
    private lateinit var frameLayout: FrameLayout
  //  private val usersListFragment = userListFragment()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var weatherTextView: TextView
    private lateinit var mapsView: GoogleMap




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        bottomNavigationView = findViewById(R.id.bottomNavigation)
        dashboardButton = findViewById(R.id.dashboardButton)
        weatherTextView=findViewById(R.id.weatherTextView)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
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
        val database = FirebaseDatabase.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser

        val userRef = database.getReference("users/location")
        userRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val latitude = dataSnapshot.child("latitude").getValue(Double::class.java)
                val longitude = dataSnapshot.child("longitude").getValue(Double::class.java)

                // Use the location data to fetch weather information
                fetchWeatherAndUpdateDatabase(latitude, longitude)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // Set bottom navigation item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_locations -> {
                    val intent = Intent(this@DashboardActivity, DashboardActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_users_list -> {
                    weatherTextView.visibility=View.VISIBLE
                    val intent = Intent(this@DashboardActivity, usersListActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }


        // Set click listener for dashboard button
        dashboardButton.setOnClickListener { view ->
            showDashboardPopupMenu(view)
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

                        // Assuming you have a TextView for weather description
                        //  descriptionTextView.text = description
                    }                    // ...
                } else {
                    // Handle error case
                    // ...
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                // Handle failure case
                // ...
            }
        })
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
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

                   // getCurrentLocationAndFetchWeather()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mapsView = googleMap
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
        mapsView.isMyLocationEnabled = true
        mapsView.uiSettings.isMyLocationButtonEnabled = true

        // Set a listener to get the current location once it's available
        mapsView.setOnMyLocationChangeListener(object : GoogleMap.OnMyLocationChangeListener {
            override fun onMyLocationChange(location: Location) {
                val latitude = location.latitude
                val longitude = location.longitude
                saveLocationToFirebase(latitude, longitude)

                fetchWeatherAndUpdateDatabase(latitude, longitude)
                val currentLatLng = LatLng(latitude, longitude)
                mapsView.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))

                // Add a marker at the current location
                mapsView.addMarker(MarkerOptions().position(currentLatLng).title("Current Location"))
            }
        })
    }

private fun saveLocationToFirebase(latitude: Double, longitude: Double) {
    val locationRef = FirebaseDatabase.getInstance().getReference("users/location")
    locationRef.child("latitude").setValue(latitude)
    locationRef.child("longitude").setValue(longitude)


    }

}



