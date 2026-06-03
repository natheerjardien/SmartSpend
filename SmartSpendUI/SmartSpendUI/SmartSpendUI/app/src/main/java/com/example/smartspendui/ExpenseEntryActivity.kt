package com.example.smartspendui

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class ExpenseEntryActivity : AppCompatActivity() { // we created this class to handle the input and storage of new expenses

    private var selectedImageUri: Uri? = null
    private lateinit var ivPreview: ImageView

    private val dbHelper = DatabaseHelper()
    private var currentUserId: String = ""

    // Baseline factory-defined category entries array list
    private val baseCategories = mutableListOf("Groceries", "Transport", "Rent", "Entertainment", "Utilities")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.expense_entry_page) // we linked the activity to the expense entry layout

        ivPreview = findViewById(R.id.ivPhotoPreview)
        val actvCategory = findViewById<AutoCompleteTextView>(R.id.actvExpenseCategory)
        val etAmount = findViewById<EditText>(R.id.etAmount)
        val etDate = findViewById<EditText>(R.id.etDate)
        val etDescription = findViewById<EditText>(R.id.etDescription)

        val btnPhoto = findViewById<Button>(R.id.btnTakePhoto)
        val btnSave = findViewById<Button>(R.id.btnSaveTransaction)

        val prefs = getSharedPreferences("SmartSpendPrefs", MODE_PRIVATE)
        currentUserId = prefs.getString("CURRENT_USER_ID", "") ?: ""

        if (currentUserId.isNotEmpty()) {
            dbHelper.getCustomBudgetCategories(currentUserId) { customCategories ->
                for (cat in customCategories) {
                    if (cat.isNotEmpty() && !baseCategories.contains(cat)) {
                        baseCategories.add(cat)
                    }
                }
                runOnUiThread {
                    val finalAdapter = ArrayAdapter(this@ExpenseEntryActivity, android.R.layout.simple_spinner_dropdown_item, baseCategories)
                    actvCategory.setAdapter(finalAdapter)
                }
            }
        } else {
            val fallbackAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, baseCategories)
            actvCategory.setAdapter(fallbackAdapter)
        }

        actvCategory.setOnClickListener { actvCategory.showDropDown() }

        // we attached a date picker listener to the date input field
        etDate.setOnClickListener { showDatePicker(etDate) }

        // we registered a result launcher to handle picking an image and gaining persistent uri permissions
        val getImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data

                uri?.let {
                    contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }

                selectedImageUri = uri
                ivPreview.setImageURI(uri)
                ivPreview.visibility = View.VISIBLE
                Log.d("SmartSpend", "Photo attached: $uri")
            }
        }

        // we triggered the system document picker to let the user select a receipt photo
        btnPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "image/*"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
            getImage.launch(intent)
        }

        // we validated the inputs before attempting to save the expense to the database
        btnSave.setOnClickListener {
            val category = actvCategory.text.toString().trim()
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
        setupNavigation()
    }

    private fun showDatePicker(editText: EditText) { // we displayed a calendar dialog to ensure the user selected a valid date format
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

    // we converted the inputs and stored the expense record in the database
    private fun saveToDatabase(category: String, amount: String, date: String, description: String) {
        val db = DatabaseHelper() // No longer requires 'this' context passed in!

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())

        val dateLong = try {
            val parsedDate = sdf.parse(date)
            val calendar = Calendar.getInstance()
            calendar.time = parsedDate!!
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.timeInMillis
        } catch (e: Exception) {
            System.currentTimeMillis()
        }

        val imagePath = selectedImageUri?.toString() ?: ""

        db.addExpense(category, amount.toDouble(), dateLong, description, imagePath) { isSuccess ->
            runOnUiThread {
                if (isSuccess) {
                    Toast.makeText(this, "Expense Saved to Firebase!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Firebase Database Error", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    private fun setupNavigation() { // we set up intent listeners to navigate to other parts of the application
        findViewById<Button>(R.id.btnNavHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
        findViewById<Button>(R.id.btnNavBudget).setOnClickListener {
            val intent = Intent(this, BudgetActivity::class.java)
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