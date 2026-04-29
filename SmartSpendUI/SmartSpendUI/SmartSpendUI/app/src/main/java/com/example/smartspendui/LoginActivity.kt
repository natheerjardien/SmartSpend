package com.example.smartspendui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// we created this class to handle user authentication and session entry
class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page) // we linked the activity to the login xml layout

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnGoToRegister)

        val db = DatabaseHelper(this)

        val etPass = findViewById<EditText>(R.id.etPassword)
        val etUser = findViewById<EditText>(R.id.etUsername)

        btnLogin.setOnClickListener {

            val users = db.getAllUsers() // we fetched all registered users from the database to verify credentials

            val inputUser = etUser.text.toString()
            val inputPass = etPass.text.toString()


            var isValidUser = false

            // we iterated through the database cursor to check for a matching username and password pair
            if (users.moveToFirst()) {
                do {
                    val username = users.getString(users.getColumnIndexOrThrow("username"))
                    val password = users.getString(users.getColumnIndexOrThrow("password"))

                    if (username == inputUser && password == inputPass) {

                        // we marked the login as successful if a match was found
                        isValidUser = true
                        break

                    }
                } while (users.moveToNext())
            }

            users.close() // we closed the cursor to free up memory resources

            if (isValidUser)
            {

                Log.d("SmartSpend", "Login success")

                // we navigated the user to the splash screen upon successful authentication
                startActivity(Intent(this, SplashActivity::class.java))
                finish()

            }
            else
            {
                Log.d("SmartSpend", "Invalid login attempted")

                // we displayed an error message if the credentials did not match any records
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
            }


        }

        btnRegister.setOnClickListener { // we provided a navigation path to the registration screen for new users
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
// AndroidKnowledge, 2025. How to Run Tests in Visual Studio Code: A Complete Guide. (Version 2.0) [Source code]
// Available at: < https://www.geeksforgeeks.org/android/how-to-create-google-sign-in-ui-using-android-studio/ > [Accessed 25 April 2026].