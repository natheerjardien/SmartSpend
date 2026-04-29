package com.example.smartspendui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
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