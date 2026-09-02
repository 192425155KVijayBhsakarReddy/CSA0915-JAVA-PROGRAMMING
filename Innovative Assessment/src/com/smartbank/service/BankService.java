package com.smartbank.service;

import com.smartbank.concurrency.AsyncAuditLogger;
import com.smartbank.concurrency.LockManager;
import com.smartbank.database.DBManager;
import com.smartbank.exceptions.BankException;
import com.smartbank.exceptions.InvalidAccountException;
import com.smartbank.exceptions.NegativeAmountException;
import com.smartbank.interfaces.Auditable;
import com.smartbank.interfaces.BankOperations;
import com.smartbank.model.Account;
import com.smartbank.model.AuditLog;
import com.smartbank.model.CheckingAccount;
import com.smartbank.model.Customer;
import com.smartbank.model.SavingsAccount;
import com.smartbank.model.Transaction;
import com.smartbank.model.enums.AccountType;
import com.smartbank.model.enums.TransactionType;
import com.smartbank.repository.AccountRepository;
import com.smartbank.repository.InMemoryRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Core business orchestrator coordinating accounts, transactions,
 * thread synchronization, audit trails, and dual persistence.
 */
public class BankService implements BankOperations<Account>, Auditable {
    private final AccountRepository accountRepo;
    private final InMemoryRepository<String, Customer> customerRepo;
    private final List<Transaction> transactionHistory;
    private final AsyncAuditLogger auditLogger;
    private final BackupService backupService;
    private final DBManager dbManager;

    public BankService() {
        this.accountRepo = new AccountRepository();
        this.customerRepo = new InMemoryRepository<>();
        this.transactionHistory = new CopyOnWriteArrayList<>();
        this.auditLogger = new AsyncAuditLogger();
        this.auditLogger.start();
        this.backupService = new BackupService();
        this.dbManager = new DBManager();
    }

    // ==================== CUSTOMER METHODS ====================

    public Customer createCustomer(String name, String email, String phone, String address, int creditScore) {
        String custId = "CUST-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Customer customer = new Customer(custId, name, email, phone, address, creditScore);
        customerRepo.save(custId, customer);
        log("SYSTEM", "CREATE_CUSTOMER", "Created Customer: " + customer.getName() + " (" + custId + ")", AuditLog.Severity.INFO);
        try {
            dbManager.saveCustomer(customer);
        } catch (Exception ignored) {}
        return customer;
    }

    public Customer getCustomer(String customerId) {
        Optional<Customer> opt = customerRepo.findById(customerId);
        return opt.orElse(null);
    }

    public List<Customer> getAllCustomers() {
        return customerRepo.findAll();
    }

    // ==================== ACCOUNT METHODS ====================

    public SavingsAccount openSavingsAccount(String customerId, double initialDeposit, double annualInterestRate, double minBalance) throws BankException {
        Customer customer = getCustomer(customerId);
        if (customer == null) {
            throw new BankException("CUSTOMER_NOT_FOUND", "Customer ID " + customerId + " does not exist");
        }
        if (initialDeposit < minBalance) {
            throw new BankException("MIN_BALANCE_VIOLATION", String.format("Initial deposit ($%.2f) must be >= minimum balance ($%.2f)", initialDeposit, minBalance));
        }

        String accNum = "SA-" + (100000 + accountRepo.count() + 1);
        SavingsAccount account = new SavingsAccount(accNum, customerId, initialDeposit, annualInterestRate, minBalance);
        accountRepo.save(accNum, account);
        customer.linkAccount(accNum);

        recordTransaction(null, accNum, TransactionType.DEPOSIT, initialDeposit, initialDeposit, true, "Initial Savings Account Opening Deposit");
        log("SYSTEM", "OPEN_SAVINGS_ACCOUNT", "Opened " + accNum + " for " + customer.getName() + " with $" + initialDeposit, AuditLog.Severity.INFO);

        try {
            dbManager.saveAccount(account);
        } catch (Exception ignored) {}

        return account;
    }

    public CheckingAccount openCheckingAccount(String customerId, double initialDeposit, double overdraftLimit, double maintenanceFee) throws BankException {
        Customer customer = getCustomer(customerId);
        if (customer == null) {
            throw new BankException("CUSTOMER_NOT_FOUND", "Customer ID " + customerId + " does not exist");
        }
        if (initialDeposit < 0) {
            throw new NegativeAmountException(initialDeposit);
        }

        String accNum = "CA-" + (200000 + accountRepo.count() + 1);
        CheckingAccount account = new CheckingAccount(accNum, customerId, initialDeposit, overdraftLimit, maintenanceFee);
        accountRepo.save(accNum, account);
        customer.linkAccount(accNum);

        recordTransaction(null, accNum, TransactionType.DEPOSIT, initialDeposit, initialDeposit, true, "Initial Checking Account Opening Deposit");
        log("SYSTEM", "OPEN_CHECKING_ACCOUNT", "Opened " + accNum + " for " + customer.getName() + " with $" + initialDeposit, AuditLog.Severity.INFO);

        try {
            dbManager.saveAccount(account);
        } catch (Exception ignored) {}

        return account;
    }

    @Override
    public Account getAccount(String accountNumber) throws InvalidAccountException {
        Optional<Account> opt = accountRepo.findById(accountNumber);
        if (!opt.isPresent()) {
            throw new InvalidAccountException(accountNumber);
        }
        return opt.get();
    }

    @Override
    public double getBalance(String accountNumber) throws BankException {
        return getAccount(accountNumber).getBalance();
    }

    public List<Account> getAllAccounts() {
        return accountRepo.findAll();
    }

    public List<Account> getAccountsSortedByBalance(boolean ascending) {
        return accountRepo.findAccountsSortedByBalance(ascending);
    }

    public double getTotalBankAssets() {
        return accountRepo.getTotalBankAssets();
    }

    // ==================== TRANSACTION OPERATIONS (THREAD-SAFE) ====================

    @Override
    public void deposit(String accountNumber, double amount, String notes) throws BankException {
        Account account = getAccount(accountNumber);
        account.deposit(amount);
        double currentBal = account.getBalance();
        recordTransaction(null, accountNumber, TransactionType.DEPOSIT, amount, currentBal, true, notes != null ? notes : "Deposit");
        log("SYSTEM", "DEPOSIT", String.format("Deposited $%.2f to %s. Balance: $%.2f", amount, accountNumber, currentBal), AuditLog.Severity.INFO);
        try {
            dbManager.saveAccount(account);
        } catch (Exception ignored) {}
    }

    @Override
    public void withdraw(String accountNumber, double amount, String notes) throws BankException {
        Account account = getAccount(accountNumber);
        try {
            account.withdraw(amount);
            double currentBal = account.getBalance();
            recordTransaction(accountNumber, null, TransactionType.WITHDRAWAL, amount, currentBal, true, notes != null ? notes : "Withdrawal");
            log("SYSTEM", "WITHDRAW", String.format("Withdrew $%.2f from %s. Balance: $%.2f", amount, accountNumber, currentBal), AuditLog.Severity.INFO);
            try {
                dbManager.saveAccount(account);
            } catch (Exception ignored) {}
        } catch (BankException e) {
            recordTransaction(accountNumber, null, TransactionType.WITHDRAWAL, amount, account.getBalance(), false, "Failed withdrawal: " + e.getMessage());
            log("SYSTEM", "WITHDRAW_FAIL", "Failed withdrawal on " + accountNumber + ": " + e.getMessage(), AuditLog.Severity.WARNING);
            throw e;
        }
    }

    @Override
    public Transaction transfer(final String sourceAccountNumber, final String targetAccountNumber,
                                final double amount, final String notes) throws BankException {
        if (amount <= 0) {
            throw new NegativeAmountException(amount);
        }

        final Account fromAcc = getAccount(sourceAccountNumber);
        final Account toAcc = getAccount(targetAccountNumber);

        try {
            // Deadlock-free ordered lock execution
            return LockManager.executeWithOrderedLocks(fromAcc, toAcc, new LockManager.TransferAction<Transaction>() {
                @Override
                public Transaction execute() throws Exception {
                    fromAcc.withdraw(amount);
                    toAcc.deposit(amount);

                    Transaction txn = recordTransaction(
                        sourceAccountNumber, targetAccountNumber, TransactionType.TRANSFER_OUT,
                        amount, fromAcc.getBalance(), true,
                        notes != null ? notes : "Transfer to " + targetAccountNumber
                    );

                    log("SYSTEM", "TRANSFER",
                        String.format("Transferred $%.2f from %s to %s", amount, sourceAccountNumber, targetAccountNumber),
                        AuditLog.Severity.INFO);

                    try {
                        dbManager.saveAccount(fromAcc);
                        dbManager.saveAccount(toAcc);
                    } catch (Exception ignored) {}

                    return txn;
                }
            });
        } catch (BankException e) {
            recordTransaction(sourceAccountNumber, targetAccountNumber, TransactionType.TRANSFER_OUT,
                amount, fromAcc.getBalance(), false, "Failed transfer: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            throw new BankException("TRANSFER_FAILED", "Transfer execution failed: " + e.getMessage(), e);
        }
    }

    private Transaction recordTransaction(String src, String dst, TransactionType type, double amount,
                                          double balanceAfter, boolean success, String desc) {
        Transaction txn = new Transaction(src, dst, type, amount, balanceAfter, success, desc);
        transactionHistory.add(txn);
        try {
            dbManager.saveTransaction(txn);
        } catch (Exception ignored) {}
        return txn;
    }

    @Override
    public List<Transaction> getTransactionHistory(String accountNumber) {
        List<Transaction> list = new ArrayList<>();
        for (Transaction t : transactionHistory) {
            if (accountNumber == null ||
                accountNumber.equalsIgnoreCase(t.getSourceAccountNumber()) ||
                accountNumber.equalsIgnoreCase(t.getTargetAccountNumber())) {
                list.add(t);
            }
        }
        Collections.sort(list); // Descending chronological
        return list;
    }

    public List<Transaction> getAllTransactions() {
        List<Transaction> list = new ArrayList<>(transactionHistory);
        Collections.sort(list);
        return list;
    }

    // ==================== AUDIT & PERSISTENCE ====================

    @Override
    public void log(String actor, String action, String details, AuditLog.Severity severity) {
        auditLogger.enqueueLog(new AuditLog(actor, action, details, severity));
    }

    @Override
    public List<AuditLog> getLogs() {
        return auditLogger.getAllLogs();
    }

    @Override
    public List<AuditLog> getLogsByActor(String actor) {
        List<AuditLog> list = new ArrayList<>();
        for (AuditLog l : auditLogger.getAllLogs()) {
            if (l.getActor().equalsIgnoreCase(actor)) {
                list.add(l);
            }
        }
        return list;
    }

    public BackupService.BankStateSnapshot createSnapshot(List<com.smartbank.model.Loan> loans, List<com.smartbank.model.Employee> employees) {
        return new BackupService.BankStateSnapshot(
            customerRepo.findAll(),
            accountRepo.findAll(),
            new ArrayList<>(transactionHistory),
            loans,
            employees
        );
    }

    public void restoreSnapshot(BackupService.BankStateSnapshot snapshot) {
        customerRepo.clear();
        accountRepo.clear();
        transactionHistory.clear();

        for (Customer c : snapshot.customers) {
            customerRepo.save(c.getCustomerId(), c);
        }
        for (Account a : snapshot.accounts) {
            accountRepo.save(a.getAccountNumber(), a);
        }
        transactionHistory.addAll(snapshot.transactions);
        log("SYSTEM", "RESTORE_SNAPSHOT", "Restored snapshot containing " + snapshot.accounts.size() + " accounts", AuditLog.Severity.SECURITY);
    }

    public BackupService getBackupService() {
        return backupService;
    }

    public DBManager getDbManager() {
        return dbManager;
    }

    public void shutdown() {
        auditLogger.stop();
    }
}
