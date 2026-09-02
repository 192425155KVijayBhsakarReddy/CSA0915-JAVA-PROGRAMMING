# 🏦 Smart Bank Enterprise Management System

<div align="center">

![Java](https://img.shields.io/badge/Java-8%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-22C55E?style=for-the-badge)
![CI Status](https://img.shields.io/badge/Tests-7%2F7%20Passed-22C55E?style=for-the-badge&logo=github-actions&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-Multi--Threaded%20OOP-1A5276?style=for-the-badge)
![GUI](https://img.shields.io/badge/GUI-AWT%20%2F%20Swing-5C6BC0?style=for-the-badge)
![Persistence](https://img.shields.io/badge/Persistence-JDBC%20%2B%20Serialization-16A085?style=for-the-badge)

**A high-performance, multi-threaded, enterprise-grade banking system built entirely in Java.**

Covers account management, real-time fund transfers, loan lifecycle processing, employee records,
and binary state persistence — all with deadlock-free synchronization and 4,400+ operations/sec throughput.

</div>

---

## 📋 Table of Contents

- [Overview & Features](#-overview--features)
- [System Architecture](#-system-architecture)
- [Concept Coverage](#-concept-coverage)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [Running the Application](#-running-the-application)
- [Automated Test Suite](#-automated-test-suite)
- [GUI Screenshots](#-gui-screenshots)
- [Concurrency & Thread Safety](#-concurrency--thread-safety)
- [Persistence Dual Engine](#-persistence-dual-engine)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🌟 Overview & Features

The **Smart Bank Enterprise Management System** is a full-featured, production-inspired banking platform implementing enterprise Java design patterns:

| Feature | Details |
|---|---|
| 🏦 **Account Management** | Polymorphic Savings & Checking accounts with interest compounding, minimum balance rules, and overdraft protection |
| 💸 **Fund Transfers** | Deadlock-free atomic transfers between any account pair using deterministic ReentrantLock ordering |
| 📑 **Loan Lifecycle** | Full loan pipeline — application → credit evaluation → approval/disbursement → EMI repayments |
| 👥 **Employee Records** | Staff directory with Role-Based Access Control (TELLER → LOAN_OFFICER → BRANCH_MANAGER → ADMIN) |
| ⚡ **Concurrency Lab** | Interactive multi-thread stress tester with live balance invariant verification (4,400+ ops/sec) |
| 💾 **Dual Persistence** | Binary Object Serialization snapshots + JDBC SQLite database with full PreparedStatement CRUD |
| 📊 **AWT/Swing GUI** | 7-tab desktop application with live status bar, modals, sortable JTables, and a dark-terminal audit console |

---

## 🏛️ System Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                      Smart Bank Management System                           │
│                                                                             │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │                    AWT / Swing GUI Layer                             │  │
│  │  Dashboard │ Accounts │ Transactions │ Loans │ Staff │ Concurrency  │  │
│  │                    Lab │ Data & Persistence                          │  │
│  └────────────────────────────┬─────────────────────────────────────────┘  │
│                               │ ActionListeners / SwingWorker               │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │                       Service Layer                                  │  │
│  │     BankService     │  LoanService  │  EmployeeService  │            │  │
│  │     BackupService   │  (Implements BankOperations<T> / LoanOps)      │  │
│  └──────┬─────────────────────┬────────────────────────────────────────┘  │
│         │                     │                                             │
│  ┌──────▼──────────┐   ┌──────▼──────────────┐   ┌──────────────────────┐ │
│  │  Concurrency    │   │   Generic Repository  │   │  Persistence Layer   │ │
│  │  ─────────────  │   │  ─────────────────── │   │  ─────────────────── │ │
│  │  LockManager    │   │  AccountRepository    │   │  DBManager (JDBC)    │ │
│  │  AsyncAudit     │   │  InMemoryRepository   │   │  BackupService       │ │
│  │  StressTester   │   │  Iterators (Custom)   │   │  (ObjectStreams+CSV) │ │
│  └──────┬──────────┘   └──────┬────────────────┘   └─────────────────────┘ │
│         │                     │                                             │
│  ┌──────▼─────────────────────▼──────────────────────────────────────────┐ │
│  │                       Domain Model Layer                              │ │
│  │  Account (abstract) ← SavingsAccount / CheckingAccount               │ │
│  │  Customer │ Employee │ Loan │ Transaction │ AuditLog │ Enums          │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │                 Custom Exception Hierarchy                            │ │
│  │  BankException ← InsufficientFundsException                          │ │
│  │               ← OverdraftLimitExceededException                       │ │
│  │               ← NegativeAmountException                               │ │
│  │               ← InvalidAccountException                               │ │
│  │               ← LoanExceededException                                 │ │
│  │               ← ConcurrencyConflictException                          │ │
│  │               ← UnauthorizedOperationException                        │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📚 Concept Coverage

Every required Java concept is implemented with depth, not just demonstration:

<details>
<summary><strong>1. OOP — Inheritance, Polymorphism & Interfaces</strong></summary>

- **Abstract `Account`** enforces a polymorphic `withdraw()` and `calculateMonthlyInterestOrFee()` contract
- **`SavingsAccount`** adds compound interest calculation and minimum balance enforcement
- **`CheckingAccount`** adds overdraft line of credit and monthly maintenance fee deduction
- **4 interfaces**: `BankOperations<T extends Account>`, `LoanOperations`, `Auditable`, `PersistenceEngine<T>`
- All entities implement `Serializable` and `Comparable<T>`

</details>

<details>
<summary><strong>2. Collections, Generics & Custom Iterators</strong></summary>

- `ConcurrentHashMap<String, Account>` — O(1) thread-safe account lookup
- `CopyOnWriteArrayList<Transaction>` — high-read, thread-safe transaction ledger
- `ArrayList` & `TreeSet` with custom `Comparator<Account>` for multi-criteria balance sorting
- **Generic Repository**: `Repository<ID, T>` → `InMemoryRepository<ID, T>` → `AccountRepository`
- **Custom Iterators**:
  - `AccountFilterIterator` — streams accounts matching balance threshold + active status
  - `TransactionIterator` — streams transactions filtered by account number and date range

</details>

<details>
<summary><strong>3. Custom Exception Hierarchy</strong></summary>

All exceptions extend the checked base `BankException(errorCode, message)`:

| Exception | Triggered When |
|---|---|
| `InsufficientFundsException` | Savings withdrawal would breach minimum balance |
| `OverdraftLimitExceededException` | Checking withdrawal exceeds balance + overdraft limit |
| `NegativeAmountException` | Any deposit or transfer amount ≤ 0 |
| `InvalidAccountException` | Account number not found in registry |
| `LoanExceededException` | Loan amount exceeds customer credit score eligibility |
| `ConcurrencyConflictException` | Lock acquisition timeout in concurrent transfer |
| `UnauthorizedOperationException` | Employee role level too low for operation |

</details>

<details>
<summary><strong>4. Multithreading, Concurrency & Synchronization</strong></summary>

- **`LockManager`**: Deadlock prevention via lexicographically ordered `ReentrantLock(fair=true)` acquisition — the classic *Resource Hierarchy* solution to the Dining Philosophers problem applied to bank transfers
- **Thread Priorities**: `TransactionTask` supports `CRITICAL (MAX)`, `NORMAL`, and `BACKGROUND (MIN)` priorities
- **Producer-Consumer with `wait()` / `notifyAll()`**: `AsyncAuditLogger` decouples audit event production from disk I/O using a shared `LinkedList` bounded queue and daemon consumer thread
- **`ConcurrencyStressTester`**: Uses `ExecutorService`, `CountDownLatch` start gate, and `AtomicInteger` to simultaneously fire N×M transfers and verify `Sum(Initial Assets) == Sum(Final Assets)`

</details>

<details>
<summary><strong>5. AWT / Swing GUI with Layout Managers & Event Listeners</strong></summary>

- `SmartBankFrame`: Main `JFrame` using `BorderLayout` (header, tabs, status bar)
- All panels use `GridLayout`, `BorderLayout`, `FlowLayout`, and `BoxLayout` as appropriate — no scattered components
- `JTabbedPane` with `ChangeListener` auto-refreshes data when switching tabs
- `SwingWorker` runs concurrency stress tests on a background thread, keeping the EDT responsive
- Live status bar using `javax.swing.Timer` refreshing every 1 second

</details>

<details>
<summary><strong>6. Persistence: JDBC + File Streams & Serialization</strong></summary>

**Object Serialization:**
- `BackupService.exportBinarySnapshot()` writes a full `BankStateSnapshot` (Serializable) to `.dat` via `ObjectOutputStream`
- `BackupService.importBinarySnapshot()` fully restores state via `ObjectInputStream`
- `exportTransactionsToCsv()` and `exportAccountsToCsv()` write delimiter-separated text reports via `BufferedWriter`

**JDBC:**
- `DBManager` uses `DriverManager.getConnection()` with SQLite WAL mode for high-concurrency reads
- Schema initialized with `CREATE TABLE IF NOT EXISTS` DDL for 5 tables
- All writes use `PreparedStatement` with parameterized queries (SQL injection safe)
- Full INSERT/UPDATE/SELECT/DELETE support for accounts, customers, transactions, loans, employees

</details>

---

## 📁 Project Structure

```
smart-bank-management-system/
├── .github/
│   └── workflows/
│       └── java-ci.yml              # GitHub Actions: auto compile + test on every push
│
├── src/
│   └── com/
│       └── smartbank/
│           ├── Main.java            # Application launcher + automated CLI test runner
│           ├── model/               # Domain entities & enums
│           │   ├── Account.java     # Abstract base (Serializable, Comparable)
│           │   ├── SavingsAccount.java
│           │   ├── CheckingAccount.java
│           │   ├── Customer.java
│           │   ├── Employee.java
│           │   ├── Loan.java        # EMI formula: E = P·r·(1+r)^n / ((1+r)^n - 1)
│           │   ├── Transaction.java # Immutable ledger record
│           │   ├── AuditLog.java
│           │   └── enums/
│           │       ├── AccountType.java
│           │       ├── TransactionType.java
│           │       ├── LoanStatus.java
│           │       └── EmployeeRole.java
│           ├── interfaces/
│           │   ├── BankOperations.java    # Generic<T extends Account>
│           │   ├── LoanOperations.java
│           │   ├── Auditable.java
│           │   └── PersistenceEngine.java # Generic<T>
│           ├── exceptions/
│           │   ├── BankException.java
│           │   ├── InsufficientFundsException.java
│           │   ├── OverdraftLimitExceededException.java
│           │   ├── NegativeAmountException.java
│           │   ├── InvalidAccountException.java
│           │   ├── LoanExceededException.java
│           │   ├── ConcurrencyConflictException.java
│           │   └── UnauthorizedOperationException.java
│           ├── repository/
│           │   ├── Repository.java              # Generic CRUD interface
│           │   ├── InMemoryRepository.java      # ConcurrentHashMap backed
│           │   ├── AccountRepository.java       # Sorting, filtering, asset aggregation
│           │   └── iterators/
│           │       ├── AccountFilterIterator.java
│           │       └── TransactionIterator.java
│           ├── concurrency/
│           │   ├── LockManager.java             # Deadlock-free ordered lock acquisition
│           │   ├── AsyncAuditLogger.java        # Producer-Consumer wait/notifyAll
│           │   ├── TransactionTask.java         # Priority-aware Callable/Runnable
│           │   └── ConcurrencyStressTester.java # CountDownLatch parallel benchmark
│           ├── database/
│           │   └── DBManager.java               # JDBC + SQLite schema + PreparedStatements
│           ├── service/
│           │   ├── BankService.java             # Central orchestrator (implements interfaces)
│           │   ├── LoanService.java             # Credit evaluation & disbursement
│           │   ├── EmployeeService.java         # RBAC verification
│           │   └── BackupService.java           # Serialization + CSV export
│           ├── gui/
│           │   ├── SmartBankFrame.java          # Main JFrame (7 tabs)
│           │   ├── util/
│           │   │   └── UITheme.java             # Color palette, fonts, component factories
│           │   └── components/
│           │       ├── DashboardPanel.java
│           │       ├── AccountPanel.java
│           │       ├── TransactionPanel.java
│           │       ├── LoanPanel.java
│           │       ├── EmployeePanel.java
│           │       ├── ConcurrencyLabPanel.java
│           │       └── DataStoragePanel.java
│           └── util/
│               └── ScreenshotGenerator.java     # Headless UI screenshot renderer
│
├── .gitignore
├── LICENSE                          # MIT License
├── README.md
├── requirements.txt                 # Python deps for report generator (python-docx)
├── generate_report_docx.py         # Submission Word document generator
├── build.bat                        # Windows: one-click compile
└── run.bat                          # Windows: one-click launch
```

**Stats:** 49 Java source files · ~4,600 lines of code · 9 packages

---

## 🚀 Getting Started

### Prerequisites

| Tool | Version | Notes |
|---|---|---|
| **Java JDK** | 8 or later | [Download Temurin](https://adoptium.net/) |
| **Git** | Any | For cloning the repo |
| **Python 3** *(optional)* | 3.7+ | Only needed for Word document generation |

> **JDBC / SQLite note:** The system works fully in embedded in-memory mode without SQLite.
> To enable the database panel's live SQL queries, drop the `sqlite-jdbc` JAR into `lib/`
> and run with `-cp "build;lib/*"`. See [SQLite JDBC releases](https://github.com/xerial/sqlite-jdbc/releases).

### Clone the Repository

```bash
git clone https://github.com/<your-username>/smart-bank-management-system.git
cd smart-bank-management-system
```

---

## 🖥️ Running the Application

### Windows (One-Click)

```bash
# Compile
build.bat

# Launch GUI
run.bat
```

### Any Platform (Command Line)

**Linux / macOS:**
```bash
# Create output directory
mkdir -p build

# Compile all sources
find src -name "*.java" > sources.txt
javac -encoding UTF-8 -d build @sources.txt

# Launch GUI
java -cp build com.smartbank.Main
```

**Windows PowerShell:**
```powershell
# Compile all sources
New-Item -ItemType Directory -Force build
javac -encoding UTF-8 -d build (Get-ChildItem -Path src -Recurse -Filter *.java).FullName

# Launch GUI
java -cp build com.smartbank.Main
```

---

## ✅ Automated Test Suite

Run the complete integration and unit validation harness (no GUI required):

```bash
java -cp build com.smartbank.Main --test
```

**Expected output:**
```
=================================================
RUNNING SMART BANK COMPREHENSIVE TEST SUITE...
=================================================

[TEST 1] Testing Polymorphism & Account Interest/Fees...
  ✓ Deposit & Polymorphism PASSED. Balance: $14500.0

[TEST 2] Testing Custom Exception Hierarchy (Insufficient Funds / Overdraft)...
  ✓ Custom Exception caught: OverdraftLimitExceededException -> Account 'CA-200002'
    cannot complete withdrawal of $9999999.00. Max possible debit: $6500.00

[TEST 3] Testing Negative Amount Guard...
  ✓ NegativeAmountException correctly thrown: Amount must be strictly positive (> 0)

[TEST 4] Testing Custom Generic Iterator (AccountFilterIterator)...
  ✓ AccountFilterIterator filtered 4 accounts with balance >= $5,000

[TEST 5] Testing Loan Financial EMI Calculation Formula...
  ✓ EMI Calculation accurate: $888.49

[TEST 6] Testing File Streams & Object Serialization (.dat)...
  ✓ Serialization & Deserialization verified. Restored 6 accounts.

[TEST 7] Testing Multithreaded Concurrency & Balance Invariance (Stress Test)...
  Total Ops: 500 | Success: 500 | Failed/Rejected: 0 | Time: 113 ms
  Initial Assets: $142,400.00 | Final Assets: $142,400.00
  Balance Invariant Preserved: YES (PASSED)
  Throughput: 4,424.78 ops/sec
  ✓ MULTITHREADED CONCURRENCY INVARIANT PASSED! 0% Balance Leaks.

=================================================
TEST SUMMARY: 7 PASSED, 0 FAILED
=================================================
```

---

## 🖼️ GUI Screenshots

### Dashboard — Executive KPI Overview
Metric cards (total assets, accounts, clients, loans), recent transaction table, and real-time system health status.

### Accounts & Clients Hub
Register customers, open Savings or Checking accounts with custom parameters, and sort the registry by balance.

### Transactions Ledger
Deposit, withdrawal, and inter-account fund transfer desk with a complete scrollable ledger and CSV export.

### Loan Management Desk
Interactive EMI Calculator, loan application form, one-click officer approval/rejection, and installment repayment tracking.

### Staff Directory (RBAC)
Employee registration, role assignment (Teller → Admin), and access-level management.

### ⚡ Concurrency Lab
Configure thread count and operations per thread, launch the stress test, and watch the live balance invariant verification update in real time.

### Data & Persistence Manager
Binary `.dat` snapshot backup/restore, CSV statement export, and an embedded SQL query console for live database inspection.

---

## ⚡ Concurrency & Thread Safety

The transfer engine uses **Resource Hierarchy Locking** — a deterministic deadlock prevention strategy:

```
Transfer: Account A → Account B

Step 1: Determine lock order (lexicographic by account number)
Step 2: Acquire lock on "smaller" account first (with 5s timeout)
Step 3: Acquire lock on "larger" account second (with 5s timeout)
Step 4: Execute atomic debit + credit inside critical section
Step 5: Release both locks in reverse order (LIFO)

Result: Circular wait is IMPOSSIBLE → Deadlock is IMPOSSIBLE
```

**Audit Logging (Producer-Consumer Pattern):**
```
Main Thread (Producer)           AuditLogger Thread (Consumer)
    |                                      |
    |-- enqueueLog(event) -->  [QUEUE]     |
    |   (notifyAll)             [QUEUE] -- drainQueue() -> writeBatch()
    |                           [QUEUE]     |
    | (continues transaction)               |
```

---

## 💾 Persistence Dual Engine

| Engine | Format | Speed | Use Case |
|---|---|---|---|
| **Object Serialization** | Binary `.dat` | Fast full-state snapshot | System backup & restore |
| **CSV Export** | Plain text `.csv` | Human-readable | Audit reporting, analysis |
| **SQLite JDBC** | Database `.db` | Queryable records | Persistent storage, SQL queries |

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!

1. Fork the repository
2. Create your feature branch: `git checkout -b feature/AmazingFeature`
3. Commit your changes: `git commit -m 'Add AmazingFeature'`
4. Push to the branch: `git push origin feature/AmazingFeature`
5. Open a Pull Request

Please make sure all 7 automated tests still pass after your changes:
```bash
java -cp build com.smartbank.Main --test
```

---

## 📄 License

Distributed under the MIT License. See [`LICENSE`](LICENSE) for more information.

---

<div align="center">
Built with Java · AWT/Swing · JDBC · ReentrantLock · Object Serialization
</div>
