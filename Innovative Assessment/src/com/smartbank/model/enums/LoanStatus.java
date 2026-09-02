package com.smartbank.model.enums;

import java.io.Serializable;

public enum LoanStatus implements Serializable {
    PENDING("Under Review"),
    APPROVED("Approved"),
    ACTIVE("Active / Disbursed"),
    REJECTED("Rejected"),
    CLOSED("Fully Repaid"),
    DEFAULTED("Defaulted");

    private final String label;

    LoanStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
