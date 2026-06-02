package com.example.smartspendui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase

class ProfileActivity : AppCompatActivity() {

    private lateinit var ivLargeProfilePic: ImageView
    private lateinit var etProfileUsername: EditText
    private lateinit var etProfileNewPassword: EditText

    // Establishing references to Firebase SDK engines
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_activity)

        ivLargeProfilePic = findViewById(R.id.ivLargeProfilePic)
        etProfileUsername = findViewById(R.id.etProfileUsername)
        etProfileNewPassword = findViewById(R.id.etProfileNewPassword)

        // Read active session parameters to pre-populate text field views
        val currentUser = auth.currentUser
        if (currentUser != null) {
            etProfileUsername.setText(currentUser.username ?: "User")
        } else {
            // Local fallback layer if offline
            val prefs = getSharedPreferences("SmartSpendPrefs", MODE_PRIVATE)
            etProfileUsername.setText(prefs.getString("USER_NAME", "User"))
        }

        // Action selector to open the phone gallery
        findViewById<TextView>(R.id.tvChangePhoto).setOnClickListener {
            val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickIntent, 400)
        }

        // Image removal logic resets graphics to vector defaults
        findViewById<TextView>(R.id.tvRemovePhoto).setOnClickListener {
            selectedImageUri = null
            ivLargeProfilePic.setImageResource(android.R.drawable.ic_menu_gallery)
            ivLargeProfilePic.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#5EC7D1"))

            // Wipe photo location URL link properties on the database object map
            currentUser?.let { user ->
                database.child("users").child(user.uid).child("profileImageUrl").setValue("")
            }
            Toast.makeText(this, "Profile picture removed", Toast.LENGTH_SHORT).show()
        }

        // Commit profile field alterations to Firebase
        findViewById<Button>(R.id.btnSaveProfileChanges).setOnClickListener {
            val inputName = etProfileUsername.text.toString().trim()
            val inputPassword = etProfileNewPassword.text.toString().trim()

            if (inputName.isEmpty()) {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentUser != null) {
                // 1. Update Username on the core user instance profile
                val profileUpdates = userProfileChangeRequest {
                    displayName = inputName
                }

                currentUser.updateProfile(profileUpdates).addOnCompleteListener { profileTask ->
                    if (profileTask.isSuccessful) {
                        // 2. Synchronize username into Kyle's Realtime Database user record collection
                        database.child("users").child(currentUser.uid).child("username").setValue(inputName)

                        // Update local caching references
                        val prefs = getSharedPreferences("SmartSpendPrefs", MODE_PRIVATE)
                        prefs.edit().putString("USER_NAME", inputName).apply()

                        // 3. Update password if the user provided one
                        if (inputPassword.isNotEmpty()) {
                            currentUser.updatePassword(inputPassword).addOnCompleteListener { passTask ->
                                if (passTask.isSuccessful) {
                                    Toast.makeText(this, "Profile and Password updated successfully!", Toast.LENGTH_SHORT).show()
                                    navigateHome()
                                } else {
                                    Toast.makeText(this, "Password update failed: ${passTask.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                            navigateHome()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Error: User entity not found", Toast.LENGTH_SHORT).show()
            }
        }

        // Flush active authentication scopes and clean out stack arrays on logout
        findViewById<Button>(R.id.btnProfileLogout).setOnClickListener {
            auth.signOut()

            // Wipe standard SharedPreferences records
            val prefs = getSharedPreferences("SmartSpendPrefs", MODE_PRIVATE)
            prefs.edit().clear().apply()

            val logoutRouteIntent = Intent(this, LoginActivity::class.java)
            logoutRouteIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(logoutRouteIntent)
            finish()
        }
    }

    // Capture files grabbed out of native mobile media selections channels
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 400 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data
            ivLargeProfilePic.setImageURI(selectedImageUri)
            ivLargeProfilePic.imageTintList = null // strip vector default parameters to allow images to pass through

            // Update database user entity tracking links
            auth.currentUser?.let { user ->
                database.child("users").child(user.uid).child("profileImageUrl").setValue(selectedImageUri.toString())
            }
        }
    }

    private fun navigateHome() {
        val homeIntent = Intent(this, MainActivity::class.java)
        homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(homeIntent)
        finish()
    }
}