package com.smartbank.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a bank customer/client.
 */
public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String customerId;
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private int creditScore; // 300 - 850
    private boolean kycVerified;
    private final List<String> associatedAccountNumbers;

    public Customer(String customerId, String name, String email, String phoneNumber, String address, int creditScore) {
        this.customerId = Objects.requireNonNull(customerId, "Customer ID cannot be null");
        this.name = Objects.requireNonNull(name, "Customer name cannot be null");
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.creditScore = Math.max(300, Math.min(850, creditScore));
        this.kycVerified = false;
        this.associatedAccountNumbers = new ArrayList<>();
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(int creditScore) {
        this.creditScore = Math.max(300, Math.min(850, creditScore));
    }

    public boolean isKycVerified() {
        return kycVerified;
    }

    public void setKycVerified(boolean kycVerified) {
        this.kycVerified = kycVerified;
    }

    public synchronized void linkAccount(String accountNumber) {
        if (!associatedAccountNumbers.contains(accountNumber)) {
            associatedAccountNumbers.add(accountNumber);
        }
    }

    public synchronized void unlinkAccount(String accountNumber) {
        associatedAccountNumbers.remove(accountNumber);
    }

    public synchronized List<String> getAssociatedAccountNumbers() {
        return Collections.unmodifiableList(new ArrayList<>(associatedAccountNumbers));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer)) return false;
        Customer customer = (Customer) o;
        return Objects.equals(customerId, customer.customerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId);
    }

    @Override
    public String toString() {
        return String.format("Customer[%s] %s | Email: %s | Phone: %s | Score: %d | KYC: %s",
            customerId, name, email, phoneNumber, creditScore, kycVerified ? "VERIFIED" : "PENDING");
    }
}
