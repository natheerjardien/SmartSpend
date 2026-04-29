package com.example.smartspendui

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class ExpenseEntryActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null
    private lateinit var ivPreview: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.expense_entry_page)

        ivPreview = findViewById(R.id.ivPhotoPreview)
        val etCategory = findViewById<EditText>(R.id.etCategory)
        val etAmount = findViewById<EditText>(R.id.etAmount)
        val etDate = findViewById<EditText>(R.id.etDate)
        val etDescription = findViewById<EditText>(R.id.etDescription)

        val btnPhoto = findViewById<Button>(R.id.btnTakePhoto)
        val btnSave = findViewById<Button>(R.id.btnSaveTransaction)
        val btnCancel = findViewById<Button>(R.id.btnCancelTransaction)

        etDate.setOnClickListener { showDatePicker(etDate) }

        val getImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                ivPreview.setImageURI(selectedImageUri)
                ivPreview.visibility = View.VISIBLE
                Log.d("SmartSpend", "Photo attached: $selectedImageUri")
            }
        }

        btnPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            getImage.launch(intent)
        }

        btnSave.setOnClickListener {
            val category = etCategory.text.toString()
            val amount = etAmount.text.toString()
            val date = etDate.text.toString()
            val description = etDescription.text.toString()

            if (category.isEmpty() || amount.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Please fill in all mandatory fields", Toast.LENGTH_SHORT).show()
                Log.e("SmartSpend", "Failed to save: Empty fields detected")
            } else {
                saveToDatabase(category, amount, date, description)
            }
        }

        btnCancel.setOnClickListener {
            finish()
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

    private fun saveToDatabase(category: String, amount: String, date: String, description: String) {
        val db = DatabaseHelper(this)

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())

        val dateLong = try
        {
            sdf.parse(date)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }

        val imagePath = selectedImageUri?.toString() ?: ""

        val result = db.addExpense(
            category,
            amount.toDouble(),
            dateLong,
            description,
            imagePath
        )

        if (result != -1L)
        {
            Toast.makeText(this, "Expense Saved!", Toast.LENGTH_SHORT).show()
            finish()
        }
        else
        {
            Toast.makeText(this, "Database Error", Toast.LENGTH_SHORT).show()
        }
    }
}