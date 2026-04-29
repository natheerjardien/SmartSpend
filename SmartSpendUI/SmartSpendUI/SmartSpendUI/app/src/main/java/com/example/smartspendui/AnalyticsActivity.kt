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

class AnalyticsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.analytics_page)

        Log.d("SmartSpend", "AnalyticsActivity: Ready for category population")

        setupNavigation()
        setupCategoryList()
        updateProgressBars()
    }

    private fun setupCategoryList() {
        val rvCategories = findViewById<RecyclerView>(R.id.rvCategoryList)
        rvCategories.layoutManager = LinearLayoutManager(this)

        val db = DatabaseHelper(this)
        val cursor = db.getCategoryTotals()

        val adapter = CategoryAdapter(cursor)
        rvCategories.adapter = adapter

        Log.d("SmartSpend", "Category totals fetched from DB: ${cursor.count} items found")
    }

    private fun updateProgressBars() {
        val db = DatabaseHelper(this)
        val prefs = getSharedPreferences("SmartSpendPrefs", MODE_PRIVATE)

        val minGoal = prefs.getFloat("BUDGET_MIN", 0f)
        val maxGoal = prefs.getFloat("BUDGET_MAX", 1000f)

        val totalSpent = db.getTotalSpent("")

        val pbGoal = findViewById<ProgressBar>(R.id.pbMonthlyGoal)
        val tvStatus = findViewById<TextView>(R.id.tvGoalStatusText)

        val percent = if (maxGoal > 0) ((totalSpent / maxGoal) * 100).toInt() else 0
        pbGoal.progress = percent

        val healthStatus = when {
            totalSpent < minGoal -> "Under Budget (Excellent)"
            totalSpent <= maxGoal -> "Within Budget (Good)"
            else -> "Over Budget (Warning)"
        }

        tvStatus.text = "Spent R${String.format("%.2f", totalSpent)} of R${String.format("%.2f", maxGoal)}\nStatus: $healthStatus"
        Log.d("SmartSpend", "Analytics Updated: Spent $totalSpent, Max $maxGoal")
    }

    private fun setupNavigation() {
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