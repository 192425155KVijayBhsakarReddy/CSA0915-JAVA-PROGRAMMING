package com.smartbank.exceptions;

public class OverdraftLimitExceededException extends BankException {
    private final String accountNumber;
    private final double attemptedAmount;
    private final double overdraftLimit;
    private final double currentBalance;

    public OverdraftLimitExceededException(String accountNumber, double attemptedAmount, double currentBalance, double overdraftLimit) {
        super("OVERDRAFT_LIMIT_EXCEEDED", String.format(
            "Account '%s' cannot complete withdrawal of $%.2f. Current balance: $%.2f, Max overdraft limit: $%.2f, Max possible debit: $%.2f",
            accountNumber, attemptedAmount, currentBalance, overdraftLimit, (currentBalance + overdraftLimit)
        ));
        this.accountNumber = accountNumber;
        this.attemptedAmount = attemptedAmount;
        this.overdraftLimit = overdraftLimit;
        this.currentBalance = currentBalance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public double getAttemptedAmount() {
        return attemptedAmount;
    }

    public double getOverdraftLimit() {
        return overdraftLimit;
    }

    public double getCurrentBalance() {
        return currentBalance;
    }
}
