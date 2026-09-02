package com.smartbank.concurrency;

import com.smartbank.exceptions.ConcurrencyConflictException;
import com.smartbank.model.Account;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Utility for deadlock-free deterministic lock acquisition across accounts.
 * Always orders locks lexicographically by account number.
 */
public class LockManager {
    private static final long DEFAULT_LOCK_TIMEOUT_MS = 5000;

    public interface TransferAction<R> {
        R execute() throws Exception;
    }

    /**
     * Executes an atomic transfer action by acquiring locks on both accounts
     * in deterministic order to prevent deadlocks (Dining Philosophers / Resource Hierarchy).
     */
    public static <R> R executeWithOrderedLocks(Account fromAcc, Account toAcc, TransferAction<R> action) throws Exception {
        if (fromAcc == null || toAcc == null) {
            throw new IllegalArgumentException("Accounts cannot be null for transfer lock");
        }

        if (fromAcc.equals(toAcc)) {
            throw new IllegalArgumentException("Cannot transfer to identical account");
        }

        // Determine deterministic order
        Account first = fromAcc.getAccountNumber().compareTo(toAcc.getAccountNumber()) < 0 ? fromAcc : toAcc;
        Account second = first == fromAcc ? toAcc : fromAcc;

        ReentrantLock lock1 = first.getLock();
        ReentrantLock lock2 = second.getLock();

        boolean acquiredFirst = false;
        boolean acquiredSecond = false;

        try {
            acquiredFirst = lock1.tryLock(DEFAULT_LOCK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            if (!acquiredFirst) {
                throw new ConcurrencyConflictException(first.getAccountNumber(), Thread.currentThread().getName());
            }

            acquiredSecond = lock2.tryLock(DEFAULT_LOCK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            if (!acquiredSecond) {
                throw new ConcurrencyConflictException(second.getAccountNumber(), Thread.currentThread().getName());
            }

            // Both locks held safely, execute critical section
            return action.execute();

        } finally {
            if (acquiredSecond) {
                lock2.unlock();
            }
            if (acquiredFirst) {
                lock1.unlock();
            }
        }
    }
}
