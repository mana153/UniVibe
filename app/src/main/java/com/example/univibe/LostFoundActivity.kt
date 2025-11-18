package com.example.univibe

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LostFoundActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lost_found)

        // Back button
        findViewById<android.widget.ImageView>(R.id.btn_back)?.setOnClickListener {
            finish()
        }

        // Show under construction message
        findViewById<TextView>(R.id.tv_empty)?.apply {
            text = "🚧 Under Construction 🚧\n\nLost & Found feature is coming soon!"
            visibility = android.view.View.VISIBLE
            textSize = 18f
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        }
    }
}
