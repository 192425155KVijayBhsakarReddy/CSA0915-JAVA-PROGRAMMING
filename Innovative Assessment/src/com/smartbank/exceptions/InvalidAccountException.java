package com.smartbank.exceptions;

public class InvalidAccountException extends BankException {
    private final String accountNumber;

    public InvalidAccountException(String accountNumber) {
        super("INVALID_ACCOUNT", String.format("Account '%s' does not exist in the bank registry or is inactive.", accountNumber));
        this.accountNumber = accountNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}
