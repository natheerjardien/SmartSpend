package com.example.smartspendui

// Kotlin (2026) demonstrates how to create data classes
// we defined this data class to represent a single expense record in our system
data class ExpenseEntity(
    val uid: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    val date: Long = 0,
    val description: String = "",
    val imagePath: String? = ""
)

// Kotlin, 2026. Data Classes. (Version 2.0) [Source code]
// Available at: < https://kotlinlang.org/docs/data-classes.html#properties-declared-in-the-class-body > [Accessed 26 April 2026].