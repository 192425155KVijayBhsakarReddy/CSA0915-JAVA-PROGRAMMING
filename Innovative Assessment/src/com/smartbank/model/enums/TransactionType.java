package com.smartbank.model.enums;

import java.io.Serializable;

public enum TransactionType implements Serializable {
    DEPOSIT("Deposit"),
    WITHDRAWAL("Withdrawal"),
    TRANSFER_IN("Transfer Received"),
    TRANSFER_OUT("Transfer Sent"),
    LOAN_DISBURSEMENT("Loan Disbursement"),
    LOAN_REPAYMENT("Loan Repayment"),
    INTEREST_CREDIT("Interest Credit"),
    FEE_DEBIT("Fee Debit");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
