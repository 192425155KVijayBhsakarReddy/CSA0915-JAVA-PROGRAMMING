import java.awt.*;
import java.awt.event.*;

public class Q1_HospitalAppointment extends Frame implements ActionListener {

    Label title, patientIdLabel, nameLabel, ageLabel, genderLabel;
    Label departmentLabel, doctorLabel, dateLabel, statusLabel;

    TextField patientIdField, nameField, ageField, dateField;

    Checkbox male, female;
    CheckboxGroup genderGroup;

    Choice departmentChoice, doctorChoice;

    Button bookButton, clearButton;

    TextArea resultArea;

    Q1_HospitalAppointment() {

        setTitle("Hospital Appointment Management");
        setSize(700, 600);
        setLayout(new BorderLayout(10, 10));

        title = new Label("HOSPITAL APPOINTMENT MANAGEMENT", Label.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        Panel formPanel = new Panel();
        formPanel.setLayout(new GridLayout(8, 2, 10, 10));

        patientIdLabel = new Label("Patient ID:");
        nameLabel = new Label("Patient Name:");
        ageLabel = new Label("Age:");
        genderLabel = new Label("Gender:");
        departmentLabel = new Label("Department:");
        doctorLabel = new Label("Doctor Name:");
        dateLabel = new Label("Appointment Date:");

        patientIdField = new TextField();
        nameField = new TextField();
        ageField = new TextField();
        dateField = new TextField();

        genderGroup = new CheckboxGroup();

        male = new Checkbox("Male", genderGroup, false);
        female = new Checkbox("Female", genderGroup, false);

        Panel genderPanel = new Panel(new FlowLayout(FlowLayout.LEFT));
        genderPanel.add(male);
        genderPanel.add(female);

        departmentChoice = new Choice();
        departmentChoice.add("Select Department");
        departmentChoice.add("Cardiology");
        departmentChoice.add("Neurology");
        departmentChoice.add("Orthopedics");
        departmentChoice.add("Dermatology");
        departmentChoice.add("Pediatrics");

        doctorChoice = new Choice();
        doctorChoice.add("Select Doctor");
        doctorChoice.add("Dr. Kumar");
        doctorChoice.add("Dr. Meena");
        doctorChoice.add("Dr. Raj");
        doctorChoice.add("Dr. Priya");

        formPanel.add(patientIdLabel);
        formPanel.add(patientIdField);

        formPanel.add(nameLabel);
        formPanel.add(nameField);

        formPanel.add(ageLabel);
        formPanel.add(ageField);

        formPanel.add(genderLabel);
        formPanel.add(genderPanel);

        formPanel.add(departmentLabel);
        formPanel.add(departmentChoice);

        formPanel.add(doctorLabel);
        formPanel.add(doctorChoice);

        formPanel.add(dateLabel);
        formPanel.add(dateField);

        Panel buttonPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        bookButton = new Button("Book Appointment");
        clearButton = new Button("Clear");

        buttonPanel.add(bookButton);
        buttonPanel.add(clearButton);

        formPanel.add(new Label(""));
        formPanel.add(buttonPanel);

        add(formPanel, BorderLayout.CENTER);

        Panel bottomPanel = new Panel(new BorderLayout(5, 5));

        statusLabel = new Label("Appointment Confirmation", Label.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));

        resultArea = new TextArea(8, 50);
        resultArea.setEditable(false);

        bottomPanel.add(statusLabel, BorderLayout.NORTH);
        bottomPanel.add(resultArea, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);

        bookButton.addActionListener(this);
        clearButton.addActionListener(this);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == bookButton) {
            bookAppointment();
        }

        if (e.getSource() == clearButton) {
            clearFields();
        }
    }

    void bookAppointment() {

        String patientId = patientIdField.getText().trim();
        String patientName = nameField.getText().trim();
        String ageText = ageField.getText().trim();
        String appointmentDate = dateField.getText().trim();

        if (patientId.isEmpty()) {
            resultArea.setText("Patient ID Required");
            return;
        }

        if (patientName.isEmpty()) {
            resultArea.setText("Patient Name Required");
            return;
        }

        if (ageText.isEmpty()) {
            resultArea.setText("Age Required");
            return;
        }

        int age;

        try {
            age = Integer.parseInt(ageText);

            if (age <= 0 || age > 120) {
                resultArea.setText("Invalid Age");
                return;
            }

        } catch (NumberFormatException ex) {
            resultArea.setText("Invalid Age");
            return;
        }

        if (genderGroup.getSelectedCheckbox() == null) {
            resultArea.setText("Gender Required");
            return;
        }

        if (departmentChoice.getSelectedIndex() == 0) {
            resultArea.setText("Department Required");
            return;
        }

        if (doctorChoice.getSelectedIndex() == 0) {
            resultArea.setText("Doctor Name Required");
            return;
        }

        if (appointmentDate.isEmpty()) {
            resultArea.setText("Appointment Date Required");
            return;
        }

        String gender =
            genderGroup.getSelectedCheckbox().getLabel();

        String department =
            departmentChoice.getSelectedItem();

        String doctor =
            doctorChoice.getSelectedItem();

        resultArea.setText(
            "APPOINTMENT BOOKED SUCCESSFULLY\n\n" +
            "Patient ID       : " + patientId + "\n" +
            "Patient Name     : " + patientName + "\n" +
            "Age              : " + age + "\n" +
            "Gender           : " + gender + "\n" +
            "Department       : " + department + "\n" +
            "Doctor Name      : " + doctor + "\n" +
            "Appointment Date : " + appointmentDate + "\n\n" +
            "Status           : Confirmed"
        );
    }

    void clearFields() {

        patientIdField.setText("");
        nameField.setText("");
        ageField.setText("");
        dateField.setText("");

        genderGroup.setSelectedCheckbox(null);

        departmentChoice.select(0);
        doctorChoice.select(0);

        resultArea.setText("");
    }

    public static void main(String[] args) {
        new Q1_HospitalAppointment();
    }
}