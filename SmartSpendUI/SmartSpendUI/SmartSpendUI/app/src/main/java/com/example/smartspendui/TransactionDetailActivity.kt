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
        val passedId = intent.getStringExtra("EXPENSE_ID")
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

        val db = DatabaseHelper()

        // we checked if a valid id was received and queried the database for the specific record
        if (!passedId.isNullOrEmpty())
        {
            db.getExpenseById(passedId) { expense ->
                expense?.let {
                    // we populated the ui components with the retrieved expense data
                    tvAmount.text = "R ${String.format("%.2f", it.amount)}"
                    tvCategory.text = "CATEGORY: ${it.category?.uppercase()}"
                    tvDesc.text = it.description

                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    tvDate.text = "${dateFormat.format(Date(it.date))}"

                    // we used the glide library to asynchronously load and display the receipt image if a path existed
                    if (!it.imagePath.isNullOrEmpty())
                    {
                        val imageUri = android.net.Uri.parse(it.imagePath)

                        Glide.with(this)
                            .load(imageUri)
                            .fitCenter()
                            .placeholder(R.drawable.receipt)
                            .error(R.drawable.receipt)
                            .into(ivReceipt)
                    }
                } ?: run {
                    Log.e("SmartSpend", "Expense record not found in Firebase storage")
                    tvDesc.text = "Error: Record not found."
                }
            }

        }
        else
        {
            Log.e("SmartSpend", "Invalid Expense ID received")
            tvDesc.text = "Error: Invalid transaction link."
        }

        ivReceipt.setOnClickListener {
            // Get the drawable from the ImageView
            val drawable = ivReceipt.drawable

            if (drawable is android.graphics.drawable.BitmapDrawable) {
                val bitmap = drawable.bitmap

                // Generate a unique name for the receipt file
                val filename = "Receipt_${System.currentTimeMillis()}.jpg"

                // Call the helper function to save it
                val isSaved = saveImageToGallery(bitmap, filename)

                if (isSaved) {
                    android.widget.Toast.makeText(this, "Image downloaded to Gallery!", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(this, "Failed to download image", android.widget.Toast.LENGTH_SHORT).show()
                }
            } else {
                android.widget.Toast.makeText(this, "No image available to download", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener {
            // we closed the activity to return the user to the transaction history list
            Log.d("SmartSpend", "Navigating back to Transaction History")
            finish()
        }
    }

    // we created a method to download the image from the imageView
    private fun saveImageToGallery(bitmap: android.graphics.Bitmap, filename: String): Boolean {
        val resolver = contentResolver
        val imageCollection = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            android.provider.MediaStore.Images.Media.getContentUri(android.provider.MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            // Saves it inside the standard 'Pictures/SmartSpend' directory
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "Pictures/SmartSpend")
                put(android.provider.MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val imageUri = resolver.insert(imageCollection, contentValues) ?: return false

        return try {
            resolver.openOutputStream(imageUri).use { outputStream ->
                if (outputStream != null) {
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(android.provider.MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(imageUri, contentValues, null, null)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            // Clean up failed file entry if it was created
            resolver.delete(imageUri, null, null)
            false
        }
    }
}