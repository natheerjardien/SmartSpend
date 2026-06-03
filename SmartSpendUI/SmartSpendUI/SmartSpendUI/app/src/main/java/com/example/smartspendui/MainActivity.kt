package com.example.smartspendui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide

// we created this class as the main dashboard for the application
class MainActivity : AppCompatActivity() {

    private val dbHelper = DatabaseHelper()
    private var currentUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main) // we linked the activity to the main dashboard layout

        // we handled system window insets to ensure the ui respects edge-to-edge display settings
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainRootLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // we initialized the basic ui state and set up the button navigation logic
        resetUiForLogic()
        setupNavigation()
    }

    override fun onResume() { // we refreshed the financial data every time the user returned to the home screen
        super.onResume()

        val prefs = getSharedPreferences("SmartSpendPrefs", MODE_PRIVATE)
        currentUserId = prefs.getString("CURRENT_USER_ID", "") ?: ""

        updateBalance()
        setupProgressBar()
        fetchAndSetUserProfile()
    }

    private fun resetUiForLogic() { // we cleared the ui components to prepare them for dynamic data loading
        findViewById<ProgressBar>(R.id.pbBadgeProgress).progress = 0
        findViewById<ProgressBar>(R.id.pbBudgetHealth).progress = 0
        findViewById<TextView>(R.id.tvBalanceAmount).text = "R 0.00"
        Log.d("SmartSpend", "UI components reset for dynamic logic initialization")
    }

    private fun updateBalance() { // we calculated and displayed the remaining budget based on total spending
        val tvBalance = findViewById<TextView>(R.id.tvBalanceAmount)
        val pbHome = findViewById<ProgressBar>(R.id.pbBudgetHealth)

        if (currentUserId.isEmpty())
        {
            return
        }

        dbHelper.getUserProfile(currentUserId) { snapshot ->
            if (snapshot.exists()) {
                val totalIncome = snapshot.child("totalIncome").value?.toString()?.toDoubleOrNull() ?: 0.0

                dbHelper.getTotalSpent("") { totalSpent ->

                    val availableBalance = totalIncome - totalSpent

                    runOnUiThread {
                        tvBalance.text = "R ${String.format("%.2f", availableBalance)}"

                        if (totalIncome > 0) {
                            pbHome.progress = ((totalSpent / totalIncome) * 100).toInt()
                        } else {
                            pbHome.progress = 0
                        }
                    }
                }
            }
        }
    }

    private fun setupProgressBar() { // we updated the budget health progress bar to reflect the current spending percentage
        val db = DatabaseHelper()
        val prefs = getSharedPreferences("SmartSpendPrefs", MODE_PRIVATE)
        val maxGoal = prefs.getFloat("BUDGET_MAX", 0f)


        val pbHome = findViewById<ProgressBar>(R.id.pbBudgetHealth)

        db.getTotalSpent("") { totalSpent ->
            if (maxGoal > 0)
            {
                pbHome.progress = ((totalSpent / maxGoal) * 100).toInt()
            }
            else
            {
                pbHome.progress = 0
            }
        }

    }

    private fun setupNavigation() { // we configured click listeners to navigate to the various modules of the app
        val btnEntry = findViewById<Button>(R.id.btnNavEntry)
        val btnBudget = findViewById<Button>(R.id.btnNavBudget)
        val btnAnalytic = findViewById<Button>(R.id.btnNavAnalytic)
        val ivProfileImage = findViewById<ImageView>(R.id.ivHeaderProfilePic)

        ivProfileImage.setOnClickListener {
            Log.d("SmartSpend", "Navigating to ProfileActivity via avatar tap event listener thread")
            val intent = Intent(this, ProfileActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        btnEntry.setOnClickListener {
            Log.d("SmartSpend", "Navigating to ExpenseEntryActivity")
            val intent = Intent(this, ExpenseEntryActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        btnBudget.setOnClickListener {
            Log.d("SmartSpend", "Navigating to BudgetActivity")
            val intent = Intent(this, BudgetActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        btnAnalytic.setOnClickListener {
            Log.d("SmartSpend", "Navigating to AnalyticsActivity")
            val intent = Intent(this, AnalyticsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }

    private fun fetchAndSetUserProfile() {
        val tvGreeting = findViewById<TextView>(R.id.tvHeaderGreeting)
        val ivProfileImage = findViewById<ImageView>(R.id.ivHeaderProfilePic)

        if (currentUserId.isNotEmpty()) {
            dbHelper.getUserProfile(currentUserId) { snapshot ->
                if (snapshot.exists()) {
                    val firstName = snapshot.child("firstName").value?.toString() ?: "User"
                    val profileImageUrl = snapshot.child("profileImageUrl").value?.toString() ?: ""

                    runOnUiThread {
                        // Personalize greeting text dynamically using the user's first name
                        tvGreeting.text = "Hello, $firstName!"

                        // Stream profile picture securely via Glide avoiding lifecycle permission faults
                        if (profileImageUrl.isNotEmpty() && ivProfileImage != null) {
                            Glide.with(this@MainActivity)
                                .load(profileImageUrl)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.ic_menu_gallery)
                                .circleCrop() // Renders profile pictures cleanly inside a round frame
                                .into(ivProfileImage)
                        }
                    }
                }
            }
        }
    }
}