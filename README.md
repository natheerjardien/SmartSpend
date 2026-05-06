# SmartSpend

**SmartSpend** is an Android-based personal finance management application developed to help users track expenses, set budget goals, and visualize spending habits through detailed analytics.

---

## Features

*   **User Authentication**: Secure registration and login system to protect personal financial data.
*   **Expense Tracking**: Log expenses with specific categories, amounts, dates, and descriptions.
*   **Digital Receipts**: Attach and view photos of receipts using the device's storage for better record-keeping.
*   **Budget Management**: Set monthly minimum and maximum budget goals to monitor financial health.
*   **Dynamic Analytics**: View spending totals by category and track progress against goals via interactive progress bars.
*   **Transaction History**: Filter and search through past transactions by date range and category.

---

## Project Structure

The application is built using **Kotlin** and follows a standard Android architectural pattern:

### Activities & Logic
*   **`MainActivity.kt`**: The central dashboard showing current balance and budget health.
*   **`ExpenseEntryActivity.kt`**: Handles the input of new transactions, including image picking.
*   **`AnalyticsActivity.kt`**: Manages the visualization of category-wise spending and budget status.
*   **`BudgetActivity.kt`**: Allows users to configure and save their monthly budget thresholds.
*   **`TransactionHistoryActivity.kt`**: Displays a scrollable list of all recorded expenses.
*   **`DatabaseHelper.kt`**: Manages the SQLite database for local storage of users and expenses.

### Data Models & Adapters
*   **`ExpenseEntity.kt`**: A data class representing the structure of an expense record.
*   **`CategoryAdapter.kt`**: Bridges database cursor data to the Analytics RecyclerView.
*   **`TransactionAdapter.kt`**: Manages the display of individual transaction rows in the history log.

---

## Tech Stack

*   **Language**: Kotlin
*   **Database**: SQLite
*   **Image Loading**: Glide
*   **UI Components**: RecyclerView, ProgressBar, DatePickerDialog, Spinner
*   **Storage**: SharedPreferences (for budget settings) and Local Internal Storage (for images)

---

## Getting Started

### Prerequisites
*   Android Studio (Ladybug or newer recommended)
*   JDK 17 or higher
*   Android SDK Level 34+
---

## Tests Screenshot (Success)

<img width="1920" height="1021" alt="image" src="https://github.com/user-attachments/assets/800b9eb4-b6b8-4e0f-80ae-bfed7b6edb22" />

---

## Workflow (Success)
<img width="1219" height="519" alt="image" src="https://github.com/user-attachments/assets/eb22c8d5-1199-4c52-8206-08bdf962cac1" />


## Credits
* Natheer Jardien ST10435542
* Kyle Daniels ST10433896
* Anique Campher ST10438712
