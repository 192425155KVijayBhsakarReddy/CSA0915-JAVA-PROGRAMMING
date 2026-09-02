package com.smartbank.gui.util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * UI Theme and styling constants for modern, professional AWT/Swing look and feel.
 */
public class UITheme {
    // Primary Colors
    public static final Color PRIMARY = new Color(26, 82, 118);       // #1A5276 Dark Blue
    public static final Color PRIMARY_DARK = new Color(17, 54, 78);   // #11364E
    public static final Color PRIMARY_LIGHT = new Color(41, 128, 185); // #2980B9
    public static final Color ACCENT = new Color(22, 160, 133);        // #16A085 Teal Accent
    public static final Color ACCENT_HOVER = new Color(19, 141, 117);

    // Functional Colors
    public static final Color SUCCESS = new Color(39, 174, 96);        // #27AE60
    public static final Color DANGER = new Color(192, 57, 43);         // #C0392B
    public static final Color WARNING = new Color(211, 84, 0);         // #D35400
    public static final Color INFO = new Color(52, 152, 219);          // #3498DB

    // Background & Surface
    public static final Color BG_MAIN = new Color(245, 247, 250);     // Light Gray/Blue
    public static final Color CARD_BG = Color.WHITE;
    public static final Color BORDER_COLOR = new Color(220, 224, 230);
    public static final Color TEXT_PRIMARY = new Color(44, 62, 80);
    public static final Color TEXT_MUTED = new Color(127, 140, 141);

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(new LineBorder(PRIMARY_DARK, 1), new EmptyBorder(8, 16, 8, 16)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createSuccessButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(SUCCESS);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(new LineBorder(new Color(30, 132, 73), 1), new EmptyBorder(8, 16, 8, 16)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(DANGER);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(new LineBorder(new Color(146, 43, 33), 1), new EmptyBorder(8, 16, 8, 16)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JPanel createCardPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(15, 15, 15, 15)
        ));

        if (title != null && !title.isEmpty()) {
            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(FONT_HEADER);
            lblTitle.setForeground(PRIMARY);
            panel.add(lblTitle, BorderLayout.NORTH);
        }

        return panel;
    }

    public static JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(FONT_BODY);
        tf.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(6, 8, 6, 8)
        ));
        return tf;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(28);
        table.setGridColor(BORDER_COLOR);
        table.setShowGrid(true);
        table.setSelectionBackground(new Color(235, 245, 251));
        table.setSelectionForeground(TEXT_PRIMARY);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BODY_BOLD);
        header.setBackground(PRIMARY);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 32));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
    }
}
