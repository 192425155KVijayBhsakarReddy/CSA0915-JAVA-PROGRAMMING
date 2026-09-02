package com.smartbank.gui.components;

import com.smartbank.gui.util.UITheme;
import com.smartbank.model.Account;
import com.smartbank.model.CheckingAccount;
import com.smartbank.model.Customer;
import com.smartbank.model.SavingsAccount;
import com.smartbank.model.enums.AccountType;
import com.smartbank.service.BankService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Management interface for opening accounts, registering customers,
 * and filtering/sorting account records.
 */
public class AccountPanel extends JPanel {
    private final BankService bankService;

    private JComboBox<String> cmbCustomer;
    private JComboBox<String> cmbAccountType;
    private JTextField txtInitialDeposit;
    private JTextField txtParam1; // Interest Rate or Overdraft
    private JTextField txtParam2; // Min Balance or Maintenance Fee
    private JLabel lblParam1;
    private JLabel lblParam2;

    private JTextField txtCustName;
    private JTextField txtCustEmail;
    private JTextField txtCustPhone;
    private JTextField txtCustCreditScore;

    private DefaultTableModel accountTableModel;
    private JTable accountTable;
    private JComboBox<String> cmbSortOption;

    public AccountPanel(BankService bankService) {
        this.bankService = bankService;
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        initComponents();
        refreshCustomerDropdown();
        refreshAccountTable();
    }

    private void initComponents() {
        // WEST: Forms in a split card layout
        JPanel westPanel = new JPanel();
        westPanel.setLayout(new BoxLayout(westPanel, BoxLayout.Y_AXIS));
        westPanel.setBackground(UITheme.BG_MAIN);
        westPanel.setPreferredSize(new Dimension(380, 0));

        // Form 1: Customer Registration
        JPanel custCard = UITheme.createCardPanel("1. Register New Customer");
        JPanel custForm = new JPanel(new GridLayout(5, 2, 8, 8));
        custForm.setBackground(Color.WHITE);

        custForm.add(new JLabel("Full Name:"));
        custForm.add(txtCustName = UITheme.createTextField(10));

        custForm.add(new JLabel("Email:"));
        custForm.add(txtCustEmail = UITheme.createTextField(10));

        custForm.add(new JLabel("Phone:"));
        custForm.add(txtCustPhone = UITheme.createTextField(10));

        custForm.add(new JLabel("Credit Score (300-850):"));
        custForm.add(txtCustCreditScore = UITheme.createTextField(10));
        txtCustCreditScore.setText("720");

        JButton btnRegisterCust = UITheme.createPrimaryButton("Register Client");
        custForm.add(new JLabel(""));
        custForm.add(btnRegisterCust);

        btnRegisterCust.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleCustomerRegistration();
            }
        });

        custCard.add(custForm, BorderLayout.CENTER);
        westPanel.add(custCard);
        westPanel.add(Box.createVerticalStrut(15));

        // Form 2: Open Bank Account
        JPanel accCard = UITheme.createCardPanel("2. Open Bank Account");
        JPanel accForm = new JPanel(new GridLayout(6, 2, 8, 8));
        accForm.setBackground(Color.WHITE);

        accForm.add(new JLabel("Select Customer:"));
        accForm.add(cmbCustomer = new JComboBox<>());

        accForm.add(new JLabel("Account Type:"));
        accForm.add(cmbAccountType = new JComboBox<>(new String[]{"Savings Account", "Checking Account"}));

        accForm.add(new JLabel("Initial Deposit ($):"));
        accForm.add(txtInitialDeposit = UITheme.createTextField(10));
        txtInitialDeposit.setText("500.00");

        accForm.add(lblParam1 = new JLabel("Interest Rate (e.g. 0.04):"));
        accForm.add(txtParam1 = UITheme.createTextField(10));
        txtParam1.setText("0.04");

        accForm.add(lblParam2 = new JLabel("Min Balance ($):"));
        accForm.add(txtParam2 = UITheme.createTextField(10));
        txtParam2.setText("100.00");

        cmbAccountType.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cmbAccountType.getSelectedIndex() == 0) {
                    lblParam1.setText("Interest Rate (e.g. 0.04):");
                    txtParam1.setText("0.04");
                    lblParam2.setText("Min Balance ($):");
                    txtParam2.setText("100.00");
                } else {
                    lblParam1.setText("Overdraft Limit ($):");
                    txtParam1.setText("500.00");
                    lblParam2.setText("Monthly Fee ($):");
                    txtParam2.setText("12.00");
                }
            }
        });

        JButton btnOpenAccount = UITheme.createSuccessButton("Open Account");
        accForm.add(new JLabel(""));
        accForm.add(btnOpenAccount);

        btnOpenAccount.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleOpenAccount();
            }
        });

        accCard.add(accForm, BorderLayout.CENTER);
        westPanel.add(accCard);

        add(westPanel, BorderLayout.WEST);

        // CENTER: Account Records Table
        JPanel tableCard = UITheme.createCardPanel("Active Bank Accounts Registry");

        // Top Filter Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterBar.setBackground(Color.WHITE);
        filterBar.add(new JLabel("Sort By:"));
        cmbSortOption = new JComboBox<>(new String[]{"Account Number", "Balance (High to Low)", "Balance (Low to High)"});
        filterBar.add(cmbSortOption);

        JButton btnRefresh = UITheme.createPrimaryButton("Apply Sort / Refresh");
        filterBar.add(btnRefresh);

        btnRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshAccountTable();
            }
        });

        tableCard.add(filterBar, BorderLayout.NORTH);

        String[] cols = {"Account No.", "Customer", "Type", "Balance", "Features / Limits", "Status"};
        accountTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        accountTable = new JTable(accountTableModel);
        UITheme.styleTable(accountTable);
        JScrollPane scrollTable = new JScrollPane(accountTable);
        scrollTable.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));
        tableCard.add(scrollTable, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);
    }

    private void handleCustomerRegistration() {
        String name = txtCustName.getText().trim();
        String email = txtCustEmail.getText().trim();
        String phone = txtCustPhone.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Customer name cannot be blank.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int score = 700;
        try {
            score = Integer.parseInt(txtCustCreditScore.getText().trim());
        } catch (NumberFormatException ignored) {}

        Customer c = bankService.createCustomer(name, email, phone, "Primary Address", score);
        JOptionPane.showMessageDialog(this, "Customer registered successfully!\nID: " + c.getCustomerId(), "Success", JOptionPane.INFORMATION_MESSAGE);

        txtCustName.setText("");
        txtCustEmail.setText("");
        txtCustPhone.setText("");
        refreshCustomerDropdown();
    }

    private void handleOpenAccount() {
        String selected = (String) cmbCustomer.getSelectedItem();
        if (selected == null || selected.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select or register a customer first.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String custId = selected.split(" - ")[0];

        try {
            double deposit = Double.parseDouble(txtInitialDeposit.getText().trim());
            double p1 = Double.parseDouble(txtParam1.getText().trim());
            double p2 = Double.parseDouble(txtParam2.getText().trim());

            if (cmbAccountType.getSelectedIndex() == 0) {
                // Savings
                SavingsAccount sa = bankService.openSavingsAccount(custId, deposit, p1, p2);
                JOptionPane.showMessageDialog(this, "Savings Account created!\nAccount No: " + sa.getAccountNumber(), "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Checking
                CheckingAccount ca = bankService.openCheckingAccount(custId, deposit, p1, p2);
                JOptionPane.showMessageDialog(this, "Checking Account created!\nAccount No: " + ca.getAccountNumber(), "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            refreshAccountTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error opening account: " + ex.getMessage(), "Banking Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshCustomerDropdown() {
        cmbCustomer.removeAllItems();
        List<Customer> list = bankService.getAllCustomers();
        for (Customer c : list) {
            cmbCustomer.addItem(c.getCustomerId() + " - " + c.getName());
        }
    }

    public void refreshAccountTable() {
        accountTableModel.setRowCount(0);
        int sortIdx = cmbSortOption.getSelectedIndex();
        List<Account> accounts;
        if (sortIdx == 1) {
            accounts = bankService.getAccountsSortedByBalance(false);
        } else if (sortIdx == 2) {
            accounts = bankService.getAccountsSortedByBalance(true);
        } else {
            accounts = bankService.getAllAccounts();
        }

        for (Account a : accounts) {
            Customer c = bankService.getCustomer(a.getCustomerId());
            String custName = c != null ? c.getName() + " (" + c.getCustomerId() + ")" : a.getCustomerId();
            String features;
            if (a instanceof SavingsAccount) {
                SavingsAccount sa = (SavingsAccount) a;
                features = String.format("Int: %.1f%%, Min: $%.0f", sa.getAnnualInterestRate() * 100, sa.getMinimumBalance());
            } else if (a instanceof CheckingAccount) {
                CheckingAccount ca = (CheckingAccount) a;
                features = String.format("Overdraft: $%.0f, Fee: $%.0f", ca.getOverdraftLimit(), ca.getMonthlyMaintenanceFee());
            } else {
                features = "-";
            }

            accountTableModel.addRow(new Object[]{
                a.getAccountNumber(),
                custName,
                a.getAccountType().getDisplayName(),
                String.format("$%,.2f", a.getBalance()),
                features,
                a.isActive() ? "ACTIVE" : "INACTIVE"
            });
        }
    }
}
