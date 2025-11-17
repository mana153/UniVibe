package com.example.univibe

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.univibe.models.Event
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddEventActivity : AppCompatActivity() {

    private val db = Firebase.firestore

    private lateinit var titleEt: EditText
    private lateinit var descriptionEt: EditText
    private lateinit var dateEt: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)

        // Initialize views
        titleEt = findViewById(R.id.et_event_title)
        descriptionEt = findViewById(R.id.et_event_description)
        dateEt = findViewById(R.id.et_event_date)
        saveButton = findViewById(R.id.btn_save_event)

        // Set up action bar title
        supportActionBar?.title = "Add New Event"

        // Make EditText non-editable - force user to use picker
        dateEt.isFocusable = false
        dateEt.isClickable = true

        // ADD THIS: Date picker dialog when clicking on date field
        dateEt.setOnClickListener {
            showDateTimePicker()
        }

        saveButton.setOnClickListener {
            saveEvent()
        }
    }

    // ADD THIS NEW FUNCTION: Shows date picker, then time picker
    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // First show date picker
        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            // After date is selected, show time picker
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                // Format and set the selected date and time
                val dateString = String.format(
                    Locale.getDefault(),
                    "%04d/%02d/%02d %02d:%02d",
                    selectedYear,
                    selectedMonth + 1, // Month is 0-indexed
                    selectedDay,
                    selectedHour,
                    selectedMinute
                )
                dateEt.setText(dateString)
            }, hour, minute, true).show() // true = 24-hour format
        }, year, month, day).show()
    }

    private fun saveEvent() {
        val title = titleEt.text.toString().trim()
        val description = descriptionEt.text.toString().trim()
        val dateString = dateEt.text.toString().trim()

        if (title.isEmpty() || description.isEmpty() || dateString.isEmpty()) {
            Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        // IMPROVED: Better error handling for date parsing
        val eventTimestamp = try {
            val format = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
            Timestamp(format.parse(dateString) ?: throw Exception("Invalid date"))
        } catch (e: Exception) {
            Toast.makeText(this, "Invalid Date Format. Please select date again.", Toast.LENGTH_LONG).show()
            return // Exit instead of saving with wrong date
        }

        // Create the Event object
        val newEvent = Event(
            title = title,
            description = description,
            date = eventTimestamp,
            createdBy = "anon_user_123",
            category = "General"
        )

        // Save to Firestore
        db.collection("events")
            .add(newEvent)
            .addOnSuccessListener {
                Toast.makeText(this, "Event added successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error adding event: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
