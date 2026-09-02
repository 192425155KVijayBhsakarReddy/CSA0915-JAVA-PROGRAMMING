package com.smartbank.gui.components;

import com.smartbank.gui.util.UITheme;
import com.smartbank.model.Account;
import com.smartbank.model.Transaction;
import com.smartbank.service.BankService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Interface for Deposit, Withdrawal, Transfer operations, and Transaction Ledger.
 */
public class TransactionPanel extends JPanel {
    private final BankService bankService;

    // Transfer fields
    private JComboBox<String> cmbSourceAcc;
    private JComboBox<String> cmbTargetAcc;
    private JTextField txtTransferAmount;
    private JTextField txtTransferNotes;

    // Deposit / Withdraw fields
    private JComboBox<String> cmbSingleAcc;
    private JTextField txtSingleAmount;
    private JTextField txtSingleNotes;

    // Table & Filter
    private DefaultTableModel txnTableModel;
    private JTable txnTable;

    public TransactionPanel(BankService bankService) {
        this.bankService = bankService;
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        initComponents();
        refreshAccountDropdowns();
        refreshTransactionTable();
    }

    private void initComponents() {
        // WEST: Action Forms
        JPanel westPanel = new JPanel();
        westPanel.setLayout(new BoxLayout(westPanel, BoxLayout.Y_AXIS));
        westPanel.setBackground(UITheme.BG_MAIN);
        westPanel.setPreferredSize(new Dimension(380, 0));

        // Form 1: Fund Transfer
        JPanel transferCard = UITheme.createCardPanel("Inter-Account Fund Transfer");
        JPanel transferForm = new JPanel(new GridLayout(5, 2, 8, 8));
        transferForm.setBackground(Color.WHITE);

        transferForm.add(new JLabel("From Account:"));
        transferForm.add(cmbSourceAcc = new JComboBox<>());

        transferForm.add(new JLabel("To Account:"));
        transferForm.add(cmbTargetAcc = new JComboBox<>());

        transferForm.add(new JLabel("Amount ($):"));
        transferForm.add(txtTransferAmount = UITheme.createTextField(10));
        txtTransferAmount.setText("100.00");

        transferForm.add(new JLabel("Description / Notes:"));
        transferForm.add(txtTransferNotes = UITheme.createTextField(10));
        txtTransferNotes.setText("Direct Transfer");

        JButton btnTransfer = UITheme.createPrimaryButton("Execute Transfer");
        transferForm.add(new JLabel(""));
        transferForm.add(btnTransfer);

        btnTransfer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleTransfer();
            }
        });

        transferCard.add(transferForm, BorderLayout.CENTER);
        westPanel.add(transferCard);
        westPanel.add(Box.createVerticalStrut(15));

        // Form 2: Deposit / Withdraw
        JPanel singleCard = UITheme.createCardPanel("Deposit / Withdrawal Desk");
        JPanel singleForm = new JPanel(new GridLayout(4, 2, 8, 8));
        singleForm.setBackground(Color.WHITE);

        singleForm.add(new JLabel("Select Account:"));
        singleForm.add(cmbSingleAcc = new JComboBox<>());

        singleForm.add(new JLabel("Amount ($):"));
        singleForm.add(txtSingleAmount = UITheme.createTextField(10));
        txtSingleAmount.setText("50.00");

        singleForm.add(new JLabel("Notes:"));
        singleForm.add(txtSingleNotes = UITheme.createTextField(10));
        txtSingleNotes.setText("Over-the-counter");

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        btnRow.setBackground(Color.WHITE);
        JButton btnDeposit = UITheme.createSuccessButton("Deposit");
        JButton btnWithdraw = UITheme.createDangerButton("Withdraw");
        btnRow.add(btnDeposit);
        btnRow.add(btnWithdraw);

        singleForm.add(new JLabel(""));
        singleForm.add(btnRow);

        btnDeposit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleDeposit();
            }
        });

        btnWithdraw.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleWithdraw();
            }
        });

        singleCard.add(singleForm, BorderLayout.CENTER);
        westPanel.add(singleCard);

        add(westPanel, BorderLayout.WEST);

        // CENTER: Transactions Ledger Table
        JPanel tableCard = UITheme.createCardPanel("Central Transaction Ledger");

        // Top Toolbar
        JPanel topToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        topToolbar.setBackground(Color.WHITE);

        JButton btnExportCsv = UITheme.createSuccessButton("Export Ledger to CSV");
        topToolbar.add(btnExportCsv);

        btnExportCsv.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleExportCsv();
            }
        });

        tableCard.add(topToolbar, BorderLayout.NORTH);

        String[] cols = {"Txn ID", "Date / Time", "Type", "Source", "Target", "Amount", "Balance After", "Status", "Description"};
        txnTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        txnTable = new JTable(txnTableModel);
        UITheme.styleTable(txnTable);
        JScrollPane scrollPane = new JScrollPane(txnTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);
    }

    private void handleTransfer() {
        String src = (String) cmbSourceAcc.getSelectedItem();
        String dst = (String) cmbTargetAcc.getSelectedItem();
        if (src == null || dst == null) {
            JOptionPane.showMessageDialog(this, "Select both source and destination accounts.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String srcAcc = src.split(" ")[0];
        String dstAcc = dst.split(" ")[0];

        try {
            double amount = Double.parseDouble(txtTransferAmount.getText().trim());
            String notes = txtTransferNotes.getText().trim();
            Transaction txn = bankService.transfer(srcAcc, dstAcc, amount, notes);
            JOptionPane.showMessageDialog(this, "Transfer Successful!\nTransaction ID: " + txn.getTransactionId(), "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshAccountDropdowns();
            refreshTransactionTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Transfer Failed: " + ex.getMessage(), "Transaction Error", JOptionPane.ERROR_MESSAGE);
            refreshTransactionTable();
        }
    }

    private void handleDeposit() {
        String sel = (String) cmbSingleAcc.getSelectedItem();
        if (sel == null) return;
        String accNum = sel.split(" ")[0];
        try {
            double amount = Double.parseDouble(txtSingleAmount.getText().trim());
            bankService.deposit(accNum, amount, txtSingleNotes.getText().trim());
            JOptionPane.showMessageDialog(this, "Deposit Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshAccountDropdowns();
            refreshTransactionTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Deposit Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleWithdraw() {
        String sel = (String) cmbSingleAcc.getSelectedItem();
        if (sel == null) return;
        String accNum = sel.split(" ")[0];
        try {
            double amount = Double.parseDouble(txtSingleAmount.getText().trim());
            bankService.withdraw(accNum, amount, txtSingleNotes.getText().trim());
            JOptionPane.showMessageDialog(this, "Withdrawal Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshAccountDropdowns();
            refreshTransactionTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Withdrawal Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleExportCsv() {
        try {
            File exportDir = new File("data/exports");
            if (!exportDir.exists()) exportDir.mkdirs();
            File file = new File(exportDir, "transactions_" + System.currentTimeMillis() + ".csv");
            bankService.getBackupService().exportTransactionsToCsv(bankService.getAllTransactions(), file);
            JOptionPane.showMessageDialog(this, "Exported successfully to:\n" + file.getAbsolutePath(), "CSV Export", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Export Failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshAccountDropdowns() {
        cmbSourceAcc.removeAllItems();
        cmbTargetAcc.removeAllItems();
        cmbSingleAcc.removeAllItems();

        List<Account> accounts = bankService.getAllAccounts();
        for (Account a : accounts) {
            String item = a.getAccountNumber() + " ($" + String.format("%.2f", a.getBalance()) + ")";
            cmbSourceAcc.addItem(item);
            cmbTargetAcc.addItem(item);
            cmbSingleAcc.addItem(item);
        }
        if (cmbTargetAcc.getItemCount() > 1) {
            cmbTargetAcc.setSelectedIndex(1);
        }
    }

    public void refreshTransactionTable() {
        txnTableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (Transaction t : bankService.getAllTransactions()) {
            txnTableModel.addRow(new Object[]{
                t.getTransactionId(),
                sdf.format(t.getTimestamp()),
                t.getType().name(),
                t.getSourceAccountNumber() != null ? t.getSourceAccountNumber() : "-",
                t.getTargetAccountNumber() != null ? t.getTargetAccountNumber() : "-",
                String.format("$%.2f", t.getAmount()),
                String.format("$%.2f", t.getResultingBalance()),
                t.isSuccessful() ? "SUCCESS" : "FAILED",
                t.getDescription()
            });
        }
    }
}
