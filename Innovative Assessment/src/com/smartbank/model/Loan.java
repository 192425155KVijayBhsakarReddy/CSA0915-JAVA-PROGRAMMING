package com.smartbank.model;

import com.smartbank.model.enums.LoanStatus;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * Represents a loan application and active loan record.
 */
public class Loan implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String loanId;
    private final String customerId;
    private final String disbursementAccountNumber;
    private final double principalAmount;
    private final double annualInterestRate; // e.g. 0.085 for 8.5%
    private final int termMonths;             // e.g. 24 months
    private final double monthlyEmi;
    private double remainingPrincipal;
    private int remainingMonths;
    private LoanStatus status;
    private final Date applicationDate;
    private Date approvalDate;

    public Loan(String loanId, String customerId, String disbursementAccountNumber,
                double principalAmount, double annualInterestRate, int termMonths) {
        if (principalAmount <= 0) throw new IllegalArgumentException("Principal must be > 0");
        if (termMonths <= 0) throw new IllegalArgumentException("Term months must be > 0");

        this.loanId = Objects.requireNonNull(loanId, "Loan ID required");
        this.customerId = Objects.requireNonNull(customerId, "Customer ID required");
        this.disbursementAccountNumber = disbursementAccountNumber;
        this.principalAmount = principalAmount;
        this.annualInterestRate = annualInterestRate;
        this.termMonths = termMonths;
        this.remainingPrincipal = principalAmount;
        this.remainingMonths = termMonths;
        this.monthlyEmi = calculateEmi(principalAmount, annualInterestRate, termMonths);
        this.status = LoanStatus.PENDING;
        this.applicationDate = new Date();
    }

    /**
     * Standard financial EMI formula:
     * E = [P * r * (1+r)^n] / [(1+r)^n - 1]
     */
    public static double calculateEmi(double principal, double annualRate, int tenureMonths) {
        if (principal <= 0 || tenureMonths <= 0) return 0.0;
        double monthlyRate = annualRate / 12.0;
        if (monthlyRate == 0) {
            return principal / tenureMonths;
        }
        double factor = Math.pow(1 + monthlyRate, tenureMonths);
        return (principal * monthlyRate * factor) / (factor - 1);
    }

    public synchronized void recordRepayment(double amount) {
        if (amount <= 0) return;
        this.remainingPrincipal = Math.max(0.0, this.remainingPrincipal - amount);
        if (this.remainingPrincipal == 0.0) {
            this.status = LoanStatus.CLOSED;
            this.remainingMonths = 0;
        } else if (remainingMonths > 0) {
            remainingMonths--;
        }
    }

    public String getLoanId() {
        return loanId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getDisbursementAccountNumber() {
        return disbursementAccountNumber;
    }

    public double getPrincipalAmount() {
        return principalAmount;
    }

    public double getAnnualInterestRate() {
        return annualInterestRate;
    }

    public int getTermMonths() {
        return termMonths;
    }

    public double getMonthlyEmi() {
        return monthlyEmi;
    }

    public synchronized double getRemainingPrincipal() {
        return remainingPrincipal;
    }

    public synchronized int getRemainingMonths() {
        return remainingMonths;
    }

    public synchronized LoanStatus getStatus() {
        return status;
    }

    public synchronized void setStatus(LoanStatus status) {
        this.status = Objects.requireNonNull(status);
        if (status == LoanStatus.APPROVED || status == LoanStatus.ACTIVE) {
            this.approvalDate = new Date();
        }
    }

    public Date getApplicationDate() {
        return new Date(applicationDate.getTime());
    }

    public Date getApprovalDate() {
        return approvalDate != null ? new Date(approvalDate.getTime()) : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Loan)) return false;
        Loan loan = (Loan) o;
        return Objects.equals(loanId, loan.loanId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loanId);
    }

    @Override
    public String toString() {
        return String.format("Loan[%s] Cust: %s | Principal: $%.2f | EMI: $%.2f | Balance: $%.2f | Status: %s",
            loanId, customerId, principalAmount, monthlyEmi, remainingPrincipal, status);
    }
}
