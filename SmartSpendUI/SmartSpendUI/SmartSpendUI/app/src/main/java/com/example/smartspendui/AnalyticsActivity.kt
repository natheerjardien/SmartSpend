package com.example.smartspendui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AnalyticsActivity : AppCompatActivity() { // we created the class to handle the financial data visualization

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.analytics_page) // we linked the code to our analytics xml layout

        Log.d("SmartSpend", "AnalyticsActivity: Ready for category population")

        // we called functions to initialize buttons, lists, and progress bars
        setupNavigation()
        setupCategoryList()
        updateProgressBars()
    }

    private fun setupCategoryList() { // We set up the RecyclerView with a linear layout manager
        val rvCategories = findViewById<RecyclerView>(R.id.rvCategoryList)
        rvCategories.layoutManager = LinearLayoutManager(this)

        val db = DatabaseHelper(this)
        val cursor = db.getCategoryTotals()

        val adapter = CategoryAdapter(cursor) // We bind the database results to the RecyclerView using our custom adapter
        rvCategories.adapter = adapter

        Log.d("SmartSpend", "Category totals fetched from DB: ${cursor.count} items found")
    }

    private fun updateProgressBars() {
        val db = DatabaseHelper(this)
        val prefs = getSharedPreferences("SmartSpendPrefs", MODE_PRIVATE)

        // we retrieved the users budget goals from shared preferences
        val minGoal = prefs.getFloat("BUDGET_MIN", 0f)
        val maxGoal = prefs.getFloat("BUDGET_MAX", 1000f)

        val totalSpent = db.getTotalSpent("")

        val pbGoal = findViewById<ProgressBar>(R.id.pbMonthlyGoal)
        val tvStatus = findViewById<TextView>(R.id.tvGoalStatusText)

        val percent = if (maxGoal > 0) ((totalSpent / maxGoal) * 100).toInt() else 0
        pbGoal.progress = percent

        val healthStatus = when { // We evaluate the spending health based on the set budget limits
            totalSpent < minGoal -> "Under Budget (Excellent)"
            totalSpent <= maxGoal -> "Within Budget (Good)"
            else -> "Over Budget (Warning)"
        }

        // We update the UI text to show the formatted spending amount and status
        tvStatus.text = "Spent R${String.format("%.2f", totalSpent)} of R${String.format("%.2f", maxGoal)}\nStatus: $healthStatus"
        Log.d("SmartSpend", "Analytics Updated: Spent $totalSpent, Max $maxGoal")
    }

    private fun setupNavigation() { // We defined click listeners to navigate between the different app screens
        findViewById<Button>(R.id.btnViewLog)?.setOnClickListener {
            Log.d("SmartSpend", "Navigating from Analytics to Transaction History")
            startActivity(Intent(this, TransactionHistoryActivity::class.java))
        }

        findViewById<Button>(R.id.btnNavHome).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        findViewById<Button>(R.id.btnNavEntry).setOnClickListener {
            startActivity(Intent(this, ExpenseEntryActivity::class.java))
        }
        findViewById<Button>(R.id.btnNavBudget).setOnClickListener {
            startActivity(Intent(this, BudgetActivity::class.java))
        }
    }
}