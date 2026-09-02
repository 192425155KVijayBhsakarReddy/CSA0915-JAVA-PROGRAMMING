package com.smartbank.gui;

import com.smartbank.gui.components.AccountPanel;
import com.smartbank.gui.components.ConcurrencyLabPanel;
import com.smartbank.gui.components.DashboardPanel;
import com.smartbank.gui.components.DataStoragePanel;
import com.smartbank.gui.components.EmployeePanel;
import com.smartbank.gui.components.LoanPanel;
import com.smartbank.gui.components.TransactionPanel;
import com.smartbank.gui.util.UITheme;
import com.smartbank.service.BankService;
import com.smartbank.service.EmployeeService;
import com.smartbank.service.LoanService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Main application window for the Smart Bank Management System.
 * Combines AWT/Swing layout managers (BorderLayout, GridBagLayout, CardLayout/Tabs)
 * and event listeners.
 */
public class SmartBankFrame extends JFrame {
    private final BankService bankService;
    private final LoanService loanService;
    private final EmployeeService employeeService;

    private JTabbedPane tabbedPane;
    private DashboardPanel dashboardPanel;
    private AccountPanel accountPanel;
    private TransactionPanel transactionPanel;
    private LoanPanel loanPanel;
    private EmployeePanel employeePanel;
    private ConcurrencyLabPanel concurrencyLabPanel;
    private DataStoragePanel dataStoragePanel;

    private JLabel lblStatusAssets;
    private JLabel lblStatusClock;

    public SmartBankFrame(BankService bankService, LoanService loanService, EmployeeService employeeService) {
        this.bankService = bankService;
        this.loanService = loanService;
        this.employeeService = employeeService;

        setTitle("Smart Bank Enterprise Management System v2.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 780);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);

        initUI();

        // Graceful shutdown hook
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                bankService.shutdown();
            }
        });

        // Start live clock timer
        Timer timer = new Timer(1000, e -> updateStatusBar());
        timer.start();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // 1. TOP HEADER BAR
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.PRIMARY);
        headerPanel.setBorder(new EmptyBorder(12, 20, 12, 20));

        JPanel titleBlock = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleBlock.setBackground(UITheme.PRIMARY);

        JLabel lblLogo = new JLabel("🏦");
        lblLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));

        JLabel lblTitle = new JLabel("SMART BANK ENTERPRISE");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(Color.WHITE);

        titleBlock.add(lblLogo);
        titleBlock.add(lblTitle);

        headerPanel.add(titleBlock, BorderLayout.WEST);

        JLabel lblSubtitle = new JLabel("Multi-Threaded • Deadlock-Free • Dual-Persistence Engine");
        lblSubtitle.setFont(UITheme.FONT_BODY);
        lblSubtitle.setForeground(new Color(214, 234, 248));
        headerPanel.add(lblSubtitle, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // 2. CENTER TABBED PANE
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_HEADER);
        tabbedPane.setBackground(Color.WHITE);

        dashboardPanel = new DashboardPanel(bankService, loanService, employeeService);
        accountPanel = new AccountPanel(bankService);
        transactionPanel = new TransactionPanel(bankService);
        loanPanel = new LoanPanel(bankService, loanService, employeeService);
        employeePanel = new EmployeePanel(employeeService);
        concurrencyLabPanel = new ConcurrencyLabPanel(bankService);
        dataStoragePanel = new DataStoragePanel(bankService, loanService, employeeService);

        tabbedPane.addTab("  📊 Dashboard  ", dashboardPanel);
        tabbedPane.addTab("  💳 Accounts & Clients  ", accountPanel);
        tabbedPane.addTab("  💸 Transactions  ", transactionPanel);
        tabbedPane.addTab("  📑 Loan Desk  ", loanPanel);
        tabbedPane.addTab("  👥 Staff Directory  ", employeePanel);
        tabbedPane.addTab("  ⚡ Concurrency Lab  ", concurrencyLabPanel);
        tabbedPane.addTab("  💾 Data & Persistence  ", dataStoragePanel);

        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                refreshActiveTab(tabbedPane.getSelectedIndex());
            }
        });

        add(tabbedPane, BorderLayout.CENTER);

        // 3. BOTTOM STATUS BAR
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(235, 240, 245));
        statusPanel.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER_COLOR, 1),
            new EmptyBorder(6, 15, 6, 15)
        ));

        lblStatusAssets = new JLabel("Total Bank Liquidity: $0.00");
        lblStatusAssets.setFont(UITheme.FONT_BODY_BOLD);
        lblStatusAssets.setForeground(UITheme.PRIMARY);
        statusPanel.add(lblStatusAssets, BorderLayout.WEST);

        lblStatusClock = new JLabel();
        lblStatusClock.setFont(UITheme.FONT_BODY);
        lblStatusClock.setForeground(UITheme.TEXT_MUTED);
        statusPanel.add(lblStatusClock, BorderLayout.EAST);

        add(statusPanel, BorderLayout.SOUTH);

        updateStatusBar();
    }

    private void refreshActiveTab(int index) {
        updateStatusBar();
        switch (index) {
            case 0:
                dashboardPanel.refreshMetrics();
                break;
            case 1:
                accountPanel.refreshCustomerDropdown();
                accountPanel.refreshAccountTable();
                break;
            case 2:
                transactionPanel.refreshAccountDropdowns();
                transactionPanel.refreshTransactionTable();
                break;
            case 3:
                loanPanel.refreshDropdowns();
                loanPanel.refreshLoanTable();
                break;
            case 4:
                employeePanel.refreshEmployeeTable();
                break;
            case 6:
                // Storage panel
                break;
        }
    }

    private void updateStatusBar() {
        lblStatusAssets.setText(String.format("Total Bank Liquidity: $%,.2f  |  Accounts: %d  |  Threads: %d",
            bankService.getTotalBankAssets(),
            bankService.getAllAccounts().size(),
            Thread.activeCount()
        ));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        lblStatusClock.setText("System Time: " + sdf.format(new Date()));
    }
}
