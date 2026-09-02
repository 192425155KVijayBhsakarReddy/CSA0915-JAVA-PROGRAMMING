package com.smartbank.gui.components;

import com.smartbank.database.DBManager;
import com.smartbank.gui.util.UITheme;
import com.smartbank.model.Account;
import com.smartbank.model.Customer;
import com.smartbank.model.Employee;
import com.smartbank.model.Loan;
import com.smartbank.model.Transaction;
import com.smartbank.service.BackupService;
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
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.List;

/**
 * Interface for Dual Persistence Management:
 * 1. File Streams & Object Serialization (.dat)
 * 2. Tabular CSV Exports
 * 3. MySQL & SQLite JDBC Database Sync & SQL Query Console
 */
public class DataStoragePanel extends JPanel {
    private final BankService bankService;
    private final LoanService loanService;
    private final EmployeeService employeeService;

    private JTextField txtBackupPath;
    private JTextArea txtQueryInput;
    private DefaultTableModel sqlResultModel;
    private JTable sqlResultTable;
    private JLabel lblDbStatus;

    public DataStoragePanel(BankService bankService, LoanService loanService, EmployeeService employeeService) {
        this.bankService = bankService;
        this.loanService = loanService;
        this.employeeService = employeeService;

        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        initComponents();
        updateDbStatusLabel();
    }

    private void initComponents() {
        // WEST: Backup & Serialization Controls
        JPanel westPanel = new JPanel();
        westPanel.setLayout(new BoxLayout(westPanel, BoxLayout.Y_AXIS));
        westPanel.setBackground(UITheme.BG_MAIN);
        westPanel.setPreferredSize(new Dimension(380, 0));

        // 1. Serialization Snapshot Card
        JPanel backupCard = UITheme.createCardPanel("Binary Object Serialization (.dat)");
        JPanel bkpForm = new JPanel(new GridLayout(4, 1, 6, 6));
        bkpForm.setBackground(Color.WHITE);

        bkpForm.add(new JLabel("Snapshot File:"));
        bkpForm.add(txtBackupPath = UITheme.createTextField(10));
        txtBackupPath.setText("data/backups/bank_state_backup.dat");

        JPanel bkpBtns = new JPanel(new GridLayout(1, 2, 8, 0));
        bkpBtns.setBackground(Color.WHITE);
        JButton btnBackup = UITheme.createPrimaryButton("Create Backup");
        JButton btnRestore = UITheme.createDangerButton("Restore Snapshot");
        bkpBtns.add(btnBackup);
        bkpBtns.add(btnRestore);

        bkpForm.add(bkpBtns);

        btnBackup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleCreateBackup();
            }
        });

        btnRestore.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRestoreSnapshot();
            }
        });

        backupCard.add(bkpForm, BorderLayout.CENTER);
        westPanel.add(backupCard);
        westPanel.add(Box.createVerticalStrut(15));

        // 2. CSV Reports Card
        JPanel csvCard = UITheme.createCardPanel("CSV Statement & Data Exports");
        JPanel csvForm = new JPanel(new GridLayout(2, 1, 8, 8));
        csvForm.setBackground(Color.WHITE);

        JButton btnExportAccounts = UITheme.createSuccessButton("Export Accounts to CSV");
        JButton btnExportTransactions = UITheme.createSuccessButton("Export Transactions to CSV");
        csvForm.add(btnExportAccounts);
        csvForm.add(btnExportTransactions);

        btnExportAccounts.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleExportAccountsCsv();
            }
        });

        btnExportTransactions.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleExportTxnCsv();
            }
        });

        csvCard.add(csvForm, BorderLayout.CENTER);
        westPanel.add(csvCard);

        add(westPanel, BorderLayout.WEST);

        // CENTER: JDBC SQL Database Console
        JPanel dbCard = UITheme.createCardPanel("JDBC Database Manager & SQL Query Engine");

        JPanel topDb = new JPanel(new BorderLayout(5, 5));
        topDb.setBackground(Color.WHITE);

        JPanel statusRow = new JPanel(new BorderLayout());
        statusRow.setBackground(Color.WHITE);

        lblDbStatus = new JLabel("Database Driver: Checking...");
        lblDbStatus.setFont(UITheme.FONT_BODY_BOLD);
        statusRow.add(lblDbStatus, BorderLayout.WEST);

        JButton btnConfigMySql = UITheme.createPrimaryButton("MySQL Settings / Connect");
        btnConfigMySql.setFont(UITheme.FONT_SMALL);
        statusRow.add(btnConfigMySql, BorderLayout.EAST);

        btnConfigMySql.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMySqlConfigDialog();
            }
        });

        topDb.add(statusRow, BorderLayout.NORTH);

        txtQueryInput = new JTextArea(3, 20);
        txtQueryInput.setFont(UITheme.FONT_MONO);
        txtQueryInput.setText("SELECT * FROM accounts;");
        txtQueryInput.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));
        topDb.add(txtQueryInput, BorderLayout.CENTER);

        JPanel queryBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        queryBtns.setBackground(Color.WHITE);
        JButton btnSyncAllToDb = UITheme.createPrimaryButton("Sync Memory -> Database");
        JButton btnExecuteSql = UITheme.createSuccessButton("Execute SQL Query");
        queryBtns.add(btnSyncAllToDb);
        queryBtns.add(btnExecuteSql);
        topDb.add(queryBtns, BorderLayout.SOUTH);

        btnSyncAllToDb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSyncToDb();
            }
        });

        btnExecuteSql.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleExecuteSql();
            }
        });

        dbCard.add(topDb, BorderLayout.NORTH);

        // SQL Results Table
        sqlResultModel = new DefaultTableModel();
        sqlResultTable = new JTable(sqlResultModel);
        UITheme.styleTable(sqlResultTable);
        JScrollPane scrollSql = new JScrollPane(sqlResultTable);
        scrollSql.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));
        dbCard.add(scrollSql, BorderLayout.CENTER);

        add(dbCard, BorderLayout.CENTER);
    }

    private void updateDbStatusLabel() {
        DBManager.DatabaseType type = bankService.getDbManager().getActiveDbType();
        if (type == DBManager.DatabaseType.MYSQL) {
            lblDbStatus.setText("Database: MySQL 8.0 Server (Connected: smartbank_db)");
            lblDbStatus.setForeground(UITheme.SUCCESS);
        } else if (type == DBManager.DatabaseType.SQLITE) {
            lblDbStatus.setText("Database: SQLite Local Engine (data/smartbank.db)");
            lblDbStatus.setForeground(UITheme.INFO);
        } else {
            lblDbStatus.setText("Database: In-Memory / Embedded Mode");
            lblDbStatus.setForeground(UITheme.WARNING);
        }
    }

    private void showMySqlConfigDialog() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 8, 8));
        JTextField txtHost = new JTextField("localhost");
        JTextField txtPort = new JTextField("3306");
        JTextField txtDb = new JTextField("smartbank_db");
        JTextField txtUser = new JTextField("root");
        JPasswordField txtPass = new JPasswordField("");

        panel.add(new JLabel("MySQL Host:"));
        panel.add(txtHost);
        panel.add(new JLabel("Port:"));
        panel.add(txtPort);
        panel.add(new JLabel("Database:"));
        panel.add(txtDb);
        panel.add(new JLabel("User:"));
        panel.add(txtUser);
        panel.add(new JLabel("Password:"));
        panel.add(txtPass);

        int res = JOptionPane.showConfirmDialog(this, panel, "MySQL Server Connection Setup", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            try {
                String host = txtHost.getText().trim();
                int port = Integer.parseInt(txtPort.getText().trim());
                String db = txtDb.getText().trim();
                String user = txtUser.getText().trim();
                String pass = new String(txtPass.getPassword());

                bankService.getDbManager().configureMySql(host, port, db, user, pass);
                updateDbStatusLabel();

                if (bankService.getDbManager().getActiveDbType() == DBManager.DatabaseType.MYSQL) {
                    JOptionPane.showMessageDialog(this, "Successfully connected to MySQL server!\nDatabase: " + db, "MySQL Connected", JOptionPane.INFORMATION_MESSAGE);
                    handleSyncToDb();
                } else {
                    JOptionPane.showMessageDialog(this, "Could not connect to MySQL server. Please verify credentials/service.", "Connection Failed", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "MySQL Error: " + ex.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleCreateBackup() {
        try {
            File file = new File(txtBackupPath.getText().trim());
            BackupService.BankStateSnapshot snapshot = bankService.createSnapshot(
                loanService.getAllLoans(),
                employeeService.getAllEmployees()
            );
            bankService.getBackupService().exportBinarySnapshot(snapshot, file);
            JOptionPane.showMessageDialog(this, "Binary backup created successfully!\nPath: " + file.getAbsolutePath(), "Backup Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Backup Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRestoreSnapshot() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to restore? Current unsaved in-memory state will be replaced.",
            "Confirm Restore", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            File file = new File(txtBackupPath.getText().trim());
            BackupService.BankStateSnapshot snapshot = bankService.getBackupService().importBinarySnapshot(file);
            bankService.restoreSnapshot(snapshot);
            loanService.populateSeedLoans(snapshot.loans);
            employeeService.populateSeedEmployees(snapshot.employees);
            JOptionPane.showMessageDialog(this, "Snapshot restored successfully!\nLoaded " + snapshot.accounts.size() + " accounts, " + snapshot.customers.size() + " customers.", "Restore Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Restore Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleExportAccountsCsv() {
        try {
            File dir = new File("data/exports");
            if (!dir.exists()) dir.mkdirs();
            File f = new File(dir, "accounts_" + System.currentTimeMillis() + ".csv");
            bankService.getBackupService().exportAccountsToCsv(bankService.getAllAccounts(), f);
            JOptionPane.showMessageDialog(this, "Accounts exported to CSV:\n" + f.getAbsolutePath(), "CSV Exported", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "CSV Export Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleExportTxnCsv() {
        try {
            File dir = new File("data/exports");
            if (!dir.exists()) dir.mkdirs();
            File f = new File(dir, "transactions_" + System.currentTimeMillis() + ".csv");
            bankService.getBackupService().exportTransactionsToCsv(bankService.getAllTransactions(), f);
            JOptionPane.showMessageDialog(this, "Transactions exported to CSV:\n" + f.getAbsolutePath(), "CSV Exported", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "CSV Export Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSyncToDb() {
        try {
            DBManager db = bankService.getDbManager();
            for (Customer c : bankService.getAllCustomers()) db.saveCustomer(c);
            for (Account a : bankService.getAllAccounts()) db.saveAccount(a);
            for (Transaction t : bankService.getAllTransactions()) db.saveTransaction(t);
            for (Loan l : loanService.getAllLoans()) db.saveLoan(l);
            for (Employee e : employeeService.getAllEmployees()) db.saveEmployee(e);
            JOptionPane.showMessageDialog(this, "Synchronized all in-memory entities to " + db.getActiveDbType().getLabel() + "!", "DB Sync", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "DB Sync Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleExecuteSql() {
        String sql = txtQueryInput.getText().trim();
        if (sql.isEmpty()) return;

        try (Connection conn = bankService.getDbManager().getConnection();
             Statement stmt = conn.createStatement()) {

            if (sql.toLowerCase().startsWith("select") || sql.toLowerCase().startsWith("show") || sql.toLowerCase().startsWith("describe")) {
                ResultSet rs = stmt.executeQuery(sql);
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();

                sqlResultModel.setRowCount(0);
                sqlResultModel.setColumnCount(0);

                for (int i = 1; i <= colCount; i++) {
                    sqlResultModel.addColumn(meta.getColumnLabel(i));
                }

                while (rs.next()) {
                    Object[] row = new Object[colCount];
                    for (int i = 1; i <= colCount; i++) {
                        row[i - 1] = rs.getObject(i);
                    }
                    sqlResultModel.addRow(row);
                }
            } else {
                int affected = stmt.executeUpdate(sql);
                JOptionPane.showMessageDialog(this, "Statement executed successfully. Rows affected: " + affected, "SQL Executed", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "SQL Execution Error: " + ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
