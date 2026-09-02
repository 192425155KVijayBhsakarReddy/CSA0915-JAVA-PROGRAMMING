package com.smartbank.exceptions;

public class InsufficientFundsException extends BankException {
    private final String accountNumber;
    private final double attemptedAmount;
    private final double currentBalance;

    public InsufficientFundsException(String accountNumber, double attemptedAmount, double currentBalance) {
        super("INSUFFICIENT_FUNDS", String.format(
            "Account '%s' has insufficient balance. Attempted: $%.2f, Available: $%.2f, Shortfall: $%.2f",
            accountNumber, attemptedAmount, currentBalance, (attemptedAmount - currentBalance)
        ));
        this.accountNumber = accountNumber;
        this.attemptedAmount = attemptedAmount;
        this.currentBalance = currentBalance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public double getAttemptedAmount() {
        return attemptedAmount;
    }

    public double getCurrentBalance() {
        return currentBalance;
    }
}
