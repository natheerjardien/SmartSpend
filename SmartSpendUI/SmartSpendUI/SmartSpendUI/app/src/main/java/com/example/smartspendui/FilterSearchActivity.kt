package com.example.smartspendui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class FilterSearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.filter_search_page)

        val etStart = findViewById<EditText>(R.id.etStartDate)
        val etEnd = findViewById<EditText>(R.id.etEndDate)
        val spnCategory = findViewById<Spinner>(R.id.spnFilterCategory)
        val btnSearch = findViewById<Button>(R.id.btnSearch)

        etStart.setOnClickListener { showDatePicker(etStart) }
        etEnd.setOnClickListener { showDatePicker(etEnd) }

        populateCategorySpinner(spnCategory)

        btnSearch.setOnClickListener {
            val startDate = etStart.text.toString()
            val endDate = etEnd.text.toString()
            val category = spnCategory.selectedItem?.toString() ?: ""

            if (startDate.isEmpty() || endDate.isEmpty()) {
                Toast.makeText(this, "Please select a date range", Toast.LENGTH_SHORT).show()
            }
            else
            {
                val sdf = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
                val startLong = sdf.parse(startDate)?.time ?: 0L
                val endLong = sdf.parse(endDate)?.time ?: 0L

                val intent = Intent(this, TransactionHistoryActivity::class.java)
                intent.putExtra("START_DATE", startLong)
                intent.putExtra("END_DATE", endLong)
                intent.putExtra("CATEGORY", category)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            val selectedDate = "$year-${month + 1}-$day"
            editText.setText(selectedDate)
            Log.d("SmartSpend", "Date Selected: $selectedDate")
        }

        DatePickerDialog(this, dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun populateCategorySpinner(spinner: Spinner) {
        val db = DatabaseHelper(this)
        val categories = db.getUniqueCategories().toMutableList()

        if (categories.isEmpty()) categories.add("No Expenses Yet")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }
}