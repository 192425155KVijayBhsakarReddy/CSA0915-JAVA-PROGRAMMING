package com.smartbank.interfaces;

import com.smartbank.exceptions.BankException;
import com.smartbank.model.Loan;

import java.util.List;

/**
 * Interface defining loan management lifecycle operations.
 */
public interface LoanOperations {
    Loan applyForLoan(String customerId, String accountNumber, double amount, double annualRate, int tenureMonths) throws BankException;

    Loan approveLoan(String loanId, String approverEmployeeId) throws BankException;

    Loan rejectLoan(String loanId, String approverEmployeeId, String reason) throws BankException;

    void repayLoanInstallment(String loanId, double amount) throws BankException;

    List<Loan> getCustomerLoans(String customerId);

    double calculateEmi(double principal, double annualRate, int tenureMonths);
}
