package com.smartbank.model;

import com.smartbank.exceptions.BankException;
import com.smartbank.exceptions.InsufficientFundsException;
import com.smartbank.exceptions.NegativeAmountException;
import com.smartbank.model.enums.AccountType;

/**
 * Savings Account with minimum balance constraint and annual interest rate.
 */
public class SavingsAccount extends Account {
    private static final long serialVersionUID = 1L;

    private double annualInterestRate; // e.g. 0.04 for 4%
    private double minimumBalance;      // e.g. 100.00

    public SavingsAccount(String accountNumber, String customerId, double initialBalance, double annualInterestRate, double minimumBalance) {
        super(accountNumber, customerId, AccountType.SAVINGS, initialBalance);
        this.annualInterestRate = Math.max(0.0, annualInterestRate);
        this.minimumBalance = Math.max(0.0, minimumBalance);
    }

    public double getAnnualInterestRate() {
        return annualInterestRate;
    }

    public void setAnnualInterestRate(double annualInterestRate) {
        this.annualInterestRate = annualInterestRate;
    }

    public double getMinimumBalance() {
        return minimumBalance;
    }

    public void setMinimumBalance(double minimumBalance) {
        this.minimumBalance = minimumBalance;
    }

    @Override
    public void withdraw(double amount) throws BankException {
        if (amount <= 0) {
            throw new NegativeAmountException(amount);
        }
        acquireLock();
        try {
            if (!active) {
                throw new BankException("INACTIVE_ACCOUNT", "Cannot withdraw from inactive savings account " + accountNumber);
            }
            if ((balance - amount) < minimumBalance) {
                throw new InsufficientFundsException(accountNumber, amount, balance - minimumBalance);
            }
            this.balance -= amount;
        } finally {
            releaseLock();
        }
    }

    @Override
    public double calculateMonthlyInterestOrFee() {
        acquireLock();
        try {
            if (balance <= 0) return 0.0;
            // Monthly interest = Balance * (Annual Rate / 12)
            return (balance * (annualInterestRate / 12.0));
        } finally {
            releaseLock();
        }
    }

    public void applyMonthlyInterest() {
        acquireLock();
        try {
            double interest = calculateMonthlyInterestOrFee();
            if (interest > 0) {
                this.balance += interest;
            }
        } finally {
            releaseLock();
        }
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | IntRate: %.2f%% | MinBal: $%.2f", annualInterestRate * 100, minimumBalance);
    }
}
