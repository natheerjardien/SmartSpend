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

class TransactionDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.transaction_item_view)

        val passedId = intent.getIntExtra("EXPENSE_ID", -1)
        Log.d("SmartSpend", "TransactionDetailActivity: Detailed view loaded")

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val tvAmount = findViewById<TextView>(R.id.tvDetailAmount)
        val tvCategory = findViewById<TextView>(R.id.tvDetailCategory)
        val tvDesc = findViewById<TextView>(R.id.tvDetailDesc)
        val tvDate = findViewById<TextView>(R.id.tvDetailDate)
        val ivReceipt = findViewById<ImageView>(R.id.ivFullReceipt)

        tvAmount.text = "R 0.00"
        tvCategory.text = "CATEGORY: N/A"
        tvDesc.text = "Loading details..."
        tvDate.text = "DATE: --/--/----"

        val db = DatabaseHelper(this)

        if (passedId != -1) {
            val expense = db.getExpenseById(passedId)

            expense?.let {
                tvAmount.text = "R ${String.format("%.2f", it.amount)}"
                tvCategory.text = "CATEGORY: ${it.category?.uppercase()}"
                tvDesc.text = it.description

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                tvDate.text = "DATE: ${dateFormat.format(Date(it.date))}"

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
            Log.d("SmartSpend", "Navigating back to Transaction History")
            finish()
        }
    }
}