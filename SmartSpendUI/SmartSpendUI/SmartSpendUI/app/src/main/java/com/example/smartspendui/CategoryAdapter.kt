package com.example.smartspendui

import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// we built this adapter to bridge our database results and the recyclerview
class CategoryAdapter(private val cursor: Cursor) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) { // we defined the viewholder to hold references to the row layout elements
        val tvName: TextView = view.findViewById(R.id.tvCategoryRowName)
        val tvTotal: TextView = view.findViewById(R.id.tvCategoryRowTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder { // we inflated the individual row layout for each category item
        val view = LayoutInflater.from(parent.context).inflate(R.layout.category_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) { // we bound the specific database record to the textviews based on the current list position
        if (cursor.moveToPosition(position))
        {
            val name = cursor.getString(0)
            val total = cursor.getDouble(1)
            holder.tvName.text = name
            holder.tvTotal.text = "R${String.format("%.2f", total)}"
        }
    }

    override fun getItemCount(): Int = cursor.count // we returned the total number of items from the database cursor
}