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

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainRootLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        updateBalance()
        setupProgressBar()
        setupNavigation()
        resetUiForLogic()
    }

    private fun resetUiForLogic() {
        findViewById<ProgressBar>(R.id.pbBadgeProgress).progress = 0
        findViewById<ProgressBar>(R.id.pbBudgetHealth).progress = 0
        findViewById<TextView>(R.id.tvBalanceAmount).text = "R 0.00"
        Log.d("SmartSpend", "UI components reset for dynamic logic initialization")
    }

    private fun updateBalance() {
        val db = DatabaseHelper(this)
        val prefs = getSharedPreferences("SmartSpendPrefs", MODE_PRIVATE)

        val totalBudget = prefs.getFloat("BUDGET_MAX", 0f)
        val totalSpent = db.getTotalSpent("") // The method we added to DatabaseHelper

        val availableBalance = totalBudget - totalSpent

        val tvBalance = findViewById<TextView>(R.id.tvBalanceAmount)
        tvBalance.text = "R ${String.format("%.2f", availableBalance)}"
    }

    private fun setupProgressBar() {
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

    private fun setupNavigation() {
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