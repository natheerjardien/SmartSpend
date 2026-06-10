# 📈 SmartSpend

**SmartSpend** is an Android-based personal finance management application developed to help users track expenses, set budget goals and visualize spending habits through detailed analytics.

## ⏱️ Recent Updates

<details>
<summary><b>View New Features (Dark Mode & Motivational Quotes)</b></summary>

### 1. Manual Light / Dark Mode Toggle 🌗
We added a fully customized theme switching engine that overrides the device's system configurations to let users control their visual space on demand.

* **How it works:** 
  * We built a user-facing toggle button (`btnToggleTheme`) inside the layout options.
  * We stored the selected boolean setting state permanently within the device cache file layers via `SharedPreferences` (`IS_DARK_MODE`).
  * We modified the main initialization launch structures inside our boot files to read user preference keys right at runtime launch windows, eliminating interface flickering.
  * We configured customized attribute pointer nodes in our style resource branch (`values-night/themes.xml`) to flip standard layout text colors to high-contrast white dynamically.

---

### 2. Contextual Motivational Financial Quotes 📝
We implemented a dynamic textual notification system to encourage smarter spending and better budget management directly from our tracking loops.

* **How it works:**
  * We embedded a curated sequence array block housing diverse asset wisdom and savings advice targets.
  * We integrated a random selector index mechanism to serve a fresh motivational prompt to the screen layouts whenever a user boots into their dashboard view space.
  * We bound the output text views directly into our clean, lightweight UI context handlers so it scales nicely alongside target theme swaps.

</details>

---

## 📋 Project Architecture & Implementation Report

<details>
<summary><b>View Comprehensive Project Report (Purpose, Design & CI/CD)</b></summary>

### 1. Purpose of the Application
**SmartSpendUI** is a lightweight, mobile financial management application engineered to give users granular tracking over their income streams, liquid cash cashflows and categorical spending habits. Designed primarily to address the challenges of personal budgeting, the platform bridges the gap between passive tracking and active financial behavioral adjustments through the following core pillars:
* **Centralized Expense Logging:** Users can systematically catalog expenses by amount, description, date timestamps and specific transaction categories while physically attaching digital receipt photo assets for verification.
* **Dynamic Budget Boundary Scoping:** Instead of strict spending limits, the app uses a flexible maximum and minimum threshold goal per category, allowing users to establish realistic protective financial highs and lows.
* **Real-Time Visual Analytics:** Financial logs are dynamically parsed and drawn onto multi-data-set bar charts which creates an insight of visual spending.

---

### 2. Design & Architecture Considerations
Building a data-driven mobile application requires balancing responsive UI performance with network connectivity and asynchronous transaction processing. The system implements several high-impact architectural choices such as:

#### Data Layer & Isolation Model
* **Decoupled Architecture:** The system relies on a dedicated `DatabaseHelper` class to isolate data manipulation queries from front-end activity presentation files. 
* **User-Branch Segregation:** Data security is achieved by partitioning cloud records strictly under nested user node branches mapping sequentially to `node -> userId -> recordId`. This ensures data privacy and fast indexing while iterating through the database.

#### UI Thread Synchronization & Memory Stability
* **Asynchronous Main-Thread Safety:** Firebase operations execute non-blocking operations on background worker threads. The app uses strict thread containment rules by executing all database callbacks inside `runOnUiThread {}` scopes to avoid multi-threaded synchronization crashes.
* **Lifecycle-Aware Image Streaming:** Attaching receipts and uploading high-resolution user avatars introduces risks of out-of-memory errors and lifecycle-based file access permission faults. The app uses the **Glide** engine to abstract raw file stream manipulation, apply efficient bitmap compression and provide safe cross-activity image caching.
* **Responsive Layout Controls:** Layout files make use of nested formatting containers alongside system window inset listeners (`ViewCompat.setOnApplyWindowInsetsListener`) to maintain seamless alignment across various screen geometries and physical aspect ratios.

---

### 3. Utilization of GitHub & GitHub Actions (CI/CD Automation)
To maintain structural codebase integrity and facilitate reliable collaborative changes across group members, the repository relies on structured source control management and continuous integration pipelines.

#### Repository Architecture
* **Version Control Scoping:** Version control management is structured to maintain project clean rooms by enforcing systematic `.gitignore` filters across standard local metadata locations (local compiler configurations, `.idea/` workspace files, cache pools and generated output builds. This protects code integrity and avoids repository pollution.

#### Continuous Integration via GitHub Actions
The project leverages automated testing and continuous integration workflows defined within the **`.github/workflow/build.yml`** file layout. Every time code is pushed or merged into central repository branches, GitHub's automated virtual runners instantly trigger a sequence of verification steps:
1. **Environment Setup:** Configures automated virtual machine environments with corresponding Java Development Kit (JDK) versions and localized caching configurations to speed up subsequent runtime operations.
2. **Dependency Verification:** Validates project-wide build scripts (`build.gradle.kts`) and remote dependency synchronization layouts to guarantee compilation completeness.
3. **Automated Build Checks:** Executes comprehensive compilation triggers through explicit Gradle wrapper commands (`./gradlew assembleDebug` or parallel check frameworks. This completely shields the master codebase from broken integration code and automatically flags any build-breaking errors before compilation files can be generated.

</details>

---

## ⚙️ Features

*   **User Authentication**: Secure registration and login system to protect personal financial data.
*   **Expense Tracking**: Log expenses with specific categories, amounts, dates, and descriptions.
*   **Digital Receipts**: Attach and view photos of receipts using the device's storage for better record-keeping.
*   **Budget Management**: Set monthly minimum and maximum budget goals to monitor financial health.
*   **Dynamic Analytics**: View spending totals by category and track progress against goals via interactive progress bars.
*   **Transaction History**: Filter and search through past transactions by date range and category.

---

## 🔨 Project Structure

The application is built using **Kotlin** and follows a standard Android architectural pattern:

### Activities & Logic
*   **`MainActivity.kt`**: The central dashboard showing current balance and budget health.
*   **`ExpenseEntryActivity.kt`**: Handles the input of new transactions, including image picking.
*   **`AnalyticsActivity.kt`**: Manages the visualization of category-wise spending and budget status.
*   **`BudgetActivity.kt`**: Allows users to configure and save their monthly budget thresholds.
*   **`TransactionHistoryActivity.kt`**: Displays a scrollable list of all recorded expenses.
*   **`DatabaseHelper.kt`**: Manages the SQLite database for local storage of users and expenses.
*   **`CategoryBudgetEntity.kt`**: A data class representing budget limits per category, including minimum and maximum spending goals.
*   **`FilterSearchActivity.kt`**: Provides filtering functionality for transactions based on date range and category.
*   **`LoginActivity.kt`**: Handles user authentication and validates credentials against Firebase user records.
*   **`ProfileActivity.kt`**:Allows users to view and update profile information such as name, income, and profile image.
*   **`SplashActivity.kt`**: Displays the launch screen and handles navigation logic before redirecting users to login or dashboard.
*   **`TransactuionDetailActivity.kt`**: Displays full details of a selected transaction including category, amount, date, description and attached image.

### Data Models & Adapters
*   **`ExpenseEntity.kt`**: A data class representing the structure of an expense record.
*   **`CategoryAdapter.kt`**: Bridges database cursor data to the Analytics RecyclerView.
*   **`TransactionAdapter.kt`**: Manages the display of individual transaction rows in the history log.
*   **`UserEntity.kt`**: A data class representing a user profile including UID, username, password, personal details and profile image URL.

---

## Tech Stack

*   **Language**: Kotlin
*   **Database**: SQLite (Part 2) & Firebase (Part 3)
*   **Image Loading**: Glide
*   **UI Components**: RecyclerView, ProgressBar, DatePickerDialog, Spinner
*   **Storage**: SharedPreferences (for budget settings) and Local Internal Storage (for images)

---

## Getting Started

### Prerequisites
*   Android Studio 
*   JDK 17 or higher
*   Android SDK Level 34+
---

## Tests Screenshot (Part 2 - Success)

<img width="1920" height="1021" alt="image" src="https://github.com/user-attachments/assets/800b9eb4-b6b8-4e0f-80ae-bfed7b6edb22" />

---

## Workflow (Part 2 - Success)
<img width="1219" height="519" alt="image" src="https://github.com/user-attachments/assets/eb22c8d5-1199-4c52-8206-08bdf962cac1" />

## Tests Screenshot (Part 3 - Success)

<img width="1875" height="742" alt="Screenshot 2026-06-03 202127" src="https://github.com/user-attachments/assets/333b0550-02fa-4813-866e-41a51beef583" />

---

## Workflow (Part 3 - Success)

---
## Credits
* Natheer Jardien ST10435542
* Kyle Daniels ST10433896
* Anique Campher ST10438712
