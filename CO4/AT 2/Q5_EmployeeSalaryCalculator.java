import java.awt.*;
import java.awt.event.*;

public class Q5_EmployeeSalaryCalculator extends Frame implements ActionListener {

    TextField employeeId, employeeName, basicSalary;
    TextField hra, da, grossSalary;
    Button calculate, clear;

    Q5_EmployeeSalaryCalculator() {

        setTitle("Employee Salary Calculator");
        setSize(650, 500);
        setLayout(new BorderLayout(10, 10));

        Label title = new Label(
            "EMPLOYEE SALARY CALCULATOR",
            Label.CENTER
        );
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        Panel form = new Panel();
        form.setLayout(new GridLayout(6, 2, 10, 10));

        form.add(new Label("Employee ID:"));
        employeeId = new TextField();
        form.add(employeeId);

        form.add(new Label("Employee Name:"));
        employeeName = new TextField();
        form.add(employeeName);

        form.add(new Label("Basic Salary:"));
        basicSalary = new TextField();
        form.add(basicSalary);

        form.add(new Label("HRA (20%):"));
        hra = new TextField();
        hra.setEditable(false);
        form.add(hra);

        form.add(new Label("DA (10%):"));
        da = new TextField();
        da.setEditable(false);
        form.add(da);

        form.add(new Label("Gross Salary:"));
        grossSalary = new TextField();
        grossSalary.setEditable(false);
        form.add(grossSalary);

        add(form, BorderLayout.CENTER);

        Panel buttonPanel = new Panel(
            new FlowLayout(FlowLayout.CENTER, 20, 10)
        );

        calculate = new Button("Calculate");
        clear = new Button("Clear");

        buttonPanel.add(calculate);
        buttonPanel.add(clear);

        add(buttonPanel, BorderLayout.SOUTH);

        calculate.addActionListener(this);
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

        if (e.getSource() == calculate) {

            try {
                double basic = Double.parseDouble(
                    basicSalary.getText().trim()
                );

                if (basic < 0) {
                    hra.setText("Invalid Salary");
                    da.setText("");
                    grossSalary.setText("");
                    return;
                }

                double hraValue = basic * 0.20;
                double daValue = basic * 0.10;
                double gross = basic + hraValue + daValue;

                hra.setText(format(hraValue));
                da.setText(format(daValue));
                grossSalary.setText(format(gross));

            } catch (NumberFormatException ex) {
                hra.setText("Invalid Salary");
                da.setText("");
                grossSalary.setText("");
            }
        }

        if (e.getSource() == clear) {
            employeeId.setText("");
            employeeName.setText("");
            basicSalary.setText("");
            hra.setText("");
            da.setText("");
            grossSalary.setText("");
        }
    }

    String format(double value) {
        if (value == (int) value) {
            return String.valueOf((int) value);
        }
        return String.valueOf(value);
    }

    public static void main(String[] args) {
        new Q5_EmployeeSalaryCalculator();
    }
}
