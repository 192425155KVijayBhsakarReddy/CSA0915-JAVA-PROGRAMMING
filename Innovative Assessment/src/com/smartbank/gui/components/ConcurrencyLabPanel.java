package com.smartbank.gui.components;

import com.smartbank.concurrency.ConcurrencyStressTester;
import com.smartbank.gui.util.UITheme;
import com.smartbank.model.Account;
import com.smartbank.service.BankService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Interactive multithreading & concurrency stress lab demonstrating
 * deadlock-free synchronization, thread priorities, and balance invariance.
 */
public class ConcurrencyLabPanel extends JPanel {
    private final BankService bankService;

    private JSpinner spinThreads;
    private JSpinner spinOpsPerThread;
    private JButton btnRunStressTest;
    private JProgressBar progressBar;

    private JLabel lblTotalOps;
    private JLabel lblSuccessOps;
    private JLabel lblFailedOps;
    private JLabel lblExecutionTime;
    private JLabel lblInitialBalance;
    private JLabel lblFinalBalance;
    private JLabel lblInvariantStatus;

    private JTextArea txtLogConsole;

    public ConcurrencyLabPanel(BankService bankService) {
        this.bankService = bankService;

        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        initComponents();
    }

    private void initComponents() {
        // WEST: Configuration & Control Panel
        JPanel westPanel = new JPanel();
        westPanel.setLayout(new BoxLayout(westPanel, BoxLayout.Y_AXIS));
        westPanel.setBackground(UITheme.BG_MAIN);
        westPanel.setPreferredSize(new Dimension(380, 0));

        JPanel configCard = UITheme.createCardPanel("Concurrency Stress Settings");
        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
        form.setBackground(Color.WHITE);

        form.add(new JLabel("Concurrent Worker Threads:"));
        form.add(spinThreads = new JSpinner(new SpinnerNumberModel(20, 2, 100, 2)));

        form.add(new JLabel("Operations per Thread:"));
        form.add(spinOpsPerThread = new JSpinner(new SpinnerNumberModel(50, 5, 500, 10)));

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");

        form.add(new JLabel("Test Status:"));
        form.add(progressBar);

        btnRunStressTest = UITheme.createPrimaryButton("Launch Concurrency Test");
        form.add(new JLabel(""));
        form.add(btnRunStressTest);

        btnRunStressTest.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runStressTestAsync();
            }
        });

        configCard.add(form, BorderLayout.CENTER);
        westPanel.add(configCard);
        westPanel.add(Box.createVerticalStrut(15));

        // Invariant Verification Card
        JPanel invariantCard = UITheme.createCardPanel("Thread-Safe Invariant Check");
        JPanel invList = new JPanel(new GridLayout(6, 1, 4, 4));
        invList.setBackground(Color.WHITE);

        invList.add(lblInitialBalance = new JLabel("Initial Total Assets: $0.00"));
        lblInitialBalance.setFont(UITheme.FONT_BODY_BOLD);

        invList.add(lblFinalBalance = new JLabel("Final Total Assets: $0.00"));
        lblFinalBalance.setFont(UITheme.FONT_BODY_BOLD);

        invList.add(lblInvariantStatus = new JLabel("Status: AWAITING TEST"));
        lblInvariantStatus.setFont(UITheme.FONT_BODY_BOLD);
        lblInvariantStatus.setForeground(UITheme.TEXT_MUTED);

        invList.add(lblTotalOps = new JLabel("Total Transfers Attempted: 0"));
        invList.add(lblSuccessOps = new JLabel("Successful Transfers: 0"));
        invList.add(lblExecutionTime = new JLabel("Execution Time: 0 ms"));

        invariantCard.add(invList, BorderLayout.CENTER);
        westPanel.add(invariantCard);

        add(westPanel, BorderLayout.WEST);

        // CENTER: Live Log Console
        JPanel consoleCard = UITheme.createCardPanel("Live Concurrency & Lock Monitoring Console");

        txtLogConsole = new JTextArea();
        txtLogConsole.setFont(UITheme.FONT_MONO);
        txtLogConsole.setEditable(false);
        txtLogConsole.setBackground(new Color(30, 30, 30));
        txtLogConsole.setForeground(new Color(0, 255, 128));
        txtLogConsole.setText("--- SMART BANK CONCURRENCY MONITOR READY ---\n" +
                              "Deterministic Lock Acquisition Algorithm: ENABLED (Deadlock-Free)\n" +
                              "ReentrantLock Fair Mode: ENABLED\n" +
                              "Producer-Consumer Logger: RUNNING\n");

        JScrollPane scrollConsole = new JScrollPane(txtLogConsole);
        scrollConsole.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));
        consoleCard.add(scrollConsole, BorderLayout.CENTER);

        add(consoleCard, BorderLayout.CENTER);
    }

    private void runStressTestAsync() {
        final int threads = (Integer) spinThreads.getValue();
        final int ops = (Integer) spinOpsPerThread.getValue();

        List<Account> accounts = bankService.getAllAccounts();
        if (accounts.size() < 2) {
            JOptionPane.showMessageDialog(this, "Need at least 2 accounts to run transfer stress test.", "Prerequisite Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        final List<String> accNums = new ArrayList<>();
        for (Account a : accounts) {
            accNums.add(a.getAccountNumber());
        }

        btnRunStressTest.setEnabled(false);
        progressBar.setIndeterminate(true);
        progressBar.setString("Running " + (threads * ops) + " Concurrent Ops...");
        txtLogConsole.append(String.format("\n[START] Launching %d parallel threads (%d transfers/thread)...\n", threads, ops));

        // Run in background swing worker to keep GUI responsive
        SwingWorker<ConcurrencyStressTester.StressTestResult, Void> worker = new SwingWorker<ConcurrencyStressTester.StressTestResult, Void>() {
            @Override
            protected ConcurrencyStressTester.StressTestResult doInBackground() throws Exception {
                return ConcurrencyStressTester.runTransferStressTest(bankService, accNums, threads, ops);
            }

            @Override
            protected void done() {
                try {
                    ConcurrencyStressTester.StressTestResult res = get();
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(100);
                    progressBar.setString("Completed in " + res.durationMs + " ms");
                    btnRunStressTest.setEnabled(true);

                    lblTotalOps.setText("Total Transfers Attempted: " + res.totalOperations);
                    lblSuccessOps.setText("Successful Transfers: " + res.successfulTransfers + " | Rejected: " + res.failedOrRejected);
                    lblExecutionTime.setText(String.format("Execution Time: %d ms (%.1f ops/sec)",
                        res.durationMs, (res.durationMs > 0 ? (res.totalOperations * 1000.0 / res.durationMs) : 0)));

                    lblInitialBalance.setText(String.format("Initial Total Assets: $%,.2f", res.initialTotalAssets));
                    lblFinalBalance.setText(String.format("Final Total Assets: $%,.2f", res.finalTotalAssets));

                    if (res.invariantPreserved) {
                        lblInvariantStatus.setText("Status: PASSED (100% Invariant Preserved)");
                        lblInvariantStatus.setForeground(UITheme.SUCCESS);
                    } else {
                        lblInvariantStatus.setText("Status: FAILED (Asset Discrepancy Detected)");
                        lblInvariantStatus.setForeground(UITheme.DANGER);
                    }

                    txtLogConsole.append("\n" + res.toString());
                    if (res.invariantPreserved) {
                        txtLogConsole.append("[SUCCESS] Invariant Verified: Initial Bank Assets == Final Bank Assets. Zero race condition leaks!\n");
                    } else {
                        txtLogConsole.append("[ERROR] Invariant Violated! Concurrency flaw detected.\n");
                    }
                } catch (Exception ex) {
                    progressBar.setString("Failed");
                    btnRunStressTest.setEnabled(true);
                    txtLogConsole.append("\n[ERROR] Stress Test Execution Exception: " + ex.getMessage() + "\n");
                }
            }
        };

        worker.execute();
    }
}
