package com.smartbank.exceptions;

public class UnauthorizedOperationException extends BankException {
    private final String employeeId;
    private final String requiredRole;

    public UnauthorizedOperationException(String employeeId, String requiredRole, String operation) {
        super("UNAUTHORIZED_ACCESS", String.format(
            "Employee '%s' lacks authority for operation '%s'. Required minimum role: %s",
            employeeId, operation, requiredRole
        ));
        this.employeeId = employeeId;
        this.requiredRole = requiredRole;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getRequiredRole() {
        return requiredRole;
    }
}
