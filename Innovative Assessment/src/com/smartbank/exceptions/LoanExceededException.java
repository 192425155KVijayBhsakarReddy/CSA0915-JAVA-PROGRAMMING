package com.smartbank.exceptions;

public class LoanExceededException extends BankException {
    private final double requestedAmount;
    private final double maxEligibleAmount;

    public LoanExceededException(double requestedAmount, double maxEligibleAmount) {
        super("LOAN_LIMIT_EXCEEDED", String.format(
            "Requested loan amount $%.2f exceeds maximum eligible limit of $%.2f based on credit evaluation.",
            requestedAmount, maxEligibleAmount
        ));
        this.requestedAmount = requestedAmount;
        this.maxEligibleAmount = maxEligibleAmount;
    }

    public double getRequestedAmount() {
        return requestedAmount;
    }

    public double getMaxEligibleAmount() {
        return maxEligibleAmount;
    }
}
