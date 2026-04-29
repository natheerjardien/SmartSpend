package com.example.smartspendui

data class ExpenseEntity(
    val uid: Int = 0,
    val category: String,
    val amount: Double,
    val date: Long,
    val description: String,
    val imagePath: String?
)
