package com.smartbank.database;

import com.smartbank.model.Account;
import com.smartbank.model.CheckingAccount;
import com.smartbank.model.Customer;
import com.smartbank.model.Employee;
import com.smartbank.model.Loan;
import com.smartbank.model.SavingsAccount;
import com.smartbank.model.Transaction;
import com.smartbank.model.enums.AccountType;
import com.smartbank.model.enums.EmployeeRole;
import com.smartbank.model.enums.LoanStatus;
import com.smartbank.model.enums.TransactionType;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * JDBC Database Manager handling schema initialization, connection pooling,
 * and CRUD operations using PreparedStatements, ResultSets, and Transactions.
 */
public class DBManager {
    private static final String DB_DIR = "data";
    private static final String DB_FILE = DB_DIR + "/smartbank.db";
    private static final String JDBC_URL = "jdbc:sqlite:" + DB_FILE;
    private static boolean sqliteDriverAvailable = false;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            sqliteDriverAvailable = true;
        } catch (ClassNotFoundException e) {
            sqliteDriverAvailable = false;
        }
    }

    public DBManager() {
        File dir = new File(DB_DIR);
        if (!dir.exists()) dir.mkdirs();
        if (sqliteDriverAvailable) {
            initSchema();
        }
    }

    public static boolean isSqliteDriverAvailable() {
        return sqliteDriverAvailable;
    }

    public Connection getConnection() throws SQLException {
        if (!sqliteDriverAvailable) {
            throw new SQLException("SQLite JDBC Driver not loaded on classpath.");
        }
        return DriverManager.getConnection(JDBC_URL);
    }

    public void initSchema() {
        if (!sqliteDriverAvailable) return;
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Enable WAL mode for high concurrency in SQLite
            stmt.execute("PRAGMA journal_mode=WAL;");

            // Customers Table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS customers (" +
                "customer_id TEXT PRIMARY KEY, " +
                "name TEXT NOT NULL, " +
                "email TEXT, " +
                "phone TEXT, " +
                "address TEXT, " +
                "credit_score INTEGER, " +
                "kyc_verified INTEGER" +
                ");"
            );

            // Accounts Table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS accounts (" +
                "account_number TEXT PRIMARY KEY, " +
                "customer_id TEXT NOT NULL, " +
                "account_type TEXT NOT NULL, " +
                "balance REAL NOT NULL, " +
                "interest_rate REAL, " +
                "min_balance REAL, " +
                "overdraft_limit REAL, " +
                "maint_fee REAL, " +
                "active INTEGER NOT NULL, " +
                "FOREIGN KEY (customer_id) REFERENCES customers(customer_id)" +
                ");"
            );

            // Transactions Table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS transactions (" +
                "transaction_id TEXT PRIMARY KEY, " +
                "timestamp INTEGER NOT NULL, " +
                "source_acc TEXT, " +
                "target_acc TEXT, " +
                "type TEXT NOT NULL, " +
                "amount REAL NOT NULL, " +
                "resulting_balance REAL NOT NULL, " +
                "successful INTEGER NOT NULL, " +
                "description TEXT" +
                ");"
            );

            // Loans Table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS loans (" +
                "loan_id TEXT PRIMARY KEY, " +
                "customer_id TEXT NOT NULL, " +
                "account_number TEXT, " +
                "principal REAL NOT NULL, " +
                "interest_rate REAL NOT NULL, " +
                "term_months INTEGER NOT NULL, " +
                "monthly_emi REAL NOT NULL, " +
                "remaining_principal REAL NOT NULL, " +
                "remaining_months INTEGER NOT NULL, " +
                "status TEXT NOT NULL, " +
                "application_date INTEGER NOT NULL, " +
                "approval_date INTEGER" +
                ");"
            );

            // Employees Table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS employees (" +
                "employee_id TEXT PRIMARY KEY, " +
                "name TEXT NOT NULL, " +
                "email TEXT, " +
                "role TEXT NOT NULL, " +
                "department TEXT, " +
                "salary REAL NOT NULL, " +
                "hire_date INTEGER NOT NULL" +
                ");"
            );

        } catch (SQLException e) {
            System.err.println("DB Schema initialization error: " + e.getMessage());
        }
    }

    // ==================== CUSTOMER OPERATIONS ====================

    public void saveCustomer(Customer c) throws SQLException {
        if (!sqliteDriverAvailable) return;
        String sql = "INSERT OR REPLACE INTO customers (customer_id, name, email, phone, address, credit_score, kyc_verified) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getCustomerId());
            ps.setString(2, c.getName());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getPhoneNumber());
            ps.setString(5, c.getAddress());
            ps.setInt(6, c.getCreditScore());
            ps.setInt(7, c.isKycVerified() ? 1 : 0);
            ps.executeUpdate();
        }
    }

    public List<Customer> getAllCustomers() throws SQLException {
        List<Customer> list = new ArrayList<>();
        if (!sqliteDriverAvailable) return list;
        String sql = "SELECT * FROM customers";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Customer c = new Customer(
                    rs.getString("customer_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("address"),
                    rs.getInt("credit_score")
                );
                c.setKycVerified(rs.getInt("kyc_verified") == 1);
                list.add(c);
            }
        }
        return list;
    }

    // ==================== ACCOUNT OPERATIONS ====================

    public void saveAccount(Account a) throws SQLException {
        if (!sqliteDriverAvailable) return;
        String sql = "INSERT OR REPLACE INTO accounts (account_number, customer_id, account_type, balance, " +
                     "interest_rate, min_balance, overdraft_limit, maint_fee, active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getAccountNumber());
            ps.setString(2, a.getCustomerId());
            ps.setString(3, a.getAccountType().name());
            ps.setDouble(4, a.getBalance());

            if (a instanceof SavingsAccount) {
                SavingsAccount sa = (SavingsAccount) a;
                ps.setDouble(5, sa.getAnnualInterestRate());
                ps.setDouble(6, sa.getMinimumBalance());
                ps.setDouble(7, 0.0);
                ps.setDouble(8, 0.0);
            } else if (a instanceof CheckingAccount) {
                CheckingAccount ca = (CheckingAccount) a;
                ps.setDouble(5, 0.0);
                ps.setDouble(6, 0.0);
                ps.setDouble(7, ca.getOverdraftLimit());
                ps.setDouble(8, ca.getMonthlyMaintenanceFee());
            }

            ps.setInt(9, a.isActive() ? 1 : 0);
            ps.executeUpdate();
        }
    }

    public List<Account> getAllAccounts() throws SQLException {
        List<Account> list = new ArrayList<>();
        if (!sqliteDriverAvailable) return list;
        String sql = "SELECT * FROM accounts";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String accNum = rs.getString("account_number");
                String custId = rs.getString("customer_id");
                String typeStr = rs.getString("account_type");
                double bal = rs.getDouble("balance");
                boolean active = rs.getInt("active") == 1;

                Account a;
                if (AccountType.SAVINGS.name().equalsIgnoreCase(typeStr)) {
                    double rate = rs.getDouble("interest_rate");
                    double minBal = rs.getDouble("min_balance");
                    a = new SavingsAccount(accNum, custId, bal, rate, minBal);
                } else {
                    double od = rs.getDouble("overdraft_limit");
                    double fee = rs.getDouble("maint_fee");
                    a = new CheckingAccount(accNum, custId, bal, od, fee);
                }
                a.setActive(active);
                list.add(a);
            }
        }
        return list;
    }

    // ==================== TRANSACTION OPERATIONS ====================

    public void saveTransaction(Transaction t) throws SQLException {
        if (!sqliteDriverAvailable) return;
        String sql = "INSERT OR REPLACE INTO transactions (transaction_id, timestamp, source_acc, target_acc, " +
                     "type, amount, resulting_balance, successful, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getTransactionId());
            ps.setLong(2, t.getTimestamp().getTime());
            ps.setString(3, t.getSourceAccountNumber());
            ps.setString(4, t.getTargetAccountNumber());
            ps.setString(5, t.getType().name());
            ps.setDouble(6, t.getAmount());
            ps.setDouble(7, t.getResultingBalance());
            ps.setInt(8, t.isSuccessful() ? 1 : 0);
            ps.setString(9, t.getDescription());
            ps.executeUpdate();
        }
    }

    // ==================== LOAN OPERATIONS ====================

    public void saveLoan(Loan l) throws SQLException {
        if (!sqliteDriverAvailable) return;
        String sql = "INSERT OR REPLACE INTO loans (loan_id, customer_id, account_number, principal, " +
                     "interest_rate, term_months, monthly_emi, remaining_principal, remaining_months, " +
                     "status, application_date, approval_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, l.getLoanId());
            ps.setString(2, l.getCustomerId());
            ps.setString(3, l.getDisbursementAccountNumber());
            ps.setDouble(4, l.getPrincipalAmount());
            ps.setDouble(5, l.getAnnualInterestRate());
            ps.setInt(6, l.getTermMonths());
            ps.setDouble(7, l.getMonthlyEmi());
            ps.setDouble(8, l.getRemainingPrincipal());
            ps.setInt(9, l.getRemainingMonths());
            ps.setString(10, l.getStatus().name());
            ps.setLong(11, l.getApplicationDate().getTime());
            ps.setLong(12, l.getApprovalDate() != null ? l.getApprovalDate().getTime() : 0);
            ps.executeUpdate();
        }
    }

    // ==================== EMPLOYEE OPERATIONS ====================

    public void saveEmployee(Employee e) throws SQLException {
        if (!sqliteDriverAvailable) return;
        String sql = "INSERT OR REPLACE INTO employees (employee_id, name, email, role, department, salary, hire_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getEmployeeId());
            ps.setString(2, e.getName());
            ps.setString(3, e.getEmail());
            ps.setString(4, e.getRole().name());
            ps.setString(5, e.getDepartment());
            ps.setDouble(6, e.getSalary());
            ps.setLong(7, e.getHireDate().getTime());
            ps.executeUpdate();
        }
    }

    public List<Employee> getAllEmployees() throws SQLException {
        List<Employee> list = new ArrayList<>();
        if (!sqliteDriverAvailable) return list;
        String sql = "SELECT * FROM employees";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                EmployeeRole role = EmployeeRole.valueOf(rs.getString("role"));
                Employee emp = new Employee(
                    rs.getString("employee_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    role,
                    rs.getString("department"),
                    rs.getDouble("salary")
                );
                list.add(emp);
            }
        }
        return list;
    }
}
