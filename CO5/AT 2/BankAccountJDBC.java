import java.sql.*;
import java.util.Scanner;

public class BankAccountJDBC {

    static final String URL = "jdbc:mysql://localhost:3306/bankdb";
    static final String USER = "root";
    static final String PASSWORD = "root";

    static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    static void createAccount(Scanner sc) {
        String sql = "INSERT INTO accounts VALUES (?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            System.out.print("Enter Account Number: ");
            int accNo = Integer.parseInt(sc.nextLine());

            System.out.print("Enter Account Holder Name: ");
            String name = sc.nextLine();

            System.out.print("Enter Balance: ");
            double balance = Double.parseDouble(sc.nextLine());

            ps.setInt(1, accNo);
            ps.setString(2, name);
            ps.setDouble(3, balance);

            ps.executeUpdate();
            System.out.println("Account created successfully!");

        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    static void deposit(Scanner sc) {
        String sql = "UPDATE accounts SET balance = balance + ? WHERE account_no = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            System.out.print("Enter Account Number: ");
            int accNo = Integer.parseInt(sc.nextLine());

            System.out.print("Enter Deposit Amount: ");
            double amount = Double.parseDouble(sc.nextLine());

            ps.setDouble(1, amount);
            ps.setInt(2, accNo);

            if (ps.executeUpdate() > 0)
                System.out.println("Amount deposited successfully!");
            else
                System.out.println("Account not found.");

        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    static void withdraw(Scanner sc) {
        try (Connection con = getConnection()) {

            System.out.print("Enter Account Number: ");
            int accNo = Integer.parseInt(sc.nextLine());

            System.out.print("Enter Withdraw Amount: ");
            double amount = Double.parseDouble(sc.nextLine());

            String select = "SELECT balance FROM accounts WHERE account_no = ?";
            PreparedStatement ps1 = con.prepareStatement(select);
            ps1.setInt(1, accNo);

            ResultSet rs = ps1.executeQuery();

            if (rs.next()) {
                double balance = rs.getDouble("balance");

                if (amount > balance) {
                    System.out.println("Insufficient Balance");
                } else {
                    String update = "UPDATE accounts SET balance = balance - ? WHERE account_no = ?";
                    PreparedStatement ps2 = con.prepareStatement(update);

                    ps2.setDouble(1, amount);
                    ps2.setInt(2, accNo);
                    ps2.executeUpdate();

                    System.out.println("Withdrawal successful!");
                }
            } else {
                System.out.println("Account not found.");
            }

        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    static void displayAccount(Scanner sc) {
        String sql = "SELECT * FROM accounts WHERE account_no = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            System.out.print("Enter Account Number: ");
            int accNo = Integer.parseInt(sc.nextLine());

            ps.setInt(1, accNo);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println("\n--- Account Details ---");
                System.out.println("Account Number: " + rs.getInt("account_no"));
                System.out.println("Account Holder: " + rs.getString("account_name"));
                System.out.println("Balance: " + rs.getDouble("balance"));
            } else {
                System.out.println("Account not found.");
            }

        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        int choice = 0;

        do {
            System.out.println("\n--- BANK ACCOUNT MANAGEMENT ---");
            System.out.println("1. Create Account");
            System.out.println("2. Deposit Money");
            System.out.println("3. Withdraw Money");
            System.out.println("4. Display Account");
            System.out.println("5. Exit");
            System.out.print("Enter Choice: ");

            try {
                choice = Integer.parseInt(sc.nextLine());

                switch (choice) {
                    case 1: createAccount(sc); break;
                    case 2: deposit(sc); break;
                    case 3: withdraw(sc); break;
                    case 4: displayAccount(sc); break;
                    case 5: System.out.println("Thank You!"); break;
                    default: System.out.println("Invalid Choice!");
                }

            } catch (NumberFormatException e) {
                System.out.println("Enter a valid number.");
            }

        } while (choice != 5);

        sc.close();
    }
}
