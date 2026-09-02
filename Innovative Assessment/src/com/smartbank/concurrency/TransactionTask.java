package com.smartbank.concurrency;

import com.smartbank.interfaces.BankOperations;
import com.smartbank.model.Account;
import com.smartbank.model.Transaction;

import java.util.concurrent.Callable;

/**
 * Encapsulates a banking transaction execution for multithreaded scheduling.
 * Supports configurable thread priorities.
 */
public class TransactionTask implements Callable<Transaction>, Runnable {
    public enum TaskPriority {
        CRITICAL(Thread.MAX_PRIORITY),
        NORMAL(Thread.NORM_PRIORITY),
        BACKGROUND(Thread.MIN_PRIORITY);

        private final int javaPriority;

        TaskPriority(int javaPriority) {
            this.javaPriority = javaPriority;
        }

        public int getJavaPriority() {
            return javaPriority;
        }
    }

    private final BankOperations<Account> bankService;
    private final String sourceAccount;
    private final String targetAccount;
    private final double amount;
    private final String notes;
    private final TaskPriority priority;
    private Transaction result;
    private Exception error;

    public TransactionTask(BankOperations<Account> bankService, String sourceAccount, String targetAccount,
                           double amount, String notes, TaskPriority priority) {
        this.bankService = bankService;
        this.sourceAccount = sourceAccount;
        this.targetAccount = targetAccount;
        this.amount = amount;
        this.notes = notes;
        this.priority = priority != null ? priority : TaskPriority.NORMAL;
    }

    @Override
    public Transaction call() throws Exception {
        Thread.currentThread().setPriority(priority.getJavaPriority());
        if (sourceAccount != null && targetAccount != null) {
            // Transfer
            this.result = bankService.transfer(sourceAccount, targetAccount, amount, notes);
        } else if (sourceAccount != null) {
            // Withdraw
            bankService.withdraw(sourceAccount, amount, notes);
        } else if (targetAccount != null) {
            // Deposit
            bankService.deposit(targetAccount, amount, notes);
        }
        return result;
    }

    @Override
    public void run() {
        try {
            call();
        } catch (Exception e) {
            this.error = e;
        }
    }

    public Transaction getResult() {
        return result;
    }

    public Exception getError() {
        return error;
    }

    public TaskPriority getPriority() {
        return priority;
    }
}
