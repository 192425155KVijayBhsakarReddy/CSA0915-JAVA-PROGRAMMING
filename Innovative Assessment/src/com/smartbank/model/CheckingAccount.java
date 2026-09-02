package com.smartbank.model;

import com.smartbank.exceptions.BankException;
import com.smartbank.exceptions.NegativeAmountException;
import com.smartbank.exceptions.OverdraftLimitExceededException;
import com.smartbank.model.enums.AccountType;

/**
 * Checking Account with overdraft line of credit and monthly maintenance fee.
 */
public class CheckingAccount extends Account {
    private static final long serialVersionUID = 1L;

    private double overdraftLimit;       // e.g. $500.00
    private double monthlyMaintenanceFee; // e.g. $12.00

    public CheckingAccount(String accountNumber, String customerId, double initialBalance, double overdraftLimit, double monthlyMaintenanceFee) {
        super(accountNumber, customerId, AccountType.CHECKING, initialBalance);
        this.overdraftLimit = Math.max(0.0, overdraftLimit);
        this.monthlyMaintenanceFee = Math.max(0.0, monthlyMaintenanceFee);
    }

    public double getOverdraftLimit() {
        return overdraftLimit;
    }

    public void setOverdraftLimit(double overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }

    public double getMonthlyMaintenanceFee() {
        return monthlyMaintenanceFee;
    }

    public void setMonthlyMaintenanceFee(double monthlyMaintenanceFee) {
        this.monthlyMaintenanceFee = monthlyMaintenanceFee;
    }

    @Override
    public void withdraw(double amount) throws BankException {
        if (amount <= 0) {
            throw new NegativeAmountException(amount);
        }
        acquireLock();
        try {
            if (!active) {
                throw new BankException("INACTIVE_ACCOUNT", "Cannot withdraw from inactive checking account " + accountNumber);
            }
            // Can withdraw up to (balance + overdraftLimit)
            double maxAllowedDebit = balance + overdraftLimit;
            if (amount > maxAllowedDebit) {
                throw new OverdraftLimitExceededException(accountNumber, amount, balance, overdraftLimit);
            }
            this.balance -= amount;
        } finally {
            releaseLock();
        }
    }

    @Override
    public double calculateMonthlyInterestOrFee() {
        // Checking accounts charge fee if balance is below threshold, or fixed maintenance fee
        return -monthlyMaintenanceFee;
    }

    public void applyMonthlyFee() {
        acquireLock();
        try {
            this.balance -= monthlyMaintenanceFee;
        } finally {
            releaseLock();
        }
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | Overdraft: $%.2f | MaintFee: $%.2f", overdraftLimit, monthlyMaintenanceFee);
    }
}
