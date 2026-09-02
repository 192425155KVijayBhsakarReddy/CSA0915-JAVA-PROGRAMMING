package com.smartbank.interfaces;

import com.smartbank.exceptions.BankException;
import com.smartbank.model.Account;
import com.smartbank.model.Transaction;

import java.util.List;

/**
 * Generic interface defining standard banking operations.
 * Type parameter T is bounded by Account.
 */
public interface BankOperations<T extends Account> {
    void deposit(String accountNumber, double amount, String notes) throws BankException;

    void withdraw(String accountNumber, double amount, String notes) throws BankException;

    Transaction transfer(String sourceAccountNumber, String targetAccountNumber, double amount, String notes) throws BankException;

    double getBalance(String accountNumber) throws BankException;

    T getAccount(String accountNumber) throws BankException;

    List<Transaction> getTransactionHistory(String accountNumber);
}
