package com.example.univibe

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.univibe.databinding.ActivityAddEventBinding
import com.example.univibe.models.Event
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEventBinding
    private val db = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // Show preview
            Glide.with(this).load(it).into(binding.ivEventImagePreview)
            binding.ivEventImagePreview.visibility = View.VISIBLE
        }
    }

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

        // Image picker
        binding.btnPickImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

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

        // Upload image if selected
        if (selectedImageUri != null) {
            val storageRef = storage.reference.child("events/${System.currentTimeMillis()}_${selectedImageUri!!.lastPathSegment}")
            storageRef.putFile(selectedImageUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        saveEventToFirestore(title, description, eventTimestamp, location, category, downloadUrl.toString())
                    }.addOnFailureListener { e ->
                        onCreateEventFailed(e.message)
                    }
                }
                .addOnFailureListener { e -> onCreateEventFailed(e.message) }
        } else {
            // Save without image
            saveEventToFirestore(title, description, eventTimestamp, location, category, "")
        }
    }

    private fun saveEventToFirestore(title: String, description: String, timestamp: Timestamp, location: String, category: String, imageUrl: String) {
        try {
            val newEvent = Event(
                id = "",
                title = title,
                description = description,
                date = timestamp,
                imageUrl = imageUrl,
                location = location,
                createdBy = "anon_user_123",
                category = category,
                createdAt = Timestamp.now()
            )

            db.collection("events")
                .add(newEvent)
                .addOnSuccessListener {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSaveEvent.isEnabled = true
                    Toast.makeText(this, "Event created successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    onCreateEventFailed(e.message)
                }
        } catch (e: Exception) {
            onCreateEventFailed("Error: ${e.message}")
        }
    }

    private fun onCreateEventFailed(message: String?) {
        binding.progressBar.visibility = View.GONE
        binding.btnSaveEvent.isEnabled = true
        Toast.makeText(this, "Failed to create event: ${message ?: "Unknown error"}", Toast.LENGTH_LONG).show()
    }
}
