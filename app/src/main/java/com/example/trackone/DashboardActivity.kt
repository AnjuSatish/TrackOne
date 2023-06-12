package com.example.trackone

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DashboardActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var dashboardButton: ImageButton
    private lateinit var frameLayout: FrameLayout
  //  private val usersListFragment = userListFragment()
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        bottomNavigationView = findViewById(R.id.bottomNavigation)
        dashboardButton = findViewById(R.id.dashboardButton)
        frameLayout = findViewById(R.id.frameLayout)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Set bottom navigation item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_locations -> {
                    val intent = Intent(this@DashboardActivity, LocationActivity::class.java)
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

        // Set click listener for dashboard button
        dashboardButton.setOnClickListener { view ->
            showDashboardPopupMenu(view)
        }
    }


    private fun showDashboardPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.dashboard_menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_mapview -> {
                    val intent = Intent(this@DashboardActivity, LocationActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_weatherview -> {
                    val intent = Intent(this@DashboardActivity, WeatherMapViewActivity::class.java)
                    startActivity(intent)
                   // getCurrentLocationAndFetchWeather()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }



}


