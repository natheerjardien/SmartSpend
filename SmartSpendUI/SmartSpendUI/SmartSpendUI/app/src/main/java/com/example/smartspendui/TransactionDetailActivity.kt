package com.example.smartspendui

import java.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Date
import java.util.Locale
import com.bumptech.glide.Glide

// we created this class to display the full details of a specific expense
class TransactionDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.transaction_item_view) // we linked the activity to the detailed item view layout

        // we retrieved the unique expense id passed from the previous activity
        val passedId = intent.getIntExtra("EXPENSE_ID", -1)
        Log.d("SmartSpend", "TransactionDetailActivity: Detailed view loaded")

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val tvAmount = findViewById<TextView>(R.id.tvDetailAmount)
        val tvCategory = findViewById<TextView>(R.id.tvDetailCategory)
        val tvDesc = findViewById<TextView>(R.id.tvDetailDesc)
        val tvDate = findViewById<TextView>(R.id.tvDetailDate)
        val ivReceipt = findViewById<ImageView>(R.id.ivFullReceipt)

        // we set default placeholder text while the data was being fetched
        tvAmount.text = "R 0.00"
        tvCategory.text = "CATEGORY: N/A"
        tvDesc.text = "Loading details..."
        tvDate.text = "DATE: --/--/----"

        val db = DatabaseHelper(this)

        // we checked if a valid id was received and queried the database for the specific record
        if (passedId != -1)
        {
            val expense = db.getExpenseById(passedId)

            expense?.let {
                // we populated the ui components with the retrieved expense data
                tvAmount.text = "R ${String.format("%.2f", it.amount)}"
                tvCategory.text = "CATEGORY: ${it.category?.uppercase()}"
                tvDesc.text = it.description

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                tvDate.text = "DATE: ${dateFormat.format(Date(it.date))}"

                // we used the glide library to asynchronously load and display the receipt image if a path existed
                if (!it.imagePath.isNullOrEmpty())
                {
                    val imageUri = android.net.Uri.parse(it.imagePath)

                    Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.receipt)
                        .error(R.drawable.receipt)
                        .into(ivReceipt)
                }
            }
        }
        else
        {
            Log.e("SmartSpend", "Invalid Expense ID received")
        }

        btnBack.setOnClickListener {
            // we closed the activity to return the user to the transaction history list
            Log.d("SmartSpend", "Navigating back to Transaction History")
            finish()
        }
    }
}