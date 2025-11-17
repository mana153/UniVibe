package com.example.univibe

import android.app.Application
import com.google.firebase.FirebaseApp

class UniVibeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
