package com.example.univibe

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setup RecyclerView (will populate with Firestore data)
        val eventsRecyclerView = findViewById<RecyclerView>(R.id.events_recycler_view)
        // TODO: Setup adapter and fetch events from Firestore

        // Setup FAB (Floating Action Button) for adding events
        val fabAddEvent = findViewById<FloatingActionButton>(R.id.fab_add_event)
        fabAddEvent.setOnClickListener {
            val intent = Intent(this, AddEventActivity::class.java)
            startActivity(intent)
        }

        // Setup Bottom Navigation
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Home is current screen, do nothing
                    true
                }
                R.id.nav_search -> {
                    // TODO: Implement search screen
                    true
                }
                R.id.nav_add -> {
                    // Navigate to AddEventActivity
                    val intent = Intent(this, AddEventActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_messages -> {
                    // TODO: Implement messages screen
                    true
                }
                R.id.nav_notifications -> {
                    // Navigate to NotificationsActivity
                    val intent = Intent(this, NotificationsActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        // Set home as default selected
        bottomNavigation.selectedItemId = R.id.nav_home
    }
}

