package com.example.smartspendui

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "SmartSpend.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE UserEntity (uid INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, password TEXT)")

        db.execSQL("CREATE TABLE ExpenseEntity (uid INTEGER PRIMARY KEY AUTOINCREMENT, category TEXT, amount REAL, date LONG, description TEXT, imagePath TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS UserEntity")
        db.execSQL("DROP TABLE IF EXISTS ExpenseEntity")
        onCreate(db)
    }

    fun addUser(username: String, password: String): Long {
        val db = this.writableDatabase

        val values = ContentValues().apply {
            put("username", username)
            put("password", password)
        }

        return db.insert("UserEntity", null, values)
    }

    fun addExpense(category: String, amount: Double, date: Long, description: String, imagePath: String): Long {
        val db = this.writableDatabase

        val values = ContentValues().apply {
            put("category", category)
            put("amount", amount)
            put("date", date)
            put("description", description)
            put("imagePath", imagePath)
        }

        return db.insert("ExpenseEntity", null, values)
    }

    fun getExpenseById(id: Int): ExpenseEntity? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ExpenseEntity WHERE uid = ?", arrayOf(id.toString()))

        var expense: ExpenseEntity? = null

        if (cursor.moveToFirst())
        {
            expense = ExpenseEntity(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getDouble(2),
                cursor.getLong(3),
                cursor.getString(4),
                cursor.getString(5)
            )
        }

        cursor.close()

        return expense
    }


    fun getExpensesByDateRange(startDate: Long, endDate: Long): Cursor {
        val db = this.readableDatabase

        return db.rawQuery(
            "SELECT * FROM ExpenseEntity WHERE date >= ? AND date <= ? ORDER BY date DESC",
            arrayOf(startDate.toString(), endDate.toString())
        )
    }

    fun getExpensesByDateAndCategory(startDate: Long, endDate: Long, category: String): Cursor {
        val db = this.readableDatabase

        return db.rawQuery(
            "SELECT * FROM ExpenseEntity WHERE date >= ? AND date <= ? AND category = ? ORDER BY date DESC",
            arrayOf(startDate.toString(), endDate.toString(), category)
        )
    }

    fun getCategoryTotalByDate(startDate: Long, endDate: Long, category: String): Double {
        val db = this.readableDatabase

        val cursor = db.rawQuery(
            "SELECT SUM(amount) FROM ExpenseEntity WHERE date >= ? AND date <= ? AND category = ?",
            arrayOf(startDate.toString(), endDate.toString(), category)
        )
        var total = 0.0

        if (cursor.moveToFirst())
        {
            total = cursor.getDouble(0)
        }

        cursor.close()

        return total
    }

    fun getAllExpenses(): Cursor {
        val db = this.readableDatabase

        return db.rawQuery("SELECT * FROM ExpenseEntity ORDER BY date DESC", null)
    }

    fun getAllUsers(): Cursor {
        val db = this.readableDatabase

        return db.rawQuery("SELECT * FROM userEntity", null)
    }

    fun getCategoryTotals(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery(
            "SELECT category, SUM(amount) FROM ExpenseEntity GROUP BY category",
            null
        )
    }

    fun getTotalSpent(category: String = ""): Double {
        val db = this.readableDatabase
        val cursor: Cursor

        if (category.isEmpty())
        {
            cursor = db.rawQuery("SELECT SUM(amount) FROM ExpenseEntity", null)
        }
        else
        {
            cursor = db.rawQuery(
                "SELECT SUM(amount) FROM ExpenseEntity WHERE category = ?",
                arrayOf(category)
            )
        }

        var total = 0.0

        if (cursor.moveToFirst())
        {
            total = cursor.getDouble(0)
        }
        cursor.close()
        return total
    }

    fun getUniqueCategories(): List<String> {
        val categories = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT DISTINCT category FROM ExpenseEntity", null)
        if (cursor.moveToFirst()) {
            do { categories.add(cursor.getString(0)) } while (cursor.moveToNext())
        }
        cursor.close()
        return categories
    }
}