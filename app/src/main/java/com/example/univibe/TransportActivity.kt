package com.example.univibe

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.univibe.adapters.TransportAdapter
import com.example.univibe.databinding.ActivityTransportBinding
import com.example.univibe.models.Transport
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class TransportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransportBinding
    private val db = FirebaseFirestore.getInstance()

    private lateinit var transportAdapter: TransportAdapter
    private var firestoreListener: ListenerRegistration? = null

    private var isListenerAttached = false
    private val TAG = "TransportActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupSwipeRefresh()
        setupBottomNavigation()
        loadTransportData()      // load ONCE
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Transport Routes"
        }
    }

    private fun setupRecyclerView() {
        transportAdapter = TransportAdapter(arrayListOf()) { transport ->
            Toast.makeText(this, "Route: ${transport.routename}", Toast.LENGTH_SHORT).show()
        }

        binding.rvTransport.apply {
            layoutManager = LinearLayoutManager(this@TransportActivity)
            adapter = transportAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            Log.d(TAG, "🔄 Swipe refresh triggered")

            // Do NOT reload Firestore listener — snapshot listener already auto-updates data
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_home

        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> { finish(); true }
                R.id.nav_search -> true
                R.id.nav_add -> false
                R.id.nav_notifications -> true
                R.id.nav_profile -> true
                else -> false
            }
        }
    }

    private fun loadTransportData() {

        if (isListenerAttached) {
            Log.d(TAG, "📌 Firestore listener already attached, skipping reattach")
            return
        }

        showLoading(true)
        isListenerAttached = true

        firestoreListener = db.collection("busRoutes")
            .whereEqualTo("active", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                binding.swipeRefresh.isRefreshing = false

                if (error != null) {
                    Log.e(TAG, "❌ Error loading: ${error.message}")
                    showLoading(false)
                    updateEmptyState(true)
                    return@addSnapshotListener
                }

                val transportItems = ArrayList<Transport>()

                snapshot?.documents?.forEach { doc ->
                    try {
                        val transport = Transport(
                            id = doc.id,
                            routename = doc.getString("routename") ?: "Unknown Route",
                            source = doc.getString("source") ?: "",
                            destination = doc.getString("destination") ?: "",
                            departureTime = doc.getString("departureTime") ?: "",
                            arrivalTime = doc.getString("arrivalTime") ?: "",
                            dayType = doc.getString("dayType") ?: "",
                            active = doc.getBoolean("active") ?: true,
                            createdBy = doc.getString("createdBy") ?: "",
                            createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L
                        )
                        transportItems.add(transport)

                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Parse error: ${e.message}")
                    }
                }

                transportAdapter.updateData(transportItems)
                updateEmptyState(transportItems.isEmpty())
                showLoading(false)
            }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.rvTransport.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.tvEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvTransport.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        firestoreListener?.remove()
        Log.d(TAG, "🛑 Firestore listener removed")
    }
}
