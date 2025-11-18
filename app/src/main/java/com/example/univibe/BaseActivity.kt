package com.example.univibe

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

abstract class BaseActivity : AppCompatActivity() {

    protected fun setupBottomNavigation(bottomNav: BottomNavigationView, currentItemId: Int) {
        // Set the current item as selected
        bottomNav.selectedItemId = currentItemId

        bottomNav.setOnItemSelectedListener { menuItem ->
            // If clicking the current item, do nothing
            if (menuItem.itemId == currentItemId) {
                return@setOnItemSelectedListener true
            }

            when (menuItem.itemId) {
                R.id.nav_home -> {
                    if (this !is MainActivity) {  // ✅ Check if not already on MainActivity
                        navigateToActivity(MainActivity::class.java)
                    }
                    true
                }
                R.id.nav_search -> {
                    if (this !is SearchActivity) {
                        navigateToActivity(SearchActivity::class.java)
                    }
                    true
                }
                R.id.nav_add -> {
                    // ✅ FIXED: Use startActivity without finish()
                    startActivity(Intent(this, AddEventActivity::class.java))
                    overridePendingTransition(0, 0)
                    false  // Return false so bottom nav doesn't get selected
                }
                R.id.nav_notifications -> {
                    if (this !is NotificationsActivity) {
                        navigateToActivity(NotificationsActivity::class.java)
                    }
                    true
                }
                R.id.nav_profile -> {
                    if (this !is ProfileActivity) {
                        navigateToActivity(ProfileActivity::class.java)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun navigateToActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        // ✅ Clear the back stack and create a new task
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(0, 0)  // No animation for smooth transition
    }
}
