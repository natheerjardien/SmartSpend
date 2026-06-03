package com.example.smartspendui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Log.e
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase

class ProfileActivity : AppCompatActivity() {

    private lateinit var ivLargeProfilePic: ImageView
    private lateinit var etProfileUsername: EditText
    private lateinit var etProfileNewPassword: EditText

    private lateinit var btnBack: ImageButton

    private val database = FirebaseDatabase.getInstance("https://smartspendui-default-rtdb.asia-southeast1.firebasedatabase.app").reference

    private val dbHelper = DatabaseHelper()
    private var selectedImageUri: Uri? = null

    private var currentUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_activity)

        ivLargeProfilePic = findViewById(R.id.ivLargeProfilePic)
        etProfileUsername = findViewById(R.id.etProfileUsername)
        etProfileNewPassword = findViewById(R.id.etProfileNewPassword)
        btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnToggleTheme = findViewById<Button>(R.id.btnToggleTheme)

        // Read active session parameters from local SharedPreferences caching
        val prefs = getSharedPreferences("SmartSpendPrefs", MODE_PRIVATE)
        currentUserId = prefs.getString("CURRENT_USER_ID", "") ?: ""
        val cachedUsername = prefs.getString("USER_NAME", "User")

        etProfileUsername.setText(cachedUsername)

        // Fetch fresh user profile records from our cloud
        if (currentUserId.isNotEmpty()) {
            database.child("users").child(currentUserId).get().addOnSuccessListener { snapshot ->
                runOnUiThread {
                    val dbUsername = snapshot.child("username").value?.toString()
                    if (!dbUsername.isNullOrEmpty()) {
                        etProfileUsername.setText(dbUsername)
                    }

                    val profileImageUrl = snapshot.child("profileImageUrl").value?.toString() ?: ""
                    if (profileImageUrl.isNotEmpty()) {
                        selectedImageUri = Uri.parse(profileImageUrl)

                        // uses Glide instead of raw setImageURI to keep permissions securely across app reboots
                        Glide.with(this@ProfileActivity) // we loaded the profile photo securely using glide to ensure smooth rendering across app sessions
                            .load(selectedImageUri)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_gallery)
                            .into(ivLargeProfilePic)

                        ivLargeProfilePic.imageTintList = null
                    }
                }
            }.addOnFailureListener { e ->
                Log.e("SmartSpend", "Failed to fetch user profile data from cloud: ${e.message}")
            }
        }

        // Action selector to open the phone gallery
        findViewById<TextView>(R.id.tvChangePhoto).setOnClickListener {
            val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickIntent, 400)
        }

        // Image removal logic
        findViewById<TextView>(R.id.tvRemovePhoto).setOnClickListener { // we handled the profile picture removal request and cleared the reference string from cloud tables
            selectedImageUri = null
            ivLargeProfilePic.setImageResource(android.R.drawable.ic_menu_gallery)
            ivLargeProfilePic.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#5EC7D1"))

            if (currentUserId.isNotEmpty()) {
                database.child("users").child(currentUserId).child("profileImageUrl").setValue("")
                    .addOnSuccessListener {
                        dbHelper.updateProfileImage(currentUserId, "") { success ->
                            if (success) {
                                runOnUiThread { Toast.makeText(applicationContext, "Profile picture removed", Toast.LENGTH_SHORT).show() }
                            }
                        }
                    }
            }
        }

        findViewById<Button>(R.id.btnSaveProfileChanges).setOnClickListener { // we validated username fields, hashed new password entries and synchronized updates with the remote server database
            val inputName = etProfileUsername.text.toString().trim()
            val inputPassword = etProfileNewPassword.text.toString().trim()

            if (inputName.isEmpty()) {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentUserId.isNotEmpty()) {
                val updatesMap = mutableMapOf<String, Any>()
                updatesMap["username"] = inputName
                if (inputPassword.isNotEmpty()) {
                    val hashedSecurePass = RegisterActivity.SecurityUtils.hashPassword(inputPassword)
                    updatesMap["password"] = hashedSecurePass
                }

                dbHelper.updateUserProfileFields(currentUserId, updatesMap) { isSuccess ->
                    runOnUiThread {
                        if (isSuccess) {
                            val editPrefs = getSharedPreferences("SmartSpendPrefs", MODE_PRIVATE)
                            editPrefs.edit().putString("USER_NAME", inputName).apply()

                            Toast.makeText(applicationContext, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                            etProfileNewPassword.text.clear()
                            navigateHome()
                        } else {
                            Toast.makeText(applicationContext, "Database synchronization failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Error: User session token not found", Toast.LENGTH_SHORT).show()
            }
        }

        btnToggleTheme.setOnClickListener { // we toggled the user theme setting preference flag and applied night or light configurations immediately
            val currentPrefs = getSharedPreferences("SmartSpendPrefs", MODE_PRIVATE)
            val isDarkModeActive = currentPrefs.getBoolean("IS_DARK_MODE", false)

            val nextThemeSetting = !isDarkModeActive
            currentPrefs.edit().putBoolean("IS_DARK_MODE", nextThemeSetting).apply()

            if (nextThemeSetting) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                Toast.makeText(this, "Dark Mode Enabled", Toast.LENGTH_SHORT).show()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                Toast.makeText(this, "Light Mode Enabled", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnProfileLogout).setOnClickListener { // we cleared the local shared preferences session data and routed the user back to the login page screen
            val clearPrefs = getSharedPreferences("SmartSpendPrefs", MODE_PRIVATE)
            clearPrefs.edit().clear().apply()

            Log.d("SmartSpend", "Custom session cleared. Logging out.")

            val logoutRouteIntent = Intent(this, LoginActivity::class.java)
            logoutRouteIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(logoutRouteIntent)
            finish()
        }

        btnBack.setOnClickListener {
            // we closed the activity to return the user to the transaction history list
            Log.d("SmartSpend", "Navigating back to Home Screen.")
            finish()
        }
    }

    // we captured the selected gallery image stream, copied it into permanent local storage and updated the remote database path
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 400 && resultCode == RESULT_OK && data != null) {
            val temporaryUri = data.data ?: return

           // Nestsiarenka (2026) demonstrates how to safely store and open user files in android.

            try{
                val inputStream = contentResolver.openInputStream(temporaryUri)
                val permanentFile = java.io.File(filesDir, "profile_${currentUserId}.jpg")
                val outputStream = java.io.FileOutputStream(permanentFile)

                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()

                val persistentPath = permanentFile.absolutePath

                Glide.with(this)
                    .load(selectedImageUri)
                    .into(ivLargeProfilePic)

                ivLargeProfilePic.imageTintList = null

                dbHelper.updateProfileImage(currentUserId, persistentPath) { isSuccess ->
                    if (!isSuccess)
                    {
                        Log.e("SmartSpend", "Failed to update profile image")
                    }
                }
            }
            catch (e: Exception){
                Log.e("SmartSpend", "Error copying profile photo stream: ${e.message}")
                Toast.makeText(this, "Failed to process selected photo.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateHome() { // we created an intent to navigate back to the main activity dashboard while clearing the history stack layout
        val homeIntent = Intent(this, MainActivity::class.java)
        homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(homeIntent)
        finish()
    }
}
// Nestsiarenka, P., 2026.  How to Safely Store and Open User Files Locally in Android. (Version 2.0) [Source code]
// Available at: < https://proandroiddev.com/how-to-safely-store-and-open-user-files-locally-in-android-c823c3775cf6 > [Accessed 28 May 2026].