package com.example.univibe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.univibe.adapters.EventAdapter
import com.example.univibe.adapters.TransportAdapter
import com.example.univibe.databinding.ActivityMainBinding
import com.example.univibe.models.Event
import com.example.univibe.models.Transport
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val db = FirebaseFirestore.getInstance()

    private lateinit var eventAdapter: EventAdapter
    private lateinit var transportAdapter: TransportAdapter

    private var currentFilter: String = "All"
    private var firestoreListener: ListenerRegistration? = null

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

        // Initialize adapters with ArrayList
        eventAdapter = EventAdapter(arrayListOf()) { event ->
            Toast.makeText(this, "Event: ${event.title}", Toast.LENGTH_SHORT).show()
        }

        transportAdapter = TransportAdapter(arrayListOf()) { transport ->
            Toast.makeText(this, "Transport: ${transport.routename}", Toast.LENGTH_SHORT).show()
        }

        // Default RecyclerView setup
        binding.eventsRecyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.eventsRecyclerView.adapter = eventAdapter

        // Pull to refresh
        binding.swipeRefresh.setOnRefreshListener {
            Log.d("MainActivity", "🔄 Refreshing...")
            setupRealtimeListener()
        }

        binding.fabAddEvent.setOnClickListener {
            addEventLauncher.launch(Intent(this, AddEventActivity::class.java))
        }

        // ✅ FIXED: Bottom Navigation Setup
        setupBottomNavigation()

        // Filter Chips
        binding.chipAll.setOnClickListener {
            currentFilter = "All"
            Log.d("MainActivity", "🔹 Filter: All")
            binding.eventsRecyclerView.layoutManager = GridLayoutManager(this, 2)
            binding.eventsRecyclerView.adapter = eventAdapter
            setupRealtimeListener()
        }

        binding.chipEvents.setOnClickListener {
            currentFilter = "Events"
            Log.d("MainActivity", "🔹 Filter: Events")
            binding.eventsRecyclerView.layoutManager = GridLayoutManager(this, 2)
            binding.eventsRecyclerView.adapter = eventAdapter
            setupRealtimeListener()
        }

        binding.chipTransport.setOnClickListener {
            currentFilter = "Transport"
            Log.d("MainActivity", "🔹 Filter: Transport")
            binding.eventsRecyclerView.layoutManager = LinearLayoutManager(this)
            binding.eventsRecyclerView.adapter = transportAdapter
            setupRealtimeListener()
        }

        binding.chipLostFound.setOnClickListener {
            currentFilter = "Lost & Found"
            Log.d("MainActivity", "🔹 Filter: Lost & Found")
            binding.eventsRecyclerView.layoutManager = GridLayoutManager(this, 2)
            binding.eventsRecyclerView.adapter = eventAdapter
            setupRealtimeListener()
        }

        // Initial load
        binding.progressBar.visibility = View.VISIBLE
        setupRealtimeListener()
    }

    // ✅ FIXED: Bottom navigation without finish() calls
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Already on home, do nothing
                    true
                }
                R.id.nav_search -> {
                    startActivity(Intent(this, SearchActivity::class.java))
                    overridePendingTransition(0, 0)
                    true // ✅ Return true, but DON'T call finish()
                }
                R.id.nav_add -> {
                    addEventLauncher.launch(Intent(this, AddEventActivity::class.java))
                    false // Keep this false since it's a dialog/overlay
                }
                R.id.nav_notifications -> {
                    startActivity(Intent(this, NotificationsActivity::class.java))
                    overridePendingTransition(0, 0)
                    true // ✅ Return true, but DON'T call finish()
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(0, 0)
                    true // ✅ Return true, but DON'T call finish()
                }
                else -> false
            }
        }

        // ✅ Handle reselection (clicking same tab again)
        binding.bottomNavigation.setOnItemReselectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Scroll to top or refresh
                    binding.eventsRecyclerView.smoothScrollToPosition(0)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "↩️ onResume called")
        // ✅ Ensure home is selected when returning
        binding.bottomNavigation.selectedItemId = R.id.nav_home
    }

    override fun onDestroy() {
        super.onDestroy()
        firestoreListener?.remove()
        Log.d("MainActivity", "🛑 Firestore listener removed")
    }

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
                // ✅ Convert List to ArrayList for adapter
                eventAdapter.updateData(ArrayList(events))
                binding.progressBar.visibility = View.GONE
                Log.d("MainActivity", "✅ Loaded ${events.size} events")
            }
        }
    }

    private fun loadTransportData() {
        firestoreListener = db.collection("busRoutes")
            .whereEqualTo("active", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                binding.swipeRefresh.isRefreshing = false

                if (error != null) {
                    Log.e("MainActivity", "❌ Error loading transport: ${error.message}")
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Error loading transport: ${error.message}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    Log.d("MainActivity", "📄 Found ${snapshot.documents.size} transport documents")

                    val transportItems = snapshot.documents.mapNotNull { doc ->
                        try {
                            Transport(
                                id = doc.id,
                                routename = doc.getString("routename") ?: "Unknown Route",
                                source = doc.getString("source") ?: "",
                                destination = doc.getString("destination") ?: "",
                                departureTime = doc.getString("departureTime") ?: "",
                                arrivalTime = doc.getString("arrivalTime") ?: "", // ✅ Changed to non-nullable
                                dayType = doc.getString("dayType") ?: "",
                                active = doc.getBoolean("active") ?: true,
                                createdBy = doc.getString("createdBy") ?: "",
                                createdAt = doc.getTimestamp("createdAt")?.seconds ?: 0L // ✅ Convert to Long
                            )
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Error parsing transport: ${e.message}")
                            null
                        }
                    }
                    // ✅ Convert List to ArrayList for adapter
                    transportAdapter.updateData(ArrayList(transportItems))
                    binding.progressBar.visibility = View.GONE
                    Log.d("MainActivity", "✅ Loaded ${transportItems.size} transport items")

                    if (transportItems.isEmpty()) {
                        Toast.makeText(this, "No transport routes found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun loadLostFoundData() {
        firestoreListener = db.collection("lostAndFound")
            .whereEqualTo("status", "active")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                binding.swipeRefresh.isRefreshing = false
                binding.progressBar.visibility = View.GONE

                if (error != null) {
                    Log.e("MainActivity", "❌ Error loading lost & found: ${error.message}")
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
                    // ✅ Convert List to ArrayList for adapter
                    eventAdapter.updateData(ArrayList(lostFoundEvents))
                    Log.d("MainActivity", "✅ Loaded ${lostFoundEvents.size} lost items")
                }
            }
    }

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
