package com.example.univibe

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Find the login button
        val btnLogin = findViewById<MaterialButton>(R.id.btn_login)

        // Set click listener to navigate to MainActivity
        btnLogin.setOnClickListener {
            // You can add login validation here later
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Close login activity so back button doesn't return here
        }
    }
}

