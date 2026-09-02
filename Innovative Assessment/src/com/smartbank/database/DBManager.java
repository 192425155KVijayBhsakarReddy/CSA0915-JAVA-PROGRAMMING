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
import java.util.List;

/**
 * Enterprise Database Manager supporting MySQL and SQLite dual-engine connectivity.
 * Executes automated schema initialization, prepared statements, and transactional CRUD.
 */
public class DBManager {

    public enum DatabaseType {
        MYSQL("MySQL Server 8.0+"),
        SQLITE("SQLite File Engine"),
        IN_MEMORY("In-Memory Mode");

        private final String label;
        DatabaseType(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    // Default MySQL configuration (standard local defaults)
    private String mysqlHost = "localhost";
    private int mysqlPort = 3306;
    private String mysqlDatabase = "smartbank_db";
    private String mysqlUser = "root";
    private String mysqlPassword = ""; // Will attempt root with no password, or user configured

    // SQLite fallback configuration
    private static final String DB_DIR = "data";
    private static final String DB_FILE = DB_DIR + "/smartbank.db";

    private DatabaseType activeDbType = DatabaseType.IN_MEMORY;
    private static boolean mysqlDriverAvailable = false;
    private static boolean sqliteDriverAvailable = false;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            mysqlDriverAvailable = true;
        } catch (ClassNotFoundException e) {
            mysqlDriverAvailable = false;
        }

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

        // Attempt MySQL connection first if driver is present
        if (mysqlDriverAvailable) {
            try {
                if (testAndInitMySql()) {
                    activeDbType = DatabaseType.MYSQL;
                    System.out.println("[DATABASE] Successfully connected to MySQL Server (" + mysqlDatabase + ")!");
                    return;
                }
            } catch (Exception e) {
                System.out.println("[DATABASE] MySQL connection note: " + e.getMessage() + " (Falling back to SQLite/Embedded)");
            }
        }

        // Fallback to SQLite
        if (sqliteDriverAvailable) {
            try {
                initSqliteSchema();
                activeDbType = DatabaseType.SQLITE;
                System.out.println("[DATABASE] Connected to SQLite local engine (" + DB_FILE + ")");
                return;
            } catch (Exception ignored) {}
        }

        activeDbType = DatabaseType.IN_MEMORY;
    }

    public static boolean isMysqlDriverAvailable() {
        return mysqlDriverAvailable;
    }

    public static boolean isSqliteDriverAvailable() {
        return sqliteDriverAvailable;
    }

    public DatabaseType getActiveDbType() {
        return activeDbType;
    }

    public void configureMySql(String host, int port, String database, String user, String password) throws SQLException {
        this.mysqlHost = host;
        this.mysqlPort = port;
        this.mysqlDatabase = database;
        this.mysqlUser = user;
        this.mysqlPassword = password;

        if (testAndInitMySql()) {
            this.activeDbType = DatabaseType.MYSQL;
        }
    }

    private boolean testAndInitMySql() {
        if (!mysqlDriverAvailable) return false;

        // 1. Connect to MySQL server root to ensure DB exists
        String serverUrl = String.format("jdbc:mysql://%s:%d/?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
            mysqlHost, mysqlPort);

        try (Connection conn = DriverManager.getConnection(serverUrl, mysqlUser, mysqlPassword);
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + mysqlDatabase + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;");
        } catch (SQLException e) {
            // Password might be required or server unreachable
            return false;
        }

        // 2. Initialize tables inside the database
        try (Connection conn = getMySqlConnection(); Statement stmt = conn.createStatement()) {
            initMySqlSchema(stmt);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public Connection getConnection() throws SQLException {
        if (activeDbType == DatabaseType.MYSQL) {
            return getMySqlConnection();
        } else if (activeDbType == DatabaseType.SQLITE && sqliteDriverAvailable) {
            return DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
        }
        throw new SQLException("No active database engine configured.");
    }

    private Connection getMySqlConnection() throws SQLException {
        String dbUrl = String.format("jdbc:mysql://%s:%d/%s?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
            mysqlHost, mysqlPort, mysqlDatabase);
        return DriverManager.getConnection(dbUrl, mysqlUser, mysqlPassword);
    }

    private void initMySqlSchema(Statement stmt) throws SQLException {
        // Customers Table
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS customers (" +
            "customer_id VARCHAR(64) PRIMARY KEY, " +
            "name VARCHAR(128) NOT NULL, " +
            "email VARCHAR(128), " +
            "phone VARCHAR(64), " +
            "address VARCHAR(255), " +
            "credit_score INT, " +
            "kyc_verified TINYINT(1) DEFAULT 0" +
            ") ENGINE=InnoDB;"
        );

        // Accounts Table
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS accounts (" +
            "account_number VARCHAR(64) PRIMARY KEY, " +
            "customer_id VARCHAR(64) NOT NULL, " +
            "account_type VARCHAR(32) NOT NULL, " +
            "balance DOUBLE NOT NULL, " +
            "interest_rate DOUBLE DEFAULT 0, " +
            "min_balance DOUBLE DEFAULT 0, " +
            "overdraft_limit DOUBLE DEFAULT 0, " +
            "maint_fee DOUBLE DEFAULT 0, " +
            "active TINYINT(1) DEFAULT 1, " +
            "FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE" +
            ") ENGINE=InnoDB;"
        );

        // Transactions Table
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS transactions (" +
            "transaction_id VARCHAR(64) PRIMARY KEY, " +
            "timestamp BIGINT NOT NULL, " +
            "source_acc VARCHAR(64), " +
            "target_acc VARCHAR(64), " +
            "type VARCHAR(64) NOT NULL, " +
            "amount DOUBLE NOT NULL, " +
            "resulting_balance DOUBLE NOT NULL, " +
            "successful TINYINT(1) NOT NULL, " +
            "description VARCHAR(255)" +
            ") ENGINE=InnoDB;"
        );

        // Loans Table
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS loans (" +
            "loan_id VARCHAR(64) PRIMARY KEY, " +
            "customer_id VARCHAR(64) NOT NULL, " +
            "account_number VARCHAR(64), " +
            "principal DOUBLE NOT NULL, " +
            "interest_rate DOUBLE NOT NULL, " +
            "term_months INT NOT NULL, " +
            "monthly_emi DOUBLE NOT NULL, " +
            "remaining_principal DOUBLE NOT NULL, " +
            "remaining_months INT NOT NULL, " +
            "status VARCHAR(32) NOT NULL, " +
            "application_date BIGINT NOT NULL, " +
            "approval_date BIGINT DEFAULT 0" +
            ") ENGINE=InnoDB;"
        );

        // Employees Table
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS employees (" +
            "employee_id VARCHAR(64) PRIMARY KEY, " +
            "name VARCHAR(128) NOT NULL, " +
            "email VARCHAR(128), " +
            "role VARCHAR(64) NOT NULL, " +
            "department VARCHAR(128), " +
            "salary DOUBLE NOT NULL, " +
            "hire_date BIGINT NOT NULL" +
            ") ENGINE=InnoDB;"
        );
    }

    private void initSqliteSchema() {
        if (!sqliteDriverAvailable) return;
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
             Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA journal_mode=WAL;");
            stmt.execute("CREATE TABLE IF NOT EXISTS customers (customer_id TEXT PRIMARY KEY, name TEXT NOT NULL, email TEXT, phone TEXT, address TEXT, credit_score INTEGER, kyc_verified INTEGER);");
            stmt.execute("CREATE TABLE IF NOT EXISTS accounts (account_number TEXT PRIMARY KEY, customer_id TEXT NOT NULL, account_type TEXT NOT NULL, balance REAL NOT NULL, interest_rate REAL, min_balance REAL, overdraft_limit REAL, maint_fee REAL, active INTEGER NOT NULL);");
            stmt.execute("CREATE TABLE IF NOT EXISTS transactions (transaction_id TEXT PRIMARY KEY, timestamp INTEGER NOT NULL, source_acc TEXT, target_acc TEXT, type TEXT NOT NULL, amount REAL NOT NULL, resulting_balance REAL NOT NULL, successful INTEGER NOT NULL, description TEXT);");
            stmt.execute("CREATE TABLE IF NOT EXISTS loans (loan_id TEXT PRIMARY KEY, customer_id TEXT NOT NULL, account_number TEXT, principal REAL NOT NULL, interest_rate REAL NOT NULL, term_months INTEGER NOT NULL, monthly_emi REAL NOT NULL, remaining_principal REAL NOT NULL, remaining_months INTEGER NOT NULL, status TEXT NOT NULL, application_date INTEGER NOT NULL, approval_date INTEGER);");
            stmt.execute("CREATE TABLE IF NOT EXISTS employees (employee_id TEXT PRIMARY KEY, name TEXT NOT NULL, email TEXT, role TEXT NOT NULL, department TEXT, salary REAL NOT NULL, hire_date INTEGER NOT NULL);");
        } catch (SQLException ignored) {}
    }

    // ==================== CRUD OPERATIONS ====================

    public void saveCustomer(Customer c) throws SQLException {
        if (activeDbType == DatabaseType.IN_MEMORY) return;
        String sql;
        if (activeDbType == DatabaseType.MYSQL) {
            sql = "INSERT INTO customers (customer_id, name, email, phone, address, credit_score, kyc_verified) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name), email=VALUES(email), phone=VALUES(phone), address=VALUES(address), credit_score=VALUES(credit_score), kyc_verified=VALUES(kyc_verified)";
        } else {
            sql = "INSERT OR REPLACE INTO customers (customer_id, name, email, phone, address, credit_score, kyc_verified) VALUES (?, ?, ?, ?, ?, ?, ?)";
        }

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
        if (activeDbType == DatabaseType.IN_MEMORY) return list;
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

    public void saveAccount(Account a) throws SQLException {
        if (activeDbType == DatabaseType.IN_MEMORY) return;
        String sql;
        if (activeDbType == DatabaseType.MYSQL) {
            sql = "INSERT INTO accounts (account_number, customer_id, account_type, balance, interest_rate, min_balance, overdraft_limit, maint_fee, active) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE balance=VALUES(balance), active=VALUES(active), interest_rate=VALUES(interest_rate), min_balance=VALUES(min_balance), overdraft_limit=VALUES(overdraft_limit), maint_fee=VALUES(maint_fee)";
        } else {
            sql = "INSERT OR REPLACE INTO accounts (account_number, customer_id, account_type, balance, interest_rate, min_balance, overdraft_limit, maint_fee, active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        }

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
        if (activeDbType == DatabaseType.IN_MEMORY) return list;
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

    public void saveTransaction(Transaction t) throws SQLException {
        if (activeDbType == DatabaseType.IN_MEMORY) return;
        String sql;
        if (activeDbType == DatabaseType.MYSQL) {
            sql = "INSERT INTO transactions (transaction_id, timestamp, source_acc, target_acc, type, amount, resulting_balance, successful, description) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE successful=VALUES(successful)";
        } else {
            sql = "INSERT OR REPLACE INTO transactions (transaction_id, timestamp, source_acc, target_acc, type, amount, resulting_balance, successful, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        }

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

    public void saveLoan(Loan l) throws SQLException {
        if (activeDbType == DatabaseType.IN_MEMORY) return;
        String sql;
        if (activeDbType == DatabaseType.MYSQL) {
            sql = "INSERT INTO loans (loan_id, customer_id, account_number, principal, interest_rate, term_months, monthly_emi, remaining_principal, remaining_months, status, application_date, approval_date) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE remaining_principal=VALUES(remaining_principal), remaining_months=VALUES(remaining_months), status=VALUES(status), approval_date=VALUES(approval_date)";
        } else {
            sql = "INSERT OR REPLACE INTO loans (loan_id, customer_id, account_number, principal, interest_rate, term_months, monthly_emi, remaining_principal, remaining_months, status, application_date, approval_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        }

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

    public void saveEmployee(Employee e) throws SQLException {
        if (activeDbType == DatabaseType.IN_MEMORY) return;
        String sql;
        if (activeDbType == DatabaseType.MYSQL) {
            sql = "INSERT INTO employees (employee_id, name, email, role, department, salary, hire_date) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name), email=VALUES(email), role=VALUES(role), department=VALUES(department), salary=VALUES(salary)";
        } else {
            sql = "INSERT OR REPLACE INTO employees (employee_id, name, email, role, department, salary, hire_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
        }

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
        if (activeDbType == DatabaseType.IN_MEMORY) return list;
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
