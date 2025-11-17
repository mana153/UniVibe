package com.example.univibe

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.univibe.adapters.EventAdapter2
import com.example.univibe.databinding.ActivityMainBinding
import com.example.univibe.models.Event
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FirebaseFirestore

class MainActivityFixed : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: EventAdapter2
    private var allEvents: MutableList<Event> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar
        setSupportActionBar(binding.appBarLayout.findViewById(R.id.tv_discover_title))

        // RecyclerView
        adapter = EventAdapter2(listOf()) { event ->
            // TODO: Open Event detail
        }
        binding.eventsRecyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.eventsRecyclerView.adapter = adapter

        // Pull to refresh
        binding.swipeRefresh.setOnRefreshListener { loadEvents() }

        // FAB
        binding.fabAddEvent.setOnClickListener {
            startActivity(Intent(this, AddEventActivity::class.java))
        }

        // Bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    true
                }
                R.id.nav_search -> {
                    startActivity(Intent(this, SearchActivity::class.java))
                    true
                }
                R.id.nav_add -> {
                    startActivity(Intent(this, AddEventActivity::class.java))
                    true
                }
                R.id.nav_messages -> {
                    startActivity(Intent(this, NotificationsActivity::class.java))
                    true
                }
                R.id.nav_notifications -> {
                    startActivity(Intent(this, NotificationsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Chips
        val chips = listOf(binding.chipAll, binding.chipEvents, binding.chipTransport, binding.chipLostFound)
        for (chip in chips) {
            chip.setOnClickListener { v ->
                val text = (v as Chip).text.toString()
                filterEvents(text)
            }
        }

        // Initial load
        binding.progressBar.visibility = View.VISIBLE
        loadEvents()
    }

    private fun loadEvents() {
        db.collection("events")
            .orderBy("date")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { it.toObject(Event::class.java) }
                allEvents.clear()
                allEvents.addAll(list)
                adapter.updateData(allEvents)
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
            }
    }

    private fun filterEvents(filter: String) {
        if (filter.equals(getString(R.string.tab_all), ignoreCase = true)) {
            adapter.updateData(allEvents)
            return
        }
        val filtered = allEvents.filter { it.category.equals(filter, ignoreCase = true) }
        adapter.updateData(filtered)
    }
}

