package com.smartbank.model.enums;

import java.io.Serializable;

public enum EmployeeRole implements Serializable {
    TELLER("Bank Teller", 1),
    LOAN_OFFICER("Loan Officer", 2),
    BRANCH_MANAGER("Branch Manager", 3),
    SYSTEM_ADMIN("System Administrator", 4);

    private final String roleTitle;
    private final int accessLevel;

    EmployeeRole(String roleTitle, int accessLevel) {
        this.roleTitle = roleTitle;
        this.accessLevel = accessLevel;
    }

    public String getRoleTitle() {
        return roleTitle;
    }

    public int getAccessLevel() {
        return accessLevel;
    }
}
