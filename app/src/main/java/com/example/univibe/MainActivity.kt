package com.example.univibe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.univibe.adapters.EventAdapter
import com.example.univibe.databinding.ActivityMainBinding
import com.example.univibe.models.Event
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: EventAdapter
    private var allEvents: MutableList<Event> = mutableListOf()
    private var currentFilter: String = "All"
    private var firestoreListener: ListenerRegistration? = null

    // ✅ Activity result launcher
    private val addEventLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Log.d("MainActivity", "✅ Event added, real-time listener will auto-refresh")
            binding.progressBar.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // RecyclerView
        adapter = EventAdapter(listOf()) { event ->
            Toast.makeText(this, "Event: ${event.title}", Toast.LENGTH_SHORT).show()
        }
        binding.eventsRecyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.eventsRecyclerView.adapter = adapter

        // Pull to refresh
        binding.swipeRefresh.setOnRefreshListener {
            Log.d("MainActivity", "🔄 Refreshing...")
            binding.progressBar.visibility = View.VISIBLE
        }

        // ✅ FAB with launcher
        binding.fabAddEvent.setOnClickListener {
            addEventLauncher.launch(Intent(this, AddEventActivity::class.java))
        }

        // Bottom navigation
        binding.bottomNavigation.selectedItemId = R.id.nav_home
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> true
                R.id.nav_search -> {
                    try {
                        startActivity(Intent(this, SearchActivity::class.java))
                    } catch (e: Exception) {
                        Toast.makeText(this, "Search coming soon!", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.nav_add -> {
                    addEventLauncher.launch(Intent(this, AddEventActivity::class.java))
                    true
                }
                R.id.nav_notifications -> {
                    try {
                        startActivity(Intent(this, NotificationsActivity::class.java))
                    } catch (e: Exception) {
                        Toast.makeText(this, "Notifications coming soon!", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.nav_profile -> {
                    Toast.makeText(this, "Profile coming soon!", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        // Filter Chips
        binding.chipAll.setOnClickListener {
            currentFilter = "All"
            Log.d("MainActivity", "🔹 Filter: All")
            applyFilter()
        }

        binding.chipEvents.setOnClickListener {
            currentFilter = "Events"
            Log.d("MainActivity", "🔹 Filter: Events")
            applyFilter()
        }

        binding.chipTransport.setOnClickListener {
            currentFilter = "Transport"
            Log.d("MainActivity", "🔹 Filter: Transport")
            applyFilter()
        }

        binding.chipLostFound.setOnClickListener {
            currentFilter = "Lost & Found"
            Log.d("MainActivity", "🔹 Filter: Lost & Found")
            applyFilter()
        }

        // Initial load with REAL-TIME listener
        binding.progressBar.visibility = View.VISIBLE
        setupRealtimeListener()
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "↩️ onResume called")
    }

    override fun onDestroy() {
        super.onDestroy()
        // ⚠️ CRITICAL: Clean up listener to prevent memory leaks
        firestoreListener?.remove()
        Log.d("MainActivity", "🛑 Firestore listener removed")
    }

    /**
     * 🔥 REAL-TIME LISTENER - Automatically updates UI when Firebase changes
     * This is the KEY fix for cards not showing after event creation!
     */
    private fun setupRealtimeListener() {
        Log.d("MainActivity", "📡 Setting up REAL-TIME Firestore listener...")

        firestoreListener = db.collection("events")
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                binding.swipeRefresh.isRefreshing = false

                if (error != null) {
                    Log.e("MainActivity", "❌ Listener error: ${error.message}")
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    try {
                        val newEventsList = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(Event::class.java)?.also {
                                    Log.d("MainActivity", "📦 Event: ${it.title} | Category: ${it.category}")
                                }
                            } catch (e: Exception) {
                                Log.e("MainActivity", "Parse error: ${e.message}")
                                null
                            }
                        }

                        allEvents.clear()
                        allEvents.addAll(newEventsList)
                        Log.d("MainActivity", "✅ Loaded ${allEvents.size} total events from Firebase")

                        // Apply current filter
                        applyFilter()

                        binding.progressBar.visibility = View.GONE

                    } catch (e: Exception) {
                        Log.e("MainActivity", "Exception: ${e.message}")
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
    }

    /**
     * Apply current filter to allEvents and update adapter
     */
    private fun applyFilter() {
        val filteredList = if (currentFilter == "All") {
            Log.d("MainActivity", "📊 Showing ALL ${allEvents.size} events")
            allEvents
        } else {
            val filtered = allEvents.filter { event ->
                event.category.equals(currentFilter, ignoreCase = true)
            }
            Log.d("MainActivity", "📊 Filtered: ${filtered.size} events (Category: $currentFilter)")
            filtered
        }

        adapter.updateData(filteredList)

        if (filteredList.isEmpty()) {
            Log.d("MainActivity", "⚠️ No events found for: $currentFilter")
        }
    }
}