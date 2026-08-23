import java.awt.*;
import java.awt.event.*;

public class Q2_FundTransfer extends Frame implements ActionListener {

    Label title, accountLabel, beneficiaryLabel, holderLabel;
    Label amountLabel, typeLabel, statusLabel;

    TextField accountField, beneficiaryField;
    TextField holderField, amountField;

    Choice transactionType;

    Button transferButton, clearButton, exitButton;

    TextArea resultArea;

    Q2_FundTransfer() {

        setTitle("Online Fund Transfer");
        setSize(700, 600);
        setLayout(new BorderLayout(10, 10));

        title = new Label("ONLINE FUND TRANSFER", Label.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        Panel formPanel = new Panel();
        formPanel.setLayout(new GridLayout(6, 2, 10, 10));

        accountLabel = new Label("Account Number:");
        beneficiaryLabel = new Label("Beneficiary Account:");
        holderLabel = new Label("Account Holder Name:");
        amountLabel = new Label("Transfer Amount:");
        typeLabel = new Label("Transaction Type:");

        accountField = new TextField();
        beneficiaryField = new TextField();
        holderField = new TextField();
        amountField = new TextField();

        transactionType = new Choice();
        transactionType.add("Select Type");
        transactionType.add("NEFT");
        transactionType.add("RTGS");
        transactionType.add("IMPS");

        formPanel.add(accountLabel);
        formPanel.add(accountField);

        formPanel.add(beneficiaryLabel);
        formPanel.add(beneficiaryField);

        formPanel.add(holderLabel);
        formPanel.add(holderField);

        formPanel.add(amountLabel);
        formPanel.add(amountField);

        formPanel.add(typeLabel);
        formPanel.add(transactionType);

        Panel buttonPanel =
            new Panel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        transferButton = new Button("Transfer");
        clearButton = new Button("Clear");
        exitButton = new Button("Exit");

        buttonPanel.add(transferButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(exitButton);

        formPanel.add(new Label(""));
        formPanel.add(buttonPanel);

        add(formPanel, BorderLayout.CENTER);

        Panel bottomPanel =
            new Panel(new BorderLayout(5, 5));

        statusLabel =
            new Label("Transaction Summary", Label.CENTER);

        statusLabel.setFont(
            new Font("Arial", Font.BOLD, 16)
        );

        resultArea = new TextArea(10, 50);
        resultArea.setEditable(false);

        bottomPanel.add(statusLabel, BorderLayout.NORTH);
        bottomPanel.add(resultArea, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);

        transferButton.addActionListener(this);
        clearButton.addActionListener(this);
        exitButton.addActionListener(this);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == transferButton) {
            transferMoney();
        }

        if (e.getSource() == clearButton) {
            clearFields();
        }

        if (e.getSource() == exitButton) {
            System.exit(0);
        }
    }

    void transferMoney() {

        String account =
            accountField.getText().trim();

        String beneficiary =
            beneficiaryField.getText().trim();

        String holder =
            holderField.getText().trim();

        String amountText =
            amountField.getText().trim();

        if (account.isEmpty()) {
            resultArea.setText(
                "Account Number Required"
            );
            return;
        }

        if (beneficiary.isEmpty()) {
            resultArea.setText(
                "Beneficiary Account Required"
            );
            return;
        }

        if (holder.isEmpty()) {
            resultArea.setText(
                "Account Holder Name Required"
            );
            return;
        }

        if (amountText.isEmpty()) {
            resultArea.setText(
                "Transfer Amount Required"
            );
            return;
        }

        double amount;

        try {

            amount = Double.parseDouble(amountText);

            if (amount <= 0) {
                resultArea.setText(
                    "Invalid Transfer Amount"
                );
                return;
            }

        } catch (NumberFormatException ex) {

            resultArea.setText(
                "Invalid Transfer Amount"
            );
            return;
        }

        if (transactionType.getSelectedIndex() == 0) {
            resultArea.setText(
                "Transaction Type Required"
            );
            return;
        }

        String type =
            transactionType.getSelectedItem();

        resultArea.setText(
            "TRANSFER SUCCESSFUL\n\n" +
            "Account Number       : " + account + "\n" +
            "Beneficiary Account  : " + beneficiary + "\n" +
            "Account Holder Name  : " + holder + "\n" +
            "Transfer Amount      : ₹" + amount + "\n" +
            "Transaction Type     : " + type + "\n\n" +
            "Transaction Status   : Successful"
        );
    }

    void clearFields() {

        accountField.setText("");
        beneficiaryField.setText("");
        holderField.setText("");
        amountField.setText("");

        transactionType.select(0);

        resultArea.setText("");
    }

    public static void main(String[] args) {
        new Q2_FundTransfer();
    }
}