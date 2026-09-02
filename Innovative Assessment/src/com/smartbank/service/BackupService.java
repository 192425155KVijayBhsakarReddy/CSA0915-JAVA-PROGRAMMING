package com.smartbank.service;

import com.smartbank.exceptions.BankException;
import com.smartbank.model.Account;
import com.smartbank.model.CheckingAccount;
import com.smartbank.model.Customer;
import com.smartbank.model.Employee;
import com.smartbank.model.Loan;
import com.smartbank.model.SavingsAccount;
import com.smartbank.model.Transaction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Service managing state persistence via Object Serialization (binary .dat streams)
 * and tabular CSV data export/import.
 */
public class BackupService {

    public static class BankStateSnapshot implements Serializable {
        private static final long serialVersionUID = 1L;

        public final Date snapshotDate;
        public final List<Customer> customers;
        public final List<Account> accounts;
        public final List<Transaction> transactions;
        public final List<Loan> loans;
        public final List<Employee> employees;

        public BankStateSnapshot(List<Customer> customers, List<Account> accounts,
                                 List<Transaction> transactions, List<Loan> loans,
                                 List<Employee> employees) {
            this.snapshotDate = new Date();
            this.customers = new ArrayList<>(customers);
            this.accounts = new ArrayList<>(accounts);
            this.transactions = new ArrayList<>(transactions);
            this.loans = new ArrayList<>(loans);
            this.employees = new ArrayList<>(employees);
        }
    }

    /**
     * Serializes complete bank state into a binary stream file.
     */
    public void exportBinarySnapshot(BankStateSnapshot snapshot, File targetFile) throws BankException {
        if (targetFile.getParentFile() != null && !targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(targetFile))) {
            oos.writeObject(snapshot);
            oos.flush();
        } catch (IOException e) {
            throw new BankException("SERIALIZATION_ERROR", "Failed to serialize bank snapshot: " + e.getMessage(), e);
        }
    }

    /**
     * Deserializes bank state from a binary stream file.
     */
    public BankStateSnapshot importBinarySnapshot(File sourceFile) throws BankException {
        if (!sourceFile.exists()) {
            throw new BankException("FILE_NOT_FOUND", "Snapshot file not found: " + sourceFile.getAbsolutePath());
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(sourceFile))) {
            Object obj = ois.readObject();
            if (obj instanceof BankStateSnapshot) {
                return (BankStateSnapshot) obj;
            } else {
                throw new BankException("CORRUPT_DATA", "File does not contain valid BankStateSnapshot");
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new BankException("DESERIALIZATION_ERROR", "Failed to deserialize bank snapshot: " + e.getMessage(), e);
        }
    }

    /**
     * Exports transaction history to CSV format for reporting/analytics.
     */
    public void exportTransactionsToCsv(List<Transaction> transactions, File csvFile) throws BankException {
        if (csvFile.getParentFile() != null && !csvFile.getParentFile().exists()) {
            csvFile.getParentFile().mkdirs();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {
            writer.write("TransactionID,Timestamp,Type,SourceAccount,TargetAccount,Amount,ResultingBalance,Status,Description");
            writer.newLine();
            for (Transaction t : transactions) {
                writer.write(String.format("%s,%s,%s,%s,%s,%.2f,%.2f,%s,\"%s\"",
                    t.getTransactionId(),
                    sdf.format(t.getTimestamp()),
                    t.getType().name(),
                    t.getSourceAccountNumber() != null ? t.getSourceAccountNumber() : "",
                    t.getTargetAccountNumber() != null ? t.getTargetAccountNumber() : "",
                    t.getAmount(),
                    t.getResultingBalance(),
                    t.isSuccessful() ? "SUCCESS" : "FAILED",
                    t.getDescription().replace("\"", "\"\"")
                ));
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            throw new BankException("CSV_EXPORT_ERROR", "Failed to export transactions to CSV: " + e.getMessage(), e);
        }
    }

    /**
     * Exports accounts to CSV format.
     */
    public void exportAccountsToCsv(List<Account> accounts, File csvFile) throws BankException {
        if (csvFile.getParentFile() != null && !csvFile.getParentFile().exists()) {
            csvFile.getParentFile().mkdirs();
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {
            writer.write("AccountNumber,CustomerID,Type,Balance,InterestRate,MinBalance,OverdraftLimit,MaintFee,Active");
            writer.newLine();
            for (Account a : accounts) {
                double rate = (a instanceof SavingsAccount) ? ((SavingsAccount) a).getAnnualInterestRate() : 0.0;
                double minBal = (a instanceof SavingsAccount) ? ((SavingsAccount) a).getMinimumBalance() : 0.0;
                double overdraft = (a instanceof CheckingAccount) ? ((CheckingAccount) a).getOverdraftLimit() : 0.0;
                double fee = (a instanceof CheckingAccount) ? ((CheckingAccount) a).getMonthlyMaintenanceFee() : 0.0;

                writer.write(String.format("%s,%s,%s,%.2f,%.4f,%.2f,%.2f,%.2f,%b",
                    a.getAccountNumber(),
                    a.getCustomerId(),
                    a.getAccountType().name(),
                    a.getBalance(),
                    rate,
                    minBal,
                    overdraft,
                    fee,
                    a.isActive()
                ));
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            throw new BankException("CSV_EXPORT_ERROR", "Failed to export accounts to CSV: " + e.getMessage(), e);
        }
    }
}
