package com.smartbank.model.enums;

import java.io.Serializable;

public enum AccountType implements Serializable {
    SAVINGS("Savings Account"),
    CHECKING("Checking Account");

    private final String displayName;

    AccountType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
