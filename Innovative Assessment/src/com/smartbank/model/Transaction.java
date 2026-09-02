package com.smartbank.model;

import com.smartbank.model.enums.TransactionType;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable transaction record with timestamp, source/target, type, amount, status.
 */
public class Transaction implements Serializable, Comparable<Transaction> {
    private static final long serialVersionUID = 1L;

    private final String transactionId;
    private final Date timestamp;
    private final String sourceAccountNumber;
    private final String targetAccountNumber;
    private final TransactionType type;
    private final double amount;
    private final double resultingBalance;
    private final boolean successful;
    private final String description;

    public Transaction(String transactionId, String sourceAccountNumber, String targetAccountNumber,
                       TransactionType type, double amount, double resultingBalance,
                       boolean successful, String description) {
        this.transactionId = transactionId != null ? transactionId : "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.timestamp = new Date();
        this.sourceAccountNumber = sourceAccountNumber;
        this.targetAccountNumber = targetAccountNumber;
        this.type = Objects.requireNonNull(type);
        this.amount = amount;
        this.resultingBalance = resultingBalance;
        this.successful = successful;
        this.description = description;
    }

    public Transaction(String sourceAccountNumber, String targetAccountNumber,
                       TransactionType type, double amount, double resultingBalance,
                       boolean successful, String description) {
        this(null, sourceAccountNumber, targetAccountNumber, type, amount, resultingBalance, successful, description);
    }

    public String getTransactionId() {
        return transactionId;
    }

    public Date getTimestamp() {
        return new Date(timestamp.getTime());
    }

    public String getSourceAccountNumber() {
        return sourceAccountNumber;
    }

    public String getTargetAccountNumber() {
        return targetAccountNumber;
    }

    public TransactionType getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public double getResultingBalance() {
        return resultingBalance;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public int compareTo(Transaction other) {
        if (other == null) return 1;
        // Descending chronological order
        return other.timestamp.compareTo(this.timestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction)) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(transactionId, that.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return String.format("[%s] ID: %s | %s | Src: %s -> Dst: %s | Amount: $%.2f | Bal: $%.2f | Status: %s",
            sdf.format(timestamp), transactionId, type,
            sourceAccountNumber != null ? sourceAccountNumber : "N/A",
            targetAccountNumber != null ? targetAccountNumber : "N/A",
            amount, resultingBalance, successful ? "SUCCESS" : "FAILED");
    }
}
