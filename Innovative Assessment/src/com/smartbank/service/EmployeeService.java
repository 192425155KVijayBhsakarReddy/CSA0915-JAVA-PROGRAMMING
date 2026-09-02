package com.smartbank.service;

import com.smartbank.exceptions.BankException;
import com.smartbank.exceptions.UnauthorizedOperationException;
import com.smartbank.model.Employee;
import com.smartbank.model.enums.EmployeeRole;
import com.smartbank.repository.InMemoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service managing Bank Employees, Staff Roles, and Role-Based Access Control (RBAC).
 */
public class EmployeeService {
    private final InMemoryRepository<String, Employee> employeeRepo;

    public EmployeeService() {
        this.employeeRepo = new InMemoryRepository<>();
    }

    public Employee registerEmployee(String employeeId, String name, String email,
                                     EmployeeRole role, String department, double salary) throws BankException {
        if (employeeRepo.existsById(employeeId)) {
            throw new BankException("DUPLICATE_EMPLOYEE", "Employee ID " + employeeId + " already exists.");
        }
        Employee emp = new Employee(employeeId, name, email, role, department, salary);
        employeeRepo.save(employeeId, emp);
        return emp;
    }

    public Optional<Employee> getEmployee(String employeeId) {
        return employeeRepo.findById(employeeId);
    }

    public List<Employee> getAllEmployees() {
        return employeeRepo.findAll();
    }

    public List<Employee> getEmployeesByRole(EmployeeRole role) {
        List<Employee> list = new ArrayList<>();
        for (Employee e : employeeRepo.findAll()) {
            if (e.getRole() == role) {
                list.add(e);
            }
        }
        return list;
    }

    public void verifyRole(String employeeId, EmployeeRole requiredRole, String operation) throws BankException {
        Optional<Employee> opt = employeeRepo.findById(employeeId);
        if (!opt.isPresent()) {
            throw new BankException("EMPLOYEE_NOT_FOUND", "Employee ID " + employeeId + " not recognized.");
        }
        Employee emp = opt.get();
        if (emp.getRole().getAccessLevel() < requiredRole.getAccessLevel()) {
            throw new UnauthorizedOperationException(employeeId, requiredRole.getRoleTitle(), operation);
        }
    }

    public void populateSeedEmployees(List<Employee> employees) {
        for (Employee e : employees) {
            employeeRepo.save(e.getEmployeeId(), e);
        }
    }
}
