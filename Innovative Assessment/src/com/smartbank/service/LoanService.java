package com.smartbank.service;

import com.smartbank.exceptions.BankException;
import com.smartbank.exceptions.InvalidAccountException;
import com.smartbank.exceptions.LoanExceededException;
import com.smartbank.interfaces.LoanOperations;
import com.smartbank.model.Account;
import com.smartbank.model.Customer;
import com.smartbank.model.Loan;
import com.smartbank.model.enums.EmployeeRole;
import com.smartbank.model.enums.LoanStatus;
import com.smartbank.repository.InMemoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service managing loan lifecycle: application, credit evaluation, approval/disbursement,
 * and EMI repayments.
 */
public class LoanService implements LoanOperations {
    private final InMemoryRepository<String, Loan> loanRepo;
    private final BankService bankService;
    private final EmployeeService employeeService;

    public LoanService(BankService bankService, EmployeeService employeeService) {
        this.loanRepo = new InMemoryRepository<>();
        this.bankService = bankService;
        this.employeeService = employeeService;
    }

    @Override
    public Loan applyForLoan(String customerId, String accountNumber, double amount, double annualRate, int tenureMonths) throws BankException {
        if (amount <= 0) {
            throw new BankException("INVALID_AMOUNT", "Loan amount must be positive.");
        }
        if (tenureMonths <= 0) {
            throw new BankException("INVALID_TENURE", "Tenure must be at least 1 month.");
        }

        // Validate customer
        Customer customer = bankService.getCustomer(customerId);
        if (customer == null) {
            throw new BankException("CUSTOMER_NOT_FOUND", "Customer ID " + customerId + " not found.");
        }

        // Validate account
        Account account = bankService.getAccount(accountNumber);
        if (account == null) {
            throw new InvalidAccountException(accountNumber);
        }

        // Credit Risk Rule: Max loan limit is based on credit score
        double maxEligible = customer.getCreditScore() * 150.0; // e.g. 700 score -> $105,000 max
        if (amount > maxEligible) {
            throw new LoanExceededException(amount, maxEligible);
        }

        String loanId = "LN-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Loan loan = new Loan(loanId, customerId, accountNumber, amount, annualRate, tenureMonths);
        loanRepo.save(loanId, loan);

        bankService.log("SYSTEM", "LOAN_APPLICATION",
            String.format("Loan application %s submitted for Customer %s. Principal: $%.2f", loanId, customerId, amount),
            null);

        return loan;
    }

    @Override
    public Loan approveLoan(String loanId, String approverEmployeeId) throws BankException {
        // Only LOAN_OFFICER or higher can approve
        employeeService.verifyRole(approverEmployeeId, EmployeeRole.LOAN_OFFICER, "APPROVE_LOAN");

        Optional<Loan> opt = loanRepo.findById(loanId);
        if (!opt.isPresent()) {
            throw new BankException("LOAN_NOT_FOUND", "Loan ID " + loanId + " does not exist.");
        }

        Loan loan = opt.get();
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new BankException("INVALID_LOAN_STATE", "Loan is not pending approval (current state: " + loan.getStatus() + ")");
        }

        loan.setStatus(LoanStatus.APPROVED);

        // Disburse loan funds directly into borrower's bank account
        try {
            bankService.deposit(loan.getDisbursementAccountNumber(), loan.getPrincipalAmount(),
                "Loan Disbursement for Loan ID: " + loan.getLoanId());
            loan.setStatus(LoanStatus.ACTIVE);
        } catch (Exception e) {
            throw new BankException("DISBURSEMENT_FAILED", "Failed to disburse loan funds: " + e.getMessage(), e);
        }

        bankService.log(approverEmployeeId, "LOAN_APPROVED",
            String.format("Loan %s approved and disbursed ($%.2f) to Account %s",
                loanId, loan.getPrincipalAmount(), loan.getDisbursementAccountNumber()),
            null);

        return loan;
    }

    @Override
    public Loan rejectLoan(String loanId, String approverEmployeeId, String reason) throws BankException {
        employeeService.verifyRole(approverEmployeeId, EmployeeRole.LOAN_OFFICER, "REJECT_LOAN");

        Optional<Loan> opt = loanRepo.findById(loanId);
        if (!opt.isPresent()) {
            throw new BankException("LOAN_NOT_FOUND", "Loan ID " + loanId + " does not exist.");
        }

        Loan loan = opt.get();
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new BankException("INVALID_LOAN_STATE", "Loan cannot be rejected from state: " + loan.getStatus());
        }

        loan.setStatus(LoanStatus.REJECTED);
        bankService.log(approverEmployeeId, "LOAN_REJECTED",
            String.format("Loan %s rejected. Reason: %s", loanId, reason), null);

        return loan;
    }

    @Override
    public void repayLoanInstallment(String loanId, double amount) throws BankException {
        if (amount <= 0) {
            throw new BankException("INVALID_AMOUNT", "Repayment amount must be > 0");
        }

        Optional<Loan> opt = loanRepo.findById(loanId);
        if (!opt.isPresent()) {
            throw new BankException("LOAN_NOT_FOUND", "Loan ID " + loanId + " not found.");
        }

        Loan loan = opt.get();
        if (loan.getStatus() != LoanStatus.ACTIVE && loan.getStatus() != LoanStatus.APPROVED) {
            throw new BankException("INACTIVE_LOAN", "Loan is not in active repayment status (" + loan.getStatus() + ")");
        }

        // Debit funds from linked account
        bankService.withdraw(loan.getDisbursementAccountNumber(), amount, "EMI Repayment for Loan " + loanId);
        loan.recordRepayment(amount);

        bankService.log("SYSTEM", "LOAN_REPAYMENT",
            String.format("Loan %s payment of $%.2f received. Remaining balance: $%.2f",
                loanId, amount, loan.getRemainingPrincipal()), null);
    }

    @Override
    public List<Loan> getCustomerLoans(String customerId) {
        List<Loan> list = new ArrayList<>();
        for (Loan l : loanRepo.findAll()) {
            if (l.getCustomerId().equalsIgnoreCase(customerId)) {
                list.add(l);
            }
        }
        return list;
    }

    @Override
    public double calculateEmi(double principal, double annualRate, int tenureMonths) {
        return Loan.calculateEmi(principal, annualRate, tenureMonths);
    }

    public List<Loan> getAllLoans() {
        return loanRepo.findAll();
    }

    public Optional<Loan> getLoan(String loanId) {
        return loanRepo.findById(loanId);
    }

    public void populateSeedLoans(List<Loan> loans) {
        for (Loan l : loans) {
            loanRepo.save(l.getLoanId(), l);
        }
    }
}
