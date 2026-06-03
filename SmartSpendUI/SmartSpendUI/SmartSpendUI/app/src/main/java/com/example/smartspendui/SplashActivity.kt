package com.example.smartspendui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

// GeeksforGeeks (2023) demonstrates how to create a functional splash screen
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("SmartSpendPrefs", MODE_PRIVATE)
        val isDarkModeEnabled = prefs.getBoolean("IS_DARK_MODE", false)

        if (isDarkModeEnabled) { // ensures dark mode, when aciavted, shows on Splash page as well
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        Log.d("SmartSpend", "SplashActivity: Interface loading simulation started")

        Handler(Looper.getMainLooper()).postDelayed({
            Log.d("SmartSpend", "SplashActivity: Loading complete, moving to MainActivity")

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            finish()
        }, 3000)
    }
}

// GeeksforGeeks, 2023. Android | Creating a Splash Screen. (Version 2.0) [Source code]
// Available at: < https://www.geeksforgeeks.org/android/android-creating-a-splash-screen/ > [Accessed 25 April 2026].