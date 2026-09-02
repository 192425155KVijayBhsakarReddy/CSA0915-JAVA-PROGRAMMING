package com.smartbank.gui.components;

import com.smartbank.gui.util.UITheme;
import com.smartbank.model.Account;
import com.smartbank.model.Transaction;
import com.smartbank.service.BankService;
import com.smartbank.service.EmployeeService;
import com.smartbank.service.LoanService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Overview Dashboard with metrics, summary cards, and latest transactions.
 */
public class DashboardPanel extends JPanel {
    private final BankService bankService;
    private final LoanService loanService;
    private final EmployeeService employeeService;

    private JLabel lblTotalAssets;
    private JLabel lblTotalAccounts;
    private JLabel lblTotalCustomers;
    private JLabel lblActiveLoans;
    private DefaultTableModel recentTxnModel;

    public DashboardPanel(BankService bankService, LoanService loanService, EmployeeService employeeService) {
        this.bankService = bankService;
        this.loanService = loanService;
        this.employeeService = employeeService;

        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initComponents();
        refreshMetrics();
    }

    private void initComponents() {
        // TOP: Metrics Cards Panel (GridLayout)
        JPanel metricsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        metricsPanel.setBackground(UITheme.BG_MAIN);

        metricsPanel.add(createMetricCard("TOTAL ASSETS", "$0.00", UITheme.PRIMARY, lblTotalAssets = new JLabel("$0.00")));
        metricsPanel.add(createMetricCard("TOTAL ACCOUNTS", "0", UITheme.ACCENT, lblTotalAccounts = new JLabel("0")));
        metricsPanel.add(createMetricCard("REGISTERED CLIENTS", "0", UITheme.INFO, lblTotalCustomers = new JLabel("0")));
        metricsPanel.add(createMetricCard("LOAN PORTFOLIO", "0", UITheme.WARNING, lblActiveLoans = new JLabel("0")));

        add(metricsPanel, BorderLayout.NORTH);

        // CENTER: Recent Activity & System Health
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        centerPanel.setBackground(UITheme.BG_MAIN);

        // Card 1: Recent Transactions Table
        JPanel txnCard = UITheme.createCardPanel("Recent Transactions");
        String[] cols = {"ID", "Type", "Source", "Target", "Amount", "Status"};
        recentTxnModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable txnTable = new JTable(recentTxnModel);
        UITheme.styleTable(txnTable);
        JScrollPane scrollTxn = new JScrollPane(txnTable);
        scrollTxn.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));
        txnCard.add(scrollTxn, BorderLayout.CENTER);
        centerPanel.add(txnCard);

        // Card 2: System Features & Status
        JPanel sysCard = UITheme.createCardPanel("Enterprise System Status");
        JPanel infoList = new JPanel();
        infoList.setLayout(new BoxLayout(infoList, BoxLayout.Y_AXIS));
        infoList.setBackground(Color.WHITE);

        infoList.add(createInfoRow("Concurrency Engine:", "Active (ReentrantLock + Deadlock Prevention)"));
        infoList.add(Box.createVerticalStrut(10));
        infoList.add(createInfoRow("Audit Logging:", "Background Producer-Consumer (Daemon Worker)"));
        infoList.add(Box.createVerticalStrut(10));
        infoList.add(createInfoRow("Database Engine:", "JDBC SQLite WAL (Dual-Engine Synced)"));
        infoList.add(Box.createVerticalStrut(10));
        infoList.add(createInfoRow("Persistence Backup:", "Binary Serialization Streams (.dat)"));
        infoList.add(Box.createVerticalStrut(10));
        infoList.add(createInfoRow("Active Employees:", String.valueOf(employeeService.getAllEmployees().size())));

        sysCard.add(infoList, BorderLayout.CENTER);
        centerPanel.add(sysCard);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createMetricCard(String title, String initialValue, Color accentColor, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(0, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER_COLOR, 1),
            new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_SMALL);
        lblTitle.setForeground(UITheme.TEXT_MUTED);

        valueLabel.setText(initialValue);
        valueLabel.setFont(UITheme.FONT_TITLE);
        valueLabel.setForeground(accentColor);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createInfoRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BODY_BOLD);
        lbl.setForeground(UITheme.TEXT_PRIMARY);

        JLabel val = new JLabel(value);
        val.setFont(UITheme.FONT_BODY);
        val.setForeground(UITheme.PRIMARY);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    public void refreshMetrics() {
        lblTotalAssets.setText(String.format("$%,.2f", bankService.getTotalBankAssets()));
        lblTotalAccounts.setText(String.valueOf(bankService.getAllAccounts().size()));
        lblTotalCustomers.setText(String.valueOf(bankService.getAllCustomers().size()));
        lblActiveLoans.setText(String.valueOf(loanService.getAllLoans().size()));

        recentTxnModel.setRowCount(0);
        List<Transaction> txns = bankService.getAllTransactions();
        int count = Math.min(10, txns.size());
        for (int i = 0; i < count; i++) {
            Transaction t = txns.get(i);
            recentTxnModel.addRow(new Object[]{
                t.getTransactionId(),
                t.getType().name(),
                t.getSourceAccountNumber() != null ? t.getSourceAccountNumber() : "-",
                t.getTargetAccountNumber() != null ? t.getTargetAccountNumber() : "-",
                String.format("$%.2f", t.getAmount()),
                t.isSuccessful() ? "SUCCESS" : "FAILED"
            });
        }
    }
}
