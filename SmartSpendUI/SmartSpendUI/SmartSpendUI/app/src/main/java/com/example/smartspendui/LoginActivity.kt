package com.example.smartspendui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// AndroidKnowledge (2025) demonstrates how to create a functional login class
// we created this class to handle user authentication and session entry
class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page) // we linked the activity to the login xml layout

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnGoToRegister)

        val db = DatabaseHelper()

        val etPass = findViewById<EditText>(R.id.etPassword)
        val etUser = findViewById<EditText>(R.id.etUsername)

        btnLogin.setOnClickListener {
            val inputUser = etUser.text.toString().trim()
            val inputPass = etPass.text.toString().trim()

            if (inputUser.isEmpty() || inputPass.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // We fetch all registered users asynchronously from the cloud database
            db.getAllUsers { usersList ->
                var isValidUser = false

                // we iterated through the collection to check for a matching username and password pair
                for (user in usersList) {
                    if (user.username == inputUser && user.password == inputPass) {
                        // we marked the login as successful if a match was found
                        isValidUser = true
                        break
                    }
                }

                if (isValidUser) {
                    Log.d("SmartSpend", "Login success")

                    // we navigated the user to the splash screen upon successful authentication
                    startActivity(Intent(this, SplashActivity::class.java))
                    finish()
                } else {
                    Log.d("SmartSpend", "Invalid login attempted")

                    // we displayed an error message if the credentials did not match any records
                    Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnRegister.setOnClickListener { // we provided a navigation path to the registration screen for new users
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
// AndroidKnowledge, 2025. Easy Login Page in Android Studio using Java – 5 Steps Only! (Version 2.0) [Source code]
// Available at: < https://www.geeksforgeeks.org/android/how-to-create-google-sign-in-ui-using-android-studio/ > [Accessed 25 April 2026].