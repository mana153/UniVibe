package com.example.univibe

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.univibe.databinding.ActivityAddEventBinding
import com.example.univibe.models.Event
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class AddEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEventBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Event"

        // Category spinner
        val categories = listOf("Events", "Transport", "Lost & Found")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter

        // Date/time picker
        binding.etEventDate.isFocusable = false
        binding.etEventDate.setOnClickListener { showDateTimePicker() }

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
        val category = binding.spinnerCategory.selectedItem.toString()

        // Validation
        if (title.isEmpty() || description.isEmpty() || dateStr.isEmpty() || location.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Parse date
        val eventTimestamp = try {
            val format = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
            Timestamp(format.parse(dateStr)!!)
        } catch (e: Exception) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loader
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSaveEvent.isEnabled = false

        // Save without image (SIMPLIFIED)
        saveEventToFirestore(title, description, eventTimestamp, location, category)
    }

    private fun saveEventToFirestore(title: String, description: String, timestamp: Timestamp, location: String, category: String) {
        val eventId = UUID.randomUUID().toString()

        try {
            val newEvent = hashMapOf(
                "id" to eventId,
                "title" to title,
                "description" to description,
                "date" to timestamp,
                "imageUrl" to "",
                "location" to location,
                "createdBy" to "user_123",
                "category" to category,
                "createdAt" to Timestamp.now()
            )

            Log.d("AddEvent", "Saving event: $newEvent")

            db.collection("events")
                .document(eventId)
                .set(newEvent)
                .addOnSuccessListener {
                    Log.d("AddEvent", "✅ Event created successfully!")
                    binding.progressBar.visibility = View.GONE
                    binding.btnSaveEvent.isEnabled = true
                    Toast.makeText(this, "Event created successfully!", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)  // Tell MainActivity to refresh
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.e("AddEvent", "❌ Failed to save", e)
                    binding.progressBar.visibility = View.GONE
                    binding.btnSaveEvent.isEnabled = true
                    Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } catch (e: Exception) {
            Log.e("AddEvent", "Error creating event", e)
            binding.progressBar.visibility = View.GONE
            binding.btnSaveEvent.isEnabled = true
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    Log.d("AddEventActivity", "✅ Creating event: $title | Category: $category | Date: $dateStr")

    .addOnSuccessListener {
        Log.d("AddEventActivity", "✅ Event saved to Firestore with ID: ${it.id}")
        binding.progressBar.visibility = View.GONE
        binding.btnSaveEvent.isEnabled = true
        Toast.makeText(this, "Event created successfully!", Toast.LENGTH_SHORT).show()

        override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
