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

            if (startDate.isNotEmpty() && endDate.isNotEmpty())
            {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())

                val startCal = Calendar.getInstance()
                startCal.time = sdf.parse(etStart.text.toString())!!
                startCal.set(Calendar.HOUR_OF_DAY, 0)
                startCal.set(Calendar.MINUTE, 0)
                startCal.set(Calendar.SECOND, 0)
                startCal.set(Calendar.MILLISECOND, 0)
                val startL = startCal.timeInMillis

                val endCal = Calendar.getInstance()
                endCal.time = sdf.parse(etEnd.text.toString())!!
                endCal.set(Calendar.HOUR_OF_DAY, 23)
                endCal.set(Calendar.MINUTE, 59)
                endCal.set(Calendar.SECOND, 59)
                endCal.set(Calendar.MILLISECOND, 999)
                val endL = endCal.timeInMillis

                val intent = Intent(this, TransactionHistoryActivity::class.java)
                intent.putExtra("FILTER_START", startL) // This is now exactly 00:00:00 of the start day
                intent.putExtra("FILTER_END", endL)     // This is now exactly 00:00:00 of the end day
                intent.putExtra("FILTER_CAT", spnCategory.selectedItem.toString())
                startActivity(intent)
                finish()
            }
            else
            {
                Toast.makeText(this, "Select a date range", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, day)
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