package com.example.smartspendui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

// we created this class as the main dashboard for the application
class MainActivity : AppCompatActivity() {
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
        updateBalance()
        setupProgressBar()
    }

    private fun resetUiForLogic() { // we cleared the ui components to prepare them for dynamic data loading
        findViewById<ProgressBar>(R.id.pbBadgeProgress).progress = 0
        findViewById<ProgressBar>(R.id.pbBudgetHealth).progress = 0
        findViewById<TextView>(R.id.tvBalanceAmount).text = "R 0.00"
        Log.d("SmartSpend", "UI components reset for dynamic logic initialization")
    }

    private fun updateBalance() { // we calculated and displayed the remaining budget based on total spending
        val db = DatabaseHelper(this)
        val prefs = getSharedPreferences("SmartSpendPrefs", MODE_PRIVATE)

        val totalBudget = prefs.getFloat("BUDGET_MAX", 0f)
        val totalSpent = db.getTotalSpent("") // The method we added to DatabaseHelper

        val availableBalance = totalBudget - totalSpent

        val tvBalance = findViewById<TextView>(R.id.tvBalanceAmount)
        tvBalance.text = "R ${String.format("%.2f", availableBalance)}"
    }

    private fun setupProgressBar() { // we updated the budget health progress bar to reflect the current spending percentage
        val db = DatabaseHelper(this)
        val prefs = getSharedPreferences("SmartSpendPrefs", MODE_PRIVATE)
        val maxGoal = prefs.getFloat("BUDGET_MAX", 0f)
        val totalSpent = db.getTotalSpent("")

        val pbHome = findViewById<ProgressBar>(R.id.pbBudgetHealth)

        if (maxGoal > 0)
        {
            pbHome.progress = ((totalSpent / maxGoal) * 100).toInt()
        }
        else
        {
            pbHome.progress = 0
        }
    }

    private fun setupNavigation() { // we configured click listeners to navigate to the various modules of the app
        val btnEntry = findViewById<Button>(R.id.btnNavEntry)
        val btnBudget = findViewById<Button>(R.id.btnNavBudget)
        val btnAnalytic = findViewById<Button>(R.id.btnNavAnalytic)

        btnEntry.setOnClickListener {
            Log.d("SmartSpend", "Navigating to ExpenseEntryActivity")
            val intent = Intent(this, ExpenseEntryActivity::class.java)
            startActivity(intent)
        }

        btnBudget.setOnClickListener {
            Log.d("SmartSpend", "Navigating to BudgetActivity")
            val intent = Intent(this, BudgetActivity::class.java)
            startActivity(intent)
        }

        btnAnalytic.setOnClickListener {
            Log.d("SmartSpend", "Navigating to AnalyticsActivity")
            val intent = Intent(this, AnalyticsActivity::class.java)
            startActivity(intent)
        }
    }
}