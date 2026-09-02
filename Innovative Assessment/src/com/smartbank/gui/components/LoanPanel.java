package com.smartbank.gui.components;

import com.smartbank.gui.util.UITheme;
import com.smartbank.model.Customer;
import com.smartbank.model.Employee;
import com.smartbank.model.Loan;
import com.smartbank.model.enums.EmployeeRole;
import com.smartbank.model.enums.LoanStatus;
import com.smartbank.service.BankService;
import com.smartbank.service.EmployeeService;
import com.smartbank.service.LoanService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Interface for Loan Applications, Interactive EMI Calculator,
 * Approval/Disbursement workflow, and Repayment management.
 */
public class LoanPanel extends JPanel {
    private final BankService bankService;
    private final LoanService loanService;
    private final EmployeeService employeeService;

    // EMI Calculator fields
    private JTextField txtCalcPrincipal;
    private JTextField txtCalcRate;
    private JTextField txtCalcTenure;
    private JLabel lblCalcEmiResult;
    private JLabel lblCalcTotalInterest;

    // Loan Application fields
    private JComboBox<String> cmbLoanCustomer;
    private JComboBox<String> cmbDisbursementAccount;
    private JTextField txtApplyPrincipal;
    private JTextField txtApplyRate;
    private JTextField txtApplyTenure;

    // Table & Actions
    private DefaultTableModel loanTableModel;
    private JTable loanTable;
    private JComboBox<String> cmbOfficer;

    public LoanPanel(BankService bankService, LoanService loanService, EmployeeService employeeService) {
        this.bankService = bankService;
        this.loanService = loanService;
        this.employeeService = employeeService;

        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        initComponents();
        refreshDropdowns();
        refreshLoanTable();
    }

    private void initComponents() {
        // WEST: EMI Calculator + Loan Application Form
        JPanel westPanel = new JPanel();
        westPanel.setLayout(new BoxLayout(westPanel, BoxLayout.Y_AXIS));
        westPanel.setBackground(UITheme.BG_MAIN);
        westPanel.setPreferredSize(new Dimension(380, 0));

        // 1. Interactive EMI Calculator Card
        JPanel emiCard = UITheme.createCardPanel("Financial EMI Calculator");
        JPanel emiForm = new JPanel(new GridLayout(5, 2, 8, 8));
        emiForm.setBackground(Color.WHITE);

        emiForm.add(new JLabel("Principal Amount ($):"));
        emiForm.add(txtCalcPrincipal = UITheme.createTextField(10));
        txtCalcPrincipal.setText("25000");

        emiForm.add(new JLabel("Annual Interest Rate (%):"));
        emiForm.add(txtCalcRate = UITheme.createTextField(10));
        txtCalcRate.setText("8.5");

        emiForm.add(new JLabel("Tenure (Months):"));
        emiForm.add(txtCalcTenure = UITheme.createTextField(10));
        txtCalcTenure.setText("36");

        emiForm.add(new JLabel("Estimated Monthly EMI:"));
        emiForm.add(lblCalcEmiResult = new JLabel("$789.24"));
        lblCalcEmiResult.setFont(UITheme.FONT_BODY_BOLD);
        lblCalcEmiResult.setForeground(UITheme.SUCCESS);

        JButton btnCalc = UITheme.createPrimaryButton("Calculate EMI");
        emiForm.add(new JLabel(""));
        emiForm.add(btnCalc);

        btnCalc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calculateEmiDisplay();
            }
        });

        emiCard.add(emiForm, BorderLayout.CENTER);
        westPanel.add(emiCard);
        westPanel.add(Box.createVerticalStrut(15));

        // 2. Loan Application Card
        JPanel appCard = UITheme.createCardPanel("Apply For New Loan");
        JPanel appForm = new JPanel(new GridLayout(6, 2, 8, 8));
        appForm.setBackground(Color.WHITE);

        appForm.add(new JLabel("Applicant Client:"));
        appForm.add(cmbLoanCustomer = new JComboBox<>());

        appForm.add(new JLabel("Disbursement Account:"));
        appForm.add(cmbDisbursementAccount = new JComboBox<>());

        appForm.add(new JLabel("Principal ($):"));
        appForm.add(txtApplyPrincipal = UITheme.createTextField(10));
        txtApplyPrincipal.setText("15000");

        appForm.add(new JLabel("Annual Interest (e.g. 0.08):"));
        appForm.add(txtApplyRate = UITheme.createTextField(10));
        txtApplyRate.setText("0.08");

        appForm.add(new JLabel("Tenure (Months):"));
        appForm.add(txtApplyTenure = UITheme.createTextField(10));
        txtApplyTenure.setText("24");

        JButton btnApply = UITheme.createSuccessButton("Submit Application");
        appForm.add(new JLabel(""));
        appForm.add(btnApply);

        btnApply.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLoanApplication();
            }
        });

        appCard.add(appForm, BorderLayout.CENTER);
        westPanel.add(appCard);

        add(westPanel, BorderLayout.WEST);

        // CENTER: Loans Management Table & Actions
        JPanel centerCard = UITheme.createCardPanel("Active Loan Portfolio & Approvals");

        // Action bar at top
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        actionBar.setBackground(Color.WHITE);
        actionBar.add(new JLabel("Reviewing Officer:"));
        actionBar.add(cmbOfficer = new JComboBox<>());

        JButton btnApprove = UITheme.createSuccessButton("Approve & Disburse");
        JButton btnReject = UITheme.createDangerButton("Reject Loan");
        JButton btnRepay = UITheme.createPrimaryButton("Pay Installment (EMI)");

        actionBar.add(btnApprove);
        actionBar.add(btnReject);
        actionBar.add(btnRepay);

        btnApprove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleApproveLoan();
            }
        });

        btnReject.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRejectLoan();
            }
        });

        btnRepay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRepayInstallment();
            }
        });

        centerCard.add(actionBar, BorderLayout.NORTH);

        String[] cols = {"Loan ID", "Customer", "Account", "Principal", "EMI", "Remaining", "Months Left", "Status"};
        loanTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        loanTable = new JTable(loanTableModel);
        UITheme.styleTable(loanTable);
        JScrollPane scrollTable = new JScrollPane(loanTable);
        scrollTable.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));
        centerCard.add(scrollTable, BorderLayout.CENTER);

        add(centerCard, BorderLayout.CENTER);
    }

    private void calculateEmiDisplay() {
        try {
            double p = Double.parseDouble(txtCalcPrincipal.getText().trim());
            double r = Double.parseDouble(txtCalcRate.getText().trim()) / 100.0;
            int n = Integer.parseInt(txtCalcTenure.getText().trim());
            double emi = loanService.calculateEmi(p, r, n);
            lblCalcEmiResult.setText(String.format("$%,.2f / mo", emi));
        } catch (Exception ex) {
            lblCalcEmiResult.setText("Invalid Inputs");
        }
    }

    private void handleLoanApplication() {
        String custSel = (String) cmbLoanCustomer.getSelectedItem();
        String accSel = (String) cmbDisbursementAccount.getSelectedItem();
        if (custSel == null || accSel == null) {
            JOptionPane.showMessageDialog(this, "Select customer and disbursement account.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String custId = custSel.split(" - ")[0];
        String accNum = accSel.split(" ")[0];

        try {
            double p = Double.parseDouble(txtApplyPrincipal.getText().trim());
            double r = Double.parseDouble(txtApplyRate.getText().trim());
            int n = Integer.parseInt(txtApplyTenure.getText().trim());

            Loan loan = loanService.applyForLoan(custId, accNum, p, r, n);
            JOptionPane.showMessageDialog(this, "Loan Application Submitted!\nID: " + loan.getLoanId() + "\nMonthly EMI: $" + String.format("%.2f", loan.getMonthlyEmi()), "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshLoanTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Loan Application Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getSelectedLoanId() {
        int row = loanTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a loan from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return (String) loanTableModel.getValueAt(row, 0);
    }

    private void handleApproveLoan() {
        String loanId = getSelectedLoanId();
        if (loanId == null) return;
        String officerSel = (String) cmbOfficer.getSelectedItem();
        String officerId = officerSel != null ? officerSel.split(" - ")[0] : "EMP-001";

        try {
            loanService.approveLoan(loanId, officerId);
            JOptionPane.showMessageDialog(this, "Loan " + loanId + " Approved & Funds Disbursed to Account!", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshLoanTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Approval Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRejectLoan() {
        String loanId = getSelectedLoanId();
        if (loanId == null) return;
        String officerSel = (String) cmbOfficer.getSelectedItem();
        String officerId = officerSel != null ? officerSel.split(" - ")[0] : "EMP-001";

        String reason = JOptionPane.showInputDialog(this, "Enter reason for loan rejection:", "Reject Reason", JOptionPane.PLAIN_MESSAGE);
        if (reason == null || reason.trim().isEmpty()) return;

        try {
            loanService.rejectLoan(loanId, officerId, reason);
            JOptionPane.showMessageDialog(this, "Loan " + loanId + " Rejected.", "Updated", JOptionPane.INFORMATION_MESSAGE);
            refreshLoanTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Rejection Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRepayInstallment() {
        String loanId = getSelectedLoanId();
        if (loanId == null) return;

        Loan loan = loanService.getLoan(loanId).orElse(null);
        if (loan == null) return;

        try {
            loanService.repayLoanInstallment(loanId, loan.getMonthlyEmi());
            JOptionPane.showMessageDialog(this, "Installment paid: $" + String.format("%.2f", loan.getMonthlyEmi()) + "\nRemaining: $" + String.format("%.2f", loan.getRemainingPrincipal()), "Payment Recorded", JOptionPane.INFORMATION_MESSAGE);
            refreshLoanTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Repayment Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshDropdowns() {
        cmbLoanCustomer.removeAllItems();
        for (Customer c : bankService.getAllCustomers()) {
            cmbLoanCustomer.addItem(c.getCustomerId() + " - " + c.getName());
        }

        cmbDisbursementAccount.removeAllItems();
        for (com.smartbank.model.Account a : bankService.getAllAccounts()) {
            cmbDisbursementAccount.addItem(a.getAccountNumber() + " ($" + String.format("%.2f", a.getBalance()) + ")");
        }

        cmbOfficer.removeAllItems();
        for (Employee e : employeeService.getAllEmployees()) {
            if (e.getRole().getAccessLevel() >= EmployeeRole.LOAN_OFFICER.getAccessLevel()) {
                cmbOfficer.addItem(e.getEmployeeId() + " - " + e.getName() + " (" + e.getRole().getRoleTitle() + ")");
            }
        }
    }

    public void refreshLoanTable() {
        loanTableModel.setRowCount(0);
        for (Loan l : loanService.getAllLoans()) {
            loanTableModel.addRow(new Object[]{
                l.getLoanId(),
                l.getCustomerId(),
                l.getDisbursementAccountNumber(),
                String.format("$%,.2f", l.getPrincipalAmount()),
                String.format("$%,.2f", l.getMonthlyEmi()),
                String.format("$%,.2f", l.getRemainingPrincipal()),
                l.getRemainingMonths(),
                l.getStatus().getLabel()
            });
        }
    }
}
