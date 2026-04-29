package com.example.smartspendui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TransactionHistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.transaction_history_page)

        Log.d("SmartSpend", "TransactionHistoryActivity: Basic UI loaded")

        val rvTransactions = findViewById<RecyclerView>(R.id.rvTransactionLog)
        rvTransactions.layoutManager = LinearLayoutManager(this)

        val db = DatabaseHelper(this)
        val expenseList = fetchAllExpensesFromDb(db)

        rvTransactions.adapter = TransactionAdapter(expenseList) { expenseId ->
            val intent = Intent(this, TransactionDetailActivity::class.java)
            intent.putExtra("EXPENSE_ID", expenseId)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnFilter).setOnClickListener {
            startActivity(Intent(this, FilterSearchActivity::class.java))
        }

        setupNavigation()
    }

    private fun fetchAllExpensesFromDb(db: DatabaseHelper): List<ExpenseEntity> {
        val list = mutableListOf<ExpenseEntity>()
        val cursor = db.getAllExpenses()

        if (cursor.moveToFirst())
        {
            do
            {
                val expense = ExpenseEntity(
                    uid = cursor.getInt(0),
                    category = cursor.getString(1),
                    amount = cursor.getDouble(2),
                    date = cursor.getLong(3),
                    description = cursor.getString(4),
                    imagePath = cursor.getString(5)
                )
                list.add(expense)
            }
            while (cursor.moveToNext())
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