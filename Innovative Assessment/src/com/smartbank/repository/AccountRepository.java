package com.smartbank.repository;

import com.smartbank.model.Account;
import com.smartbank.model.enums.AccountType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Specialized repository for Account operations.
 * Demonstrates collection filtering, sorting with Comparators, and aggregations.
 */
public class AccountRepository extends InMemoryRepository<String, Account> {
    private static final long serialVersionUID = 1L;

    public List<Account> findByCustomerId(String customerId) {
        List<Account> result = new ArrayList<>();
        if (customerId == null) return result;
        for (Account acc : storage.values()) {
            if (customerId.equalsIgnoreCase(acc.getCustomerId())) {
                result.add(acc);
            }
        }
        return result;
    }

    public List<Account> findAccountsByType(AccountType type) {
        List<Account> result = new ArrayList<>();
        for (Account acc : storage.values()) {
            if (acc.getAccountType() == type) {
                result.add(acc);
            }
        }
        return result;
    }

    public List<Account> findAccountsSortedByBalance(final boolean ascending) {
        List<Account> list = new ArrayList<>(storage.values());
        Collections.sort(list, new Comparator<Account>() {
            @Override
            public int compare(Account a1, Account a2) {
                int cmp = Double.compare(a1.getBalance(), a2.getBalance());
                return ascending ? cmp : -cmp;
            }
        });
        return list;
    }

    public double getTotalBankAssets() {
        double total = 0.0;
        for (Account acc : storage.values()) {
            total += acc.getBalance();
        }
        return total;
    }
}
