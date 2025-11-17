package com.example.univibe

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Find the "Join Now" button
        val btnJoinNow = findViewById<MaterialButton>(R.id.btn_join_now)

        // Set click listener to navigate to LoginActivity
        btnJoinNow.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Close splash activity so back button doesn't return here
        }
    }
}

