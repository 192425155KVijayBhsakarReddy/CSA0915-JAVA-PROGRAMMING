package com.smartbank.model;

import com.smartbank.exceptions.BankException;
import com.smartbank.exceptions.NegativeAmountException;
import com.smartbank.model.enums.AccountType;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Abstract base class representing a bank account.
 * Demonstrates encapsulation, abstraction, polymorphism, and comparability.
 */
public abstract class Account implements Serializable, Comparable<Account> {
    private static final long serialVersionUID = 1L;

    protected final String accountNumber;
    protected final String customerId;
    protected final AccountType accountType;
    protected double balance;
    protected final Date creationDate;
    protected boolean active;

    // Transient reentrant lock for thread safety (not serialized)
    protected transient ReentrantLock lock = new ReentrantLock(true);

    public Account(String accountNumber, String customerId, AccountType accountType, double initialBalance) {
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }
        this.accountNumber = Objects.requireNonNull(accountNumber, "Account number required");
        this.customerId = Objects.requireNonNull(customerId, "Customer ID required");
        this.accountType = Objects.requireNonNull(accountType, "Account type required");
        this.balance = initialBalance;
        this.creationDate = new Date();
        this.active = true;
        this.lock = new ReentrantLock(true);
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getCustomerId() {
        return customerId;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public double getBalance() {
        acquireLock();
        try {
            return balance;
        } finally {
            releaseLock();
        }
    }

    public Date getCreationDate() {
        return new Date(creationDate.getTime());
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public ReentrantLock getLock() {
        if (lock == null) {
            lock = new ReentrantLock(true);
        }
        return lock;
    }

    public void acquireLock() {
        getLock().lock();
    }

    public void releaseLock() {
        getLock().unlock();
    }

    /**
     * Thread-safe deposit operation.
     */
    public void deposit(double amount) throws BankException {
        if (amount <= 0) {
            throw new NegativeAmountException(amount);
        }
        acquireLock();
        try {
            if (!active) {
                throw new BankException("INACTIVE_ACCOUNT", "Cannot deposit to inactive account " + accountNumber);
            }
            this.balance += amount;
        } finally {
            releaseLock();
        }
    }

    /**
     * Polymorphic withdrawal operation to be implemented/customized by subtypes.
     */
    public abstract void withdraw(double amount) throws BankException;

    /**
     * Polymorphic monthly interest / fee calculation.
     */
    public abstract double calculateMonthlyInterestOrFee();

    @Override
    public int compareTo(Account other) {
        if (other == null) return 1;
        // Natural ordering by Account Number
        return this.accountNumber.compareTo(other.accountNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;
        Account account = (Account) o;
        return Objects.equals(accountNumber, account.accountNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountNumber);
    }

    @Override
    public String toString() {
        return String.format("[%s] Acc: %s | Customer: %s | Balance: $%.2f | Active: %s",
            accountType, accountNumber, customerId, balance, active);
    }
}
