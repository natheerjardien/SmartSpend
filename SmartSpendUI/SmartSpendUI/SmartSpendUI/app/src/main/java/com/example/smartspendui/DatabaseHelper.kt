package com.example.smartspendui

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DatabaseHelper {

    // Get reference to the root of your Firebase Database
    private val database = FirebaseDatabase.getInstance("https://bcad3-st10433896-default-rtdb.asia-southeast1.firebasedatabase.app").reference

    // 1. Add User
    fun addUser(username: String, password: String, firstName: String, lastName: String, onResult: (Boolean) -> Unit) {
        val userId = database.child("users").push().key ?: return onResult(false)
        val user = UserEntity(
            uid = userId,
            username = username,
            password = password,
            firstName = firstName,
            lastName = lastName
        )

        database.child("users").child(userId).setValue(user)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }



    // 2. Add Expense
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

    // 3. Get Expense By ID
    fun getExpenseById(id: String, onResult: (ExpenseEntity?) -> Unit) {
        database.child("expenses").child(id).get().addOnSuccessListener { snapshot ->
            val expense = snapshot.getValue(ExpenseEntity::class.java)
            onResult(expense)
        }.addOnFailureListener {
            onResult(null)
        }
    }

    // 4. Fetch All Expenses (Ordered by Date)
    fun getAllExpenses(onResult: (List<ExpenseEntity>) -> Unit) {
        database.child("expenses").orderByChild("date")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val expenses = mutableListOf<ExpenseEntity>()
                    // Firebase orders ascending by default, we reverse to mirror your original DESC order
                    for (child in snapshot.children) {
                        child.getValue(ExpenseEntity::class.java)?.let { expenses.add(it) }
                    }
                    onResult(expenses.reversed())
                }
                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    // 5. Get Expenses by Date Range
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

    // 6. Get Expenses by Date and Category
    // Note: Firebase Realtime DB doesn't cleanly support multi-property queries natively without composite keys.
    // The easiest approach is filtering the category client-side from the date range results.
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

    // 7. Get Total Spent (Grand total or Category total)
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

    // 8. Get Unique Categories
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

    // 9. Get All Users
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
}