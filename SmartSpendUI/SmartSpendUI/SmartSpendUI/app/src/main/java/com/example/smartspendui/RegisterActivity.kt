package com.example.smartspendui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_page)

        val etUsername = findViewById<EditText>(R.id.etRegUsername)
        val etPassword = findViewById<EditText>(R.id.etRegPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etRegConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnBackToLogin = findViewById<Button>(R.id.btnBackToLogin)

        btnRegister.setOnClickListener {
            val user = etUsername.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            val confirmPass = etConfirmPassword.text.toString().trim()

            if (user.isEmpty() || pass.isEmpty() || confirmPass.isEmpty())
            {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                Log.e("SmartSpend", "Registration failed: Empty fields")
                return@setOnClickListener
            }

            if (pass != confirmPass)
            {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                Log.w("SmartSpend", "Registration failed: Password mismatch")
                return@setOnClickListener
            }

            saveUserToDatabase(user, pass)
        }

        btnBackToLogin.setOnClickListener {
            Log.d("SmartSpend", "Navigating back to Login page")
            finish()
        }
    }

    private fun saveUserToDatabase(user: String, pass: String) {
        Log.i("SmartSpend", "Saving new user $user to Database")

        Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show()

        val db = DatabaseHelper(this)
        db.addUser(user, pass)

        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}