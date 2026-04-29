package com.example.smartspendui

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseHelperTest {

    private lateinit var db: DatabaseHelper

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = DatabaseHelper(context)
    }

    // 1. Test user insertion
    @Test
    fun testAddUser() {
        val result = db.addUser("testUser", "1234")
        assertTrue(result != -1L)
    }

    // 2. Test expense insertion
    @Test
    fun testAddExpense() {
        val result = db.addExpense("Food", 50.0, System.currentTimeMillis(), "Lunch", "")
        assertTrue(result != -1L)
    }

    // 3. Test get expense by ID
    @Test
    fun testGetExpenseById() {
        val id = db.addExpense("Food", 30.0, System.currentTimeMillis(), "Test", "")
        val expense = db.getExpenseById(id.toInt())
        assertNotNull(expense)
        assertEquals("Food", expense?.category)
    }

    // 4. Test get all expenses
    @Test
    fun testGetAllExpenses() {
        db.addExpense("Transport", 20.0, System.currentTimeMillis(), "", "")
        val cursor = db.getAllExpenses()
        assertTrue(cursor.count > 0)
    }

    // 5. Test total spent (all)
    @Test
    fun testTotalSpentAll() {
        db.addExpense("Food", 10.0, System.currentTimeMillis(), "", "")
        db.addExpense("Food", 20.0, System.currentTimeMillis(), "", "")
        val total = db.getTotalSpent("")
        assertTrue(total >= 30.0)
    }

    // 6. Test total spent by category
    @Test
    fun testTotalSpentByCategory() {
        db.addExpense("Groceries", 40.0, System.currentTimeMillis(), "", "")
        val total = db.getTotalSpent("Groceries")
        assertTrue(total >= 40.0)
    }

    // 7. Test unique categories
    @Test
    fun testUniqueCategories() {
        db.addExpense("Bills", 100.0, System.currentTimeMillis(), "", "")
        val categories = db.getUniqueCategories()
        assertTrue(categories.contains("Bills"))
    }

    // 8. Test category totals query
    @Test
    fun testCategoryTotals() {
        db.addExpense("Food", 10.0, System.currentTimeMillis(), "", "")
        val cursor = db.getCategoryTotals()
        assertTrue(cursor.count > 0)
    }

    // 9. Test date range filtering
    @Test
    fun testDateRangeFilter() {
        val now = System.currentTimeMillis()
        db.addExpense("Test", 15.0, now, "", "")
        val cursor = db.getExpensesByDateRange(now - 1000, now + 1000)
        assertTrue(cursor.count > 0)
    }

    // 10. Test date + category filtering
    @Test
    fun testDateAndCategoryFilter() {
        val now = System.currentTimeMillis()
        db.addExpense("Health", 60.0, now, "", "")
        val cursor = db.getExpensesByDateAndCategory(now - 1000, now + 1000, "Health")
        assertTrue(cursor.count > 0)
    }

    // 11. Test empty category filter returns nothing
    @Test
    fun testInvalidCategoryFilter() {
        val now = System.currentTimeMillis()
        val cursor = db.getExpensesByDateAndCategory(now - 1000, now + 1000, "NonExistent")
        assertTrue(cursor.count == 0)
    }

    // 12. Test get all users
    @Test
    fun testGetAllUsers() {
        db.addUser("user1", "pass")
        val cursor = db.getAllUsers()
        assertTrue(cursor.count > 0)
    }

    // 13. Test multiple expenses sum
    @Test
    fun testMultipleExpenseSum() {
        db.addExpense("A", 10.0, System.currentTimeMillis(), "", "")
        db.addExpense("A", 15.0, System.currentTimeMillis(), "", "")
        val total = db.getTotalSpent("A")
        assertTrue(total >= 25.0)
    }

    // 14. Test expense description saved
    @Test
    fun testExpenseDescription() {
        val id = db.addExpense("Misc", 5.0, System.currentTimeMillis(), "Test Desc", "")
        val expense = db.getExpenseById(id.toInt())
        assertEquals("Test Desc", expense?.description)
    }

    // 15. Test image path stored
    @Test
    fun testImagePathStored() {
        val id = db.addExpense("Misc", 5.0, System.currentTimeMillis(), "", "path123")
        val expense = db.getExpenseById(id.toInt())
        assertEquals("path123", expense?.imagePath)
    }
}