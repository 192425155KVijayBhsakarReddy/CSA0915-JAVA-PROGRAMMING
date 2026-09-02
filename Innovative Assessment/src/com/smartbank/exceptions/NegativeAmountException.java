package com.smartbank.exceptions;

public class NegativeAmountException extends BankException {
    private final double attemptedAmount;

    public NegativeAmountException(double attemptedAmount) {
        super("INVALID_AMOUNT", String.format("Transaction amount must be strictly positive (> 0). Attempted: $%.2f", attemptedAmount));
        this.attemptedAmount = attemptedAmount;
    }

    public double getAttemptedAmount() {
        return attemptedAmount;
    }
}
