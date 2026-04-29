package com.example.smartspendui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnGoToRegister)

        val db = DatabaseHelper(this)

        val etPass = findViewById<EditText>(R.id.etPassword)
        val etUser = findViewById<EditText>(R.id.etUsername)

        btnLogin.setOnClickListener {

            val users = db.getAllUsers()

            val inputUser = etUser.text.toString()
            val inputPass = etPass.text.toString()


            var isValidUser = false

            if (users.moveToFirst()) {
                do {
                    val username = users.getString(users.getColumnIndexOrThrow("username"))
                    val password = users.getString(users.getColumnIndexOrThrow("password"))

                    if (username == inputUser && password == inputPass) {

                        // Valid login
                        isValidUser = true
                        break

                    }
                } while (users.moveToNext())
            }

            users.close()

            if (isValidUser) {

                Log.d("SmartSpend", "Login success")

                startActivity(Intent(this, SplashActivity::class.java))
                finish()

            }
            else
            {
                Log.d("SmartSpend", "Invalid login attempted")

                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
            }


        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}