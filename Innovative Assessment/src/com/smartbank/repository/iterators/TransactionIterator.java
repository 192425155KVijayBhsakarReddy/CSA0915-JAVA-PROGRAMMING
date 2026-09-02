package com.smartbank.repository.iterators;

import com.smartbank.model.Transaction;

import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Custom Iterator for traversing transaction streams matching account and date filters.
 */
public class TransactionIterator implements Iterator<Transaction> {
    private final Iterator<Transaction> sourceIterator;
    private final String targetAccount;
    private final Date fromDate;
    private final Date toDate;
    private Transaction nextMatch;

    public TransactionIterator(Iterator<Transaction> sourceIterator, String targetAccount, Date fromDate, Date toDate) {
        this.sourceIterator = sourceIterator;
        this.targetAccount = targetAccount;
        this.fromDate = fromDate;
        this.toDate = toDate;
        advance();
    }

    private void advance() {
        nextMatch = null;
        while (sourceIterator.hasNext()) {
            Transaction txn = sourceIterator.next();
            if (targetAccount != null && !targetAccount.isEmpty()) {
                boolean matchesSource = targetAccount.equalsIgnoreCase(txn.getSourceAccountNumber());
                boolean matchesDest = targetAccount.equalsIgnoreCase(txn.getTargetAccountNumber());
                if (!matchesSource && !matchesDest) {
                    continue;
                }
            }
            if (fromDate != null && txn.getTimestamp().before(fromDate)) {
                continue;
            }
            if (toDate != null && txn.getTimestamp().after(toDate)) {
                continue;
            }
            nextMatch = txn;
            break;
        }
    }

    @Override
    public boolean hasNext() {
        return nextMatch != null;
    }

    @Override
    public Transaction next() {
        if (nextMatch == null) {
            throw new NoSuchElementException("No more transactions");
        }
        Transaction current = nextMatch;
        advance();
        return current;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Transactions are immutable");
    }
}
