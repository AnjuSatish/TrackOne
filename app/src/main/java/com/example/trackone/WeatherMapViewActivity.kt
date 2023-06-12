package com.example.trackone

import WeatherResponse
import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherMapViewActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: GoogleMap
    private lateinit var weatherTextView: TextView
    private lateinit var weatherApi: weatherMapApi
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_weather_map_view)

        weatherTextView = findViewById(R.id.weatherTextView)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
        val retrofit = Retrofit.Builder()
            .baseUrl("http://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Create the WeatherApi service
        weatherApi = retrofit.create(weatherMapApi::class.java)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mapView = googleMap

        // Check location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 1
            )
            return
        }
        mapView.isMyLocationEnabled = true
        mapView.uiSettings.isMyLocationButtonEnabled = true
        // Get the current location


        // Get the location data
        val locationData =
            "{\"coord\":{\"lon\":76.3422,\"lat\":10.0197},\"weather\":[{\"id\":721," +
                    "\"main\":\"Haze\",\"description\":\"haze\",\"icon\":\"50d\"}],\"base\":\"stations\",\"main\":" +
                    "{\"temp\":304.04,\"feels_like\":311.04,\"temp_min\":304.04,\"temp_max\":304.04,\"pressure\":1007," +
                    "\"humidity\":74},\"visibility\":5000,\"wind\":{\"speed\":1.54,\"deg\":140},\"clouds\":{\"all\":75}," +
                    "\"dt\":1686469428,\"sys\":{\"type\":1,\"id\":9211,\"country\":\"IN\",\"sunrise\":1686443600," +
                    "\"sunset\":1686489311},\"timezone\":19800,\"id\":1272018,\"name\":\"Ernākulam\",\"cod\":200}"

        // Parse the location data JSON
        val locationJson = JSONObject(locationData)
        val temperature = locationJson.getJSONObject("main").getDouble("temp")
        val weatherDescription =
            locationJson.getJSONArray("weather").getJSONObject(0).getString("description")
        val cityName = locationJson.getString("name")

        // Set the weather text view
        weatherTextView.text =
            "Temperature: $temperature°C\nWeather: $weatherDescription"

        // Get the current location coordinates
        val latitude = locationJson.getJSONObject("coord").getDouble("lat")
        val longitude = locationJson.getJSONObject("coord").getDouble("lon")

        // Create a LatLng object for the current location
        val currentLatLng = LatLng(latitude, longitude)

        // Move the camera to the current location and add a marker
        mapView.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
        mapView.addMarker(MarkerOptions().position(currentLatLng).title(cityName))
    }
}
