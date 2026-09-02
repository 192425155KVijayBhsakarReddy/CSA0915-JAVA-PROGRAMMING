package com.smartbank.repository.iterators;

import com.smartbank.model.Account;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Custom generic Iterator demonstrating streaming traversal and conditional filtering.
 */
public class AccountFilterIterator implements Iterator<Account> {
    private final Iterator<Account> sourceIterator;
    private final double minBalance;
    private final boolean activeOnly;
    private Account nextMatch;

    public AccountFilterIterator(Iterator<Account> sourceIterator, double minBalance, boolean activeOnly) {
        this.sourceIterator = sourceIterator;
        this.minBalance = minBalance;
        this.activeOnly = activeOnly;
        advance();
    }

    private void advance() {
        nextMatch = null;
        while (sourceIterator.hasNext()) {
            Account acc = sourceIterator.next();
            if (activeOnly && !acc.isActive()) {
                continue;
            }
            if (acc.getBalance() < minBalance) {
                continue;
            }
            nextMatch = acc;
            break;
        }
    }

    @Override
    public boolean hasNext() {
        return nextMatch != null;
    }

    @Override
    public Account next() {
        if (nextMatch == null) {
            throw new NoSuchElementException("No more matching accounts");
        }
        Account current = nextMatch;
        advance();
        return current;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove is not supported on filter iterator");
    }
}
