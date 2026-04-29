package com.example.smartspendui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(private val expenseList: List<ExpenseEntity>, private val onClick: (Int) -> Unit) :
    RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRowDate: TextView = view.findViewById(R.id.tvRowDate)
        val tvRowCategory: TextView = view.findViewById(R.id.tvRowCategory)
        val tvRowAmount: TextView = view.findViewById(R.id.tvRowAmount)
        val ivHasPhoto: ImageView = view.findViewById(R.id.ivHasPhoto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.transaction_row_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val expense = expenseList[position]

        holder.tvRowCategory.text = expense.description ?: expense.category
        holder.tvRowAmount.text = "R ${String.format("%.2f", expense.amount)}"

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        holder.tvRowDate.text = sdf.format(java.util.Date(expense.date))

        holder.ivHasPhoto.visibility = if (expense.imagePath.isNullOrEmpty()) View.GONE else View.VISIBLE

        holder.itemView.setOnClickListener { onClick(expense.uid) }
    }

    override fun getItemCount() = expenseList.size
}