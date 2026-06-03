package com.example.smartspendui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// we created this class to display and manage the chronological list of user expenses
class TransactionHistoryActivity : AppCompatActivity() {
    private var currentUserId: String = ""
    private lateinit var rvTransactions: RecyclerView
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.transaction_history_page) // we linked the activity to the transaction history layout

        db = DatabaseHelper()
        rvTransactions = findViewById(R.id.rvTransactionLog)
        rvTransactions.layoutManager = LinearLayoutManager(this)

        val prefs = getSharedPreferences("SmartSpendPrefs", MODE_PRIVATE)
        currentUserId = prefs.getString("CURRENT_USER_ID", "") ?: ""

        // we added a listener to open the filter search activity
        findViewById<Button>(R.id.btnFilter).setOnClickListener {
            startActivity(Intent(this, FilterSearchActivity::class.java))
        }

        setupNavigation()
    }

    override fun onResume() { // we refreshed the data every time the activity became visible to catch any new filters
        super.onResume()
        loadFilteredData()
    }

    private fun loadFilteredData() { // we retrieved the filter criteria from the intent and queried the database accordingly
        val startDate = intent.getLongExtra("FILTER_START", -1L)
        val endDate = intent.getLongExtra("FILTER_END", -1L)
        val category = intent.getStringExtra("FILTER_CAT") ?: ""

        Log.d("SmartSpend", "Filtering: $category from $startDate to $endDate")

        // we chose the appropriate database query based on whether date or category filters were active
        // and attached our UI adapter updates inside the asynchronous lambda callbacks
        when {
            startDate != -1L && endDate != -1L && category.isNotEmpty() && category != "All Categories" -> {
                db.getExpensesByDateAndCategory(currentUserId, startDate, endDate, category) { filteredList ->
                    updateAdapter(filteredList)
                }
            }
            startDate != -1L && endDate != -1L -> {
                db.getExpensesByDateRange(currentUserId, startDate, endDate) { dateRangeList ->
                    updateAdapter(dateRangeList)
                }
            }
            else -> {
                db.getAllExpenses(currentUserId) { allExpensesList ->
                    updateAdapter(allExpensesList)
                }
            }
        }
    }

    // Helper method to attach the fetched cloud list to the adapter logic
    private fun updateAdapter(expenseList: List<ExpenseEntity>) {
        // we updated the recyclerview adapter and defined the click behavior for viewing specific transaction details
        rvTransactions.adapter = TransactionAdapter(expenseList) { expenseId ->
            val intent = Intent(this, TransactionDetailActivity::class.java)
            intent.putExtra("EXPENSE_ID", expenseId)
            startActivity(intent)
        }
    }

    private fun setupNavigation() { // we configured the bottom navigation buttons to switch between core app screens
        findViewById<Button>(R.id.btnNavHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
        findViewById<Button>(R.id.btnNavEntry).setOnClickListener {
            val intent = Intent(this, ExpenseEntryActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
        findViewById<Button>(R.id.btnNavBudget).setOnClickListener {
            val intent = Intent(this, BudgetActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
        findViewById<Button>(R.id.btnNavAnalytic).setOnClickListener {
            finish()
        }
    }
}