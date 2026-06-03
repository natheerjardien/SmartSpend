package com.example.smartspendui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.security.MessageDigest

// Medium (2018) demonstrates how to create a functional login and register class
class RegisterActivity : AppCompatActivity() { // we created this class to handle the registration of new user accounts

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_page) // we linked the activity to the registration xml layout

        val etUsername = findViewById<EditText>(R.id.etRegUsername)
        val etPassword = findViewById<EditText>(R.id.etRegPassword)
        val etFirstName = findViewById<EditText>(R.id.etFirstName)
        val etLastName = findViewById<EditText>(R.id.etLastName)
        val etConfirmPassword = findViewById<EditText>(R.id.etRegConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnBackToLogin = findViewById<Button>(R.id.btnBackToLogin)


        btnRegister.setOnClickListener {
            val user = etUsername.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()


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

            saveUserToDatabase(user, pass, firstName, lastName) // we proceeded to save the validated user details to the database
        }

        btnBackToLogin.setOnClickListener { // we closed the activity to return the user to the previous login screen
            Log.d("SmartSpend", "Navigating back to Login page")
            finish()
        }
    }

    // we implemented a helper method to store user credentials in the database
    private fun saveUserToDatabase(user: String, pass: String, firstName: String, lastName: String) {
        Log.i("SmartSpend", "Saving new user $user to Database")

        val securedHashPassword = SecurityUtils.hashPassword(pass)

        val db = DatabaseHelper()
        db.addUser(user, securedHashPassword, firstName, lastName) { isSuccess ->
            runOnUiThread {
                if(isSuccess) {
                    Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show()
                    // we navigated the user back to the login screen after successful registration
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Registration failed. Cloud server error.", Toast.LENGTH_SHORT).show()
                    Log.e("SmartSpend", "Firebase authentication node push failed for $user")
                }
            }
        }


    }

    object SecurityUtils {
        fun hashPassword(password: String): String {
            return try {
                // Instantiate a MessageDigest engine running SHA-256
                val digest = MessageDigest.getInstance("SHA-256")
                val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))

                // Convert the raw bytes into a readable Hexadecimal string representation
                val hexString = StringBuilder()
                for (b in hashBytes) {
                    val hex = Integer.toHexString(0xff and b.toInt())
                    if (hex.length == 1) hexString.append('0')
                    hexString.append(hex)
                }
                hexString.toString()
            } catch (e: Exception) {
                android.util.Log.e("SmartSpendSecurity", "Error calculating password hash: ${e.message}")
                ""
            }
        }
    }
}

// Medium, 2018. How to Create User Interface Login & Register with Android Studio. (Version 2.0) [Source code]
// Available at: < https://medium.com/muhamadjalaludin/how-to-create-user-interface-login-register-with-android-studio-34da847b05b2 > [Accessed 25 April 2026].