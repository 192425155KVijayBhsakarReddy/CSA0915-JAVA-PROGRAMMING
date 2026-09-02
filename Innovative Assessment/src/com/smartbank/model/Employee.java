package com.smartbank.model;

import com.smartbank.model.enums.EmployeeRole;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * Represents a bank employee / staff member.
 */
public class Employee implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String employeeId;
    private String name;
    private String email;
    private EmployeeRole role;
    private String department;
    private double salary;
    private final Date hireDate;

    public Employee(String employeeId, String name, String email, EmployeeRole role, String department, double salary) {
        this.employeeId = Objects.requireNonNull(employeeId, "Employee ID cannot be null");
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.email = email;
        this.role = Objects.requireNonNull(role, "Role cannot be null");
        this.department = department;
        this.salary = Math.max(0.0, salary);
        this.hireDate = new Date();
    }

    public String getEmployeeId() {
        return employeeId;
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

    public EmployeeRole getRole() {
        return role;
    }

    public void setRole(EmployeeRole role) {
        this.role = Objects.requireNonNull(role);
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = Math.max(0.0, salary);
    }

    public Date getHireDate() {
        return new Date(hireDate.getTime());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee)) return false;
        Employee employee = (Employee) o;
        return Objects.equals(employeeId, employee.employeeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId);
    }

    @Override
    public String toString() {
        return String.format("Employee[%s] %s | Role: %s | Dept: %s | Salary: $%.2f",
            employeeId, name, role.getRoleTitle(), department, salary);
    }
}
