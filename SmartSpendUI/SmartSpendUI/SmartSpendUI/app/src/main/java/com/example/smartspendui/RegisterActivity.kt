package com.example.smartspendui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() { // we created this class to handle the registration of new user accounts

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_page) // we linked the activity to the registration xml layout

        val etUsername = findViewById<EditText>(R.id.etRegUsername)
        val etPassword = findViewById<EditText>(R.id.etRegPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etRegConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnBackToLogin = findViewById<Button>(R.id.btnBackToLogin)

        btnRegister.setOnClickListener {
            val user = etUsername.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            val confirmPass = etConfirmPassword.text.toString().trim()

            // we checked if any of the required input fields were left empty
            if (user.isEmpty() || pass.isEmpty() || confirmPass.isEmpty())
            {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                Log.e("SmartSpend", "Registration failed: Empty fields")
                return@setOnClickListener
            }

            // we verified that the password and confirmation password matched exactly
            if (pass != confirmPass)
            {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                Log.w("SmartSpend", "Registration failed: Password mismatch")
                return@setOnClickListener
            }

            saveUserToDatabase(user, pass) // we proceeded to save the validated user details to the database
        }

        btnBackToLogin.setOnClickListener { // we closed the activity to return the user to the previous login screen
            Log.d("SmartSpend", "Navigating back to Login page")
            finish()
        }
    }

    // we implemented a helper method to store user credentials in the database
    private fun saveUserToDatabase(user: String, pass: String) {
        Log.i("SmartSpend", "Saving new user $user to Database")

        Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show()

        val db = DatabaseHelper(this)
        db.addUser(user, pass)

        // we navigated the user back to the login screen after successful registration
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}