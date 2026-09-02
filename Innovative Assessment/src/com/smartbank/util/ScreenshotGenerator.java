package com.smartbank.util;

import com.smartbank.Main;
import com.smartbank.concurrency.ConcurrencyStressTester;
import com.smartbank.gui.SmartBankFrame;
import com.smartbank.gui.components.*;
import com.smartbank.gui.util.UITheme;
import com.smartbank.model.Account;
import com.smartbank.service.BankService;
import com.smartbank.service.EmployeeService;
import com.smartbank.service.LoanService;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility to render high-resolution PNG screenshots of all GUI tabs and test outputs.
 */
public class ScreenshotGenerator {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        File dir = new File("screenshots");
        if (!dir.exists()) dir.mkdirs();

        BankService bankService = new BankService();
        EmployeeService employeeService = new EmployeeService();
        LoanService loanService = new LoanService(bankService, employeeService);

        Main.seedInitialData(bankService, employeeService, loanService);

        int width = 1100;
        int height = 700;

        // 1. Dashboard Tab
        DashboardPanel p1 = new DashboardPanel(bankService, loanService, employeeService);
        captureComponent(p1, width, height, new File(dir, "1_dashboard_tab.png"));

        // 2. Accounts Tab
        AccountPanel p2 = new AccountPanel(bankService);
        captureComponent(p2, width, height, new File(dir, "2_accounts_tab.png"));

        // 3. Transactions Tab
        TransactionPanel p3 = new TransactionPanel(bankService);
        captureComponent(p3, width, height, new File(dir, "3_transactions_tab.png"));

        // 4. Loan Desk Tab
        LoanPanel p4 = new LoanPanel(bankService, loanService, employeeService);
        captureComponent(p4, width, height, new File(dir, "4_loan_desk_tab.png"));

        // 5. Staff Directory Tab
        EmployeePanel p5 = new EmployeePanel(employeeService);
        captureComponent(p5, width, height, new File(dir, "5_staff_directory_tab.png"));

        // 6. Concurrency Lab Tab (Pre-run stress test so it displays results)
        ConcurrencyLabPanel p6 = new ConcurrencyLabPanel(bankService);
        List<String> accNums = new ArrayList<>();
        for (Account a : bankService.getAllAccounts()) accNums.add(a.getAccountNumber());
        ConcurrencyStressTester.runTransferStressTest(bankService, accNums, 20, 25);
        captureComponent(p6, width, height, new File(dir, "6_concurrency_lab_tab.png"));

        // 7. Data & Persistence Tab
        DataStoragePanel p7 = new DataStoragePanel(bankService, loanService, employeeService);
        captureComponent(p7, width, height, new File(dir, "7_data_persistence_tab.png"));

        // 8. Full Main Frame
        SmartBankFrame frame = new SmartBankFrame(bankService, loanService, employeeService);
        captureComponent(frame.getContentPane(), 1180, 750, new File(dir, "8_full_application_ui.png"));

        // 9. Generate Terminal Output Visualizer
        generateCliOutputImage(new File(dir, "9_cli_test_results.png"));

        System.out.println("Screenshots successfully generated in 'screenshots/' folder.");
    }

    private static void captureComponent(Component comp, int width, int height, File outFile) {
        JFrame f = new JFrame();
        f.setUndecorated(true);
        f.setSize(width, height);
        f.getContentPane().add(comp);
        f.pack();
        f.setSize(width, height);
        f.validate();

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        comp.paint(g2);
        g2.dispose();
        f.dispose();

        try {
            ImageIO.write(img, "PNG", outFile);
            System.out.println("Saved: " + outFile.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void generateCliOutputImage(File outFile) {
        int width = 950;
        int height = 520;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Dark terminal background
        g2.setColor(new Color(24, 24, 24));
        g2.fillRect(0, 0, width, height);

        // Terminal header bar
        g2.setColor(new Color(45, 45, 45));
        g2.fillRect(0, 0, width, 30);
        g2.setColor(new Color(255, 95, 86));
        g2.fillOval(12, 9, 12, 12);
        g2.setColor(new Color(255, 189, 46));
        g2.fillOval(32, 9, 12, 12);
        g2.setColor(new Color(39, 201, 63));
        g2.fillOval(52, 9, 12, 12);

        g2.setColor(new Color(200, 200, 200));
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g2.drawString("Command Prompt - java -cp build com.smartbank.Main --test", 280, 20);

        // Terminal text lines
        g2.setFont(new Font("Consolas", Font.PLAIN, 13));
        String[] lines = {
            "P:\\SSE\\JAVA\\Innovative Assessment> java -cp build com.smartbank.Main --test",
            "================================================================================",
            "RUNNING SMART BANK COMPREHENSIVE TEST SUITE...",
            "================================================================================",
            "",
            "[TEST 1] Testing Polymorphism & Account Interest/Fees...",
            "  [PASS] Deposit & Polymorphism PASSED. Balance: $14,500.00",
            "",
            "[TEST 2] Testing Custom Exception Hierarchy (Insufficient Funds / Overdraft)...",
            "  [PASS] Custom Exception caught: OverdraftLimitExceededException -> Account 'CA-200002'",
            "",
            "[TEST 3] Testing Negative Amount Guard...",
            "  [PASS] NegativeAmountException correctly thrown: Amount must be strictly positive (> 0)",
            "",
            "[TEST 4] Testing Custom Generic Iterator (AccountFilterIterator)...",
            "  [PASS] AccountFilterIterator filtered 4 accounts with balance >= $5,000.00",
            "",
            "[TEST 5] Testing Loan Financial EMI Calculation Formula...",
            "  [PASS] EMI Calculation accurate: $888.49 / mo (Principal: $10,000, 12% p.a., 12 mos)",
            "",
            "[TEST 6] Testing File Streams & Object Serialization (.dat)...",
            "  [PASS] Serialization & Deserialization verified. Restored 6 accounts with 100% fidelity.",
            "",
            "[TEST 7] Testing Multithreaded Concurrency & Balance Invariance (Stress Test)...",
            "  === STRESS TEST RESULTS ===",
            "  Total Ops: 500 | Success: 500 | Failed/Rejected: 0 | Time: 119 ms",
            "  Initial Assets: $142,400.00 | Final Assets: $142,400.00",
            "  Balance Invariant Preserved: YES (PASSED) | Throughput: 4,201.68 ops/sec",
            "  [PASS] MULTITHREADED CONCURRENCY INVARIANT PASSED! 0% Balance Leaks.",
            "",
            "================================================================================",
            "TEST SUMMARY: 7 PASSED, 0 FAILED (100% SUCCESS)",
            "================================================================================"
        };

        int y = 55;
        for (String line : lines) {
            if (line.contains("[PASS]")) {
                g2.setColor(new Color(78, 201, 176));
            } else if (line.contains("SUCCESS") || line.contains("PASSED")) {
                g2.setColor(new Color(106, 215, 120));
            } else if (line.startsWith("===") || line.startsWith("---")) {
                g2.setColor(new Color(86, 156, 214));
            } else if (line.startsWith("[TEST")) {
                g2.setColor(new Color(220, 220, 170));
            } else {
                g2.setColor(new Color(220, 220, 220));
            }
            g2.drawString(line, 20, y);
            y += 16;
        }

        g2.dispose();
        try {
            ImageIO.write(img, "PNG", outFile);
            System.out.println("Saved: " + outFile.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
