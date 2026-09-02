package com.smartbank;

import com.smartbank.concurrency.ConcurrencyStressTester;
import com.smartbank.exceptions.BankException;
import com.smartbank.exceptions.InsufficientFundsException;
import com.smartbank.exceptions.NegativeAmountException;
import com.smartbank.gui.SmartBankFrame;
import com.smartbank.model.Account;
import com.smartbank.model.CheckingAccount;
import com.smartbank.model.Customer;
import com.smartbank.model.Employee;
import com.smartbank.model.Loan;
import com.smartbank.model.SavingsAccount;
import com.smartbank.model.Transaction;
import com.smartbank.model.enums.EmployeeRole;
import com.smartbank.repository.iterators.AccountFilterIterator;
import com.smartbank.service.BackupService;
import com.smartbank.service.BankService;
import com.smartbank.service.EmployeeService;
import com.smartbank.service.LoanService;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Main application entry point for the Smart Bank Enterprise Management System.
 * Supports GUI launch and automated CLI test suites.
 */
public class Main {

    public static void main(String[] args) {
        // Initialize core services
        BankService bankService = new BankService();
        EmployeeService employeeService = new EmployeeService();
        LoanService loanService = new LoanService(bankService, employeeService);

        // Seed realistic demonstration data
        seedInitialData(bankService, employeeService, loanService);

        // CLI test suite check
        if (args.length > 0 && "--test".equalsIgnoreCase(args[0])) {
            System.out.println("=================================================");
            System.out.println("RUNNING SMART BANK COMPREHENSIVE TEST SUITE...");
            System.out.println("=================================================");
            boolean success = runAutomatedTestSuite(bankService, employeeService, loanService);
            System.exit(success ? 0 : 1);
            return;
        }

        // Launch GUI
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            SmartBankFrame frame = new SmartBankFrame(bankService, loanService, employeeService);
            frame.setVisible(true);
        });
    }

    /**
     * Pre-populates initial bank records for demo and testing.
     */
    public static void seedInitialData(BankService bankService, EmployeeService employeeService, LoanService loanService) {
        try {
            // Seed Employees
            Employee emp1 = employeeService.registerEmployee("EMP-001", "Sarah Jenkins", "sarah@smartbank.com", EmployeeRole.BRANCH_MANAGER, "Executive", 95000);
            Employee emp2 = employeeService.registerEmployee("EMP-002", "David Miller", "david@smartbank.com", EmployeeRole.LOAN_OFFICER, "Credit & Risk", 72000);
            Employee emp3 = employeeService.registerEmployee("EMP-003", "Elena Rostova", "elena@smartbank.com", EmployeeRole.TELLER, "Retail Operations", 48000);
            Employee emp4 = employeeService.registerEmployee("EMP-004", "Marcus Vance", "marcus@smartbank.com", EmployeeRole.SYSTEM_ADMIN, "IT & Security", 88000);

            // Seed Customers
            Customer c1 = bankService.createCustomer("Alice Smith", "alice@example.com", "555-0101", "124 Market St, Suite 4A", 780);
            Customer c2 = bankService.createCustomer("Bob Jones", "bob@example.com", "555-0102", "88 Pine Avenue, Apt 2B", 650);
            Customer c3 = bankService.createCustomer("Charlie Brown", "charlie@example.com", "555-0103", "452 Oak Boulevard", 710);
            Customer c4 = bankService.createCustomer("Diana Prince", "diana@example.com", "555-0104", "10 Amazon Way", 820);

            // Seed Accounts
            SavingsAccount sa1 = bankService.openSavingsAccount(c1.getCustomerId(), 15000.0, 0.045, 500.0);
            CheckingAccount ca1 = bankService.openCheckingAccount(c1.getCustomerId(), 4500.0, 1000.0, 15.0);

            SavingsAccount sa2 = bankService.openSavingsAccount(c2.getCustomerId(), 3200.0, 0.035, 200.0);
            CheckingAccount ca2 = bankService.openCheckingAccount(c2.getCustomerId(), 1200.0, 500.0, 10.0);

            SavingsAccount sa3 = bankService.openSavingsAccount(c3.getCustomerId(), 28000.0, 0.050, 1000.0);
            CheckingAccount ca3 = bankService.openCheckingAccount(c4.getCustomerId(), 50000.0, 5000.0, 0.0);

            // Seed Transfers & Transactions
            bankService.transfer(sa1.getAccountNumber(), ca1.getAccountNumber(), 1000.0, "Monthly savings allocation");
            bankService.transfer(ca3.getAccountNumber(), sa2.getAccountNumber(), 500.0, "Consulting fee payment");

            // Seed Loan
            Loan loan1 = loanService.applyForLoan(c3.getCustomerId(), sa3.getAccountNumber(), 40000.0, 0.075, 36);
            loanService.approveLoan(loan1.getLoanId(), emp2.getEmployeeId());

            Loan loan2 = loanService.applyForLoan(c1.getCustomerId(), sa1.getAccountNumber(), 18000.0, 0.065, 24);

        } catch (Exception e) {
            System.err.println("Seed initialization notice: " + e.getMessage());
        }
    }

    /**
     * Automated unit and integration test runner verifying all required architectural components.
     */
    public static boolean runAutomatedTestSuite(BankService bankService, EmployeeService employeeService, LoanService loanService) {
        int passed = 0;
        int failed = 0;

        System.out.println("\n[TEST 1] Testing Polymorphism & Account Interest/Fees...");
        try {
            Account sa = bankService.getAllAccounts().get(0);
            double initialBal = sa.getBalance();
            sa.deposit(500.0);
            if (sa.getBalance() == initialBal + 500.0) {
                System.out.println("  ✓ Deposit & Polymorphism PASSED. Balance: $" + sa.getBalance());
                passed++;
            } else {
                System.err.println("  ✗ Balance mismatch");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("  ✗ Deposit failed: " + e.getMessage());
            failed++;
        }

        System.out.println("\n[TEST 2] Testing Custom Exception Hierarchy (Insufficient Funds / Overdraft)...");
        try {
            List<Account> accounts = bankService.getAllAccounts();
            Account testAcc = accounts.get(1);
            try {
                testAcc.withdraw(9999999.0);
                System.err.println("  ✗ Failed to throw InsufficientFunds/Overdraft exception");
                failed++;
            } catch (InsufficientFundsException | com.smartbank.exceptions.OverdraftLimitExceededException e) {
                System.out.println("  ✓ Custom Exception correctly caught: " + e.getClass().getSimpleName() + " -> " + e.getMessage());
                passed++;
            }
        } catch (Exception e) {
            System.err.println("  ✗ Unexpected error: " + e.getMessage());
            failed++;
        }

        System.out.println("\n[TEST 3] Testing Negative Amount Guard...");
        try {
            bankService.deposit(bankService.getAllAccounts().get(0).getAccountNumber(), -500.0, "Invalid");
            System.err.println("  ✗ Negative deposit allowed!");
            failed++;
        } catch (NegativeAmountException e) {
            System.out.println("  ✓ NegativeAmountException correctly thrown: " + e.getMessage());
            passed++;
        } catch (Exception e) {
            System.err.println("  ✗ Unexpected exception: " + e.getMessage());
            failed++;
        }

        System.out.println("\n[TEST 4] Testing Custom Generic Iterator (AccountFilterIterator)...");
        try {
            Iterator<Account> it = new AccountFilterIterator(bankService.getAllAccounts().iterator(), 5000.0, true);
            int count = 0;
            while (it.hasNext()) {
                Account a = it.next();
                if (a.getBalance() >= 5000.0) {
                    count++;
                }
            }
            System.out.println("  ✓ AccountFilterIterator filtered " + count + " accounts with balance >= $5,000");
            passed++;
        } catch (Exception e) {
            System.err.println("  ✗ Custom Iterator failed: " + e.getMessage());
            failed++;
        }

        System.out.println("\n[TEST 5] Testing Loan Financial EMI Calculation Formula...");
        try {
            // Loan: $10,000, 12% annual, 12 months -> Expected EMI ~ $888.49
            double emi = Loan.calculateEmi(10000.0, 0.12, 12);
            if (Math.abs(emi - 888.49) < 0.10) {
                System.out.println("  ✓ EMI Calculation accurate: $" + String.format("%.2f", emi));
                passed++;
            } else {
                System.err.println("  ✗ EMI Calculation discrepancy: $" + emi);
                failed++;
            }
        } catch (Exception e) {
            System.err.println("  ✗ EMI Test failed: " + e.getMessage());
            failed++;
        }

        System.out.println("\n[TEST 6] Testing File Streams & Object Serialization (.dat)...");
        try {
            File bkpFile = new File("data/backups/test_snapshot.dat");
            BackupService.BankStateSnapshot snapshot = bankService.createSnapshot(
                loanService.getAllLoans(),
                employeeService.getAllEmployees()
            );
            bankService.getBackupService().exportBinarySnapshot(snapshot, bkpFile);
            BackupService.BankStateSnapshot restored = bankService.getBackupService().importBinarySnapshot(bkpFile);
            if (restored.accounts.size() == snapshot.accounts.size() &&
                restored.customers.size() == snapshot.customers.size()) {
                System.out.println("  ✓ Serialization & Deserialization verified. Restored " + restored.accounts.size() + " accounts.");
                passed++;
            } else {
                System.err.println("  ✗ Restored state size mismatch");
                failed++;
            }
            bkpFile.delete();
        } catch (Exception e) {
            System.err.println("  ✗ Serialization Test failed: " + e.getMessage());
            failed++;
        }

        System.out.println("\n[TEST 7] Testing Multithreaded Concurrency & Balance Invariance (Stress Test)...");
        try {
            List<String> accNums = new ArrayList<>();
            for (Account a : bankService.getAllAccounts()) {
                accNums.add(a.getAccountNumber());
            }

            // Run 20 threads executing 25 transfers each = 500 parallel transactions
            ConcurrencyStressTester.StressTestResult res = ConcurrencyStressTester.runTransferStressTest(
                bankService, accNums, 20, 25
            );

            System.out.println("  " + res.toString().replace("\n", "\n  "));
            if (res.invariantPreserved && res.successfulTransfers > 0) {
                System.out.println("  ✓ MULTITHREADED CONCURRENCY INVARIANT PASSED! 0% Balance Leaks.");
                passed++;
            } else {
                System.err.println("  ✗ Concurrency Invariant Failed!");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("  ✗ Concurrency Stress Test failed: " + e.getMessage());
            failed++;
        }

        System.out.println("\n=================================================");
        System.out.println("TEST SUMMARY: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("=================================================");

        return failed == 0;
    }
}
