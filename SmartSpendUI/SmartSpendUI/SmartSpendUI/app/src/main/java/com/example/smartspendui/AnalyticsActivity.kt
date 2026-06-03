package com.example.smartspendui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Color
import android.widget.Toast
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// PostHog (2024) demonstrates how to set up  an analytics class

class AnalyticsActivity : AppCompatActivity() { // we created the class to handle the financial data visualization

    private val dbHelper = DatabaseHelper() // calls databasehelper and pulls from Firebase database
    private var currentUserId: String= ""

    private lateinit var rvCategories: RecyclerView
    private lateinit var barChart: BarChart

    private lateinit var actvPeriod: AutoCompleteTextView
    private lateinit var actvMonth: AutoCompleteTextView

    private var selectedPeriod = "1 Month"
    private var selectedMonth = "June"

    private var customStartDate: Long? = null
    private var customEndDate: Long? = null

    private val months = arrayOf(
        "January", "February", "March", "April",
        "May", "June", "July", "August",
        "September", "October", "November", "December"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.analytics_page) // we linked the code to our analytics xml layout

        Log.d("SmartSpend", "AnalyticsActivity: Ready for category population")

        rvCategories = findViewById(R.id.rvCategoryList)
        rvCategories.layoutManager = LinearLayoutManager(this)
        barChart = findViewById(R.id.barChart)

        actvPeriod = findViewById(R.id.actvPeriod)
        actvMonth = findViewById(R.id.actvMonth)

        val periods = arrayOf(
            "1 Week",
            "2 Weeks",
            "1 Month",
            "3 Months",
            "6 Months",
            "1 Year",
            "All Time",
            "Custom Range"
        )

        // we linked array string resources to list choice spinners dropdowns
        actvPeriod.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, periods)
        )

        actvMonth.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, months)
        )

        actvPeriod.setOnClickListener {
            actvPeriod.showDropDown()
        }

        actvMonth.setOnClickListener {
            actvMonth.showDropDown()
        }

        actvPeriod.setText(selectedPeriod, false)
        actvMonth.setText(selectedMonth, false)

        // we monitored dropdown selectors changes to apply time filters dynamically
        actvPeriod.setOnItemClickListener { _, _, position, _ ->

            selectedPeriod = periods[position]

            when (selectedPeriod) {

                "1 Month" -> {
                    actvMonth.isEnabled = true
                }

                "Custom Range" -> {
                    actvMonth.isEnabled = false
                    showStartDatePicker()
                }

                else -> {
                    actvMonth.isEnabled = false
                }
            }

            syncCloudAnalyticsData()
        }

        actvMonth.setOnItemClickListener { _, _, position, _ ->

            selectedMonth = months[position]
            syncCloudAnalyticsData()
        }

        // we checked local shared preferences state keys to find active user token parameters
        val prefs = getSharedPreferences("SmartSpendPrefs", MODE_PRIVATE)
        currentUserId = prefs.getString("CURRENT_USER_ID", "") ?: ""

        if (currentUserId.isEmpty()) {
            Toast.makeText(this, "Session expired. Please log in.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // we called functions to initialize nav buttons
        setupNavigation()

    }
    override fun onResume() {
        super.onResume()
        syncCloudAnalyticsData()
        updateProgressBars()
    }

    //Jahoda (2020) demonstrates how to implement a bar chart
    private fun syncCloudAnalyticsData() { // we pulled and structured the database metrics to rebuild the graph views
        dbHelper.getAllExpenses(currentUserId) { expensesList ->

            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            val now = Calendar.getInstance()

            // we filtered the master transaction records based on the chosen timeframe options
            val filteredExpenses = expensesList.filter { expense -> // filter transaction lists parameters according to the selected timeline constraints

                val expenseTime = expense.date

                val expenseCal = Calendar.getInstance()
                expenseCal.timeInMillis = expenseTime

                when (selectedPeriod) {

                    "1 Week" -> {
                        val weekAgo = Calendar.getInstance()
                        weekAgo.add(Calendar.DAY_OF_YEAR, -7)

                        expenseTime >= weekAgo.timeInMillis
                    }

                    "2 Weeks" -> {
                        val twoWeeksAgo = Calendar.getInstance()
                        twoWeeksAgo.add(Calendar.DAY_OF_YEAR, -14)

                        expenseTime >= twoWeeksAgo.timeInMillis
                    }

                    "1 Month" -> {
                        val monthIndex = months.indexOf(selectedMonth)

                        expenseCal.get(Calendar.MONTH) == monthIndex &&
                                expenseCal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                    }

                    "3 Months" -> {
                        val threeMonthsAgo = Calendar.getInstance()
                        threeMonthsAgo.add(Calendar.MONTH, -3)

                        expenseTime >= threeMonthsAgo.timeInMillis
                    }

                    "6 Months" -> {
                        val sixMonthsAgo = Calendar.getInstance()
                        sixMonthsAgo.add(Calendar.MONTH, -6)

                        expenseTime >= sixMonthsAgo.timeInMillis
                    }

                    "1 Year" -> {
                        val yearAgo = Calendar.getInstance()
                        yearAgo.add(Calendar.YEAR, -1)

                        expenseTime >= yearAgo.timeInMillis
                    }

                    "All Time" -> true

                    "Custom Range" -> {
                        if (customStartDate == null || customEndDate == null) {
                            false
                        } else {
                            expenseTime in customStartDate!!..customEndDate!!
                        }
                    }

                    else -> true
                }
            }

            // we grouped expenses by category and computed the subtotal floating point amounts
            val categoryTotalsMap =
                filteredExpenses
                    .groupBy { it.category }
                    .mapValues { entry -> entry.value.sumOf { it.amount }.toFloat() }

            // we iterated through all known categories to pull parallel budget boundaries from cloud endpoints
            dbHelper.getCustomBudgetCategories(currentUserId) { customCategories ->

                val allCategories = (categoryTotalsMap.keys + customCategories).distinct().toList()

                if (allCategories.isEmpty()) { // prevents the app from crashing if the graph doesn't have any data to read
                    runOnUiThread {
                        rvCategories.adapter = CategoryAdapter(emptyMap()) { }

                        barChart.clear()

                        barChart.setNoDataText("No financial data found. Log an expense or budget goal to begin tracking!")
                        barChart.setNoDataTextColor(Color.GRAY)
                        barChart.invalidate()
                    }
                    return@getCustomBudgetCategories // Safely exit early
                }

                val minEntries = ArrayList<BarEntry>()
                val actualEntries = ArrayList<BarEntry>()
                val maxEntries = ArrayList<BarEntry>()

                var remainingCategoryQueries = allCategories.size
                val lock = Any()

                val completeCategoryMap = allCategories.associateWith { category ->
                    categoryTotalsMap[category] ?: 0f
                }

                if (remainingCategoryQueries == 0) {
                    runOnUiThread {
                        rvCategories.adapter =
                            CategoryAdapter(categoryTotalsMap) {}
                    }
                    return@getCustomBudgetCategories
                }

                for (i in allCategories.indices) {
                    val category = allCategories[i]
                    val actualAmount = categoryTotalsMap[category]?.toFloat() ?: 0f
                    val currentX = i.toFloat()

                    dbHelper.getCategoryBudget(currentUserId, category) { minGoalStr, maxGoalStr ->
                        val minGoal = minGoalStr.toFloatOrNull() ?: 0f
                        val maxGoal = maxGoalStr.toFloatOrNull() ?: 0f

                        // we dynamically appended chart coordinates inside a thread-safe synchronized lock
                        synchronized(lock) {
                            // map the entry locations using clean floating point index coordinates
                            minEntries.add(BarEntry(currentX, minGoal))
                            actualEntries.add(BarEntry(currentX, actualAmount))
                            maxEntries.add(BarEntry(currentX, maxGoal))

                            remainingCategoryQueries--

                            if (remainingCategoryQueries == 0) {
                                // we sorted dataset coordinate lists along the horizontal grid scale before drawing
                                minEntries.sortBy{ it.x }
                                actualEntries.sortBy{ it.x }
                                maxEntries.sortBy{ it.x }

                                runOnUiThread {
                                    // we bound summarized categories and attached navigation click listeners for single category drilling
                                    val adapter = CategoryAdapter(categoryTotalsMap) { clickedCategory ->
                                        Log.d("SmartSpend", "Category row clicked: $clickedCategory")

                                        val intent = Intent(this@AnalyticsActivity,
                                            TransactionHistoryActivity::class.java)
                                        intent.putExtra("CATEGORY_FILTER", clickedCategory)
                                        startActivity(intent)
                                    }
                                    rvCategories.adapter = adapter

                                    val minSet = BarDataSet(minEntries, "Min Goal").apply { color = Color.GREEN }
                                    val actualSet = BarDataSet(actualEntries, "Actual Spent").apply { color = Color.BLUE }
                                    val maxSet = BarDataSet(maxEntries, "Max Goal").apply { color = Color.RED }

                                    val barData = BarData(minSet, actualSet, maxSet)

                                    val groupSpace = 0.28f
                                    val barSpace = 0.04f
                                    val barWidth = 0.20f

                                    barData.barWidth = barWidth
                                    barChart.data = barData

                                    val xAxis = barChart.xAxis
                                    // we enforced safe array index boundary checks to shield the chart axis labels from crashing
                                    xAxis.valueFormatter = object : ValueFormatter() {
                                        override fun getFormattedValue(value: Float): String {
                                            val index = value.toInt()
                                            return if (index >= 0 && index < allCategories.size) {
                                                allCategories[index]
                                            } else {
                                                ""
                                            }
                                        }
                                    }
                                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                                    xAxis.granularity = 1f
                                    xAxis.setCenterAxisLabels(true)

                                    xAxis.axisMinimum = 0f
                                    xAxis.axisMaximum = 0f + barChart.barData.getGroupWidth(groupSpace, barSpace) * allCategories.size

                                    // we styled dataset bars and grouped columns across multi-entry structural calculations
                                    barChart.axisRight.isEnabled = false
                                    barChart.description.isEnabled = false
                                    barChart.setFitBars(true)

                                    barChart.groupBars(0f, groupSpace, barSpace)
                                    barChart.animateY(1000)
                                    barChart.invalidate()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateProgressBars() { // we fetched user income thresholds to track overall budget performance metrics
        val pbGoal = findViewById<ProgressBar>(R.id.pbMonthlyGoal)
        val tvStatus = findViewById<TextView>(R.id.tvGoalStatusText)

        if (currentUserId.isEmpty())
        {
            return
        }

        // Fetch our parent Cash Flow track data metrics profile records from the server node
        dbHelper.getUserProfile(currentUserId) { snapshot ->
            if (snapshot.exists()) {
                val totalIncome = snapshot.child("totalIncome").value?.toString()?.toDoubleOrNull() ?: 0.0
                val monthlySalary = snapshot.child("monthlySalary").value?.toString()?.toDoubleOrNull() ?: 0.0

                // Use monthlySalary as the baseline baseline target parameter tracking floor
                dbHelper.getTotalSpent(currentUserId) { totalSpent ->
                    runOnUiThread {
                                               
                        if (monthlySalary > 0)
                        {
                            pbGoal.progress = ((totalSpent / monthlySalary) * 100).toInt()
                            pbGoal.progressTintList =
                                ColorStateList.valueOf(Color.parseColor("#00FFFF"))

                            if  (pbGoal.progress >= 100)
                            {
                                pbGoal.progressTintList =
                                    ColorStateList.valueOf(Color.parseColor("#F44336"))
                            }
                        }
                        else
                        {
                            pbGoal.progress = 0
                        }


                        val healthStatus = when {
                            totalSpent < (monthlySalary * 0.5) -> "Under Budget (Excellent)"
                            totalSpent <= monthlySalary -> "Within Budget (Good)"
                            else -> "Over Budget (Warning)"
                        }

                        tvStatus.text = "Spent R${String.format("%.2f", totalSpent)} of R${String.format("%.2f", monthlySalary)}\nStatus: $healthStatus"
                    }
                }
            }
        }
    }

    private fun setupNavigation() { // We defined click listeners to navigate between the different app screens
        findViewById<Button>(R.id.btnViewLog)?.setOnClickListener {
            val intent = Intent(this, TransactionHistoryActivity::class.java)
            // we added flags to clear the history stack and refresh the page (refreshes the page instead of creating a new instance everytime)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            Log.d("SmartSpend", "Navigating from Analytics to Transaction History")
            startActivity(intent)
        }

        // we configured click vectors to handle smooth switching between app activities
        findViewById<Button>(R.id.btnNavHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            // we added flags to clear the history stack and refresh the page (refreshes the page instead of creating a new instance everytime)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
        findViewById<Button>(R.id.btnNavEntry).setOnClickListener {
            val intent = Intent(this, ExpenseEntryActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
        findViewById<Button>(R.id.btnNavBudget).setOnClickListener {
            val intent = Intent(this, BudgetActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }

    private fun showStartDatePicker() { // we displayed a native date picker dialog to record custom sequence start times

        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->

                val startCal = Calendar.getInstance()
                startCal.set(year, month, dayOfMonth)

                customStartDate = startCal.timeInMillis

                showEndDatePicker()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showEndDatePicker() { // we recorded custom sequence end times and initiated data refresh sequences

        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->

                val endCal = Calendar.getInstance()
                endCal.set(year, month, dayOfMonth)

                customEndDate = endCal.timeInMillis

                syncCloudAnalyticsData()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}

// PostHog, 2024.  How to set up analytics in Android. (Version 2.0) [Source code]
// Available at: < https://posthog.com/tutorials/android-analytics > [Accessed 26 April 2026].

// Jahoda, P., 2020.  MPAndroidChart. (Version 3.1.0) [Source code]
// Available at: < https://github.com/PhilJay/MPAndroidChart > [Accessed 28 May 2026].