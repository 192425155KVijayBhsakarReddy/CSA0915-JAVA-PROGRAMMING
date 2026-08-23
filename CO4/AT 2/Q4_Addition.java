import java.awt.*;
import java.awt.event.*;

public class Q4_Addition extends Frame implements ActionListener {

    TextField number1, number2, result;
    Button add, clear;

    Q4_Addition() {

        setTitle("Addition of Two Numbers");
        setSize(500, 350);
        setLayout(new BorderLayout(10, 10));

        Label title = new Label(
            "ADDITION OF TWO NUMBERS",
            Label.CENTER
        );
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        Panel form = new Panel();
        form.setLayout(new GridLayout(3, 2, 10, 10));

        form.add(new Label("Number 1:"));
        number1 = new TextField();
        form.add(number1);

        form.add(new Label("Number 2:"));
        number2 = new TextField();
        form.add(number2);

        form.add(new Label("Sum:"));
        result = new TextField();
        result.setEditable(false);
        form.add(result);

        add(form, BorderLayout.CENTER);

        Panel buttonPanel = new Panel(
            new FlowLayout(FlowLayout.CENTER, 20, 10)
        );

        add = new Button("ADD");
        clear = new Button("CLEAR");

        buttonPanel.add(add);
        buttonPanel.add(clear);

        add(buttonPanel, BorderLayout.SOUTH);

        add.addActionListener(this);
        clear.addActionListener(this);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == add) {

            try {
                double n1 = Double.parseDouble(
                    number1.getText().trim()
                );

                double n2 = Double.parseDouble(
                    number2.getText().trim()
                );

                double sum = n1 + n2;

                if (sum == (int) sum) {
                    result.setText("Sum = " + (int) sum);
                } else {
                    result.setText("Sum = " + sum);
                }

            } catch (NumberFormatException ex) {
                result.setText("Invalid Number");
            }
        }

        if (e.getSource() == clear) {
            number1.setText("");
            number2.setText("");
            result.setText("");
        }
    }

    public static void main(String[] args) {
        new Q4_Addition();
    }
}
