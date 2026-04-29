package com.example.smartspendui

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// we created this class to display and manage the chronological list of user expenses
class TransactionHistoryActivity : AppCompatActivity() {

    private lateinit var rvTransactions: RecyclerView
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.transaction_history_page) // we linked the activity to the transaction history layout

        db = DatabaseHelper(this)
        rvTransactions = findViewById(R.id.rvTransactionLog)
        rvTransactions.layoutManager = LinearLayoutManager(this)

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
        val cursor = when {
            startDate != -1L && endDate != -1L && category.isNotEmpty() && category != "All Categories" -> {
                db.getExpensesByDateAndCategory(startDate, endDate, category)
            }
            startDate != -1L && endDate != -1L -> {
                db.getExpensesByDateRange(startDate, endDate)
            }
            else -> {
                db.getAllExpenses()
            }
        }

        // we converted the database results into a list of expense objects
        val expenseList = parseCursorToList(cursor)

        // we updated the recyclerview adapter and defined the click behavior for viewing specific transaction details
        rvTransactions.adapter = TransactionAdapter(expenseList) { expenseId ->
            val intent = Intent(this, TransactionDetailActivity::class.java)
            intent.putExtra("EXPENSE_ID", expenseId)
            startActivity(intent)
        }
    }

    // we iterated through the database cursor to populate our list of expense entities
    private fun parseCursorToList(cursor: Cursor): List<ExpenseEntity> {
        val list = mutableListOf<ExpenseEntity>()
        if (cursor.moveToFirst()) {
            do {
                list.add(ExpenseEntity(
                    uid = cursor.getInt(0),
                    category = cursor.getString(1),
                    amount = cursor.getDouble(2),
                    date = cursor.getLong(3),
                    description = cursor.getString(4),
                    imagePath = cursor.getString(5)
                ))
            } while (cursor.moveToNext())
        }
        cursor.close() // we ensured the cursor was closed after processing the data
        return list
    }

    private fun setupNavigation() { // we configured the bottom navigation buttons to switch between core app screens
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