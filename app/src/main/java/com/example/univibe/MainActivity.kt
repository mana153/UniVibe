package com.example.univibe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.FloatingActionButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.univibe.adapters.EventAdapter
import com.example.univibe.models.Event
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {

    private val db = Firebase.firestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var eventAdapter: EventAdapter
    private val eventList = mutableListOf<Event>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.events_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        eventAdapter = EventAdapter(eventList)
        recyclerView.adapter = eventAdapter

        // Floating Action Button to add new events
        findViewById<FloatingActionButton>(R.id.fab_add_event).setOnClickListener {
            val intent = Intent(this, AddEventActivity::class.java)
            startActivity(intent)
        }

        loadEvents()
    }

    private fun loadEvents() {
        eventList.clear()

        db.collection("events")
            .orderBy("date")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val event = document.toObject(Event::class.java)
                    eventList.add(event)
                }
                eventAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w("MainActivity", "Error getting documents: ", exception)
            }
    }

    override fun onResume() {
        super.onResume()
        // Reload events when returning to MainActivity
        loadEvents()
    }
}
