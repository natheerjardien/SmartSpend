package com.example.smartspendui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class BudgetActivity : AppCompatActivity() { // we created this class to manage user budget settings

    private lateinit var actvCategory: AutoCompleteTextView
    private lateinit var etMin: EditText
    private lateinit var etMax: EditText

    private lateinit var etIncomeAmount: EditText
    private lateinit var rgIncomeType: RadioGroup
    private lateinit var rbSideHustle: RadioButton
    private lateinit var btnSaveIncome: Button

    private val dbHelper = DatabaseHelper()
    private var currentUserId: String = ""

    // Predefined default structural category selection array items
    private val baseCategories = mutableListOf("Groceries", "Transport", "Rent", "Entertainment", "Utilities")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.monthly_budget) // we linked the layout for the budget configuration page

        actvCategory = findViewById(R.id.actvBudgetCategory)
        etMin = findViewById(R.id.etMinGoal)
        etMax = findViewById(R.id.etMaxGoal)

        etIncomeAmount = findViewById(R.id.etIncomeAmount)
        rgIncomeType = findViewById(R.id.rgIncomeType)
        rbSideHustle = findViewById(R.id.rbSideHustle)
        btnSaveIncome = findViewById(R.id.btnSaveIncome)

        val prefs = getSharedPreferences("SmartSpendPrefs", MODE_PRIVATE)
        currentUserId = prefs.getString("CURRENT_USER_ID", "") ?: ""

        if (currentUserId.isEmpty()) { // checks if user id is missing
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
            Log.e("SmartSpend", "BudgetActivity aborted: CURRENT_USER_ID token is empty.")
            finish()
            return
        }

        dbHelper.getCustomBudgetCategories(currentUserId) { unifiedList ->
            for (cat in unifiedList) {
                if (cat.isNotEmpty() && !baseCategories.contains(cat)) {
                    baseCategories.add(cat)
                }
            }
            runOnUiThread {
                val categoryAdapter = ArrayAdapter(this@BudgetActivity, android.R.layout.simple_spinner_dropdown_item, baseCategories)
                actvCategory.setAdapter(categoryAdapter)
            }
        }

        actvCategory.setOnClickListener { actvCategory.showDropDown() }

        actvCategory.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val selectedCat = actvCategory.text.toString().trim()
                if (selectedCat.isNotEmpty()) {
                    dbHelper.getCategoryBudget(currentUserId, selectedCat) { minGoal, maxGoal ->
                        runOnUiThread {
                            etMin.setText(minGoal)
                            etMax.setText(maxGoal)
                        }
                    }
                }
            }
        }

        btnSaveIncome.setOnClickListener {
            val incomeInputText = etIncomeAmount.text.toString().trim()

            if (incomeInputText.isEmpty()) {
                Toast.makeText(this, "Please enter an income amount first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val incomeAmount = incomeInputText.toDoubleOrNull() ?: 0.0
            val isSideHustle = rbSideHustle.isChecked

            dbHelper.updateUserIncome(currentUserId, incomeAmount, isSideHustle) { isSuccess ->
                runOnUiThread {
                    if (isSuccess) {
                        Toast.makeText(applicationContext, "Global Cash Flow Updated!", Toast.LENGTH_SHORT).show()
                        etIncomeAmount.text.clear() // Reset the text field cleanly
                    } else {
                        Toast.makeText(applicationContext, "Failed to update income pool.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        findViewById<Button>(R.id.btnSaveBudget).setOnClickListener {
            val category = actvCategory.text.toString().trim()
            val minText = etMin.text.toString().trim()
            val maxText = etMax.text.toString().trim()

            if (category.isEmpty() || minText.isEmpty() || maxText.isEmpty()) {
                Toast.makeText(this, "Please fulfill all goal properties.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            dbHelper.addCategoryBudget(currentUserId, category, minText.toDouble(), maxText.toDouble()) { isSuccess ->
                runOnUiThread {
                    if (isSuccess) {
                        Toast.makeText(applicationContext, "Category Goals Saved Successfully!", Toast.LENGTH_SHORT).show()

                        actvCategory.text.clear()
                        etMin.text.clear()
                        etMax.text.clear()

                        prefs.edit().putFloat("BUDGET_MIN", minText.toFloat()).putFloat("BUDGET_MAX", maxText.toFloat()).apply()
                    } else {
                        Toast.makeText(applicationContext, "Database Connection Error.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        setupNavigation()
    }

    private fun setupNavigation() { // we set up intent listeners to navigate to other parts of the application
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
        findViewById<Button>(R.id.btnNavAnalytic).setOnClickListener {
            val intent = Intent(this, AnalyticsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }
}