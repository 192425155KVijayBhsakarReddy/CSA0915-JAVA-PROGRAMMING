import java.io.*;
import java.util.Scanner;

public class EmployeeFileHandling {

    static final String FILE = "employees.txt";

    static void addEmployee(Scanner sc) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE, true))) {

            System.out.print("Enter Employee ID: ");
            String id = sc.nextLine();

            System.out.print("Enter Employee Name: ");
            String name = sc.nextLine();

            System.out.print("Enter Department: ");
            String dept = sc.nextLine();

            System.out.print("Enter Salary: ");
            String salary = sc.nextLine();

            bw.write(id + "," + name + "," + dept + "," + salary);
            bw.newLine();

            System.out.println("Employee record added successfully!");

        } catch (IOException e) {
            System.out.println("Error while writing employee record.");
        }
    }

    static void displayEmployees() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {

            String line;

            System.out.println("\nEmployee Records:");
            System.out.println("-------------------------------");

            while ((line = br.readLine()) != null) {
                String[] e = line.split(",");

                System.out.println("ID: " + e[0]);
                System.out.println("Name: " + e[1]);
                System.out.println("Department: " + e[2]);
                System.out.println("Salary: " + e[3]);
                System.out.println("-------------------------------");
            }

        } catch (FileNotFoundException e) {
            System.out.println("Employee file is not available.");
        } catch (IOException e) {
            System.out.println("Error while reading the file.");
        }
    }

    static void searchEmployee(Scanner sc) {
        System.out.print("Enter Employee ID to search: ");
        String id = sc.nextLine();

        boolean found = false;

        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {

            String line;

            while ((line = br.readLine()) != null) {
                String[] e = line.split(",");

                if (e[0].equalsIgnoreCase(id)) {
                    System.out.println("\nEmployee Record Found:");
                    System.out.println("ID: " + e[0]);
                    System.out.println("Name: " + e[1]);
                    System.out.println("Department: " + e[2]);
                    System.out.println("Salary: " + e[3]);

                    found = true;
                    break;
                }
            }

            if (!found)
                System.out.println("Employee record not found.");

        } catch (FileNotFoundException e) {
            System.out.println("Employee file is not available.");
        } catch (IOException e) {
            System.out.println("Error while searching the file.");
        }
    }

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        int choice;

        do {
            System.out.println("\n--- Employee Record Management ---");
            System.out.println("1. Add Employee");
            System.out.println("2. Display Employees");
            System.out.println("3. Search Employee");
            System.out.println("4. Exit");

            System.out.print("Enter your choice: ");

            try {
                choice = Integer.parseInt(sc.nextLine());

                switch (choice) {
                    case 1:
                        addEmployee(sc);
                        break;
                    case 2:
                        displayEmployees();
                        break;
                    case 3:
                        searchEmployee(sc);
                        break;
                    case 4:
                        System.out.println("Program closed.");
                        break;
                    default:
                        System.out.println("Invalid choice!");
                }

            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
                choice = 0;
            }

        } while (choice != 4);

        sc.close();
    }
}
