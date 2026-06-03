package com.example.smartspendui

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

//Firebase (2026) demonstrates how to add Firebase to a project
class DatabaseHelper {

    // we initialized the database reference pointing straight to our firebase live database link
    private val database = FirebaseDatabase.getInstance("https://bcad3-st10433896-default-rtdb.asia-southeast1.firebasedatabase.app").reference

    // Add User
    fun addUser(username: String, password: String, firstName: String, lastName: String, onResult: (Boolean) -> Unit) { // we created a new user profile record and saved it to the cloud database
        val userId = database.child("users").push().key ?: return onResult(false)

        val user = UserEntity(
            uid = userId,
            username = username,
            password = password,
            firstName = firstName,
            lastName = lastName,
            profileImageUrl = "",
            totalIncome = 0.0,
            monthlySalary = 0.0
        )

        database.child("users").child(userId).setValue(user)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    // Add Expense
    fun addExpense( // we pushed a new transaction entry into the user's secure expense branch layout
        userId: String,
        category: String,
        amount: Double,
        date: Long,
        description: String,
        imagePath: String,
        onResult: (Boolean) -> Unit
    ) {
        val expenseRef = database.child("expenses").child(userId)
        val expenseId = expenseRef.push().key ?: return onResult(false)
        val expense = ExpenseEntity(expenseId, category, amount, date, description, imagePath)

        // we wrote the transaction payload straight to the generated unique expense child node
        expenseRef.child(expenseId).setValue(expense)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    // Get Expense By ID
    fun getExpenseById(userId: String, id: String, onResult: (ExpenseEntity?) -> Unit) { // we fetched a single expense transaction using its unique identification key
        database.child("expenses").child(userId).child(id).get().addOnSuccessListener { snapshot ->
            val expense = snapshot.getValue(ExpenseEntity::class.java)
            onResult(expense)
        }.addOnFailureListener {
            onResult(null)
        }
    }

    // Fetch All Expenses (Ordered by Date)
    fun getAllExpenses(userId: String, onResult: (List<ExpenseEntity>) -> Unit) { // we retrieved all recorded transactions belonging to the active user ordered by date
        database.child("expenses").child(userId).orderByChild("date")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val expenses = mutableListOf<ExpenseEntity>()
                    // Firebase orders ascending by default, we reverse to mirror the original DESC order
                    for (child in snapshot.children) {
                        child.getValue(ExpenseEntity::class.java)?.let { expenses.add(it) }
                    }
                    // we reversed the array to mirror a descending chronological list breakdown view
                    onResult(expenses.reversed())
                }
                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    // Get Expenses by Date Range
    fun getExpensesByDateRange(userId: String, startDate: Long, endDate: Long, onResult: (List<ExpenseEntity>) -> Unit) { // we queried a list of transactions that fell within a specific timestamp window
        database.child("expenses").child(userId).orderByChild("date")
            .startAt(startDate.toDouble())
            .endAt(endDate.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val expenses = mutableListOf<ExpenseEntity>()
                    for (child in snapshot.children) {
                        child.getValue(ExpenseEntity::class.java)?.let { expenses.add(it) }
                    }
                    onResult(expenses.reversed())
                }
                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    // Get Expenses by Date and Category
    fun getExpensesByDateAndCategory( // we fetched date-filtered transactions and sorted them by their specific category string
        userId: String,
        startDate: Long,
        endDate: Long,
        category: String,
        onResult: (List<ExpenseEntity>) -> Unit
    ) {
        getExpensesByDateRange(userId, startDate, endDate) { expenses ->
            // we filtered the local array to keep entries matching the requested category target name
            val filtered = expenses.filter { it.category.equals(category, ignoreCase = true) }
            onResult(filtered)
        }
    }

    // Get Total Spent (Grand total or Category total)
    fun getTotalSpent(userId: String, category: String = "", onResult: (Double) -> Unit) { // we calculated the sum of all expenses or filtered them down to a single category subtotal
        database.child("expenses").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var total = 0.0
                for (child in snapshot.children) {
                    val expense = child.getValue(ExpenseEntity::class.java)
                    if (expense != null) {
                        if (category.isEmpty() || expense.category.equals(category, ignoreCase = true)) {
                            total += expense.amount
                        }
                    }
                }
                onResult(total)
            }
            override fun onCancelled(error: DatabaseError) { onResult(0.0) }
        })
    }

    // Get Unique Categories
    fun getUniqueCategories(userId: String, onResult: (List<String>) -> Unit) { // we collected a distinct set of all category strings used across logged transactions
        database.child("expenses").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categories = mutableSetOf<String>()
                for (child in snapshot.children) {
                    child.getValue(ExpenseEntity::class.java)?.let { categories.add(it.category) }
                }
                onResult(categories.toList())
            }
            override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
        })
    }

    // Get All Users
    fun getAllUsers(onResult: (List<UserEntity>) -> Unit) { // we pulled a list of all user profiles from the server to handle login validation checks
        // Explicitly target the correct node path
        database.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val usersList = mutableListOf<UserEntity>()

                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        try {
                            // we extracted core string fields manually to shield against data class conversion crashes
                            val uid = child.child("uid").value?.toString() ?: ""
                            val username = child.child("username").value?.toString() ?: ""
                            val password = child.child("password").value?.toString() ?: ""
                            val firstName = child.child("firstName").value?.toString() ?: ""
                            val lastName = child.child("lastName").value?.toString() ?: ""

                            // Only add valid structural entities to our validation list
                            if (username.isNotEmpty() && password.isNotEmpty()) {
                                val user = UserEntity(uid, username, password, firstName, lastName)
                                usersList.add(user)
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("SmartSpendDB", "Error mapping individual user node: ${e.message}")
                        }
                    }
                } else {
                    android.util.Log.w("SmartSpendDB", "Read complete: The 'UserEntity' node is completely missing or empty in the cloud.")
                }

                // Send the finalized list back to the Login Activity callback hook
                onResult(usersList)
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("SmartSpendDB", "Firebase cloud read canceled or blocked: ${error.message}")
                onResult(emptyList())
            }
        })
    }

    // we saved the minimum and maximum threshold rules for a specific category goal
    fun addCategoryBudget(userId: String, category: String, minGoal: Double, maxGoal: Double, onResult: (Boolean) -> Unit) {
        val budgetPlan = mapOf(
            "categoryName" to category,
            "minGoal" to minGoal,
            "maxGoal" to maxGoal
        )

        database.child("CategoryBudget").child(userId).child(category).setValue(budgetPlan)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { exception ->
                Log.e("SmartSpendDB", "Firebase Budget Save Failed: ${exception.message}")
                onResult(false)
            }
    }

    // we read the saved budget boundaries for a single category node from the database
    fun getCategoryBudget(userId: String, category: String, onResult: (minGoal: String, maxGoal: String) -> Unit) {
        database.child("CategoryBudget").child(userId).child(category).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val min = snapshot.child("minGoal").value?.toString() ?: ""
                    val max = snapshot.child("maxGoal").value?.toString() ?: ""
                    onResult(min, max)
                } else {
                    onResult("", "")
                }
            }
            .addOnFailureListener {
                onResult("", "")
            }
    }

    fun getCustomBudgetCategories(userId: String, onResult: (List<String>) -> Unit) { // we combined category strings found across both budget targets and expense items into a unified collection
        val combinedCategories = mutableSetOf<String>()

        database.child("CategoryBudget").child(userId).get().addOnSuccessListener { budgetSnapshot ->
            if (budgetSnapshot.exists()) {
                for (child in budgetSnapshot.children) {
                    child.key?.let { combinedCategories.add(it) }
                }
            }

            database.child("expenses").child(userId).addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(expenseSnapshot: com.google.firebase.database.DataSnapshot) {
                    for (child in expenseSnapshot.children) {
                        val expenseCat = child.child("category").value?.toString() ?: ""
                        if (expenseCat.isNotEmpty()) {
                            combinedCategories.add(expenseCat)
                        }
                    }
                    onResult(combinedCategories.toList())
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    onResult(combinedCategories.toList())
                }
            })
        }.addOnFailureListener {
            onResult(emptyList())
        }
    }

    fun updateUserIncome(userId: String, amount: Double, isSideHustle: Boolean, onResult: (Boolean) -> Unit) { // we adjusted the user's liquid income balances depending on salary or side hustle streams
        val userRef = database.child("users").child(userId)

        userRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val currentTotal = snapshot.child("totalIncome").value?.toString()?.toDoubleOrNull() ?: 0.0
                val updates = mutableMapOf<String, Any>()

                if (isSideHustle) {
                    // Side Hustle adds on top of the liquid cash pool
                    updates["totalIncome"] = currentTotal + amount
                } else {
                    // Monthly Income completely overrides both pools to set a fresh baseline
                    updates["totalIncome"] = amount
                    updates["monthlySalary"] = amount
                }

                // we submitted specific modified node map tracks back to the profile node parameters
                userRef.updateChildren(updates)
                    .addOnSuccessListener { onResult(true) }
                    .addOnFailureListener { onResult(false) }
            } else {
                onResult(false)
            }
        }.addOnFailureListener { onResult(false) }
    }

    // we updated the permanent local file path reference string for the user avatar
    fun updateProfileImage(userId: String, imageUrlString: String, onResult: (Boolean) -> Unit) {
        database.child("users").child(userId).child("profileImageUrl").setValue(imageUrlString)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun getUserProfile(userId: String, onResult: (DataSnapshot) -> Unit) { // we fetched a snapshot of the user's complete profile dataset from the cloud
        database.child("users").child(userId).get()
            .addOnSuccessListener { snapshot ->
                onResult(snapshot)
            }
            .addOnFailureListener {
                database.database.getReference("empty_fallback").get().addOnCompleteListener { task ->
                    onResult(task.result)
                }
            }
    }

    // we updated multiple profile variables at the same time using a key-value data map
    fun updateUserProfileFields(userId: String, updatesMap: Map<String, Any>, onResult: (Boolean) -> Unit) {
        database.child("users").child(userId).updateChildren(updatesMap)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { exception ->
                Log.e("SmartSpendDB", "Failed to update profile nodes: ${exception.message}")
                onResult(false)
            }
    }
}
// Firebase, 2026.  Add Firebase to your Android project. (Version 2.0) [Source code]
// Available at: < https://firebase.google.com/docs/android/setup > [Accessed 28 May 2026].