package com.smartbank.gui.components;

import com.smartbank.gui.util.UITheme;
import com.smartbank.model.Employee;
import com.smartbank.model.enums.EmployeeRole;
import com.smartbank.service.EmployeeService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Interface for Bank Staff Records, Role assignment, and HR directory.
 */
public class EmployeePanel extends JPanel {
    private final EmployeeService employeeService;

    private JTextField txtEmpId;
    private JTextField txtEmpName;
    private JTextField txtEmpEmail;
    private JComboBox<EmployeeRole> cmbEmpRole;
    private JTextField txtEmpDept;
    private JTextField txtEmpSalary;

    private DefaultTableModel empTableModel;
    private JTable empTable;

    public EmployeePanel(EmployeeService employeeService) {
        this.employeeService = employeeService;

        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        initComponents();
        refreshEmployeeTable();
    }

    private void initComponents() {
        // WEST: Employee Registration Form
        JPanel westPanel = new JPanel();
        westPanel.setLayout(new BoxLayout(westPanel, BoxLayout.Y_AXIS));
        westPanel.setBackground(UITheme.BG_MAIN);
        westPanel.setPreferredSize(new Dimension(380, 0));

        JPanel formCard = UITheme.createCardPanel("Register Bank Staff");
        JPanel form = new JPanel(new GridLayout(7, 2, 8, 8));
        form.setBackground(Color.WHITE);

        form.add(new JLabel("Employee ID:"));
        form.add(txtEmpId = UITheme.createTextField(10));
        txtEmpId.setText("EMP-" + (100 + employeeService.getAllEmployees().size() + 1));

        form.add(new JLabel("Full Name:"));
        form.add(txtEmpName = UITheme.createTextField(10));

        form.add(new JLabel("Email:"));
        form.add(txtEmpEmail = UITheme.createTextField(10));

        form.add(new JLabel("Role & Access Level:"));
        form.add(cmbEmpRole = new JComboBox<>(EmployeeRole.values()));

        form.add(new JLabel("Department:"));
        form.add(txtEmpDept = UITheme.createTextField(10));
        txtEmpDept.setText("Retail Banking");

        form.add(new JLabel("Annual Salary ($):"));
        form.add(txtEmpSalary = UITheme.createTextField(10));
        txtEmpSalary.setText("65000");

        JButton btnAddEmp = UITheme.createPrimaryButton("Save Employee");
        form.add(new JLabel(""));
        form.add(btnAddEmp);

        btnAddEmp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRegisterEmployee();
            }
        });

        formCard.add(form, BorderLayout.CENTER);
        westPanel.add(formCard);

        add(westPanel, BorderLayout.WEST);

        // CENTER: Employee Records Table
        JPanel centerCard = UITheme.createCardPanel("Bank Staff Directory & Role-Based Access");

        String[] cols = {"Emp ID", "Full Name", "Email", "Role Title", "Department", "Salary", "Hire Date"};
        empTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        empTable = new JTable(empTableModel);
        UITheme.styleTable(empTable);
        JScrollPane scrollPane = new JScrollPane(empTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));
        centerCard.add(scrollPane, BorderLayout.CENTER);

        add(centerCard, BorderLayout.CENTER);
    }

    private void handleRegisterEmployee() {
        String id = txtEmpId.getText().trim();
        String name = txtEmpName.getText().trim();
        String email = txtEmpEmail.getText().trim();
        EmployeeRole role = (EmployeeRole) cmbEmpRole.getSelectedItem();
        String dept = txtEmpDept.getText().trim();

        if (id.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Employee ID and Name are required.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double salary = Double.parseDouble(txtEmpSalary.getText().trim());
            employeeService.registerEmployee(id, name, email, role, dept, salary);
            JOptionPane.showMessageDialog(this, "Employee registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

            txtEmpName.setText("");
            txtEmpEmail.setText("");
            txtEmpId.setText("EMP-" + (100 + employeeService.getAllEmployees().size() + 1));
            refreshEmployeeTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshEmployeeTable() {
        empTableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<Employee> list = employeeService.getAllEmployees();
        for (Employee e : list) {
            empTableModel.addRow(new Object[]{
                e.getEmployeeId(),
                e.getName(),
                e.getEmail(),
                e.getRole().getRoleTitle(),
                e.getDepartment(),
                String.format("$%,.2f", e.getSalary()),
                sdf.format(e.getHireDate())
            });
        }
    }
}
