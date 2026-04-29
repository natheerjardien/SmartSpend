package com.example.smartspendui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// we created this adapter to display our list of transactions in a recyclerview
class TransactionAdapter(private val expenseList: List<ExpenseEntity>, private val onClick: (Int) -> Unit) :
    RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    // we defined the viewholder to link the specific ui elements for each transaction row
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRowDate: TextView = view.findViewById(R.id.tvRowDate)
        val tvRowCategory: TextView = view.findViewById(R.id.tvRowCategory)
        val tvRowAmount: TextView = view.findViewById(R.id.tvRowAmount)
        val ivHasPhoto: ImageView = view.findViewById(R.id.ivHasPhoto)
    }

    // we inflated the custom row layout designed for individual transaction items
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.transaction_row_item, parent, false)
        return ViewHolder(view)
    }

    // we mapped the expense data to the views and formatted the currency and date strings
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val expense = expenseList[position]

        holder.tvRowCategory.text = expense.description ?: expense.category
        holder.tvRowAmount.text = "R ${String.format("%.2f", expense.amount)}"

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        holder.tvRowDate.text = sdf.format(java.util.Date(expense.date))

        // we checked for an image path to toggle the visibility of the photo icon
        holder.ivHasPhoto.visibility = if (expense.imagePath.isNullOrEmpty()) View.GONE else View.VISIBLE

        // we set an item click listener to trigger the detail view for the selected transaction
        holder.itemView.setOnClickListener { onClick(expense.uid) }
    }

    override fun getItemCount() = expenseList.size // we returned the total size of the transaction list
}