package com.example.smartspendui

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class DatabaseHelperTest {

    private lateinit var db: DatabaseHelper
    private fun newUserId() = "test_" + System.currentTimeMillis()

    @Before
    fun setup() {
        db = DatabaseHelper()
    }

    @After
    fun tearDown() {
        // Firebase cannot be fully wiped easily in tests,
        // but we close references / reset objects
    }

    // 1. Test Add User
    @Test
    fun testAddUser() {
        val latch = CountDownLatch(1)
        var result = false
        val userId = newUserId()

        db.addUser("testUser", "1234", "Test", "User") {
            result = it
            latch.countDown()
        }

        latch.await(5, TimeUnit.SECONDS)
        assertTrue(result)
    }

    // 2. Test Add Expense
    @Test
    fun testAddExpense() {
        val latch = CountDownLatch(1)
        var result = false
        val userId = newUserId()

        db.addExpense(userId, "Food", 50.0, System.currentTimeMillis(), "Lunch", "") {
            result = it
            latch.countDown()
        }

        latch.await(5, TimeUnit.SECONDS)
        assertTrue(result)
    }

    // 3. Test Get Expense By ID
    @Test
    fun testGetExpenseById() {
        val latch = CountDownLatch(1)
        val userId = newUserId()

        db.addExpense(userId, "Food", 30.0, System.currentTimeMillis(), "Test", "") { success ->
            assertTrue(success)

            db.getAllExpenses(userId) { list ->
                val expense = list.firstOrNull()

                if (expense != null) {
                    db.getExpenseById(userId, expense.uid) { result ->
                        assertNotNull(result)
                        assertEquals("Food", result?.category)
                        latch.countDown()
                    }
                } else {
                    latch.countDown()
                    fail("No expense found")
                }
            }
        }

        latch.await(8, TimeUnit.SECONDS)
    }

    // 4. Test Get All Expenses
    @Test
    fun testGetAllExpenses() {
        val latch = CountDownLatch(1)
        val userId = newUserId()

        db.addExpense(userId, "Transport", 20.0, System.currentTimeMillis(), "", "") {
            db.getAllExpenses(userId) { list ->
                assertTrue(list.isNotEmpty())
                latch.countDown()
            }
        }

        latch.await(5, TimeUnit.SECONDS)
    }

    // 5. Test Total Spent (All)
    @Test
    fun testTotalSpentAll() {
        val latch = CountDownLatch(1)
        val userId = newUserId()

        db.addExpense(userId, "Food", 10.0, System.currentTimeMillis(), "", "") {
            db.addExpense(userId, "Food", 20.0, System.currentTimeMillis(), "", "") {

                db.getTotalSpent(userId, "") { total ->
                    assertEquals(30.0, total, 0.01)
                    latch.countDown()
                }
            }
        }

        latch.await(8, TimeUnit.SECONDS)
    }

    // 6. Test Total Spent by Category
    @Test
    fun testTotalSpentByCategory() {
        val latch = CountDownLatch(1)
        val userId = newUserId()

        db.addExpense(userId, "Groceries", 40.0, System.currentTimeMillis(), "", "") {
            db.getTotalSpent(userId, "Groceries") { total ->
                assertEquals(40.0, total, 0.01)
                latch.countDown()
            }
        }

        latch.await(5, TimeUnit.SECONDS)
    }

    // 7. Test Unique Categories
    @Test
    fun testUniqueCategories() {
        val latch = CountDownLatch(1)
        val userId = newUserId()

        db.addExpense(userId, "Bills", 100.0, System.currentTimeMillis(), "", "") {
            db.getUniqueCategories(userId) { categories ->
                assertTrue(categories.contains("Bills"))
                latch.countDown()
            }
        }

        latch.await(5, TimeUnit.SECONDS)
    }

    // 8. Test Category Totals
    @Test
    fun testCategoryTotals() {
        val latch = CountDownLatch(1)
        val userId = newUserId()

        db.addExpense(userId, "Food", 25.0, System.currentTimeMillis(), "", "") {
            db.getTotalSpent(userId, "Food") { total ->
                assertTrue(total > 0)
                latch.countDown()
            }
        }

        latch.await(5, TimeUnit.SECONDS)
    }

    // 9. Test Date Range Filter
    @Test
    fun testDateRangeFilter() {
        val latch = CountDownLatch(1)
        val now = System.currentTimeMillis()
        val userId = newUserId()

        db.addExpense(userId, "Test", 15.0, now, "", "") {
            db.getExpensesByDateRange(userId, now - 1000, now + 1000) { list ->
                assertTrue(list.isNotEmpty())
                latch.countDown()
            }
        }

        latch.await(5, TimeUnit.SECONDS)
    }

    // 10. Test Date + Category Filter
    @Test
    fun testDateAndCategoryFilter() {
        val latch = CountDownLatch(1)
        val now = System.currentTimeMillis()
        val userId = newUserId()

        db.addExpense(userId, "Health", 60.0, now, "", "") {
            db.getExpensesByDateAndCategory(userId, now - 1000, now + 1000, "Health") { list ->
                assertTrue(list.isNotEmpty())
                latch.countDown()
            }
        }

        latch.await(5, TimeUnit.SECONDS)
    }

    // 11. Test Invalid Category Filter
    @Test
    fun testInvalidCategoryFilter() {
        val latch = CountDownLatch(1)
        val now = System.currentTimeMillis()
        val userId = newUserId()

        db.getExpensesByDateAndCategory(userId, now - 1000, now + 1000, "NonExistent") { list ->
            assertTrue(list.isEmpty())
            latch.countDown()
        }

        latch.await(5, TimeUnit.SECONDS)
    }

    // 12. Test Get All Users
    @Test
    fun testGetAllUsers() {
        val latch = CountDownLatch(1)
        val userId = newUserId()

        db.addUser("user1", "pass", "First", "Last") {
            db.getAllUsers { users ->
                assertTrue(users.isNotEmpty())
                latch.countDown()
            }
        }

        latch.await(5, TimeUnit.SECONDS)
    }

    // 13. Test Multiple Expense Sum
    @Test
    fun testMultipleExpenseSum() {
        val latch = CountDownLatch(1)
        val userId = newUserId()

        db.addExpense(userId, "A", 10.0, System.currentTimeMillis(), "", "") {
            db.addExpense(userId, "A", 15.0, System.currentTimeMillis(), "", "") {

                db.getTotalSpent(userId, "A") { total ->
                    assertEquals(25.0, total, 0.01)
                    latch.countDown()
                }
            }
        }

        latch.await(8, TimeUnit.SECONDS)
    }

    // 14. Test Expense Description
    @Test
    fun testExpenseDescription() {
        val latch = CountDownLatch(1)
        val userId = newUserId()

        db.addExpense(userId, "Misc", 5.0, System.currentTimeMillis(), "Test Desc", "") {
            db.getAllExpenses(userId) { list ->
                val expense = list.firstOrNull()

                assertNotNull(expense)
                assertEquals("Test Desc", expense?.description)
                latch.countDown()
            }
        }

        latch.await(5, TimeUnit.SECONDS)
    }

    // 15. Test Image Path Stored
    @Test
    fun testImagePathStored() {
        val latch = CountDownLatch(1)
        val userId = newUserId()

        db.addExpense(userId, "Misc", 5.0, System.currentTimeMillis(), "", "path123") {
            db.getAllExpenses(userId) { list ->
                val expense = list.firstOrNull()

                assertNotNull(expense)
                assertEquals("path123", expense?.imagePath)
                latch.countDown()
            }
        }

        latch.await(5, TimeUnit.SECONDS)
    }
}