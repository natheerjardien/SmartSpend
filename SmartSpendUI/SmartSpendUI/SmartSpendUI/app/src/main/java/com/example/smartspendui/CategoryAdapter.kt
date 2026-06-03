package com.example.smartspendui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// GeeksforGeeks (2025) demonstrates how to create a custom adapter class
// we built this adapter to bridge our database results and the recyclerview
class CategoryAdapter(private val categoryTotals: Map<String, Float>,
private val onCategoryClick: (String) -> Unit) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    // we converted map keys into an indexed list to easily handle position requests inside the adapter
    private val categoriesList = categoryTotals.keys.toList()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) { // we defined the viewholder to hold references to the row layout elements
        val tvName: TextView = view.findViewById(R.id.tvCategoryRowName)
        val tvTotal: TextView = view.findViewById(R.id.tvCategoryRowTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder { // we inflated the individual row layout for each category item
        val view = LayoutInflater.from(parent.context).inflate(R.layout.category_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) { // we bound the specific database record to the textviews based on the current list position

            val name = categoriesList[position]
            val total = categoryTotals[name] ?: 0.0

            // we populated the interface elements with the category metadata and total spending balance
            holder.tvName.text = name
            holder.tvTotal.text = "R${String.format("%.2f", total)}"

        // we attached an active click event listener to route the selected filter string back to analytics
            holder.itemView.setOnClickListener {
                onCategoryClick(name)
            }
    }

    override fun getItemCount(): Int = categoriesList.size // we returned the total number of items from the database cursor
}
// GeeksforGeeks, 2025. CustomArrayAdapter in Android with Example. (Version 2.0) [Source code]
// Available at: < https://www.geeksforgeeks.org/android/customarrayadapter-in-android-with-example/ > [Accessed 26 April 2026].

