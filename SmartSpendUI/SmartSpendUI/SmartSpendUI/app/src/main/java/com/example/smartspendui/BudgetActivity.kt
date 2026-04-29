package com.example.smartspendui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class BudgetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.monthly_budget)

        val etMinGoal = findViewById<EditText>(R.id.etMinGoal)
        val etMaxGoal = findViewById<EditText>(R.id.etMaxGoal)
        val etCatLimit = findViewById<EditText>(R.id.etCategoryLimit)
        val spnCategory = findViewById<Spinner>(R.id.spnCategorySelect)
        val btnSave = findViewById<Button>(R.id.btnSaveBudget)

        Log.d("SmartSpend", "BudgetActivity: Initializing view components")

        setupNavigation()
        populateCategoryDropdown(spnCategory)

        btnSave.setOnClickListener {
            val min = etMinGoal.text.toString()
            val max = etMaxGoal.text.toString()
            val limit = etCatLimit.text.toString()
            val selectedCategory = spnCategory.selectedItem?.toString() ?: ""

            if (min.isEmpty() || max.isEmpty())
            {
                Toast.makeText(this, "Please set both your Min and Max goals", Toast.LENGTH_SHORT).show()
            }
            else
            {
                val prefs = getSharedPreferences("SmartSpendPrefs", MODE_PRIVATE)
                prefs.edit().apply {
                    putFloat("BUDGET_MIN", min.toFloatOrNull() ?: 0f)
                    putFloat("BUDGET_MAX", max.toFloatOrNull() ?: 0f)
                    putFloat("LIMIT_$selectedCategory", limit.toFloatOrNull() ?: 0f)
                    putString("LAST_CONFIGURED_CAT", selectedCategory)
                    apply()
                }

                Log.i("SmartSpend", "Budget Saved: Min R$min, Max R$max, $selectedCategory Limit: R$limit")
                Toast.makeText(this, "Budget Plan Saved Successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun populateCategoryDropdown(spinner: Spinner) {
        val db = DatabaseHelper(this)
        val categories = db.getUniqueCategories().toMutableList()

        if (categories.isEmpty()) categories.add("No Expenses Yet")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun setupNavigation() {
        findViewById<Button>(R.id.btnNavHome).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        findViewById<Button>(R.id.btnNavEntry).setOnClickListener {
            startActivity(Intent(this, ExpenseEntryActivity::class.java))
        }
        findViewById<Button>(R.id.btnNavAnalytic).setOnClickListener {
            startActivity(Intent(this, AnalyticsActivity::class.java))
        }
    }
}