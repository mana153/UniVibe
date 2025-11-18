package com.example.univibe

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.univibe.databinding.ActivityAddEventBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class AddEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEventBinding
    private val db = FirebaseFirestore.getInstance()
    private var selectedCategory: String = "Events"  // Default category

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar setup
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Event"

        // Back button
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // Category spinner setup
        val categories = listOf("Events", "Transport", "Lost & Found")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter

        // Listen for spinner selection to update selectedCategory
        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCategory = parent.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        // Date/time picker setup
        binding.etEventDate.isFocusable = false
        binding.etEventDate.setOnClickListener { showDateTimePicker() }

        // Save button click listener
        binding.btnSaveEvent.setOnClickListener {
            createEvent()
        }
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                val dateString = String.format(
                    Locale.getDefault(),
                    "%04d/%02d/%02d %02d:%02d",
                    selectedYear,
                    selectedMonth + 1,
                    selectedDay,
                    selectedHour,
                    selectedMinute
                )
                binding.etEventDate.setText(dateString)
            }, hour, minute, true).show()
        }, year, month, day).show()
    }

    private fun createEvent() {
        val title = binding.etEventTitle.text.toString().trim()
        val description = binding.etEventDescription.text.toString().trim()
        val dateStr = binding.etEventDate.text.toString().trim()
        val location = binding.etEventLocation.text.toString().trim()
        val category = selectedCategory

        // Validation
        if (title.isEmpty() || description.isEmpty() || dateStr.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Parse date string into Timestamp
        val eventTimestamp = try {
            val format = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
            Timestamp(format.parse(dateStr)!!)
        } catch (e: Exception) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show()
            return
        }

        // Show progress loader and disable save button
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSaveEvent.isEnabled = false

        // Save event data to Firestore
        saveEventToFirestore(title, description, eventTimestamp, location, category)
    }

    private fun saveEventToFirestore(
        title: String,
        description: String,
        timestamp: Timestamp,
        location: String,
        category: String
    ) {
        val eventId = UUID.randomUUID().toString()
        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        val dateString = dateFormat.format(timestamp.toDate())

        val newEvent = hashMapOf(
            "id" to eventId,
            "title" to title,
            "description" to description,
            "timestamp" to timestamp,
            "date" to dateString,
            "imageUrl" to "",
            "location" to location,
            "createdBy" to "admin",
            "category" to category,
            "createdAt" to Timestamp.now()
        )

        Log.d("AddEvent", "Saving event: $newEvent")

        db.collection("events")
            .document(eventId)
            .set(newEvent)
            .addOnSuccessListener {
                Log.d("AddEvent", "Event created successfully!")
                binding.progressBar.visibility = View.GONE
                binding.btnSaveEvent.isEnabled = true
                Toast.makeText(this, "Event created successfully!", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("AddEvent", "Failed to save event", e)
                binding.progressBar.visibility = View.GONE
                binding.btnSaveEvent.isEnabled = true
                Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
