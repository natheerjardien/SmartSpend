package com.example.smartspendui

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TransactionHistoryActivity : AppCompatActivity() {

    private lateinit var rvTransactions: RecyclerView
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.transaction_history_page)

        db = DatabaseHelper(this)
        rvTransactions = findViewById(R.id.rvTransactionLog)
        rvTransactions.layoutManager = LinearLayoutManager(this)

        findViewById<Button>(R.id.btnFilter).setOnClickListener {
            startActivity(Intent(this, FilterSearchActivity::class.java))
        }

        setupNavigation()
    }

    override fun onResume() {
        super.onResume()
        loadFilteredData()
    }

    private fun loadFilteredData() {
        val startDate = intent.getLongExtra("FILTER_START", -1L)
        val endDate = intent.getLongExtra("FILTER_END", -1L)
        val category = intent.getStringExtra("FILTER_CAT") ?: ""

        Log.d("SmartSpend", "Filtering: $category from $startDate to $endDate")

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

        val expenseList = parseCursorToList(cursor)

        rvTransactions.adapter = TransactionAdapter(expenseList) { expenseId ->
            val intent = Intent(this, TransactionDetailActivity::class.java)
            intent.putExtra("EXPENSE_ID", expenseId)
            startActivity(intent)
        }
    }

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
        cursor.close()
        return list
    }

    private fun setupNavigation() {
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