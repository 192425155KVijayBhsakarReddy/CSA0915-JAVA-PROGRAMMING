import java.awt.*;
import java.awt.event.*;

public class Q3_StudentRegistration extends Frame implements ActionListener {

    TextField studentId, studentName;
    CheckboxGroup genderGroup;
    Choice department;
    Button register, clear;
    TextArea result;

    Q3_StudentRegistration() {

        setTitle("Student Registration Form");
        setSize(700, 550);
        setLayout(new BorderLayout(10, 10));

        Label title = new Label(
            "STUDENT REGISTRATION FORM",
            Label.CENTER
        );
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        Panel form = new Panel();
        form.setLayout(new GridLayout(5, 2, 10, 10));

        form.add(new Label("Student ID:"));
        studentId = new TextField();
        form.add(studentId);

        form.add(new Label("Student Name:"));
        studentName = new TextField();
        form.add(studentName);

        form.add(new Label("Gender:"));

        genderGroup = new CheckboxGroup();

        Panel genderPanel = new Panel(
            new FlowLayout(FlowLayout.LEFT)
        );

        genderPanel.add(
            new Checkbox("Male", genderGroup, false)
        );

        genderPanel.add(
            new Checkbox("Female", genderGroup, false)
        );

        form.add(genderPanel);

        form.add(new Label("Department:"));

        department = new Choice();
        department.add("Select Department");
        department.add("Information Technology");
        department.add("Computer Science");
        department.add("Electronics and Communication");
        department.add("Mechanical Engineering");
        department.add("Civil Engineering");

        form.add(department);

        Panel buttonPanel = new Panel(
            new FlowLayout(FlowLayout.CENTER, 20, 5)
        );

        register = new Button("Register");
        clear = new Button("Clear");

        buttonPanel.add(register);
        buttonPanel.add(clear);

        form.add(new Label(""));
        form.add(buttonPanel);

        add(form, BorderLayout.CENTER);

        Panel resultPanel = new Panel(
            new BorderLayout(5, 5)
        );

        resultPanel.add(
            new Label(
                "Registration Details",
                Label.CENTER
            ),
            BorderLayout.NORTH
        );

        result = new TextArea(8, 50);
        result.setEditable(false);

        resultPanel.add(result, BorderLayout.CENTER);

        add(resultPanel, BorderLayout.SOUTH);

        register.addActionListener(this);
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

        if (e.getSource() == register) {
            registerStudent();
        }

        if (e.getSource() == clear) {
            clearFields();
        }
    }

    void registerStudent() {

        String id = studentId.getText().trim();
        String name = studentName.getText().trim();

        if (id.isEmpty()) {
            result.setText("Student ID is required");
            return;
        }

        if (name.isEmpty()) {
            result.setText("Student Name is required");
            return;
        }

        if (genderGroup.getSelectedCheckbox() == null) {
            result.setText("Gender is required");
            return;
        }

        if (department.getSelectedIndex() == 0) {
            result.setText("Department is required");
            return;
        }

        String gender =
            genderGroup.getSelectedCheckbox().getLabel();

        String selectedDepartment =
            department.getSelectedItem();

        result.setText(
            "STUDENT REGISTERED SUCCESSFULLY\n\n" +
            "Student ID   : " + id + "\n" +
            "Student Name : " + name + "\n" +
            "Gender       : " + gender + "\n" +
            "Department   : " + selectedDepartment + "\n\n" +
            "Registration Status : Successful"
        );
    }

    void clearFields() {

        studentId.setText("");
        studentName.setText("");
        genderGroup.setSelectedCheckbox(null);
        department.select(0);
        result.setText("");
    }

    public static void main(String[] args) {
        new Q3_StudentRegistration();
    }
}
