package com.example.smartspendui

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DatabaseHelper {

    // Get reference to the root of our Firebase Database
    private val database = FirebaseDatabase.getInstance("https://bcad3-st10433896-default-rtdb.asia-southeast1.firebasedatabase.app").reference

    // Add User
    fun addUser(username: String, password: String, firstName: String, lastName: String, onResult: (Boolean) -> Unit) {
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
    fun addExpense(
        category: String,
        amount: Double,
        date: Long,
        description: String,
        imagePath: String,
        onResult: (Boolean) -> Unit
    ) {
        val expenseId = database.child("expenses").push().key ?: return onResult(false)
        val expense = ExpenseEntity(expenseId, category, amount, date, description, imagePath)

        database.child("expenses").child(expenseId).setValue(expense)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    // Get Expense By ID
    fun getExpenseById(id: String, onResult: (ExpenseEntity?) -> Unit) {
        database.child("expenses").child(id).get().addOnSuccessListener { snapshot ->
            val expense = snapshot.getValue(ExpenseEntity::class.java)
            onResult(expense)
        }.addOnFailureListener {
            onResult(null)
        }
    }

    // Fetch All Expenses (Ordered by Date)
    fun getAllExpenses(onResult: (List<ExpenseEntity>) -> Unit) {
        database.child("expenses").orderByChild("date")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val expenses = mutableListOf<ExpenseEntity>()
                    // Firebase orders ascending by default, we reverse to mirror the original DESC order
                    for (child in snapshot.children) {
                        child.getValue(ExpenseEntity::class.java)?.let { expenses.add(it) }
                    }
                    onResult(expenses.reversed())
                }
                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    // Get Expenses by Date Range
    fun getExpensesByDateRange(startDate: Long, endDate: Long, onResult: (List<ExpenseEntity>) -> Unit) {
        database.child("expenses").orderByChild("date")
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
    fun getExpensesByDateAndCategory(
        startDate: Long,
        endDate: Long,
        category: String,
        onResult: (List<ExpenseEntity>) -> Unit
    ) {
        getExpensesByDateRange(startDate, endDate) { expenses ->
            val filtered = expenses.filter { it.category.equals(category, ignoreCase = true) }
            onResult(filtered)
        }
    }

    // Get Total Spent (Grand total or Category total)
    fun getTotalSpent(category: String = "", onResult: (Double) -> Unit) {
        database.child("expenses").addListenerForSingleValueEvent(object : ValueEventListener {
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
    fun getUniqueCategories(onResult: (List<String>) -> Unit) {
        database.child("expenses").addListenerForSingleValueEvent(object : ValueEventListener {
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
    fun getAllUsers(onResult: (List<UserEntity>) -> Unit) {
        // Explicitly target the correct node path
        database.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val usersList = mutableListOf<UserEntity>()

                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        try {
                            // Extract properties manually to shield against data class casting failures
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

    fun getCustomBudgetCategories(userId: String, onResult: (List<String>) -> Unit) {
        val combinedCategories = mutableSetOf<String>()

        database.child("CategoryBudget").child(userId).get().addOnSuccessListener { budgetSnapshot ->
            if (budgetSnapshot.exists()) {
                for (child in budgetSnapshot.children) {
                    child.key?.let { combinedCategories.add(it) }
                }
            }

            database.child("expenses").addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
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

    fun updateUserIncome(userId: String, amount: Double, isSideHustle: Boolean, onResult: (Boolean) -> Unit) {
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

                userRef.updateChildren(updates)
                    .addOnSuccessListener { onResult(true) }
                    .addOnFailureListener { onResult(false) }
            } else {
                onResult(false)
            }
        }.addOnFailureListener { onResult(false) }
    }

    fun updateProfileImage(userId: String, imageUrlString: String, onResult: (Boolean) -> Unit) {
        database.child("users").child(userId).child("profileImageUrl").setValue(imageUrlString)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun getUserProfile(userId: String, onResult: (DataSnapshot) -> Unit) {
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

    fun updateUserProfileFields(userId: String, updatesMap: Map<String, Any>, onResult: (Boolean) -> Unit) {
        database.child("users").child(userId).updateChildren(updatesMap)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { exception ->
                Log.e("SmartSpendDB", "Failed to update profile nodes: ${exception.message}")
                onResult(false)
            }
    }
}