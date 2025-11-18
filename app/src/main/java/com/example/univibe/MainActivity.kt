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
import com.example.univibe.models.LostFoundItem
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

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
            Toast.makeText(this, "Event added successfully", Toast.LENGTH_SHORT).show()
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
            setupRealtimeListener()
        }

        // ✅ FAB with launcher
        binding.fabAddEvent.setOnClickListener {
            addEventLauncher.launch(Intent(this, AddEventActivity::class.java))
        }

        // ✅ Bottom navigation - FIXED
        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.nav_home
        }

        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> true
                R.id.nav_search -> {
                    startActivity(Intent(this, SearchActivity::class.java))
                    overridePendingTransition(0, 0)
                    false
                }
                R.id.nav_add -> {
                    addEventLauncher.launch(Intent(this, AddEventActivity::class.java))
                    false
                }
                R.id.nav_notifications -> {
                    startActivity(Intent(this, NotificationsActivity::class.java))
                    overridePendingTransition(0, 0)
                    false
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(0, 0)
                    false
                }
                else -> false
            }
        }

        // Filter Chips
        binding.chipAll.setOnClickListener {
            currentFilter = "All"
            Log.d("MainActivity", "🔹 Filter: All")
            setupRealtimeListener()
        }

        binding.chipEvents.setOnClickListener {
            currentFilter = "Events"
            Log.d("MainActivity", "🔹 Filter: Events")
            setupRealtimeListener()
        }

        binding.chipTransport.setOnClickListener {
            currentFilter = "Transport"
            Log.d("MainActivity", "🔹 Filter: Transport")
            setupRealtimeListener()
        }

        binding.chipLostFound.setOnClickListener {
            currentFilter = "Lost & Found"
            Log.d("MainActivity", "🔹 Filter: Lost & Found")
            setupRealtimeListener()
        }

        // Initial load with REAL-TIME listener
        binding.progressBar.visibility = View.VISIBLE
        setupRealtimeListener()
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "↩️ onResume called")
        // ✅ Re-select home when returning to MainActivity
        binding.bottomNavigation.selectedItemId = R.id.nav_home
    }

    override fun onDestroy() {
        super.onDestroy()
        firestoreListener?.remove()
        Log.d("MainActivity", "🛑 Firestore listener removed")
    }

    /**
     * 🔥 REAL-TIME LISTENER - Handles Events, Transport, and Lost & Found
     */
    private fun setupRealtimeListener() {
        firestoreListener?.remove()
        binding.progressBar.visibility = View.VISIBLE

        Log.d("MainActivity", "📡 Setting up listener for: $currentFilter")

        when (currentFilter) {
            "Transport" -> loadTransportData()
            "Lost & Found" -> loadLostFoundData()
            else -> loadEventsData()
        }
    }

    // ✅ Load Events - Properly handles Timestamp fields
    private fun loadEventsData() {
        val query = if (currentFilter == "All") {
            db.collection("events")
                .orderBy("timestamp", Query.Direction.DESCENDING)
        } else {
            db.collection("events")
                .whereEqualTo("category", currentFilter)
                .orderBy("timestamp", Query.Direction.DESCENDING)
        }

        firestoreListener = query.addSnapshotListener { snapshot, error ->
            binding.swipeRefresh.isRefreshing = false

            if (error != null) {
                Log.e("MainActivity", "❌ Error loading events: ${error.message}")
                binding.progressBar.visibility = View.GONE

                // Handle missing index error
                if (error.message?.contains("index") == true) {
                    Toast.makeText(this, "Creating database index. Please wait...", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Error loading events", Toast.LENGTH_SHORT).show()
                }
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val events = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Event::class.java)?.copy(
                            id = doc.id,
                            // Ensure date is properly formatted if timestamp exists
                            date = if (doc.getTimestamp("timestamp") != null) {
                                formatTimestamp(doc.getTimestamp("timestamp"))
                            } else {
                                doc.getString("date") ?: ""
                            }
                        )
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error parsing event: ${e.message}")
                        null
                    }
                }
                allEvents.clear()
                allEvents.addAll(events)
                adapter.updateData(allEvents)
                binding.progressBar.visibility = View.GONE
                Log.d("MainActivity", "✅ Loaded ${events.size} events")
            }
        }
    }

    // ✅ Load Transport - Updated for busRoutes collection
    private fun loadTransportData() {
        firestoreListener = db.collection("busRoutes")
            .whereEqualTo("active", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                binding.swipeRefresh.isRefreshing = false

                if (error != null) {
                    Log.e("MainActivity", "❌ Error loading transport: ${error.message}")
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Error loading transport", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val transportEvents = snapshot.documents.mapNotNull { doc ->
                        try {
                            Event(
                                id = doc.id,
                                title = doc.getString("routename") ?: "Unknown Route",
                                description = "${doc.getString("source") ?: ""} → ${doc.getString("destination") ?: ""}\n${doc.getString("departureTime") ?: ""}",
                                date = doc.getString("departureTime") ?: "",
                                timestamp = doc.getTimestamp("createdAt"),
                                location = doc.getString("source") ?: "",
                                category = "Transport",
                                createdBy = doc.getString("createdBy") ?: "",
                                createdAt = doc.getTimestamp("createdAt")
                            )
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Error parsing transport: ${e.message}")
                            null
                        }
                    }
                    allEvents.clear()
                    allEvents.addAll(transportEvents)
                    adapter.updateData(allEvents)
                    binding.progressBar.visibility = View.GONE
                    Log.d("MainActivity", "✅ Loaded ${transportEvents.size} transport items")
                }
            }
    }

    // ✅ Load Lost & Found
    private fun loadLostFoundData() {
        firestoreListener = db.collection("lostAndFound")
            .whereEqualTo("status", "active")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                binding.swipeRefresh.isRefreshing = false

                if (error != null) {
                    Log.e("MainActivity", "❌ Error loading lost & found: ${error.message}")
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Error loading lost & found", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val lostFoundEvents = snapshot.documents.mapNotNull { doc ->
                        try {
                            val createdAtTimestamp = doc.getTimestamp("createdAt")
                            Event(
                                id = doc.id,
                                title = doc.getString("itemName") ?: "Unknown Item",
                                description = doc.getString("description") ?: "",
                                date = formatTimestamp(createdAtTimestamp),
                                timestamp = createdAtTimestamp,
                                location = doc.getString("locationLost") ?: "",
                                category = "Lost & Found",
                                createdBy = doc.getString("createdBy") ?: "",
                                createdAt = createdAtTimestamp
                            )
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Error parsing lost item: ${e.message}")
                            null
                        }
                    }
                    allEvents.clear()
                    allEvents.addAll(lostFoundEvents)
                    adapter.updateData(allEvents)
                    binding.progressBar.visibility = View.GONE
                    Log.d("MainActivity", "✅ Loaded ${lostFoundEvents.size} lost items")
                }
            }
    }

    // ✅ Helper function to format Timestamp to readable date string
    private fun formatTimestamp(timestamp: Timestamp?): String {
        return try {
            timestamp?.toDate()?.let {
                val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                sdf.format(it)
            } ?: ""
        } catch (e: Exception) {
            Log.e("MainActivity", "Error formatting timestamp: ${e.message}")
            ""
        }
    }
}
