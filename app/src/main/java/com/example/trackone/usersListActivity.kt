package com.example.trackone

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class usersListActivity : AppCompatActivity() , UserAdapter.UserClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dashboardButton: ImageButton
    private lateinit var userDatabaseRef: DatabaseReference
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var userAdapter: UserAdapter
    private var userList: List<User> = emptyList()
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users_list)
        dashboardButton = findViewById(R.id.dashboardButton)
        recyclerView = findViewById(R.id.recycler_view)
        searchView = findViewById(R.id.search_view)
        firebaseAuth = FirebaseAuth.getInstance()
        bottomNavigationView = findViewById(R.id.bottomNavigation)

        userAdapter = UserAdapter(this)
        recyclerView.adapter=userAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchUsers(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchUsers(newText)
                return true
            }
        })
        showUsers(userList)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_locations -> {
                    val intent = Intent(this@usersListActivity, LocationActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_users_list -> {
                    val intent = Intent(this@usersListActivity, usersListActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        dashboardButton.setOnClickListener { view ->
            showDashboardPopupMenu(view)
        }

        userDatabaseRef = FirebaseDatabase.getInstance().reference.child("users")
        userDatabaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userList = ArrayList<User>()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    user?.let { userList.add(it)
                    }
                }
                userAdapter.setUserList(userList)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
}

    private fun searchUsers(query: String) {
        val filteredList = userList.filter { user ->
            user.email.contains(query, ignoreCase = true)
        }
        showUsers(filteredList)
    }

    private fun showUsers(users: List<User>) {
        userAdapter.filterList(users)
    }

    private fun showDashboardPopupMenu(view: View?) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.dashboard_menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_mapview -> {
                    val intent = Intent(this@usersListActivity, LocationActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_weatherview -> {
                    val intent = Intent(this@usersListActivity, WeatherMapViewActivity::class.java)
                    startActivity(intent)
                    // getCurrentLocationAndFetchWeather()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    override fun onUserClick(user: User) {
        val selectedUserId = user.userId

        val intent = Intent(this, LocationActivity::class.java)
        intent.putExtra("userId", selectedUserId)
        startActivity(intent)
    }

}