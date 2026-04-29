package com.example.smartspendui

// we defined this data class to represent a single expense record in our system
data class ExpenseEntity(
    val uid: Int = 0,
    val category: String,
    val amount: Double,
    val date: Long,
    val description: String,
    val imagePath: String?
)
