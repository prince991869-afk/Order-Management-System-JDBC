import java.sql.*;
import java.util.Scanner;

public class OrderManagement {

    static final String URL = "jdbc:mysql://localhost:3306/ecommerce_db";
    static final String USER = "root";
    static final String PASSWORD = "password";

    static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    static void insertOrders() {

        String sql = "INSERT INTO Orders (order_id, customer_name, product_name, quantity, unit_price, status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            int[] ids = {101, 102, 103};
            String[] customers = {"Prince", "Rahul", "Aman"};
            String[] products = {"Laptop", "Mobile", "Headphones"};
            int[] quantities = {3, 2, 5};
            double[] prices = {55000, 20000, 1500};
            String[] statuses = {"Pending", "Shipped", "Delivered"};

            System.out.println("Database Connected Successfully!");

            for (int i = 0; i < 3; i++) {
                try {
                    ps.setInt(1, ids[i]);
                    ps.setString(2, customers[i]);
                    ps.setString(3, products[i]);
                    ps.setInt(4, quantities[i]);
                    ps.setDouble(5, prices[i]);
                    ps.setString(6, statuses[i]);

                    ps.executeUpdate();

                    System.out.println("Order " + ids[i] + " inserted successfully!");

                } catch (SQLIntegrityConstraintViolationException e) {
                    System.out.println("Duplicate Order ID: " + ids[i]);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void displayOrders(Scanner sc) {

        System.out.print("Enter order status: ");
        String status = sc.nextLine();

        String sql = "SELECT * FROM Orders WHERE status = ? AND quantity > 2 ORDER BY unit_price DESC";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);

            ResultSet rs = ps.executeQuery();

            boolean found = false;

            System.out.println("\nOrder ID | Customer | Product | Quantity | Price | Status");

            while (rs.next()) {
                found = true;

                System.out.println(
                    rs.getInt("order_id") + " | " +
                    rs.getString("customer_name") + " | " +
                    rs.getString("product_name") + " | " +
                    rs.getInt("quantity") + " | " +
                    rs.getDouble("unit_price") + " | " +
                    rs.getString("status")
                );
            }

            if (!found) {
                System.out.println("No matching orders found.");
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void updateQuantity(Scanner sc) {

        System.out.print("Enter Order ID: ");
        int id = sc.nextInt();

        System.out.print("Enter additional quantity: ");
        int quantity = sc.nextInt();

        if (quantity <= 0) {
            System.out.println("Additional quantity must be positive.");
            return;
        }

        String sql = "UPDATE Orders SET quantity = quantity + ? WHERE order_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, quantity);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Order quantity updated successfully!");
            } else {
                System.out.println("Order does not exist.");
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void updateStatus(Scanner sc) {

        System.out.print("Enter Order ID: ");
        int id = sc.nextInt();
        sc.nextLine();

        System.out.print("Enter new status: ");
        String status = sc.nextLine().trim();

        if (status.equalsIgnoreCase("Pending")) {
            status = "Pending";
        } else if (status.equalsIgnoreCase("Shipped")) {
            status = "Shipped";
        } else if (status.equalsIgnoreCase("Delivered")) {
            status = "Delivered";
        } else {
            System.out.println("Invalid status! Use Pending, Shipped or Delivered.");
            return;
        }

        String check = "SELECT * FROM Orders WHERE order_id = ?";
        String update = "UPDATE Orders SET status = ? WHERE order_id = ?";

        try (Connection con = getConnection();
             PreparedStatement checkPs = con.prepareStatement(check)) {

            checkPs.setInt(1, id);

            ResultSet rs = checkPs.executeQuery();

            if (rs.next()) {

                try (PreparedStatement updatePs = con.prepareStatement(update)) {

                    updatePs.setString(1, status);
                    updatePs.setInt(2, id);

                    updatePs.executeUpdate();
                }

                System.out.println("\nUpdated Order Details:");

                try (PreparedStatement displayPs = con.prepareStatement(check)) {

                    displayPs.setInt(1, id);

                    ResultSet updatedRs = displayPs.executeQuery();

                    if (updatedRs.next()) {
                        System.out.println("Order ID: " + updatedRs.getInt("order_id"));
                        System.out.println("Customer: " + updatedRs.getString("customer_name"));
                        System.out.println("Product: " + updatedRs.getString("product_name"));
                        System.out.println("Quantity: " + updatedRs.getInt("quantity"));
                        System.out.println("Price: " + updatedRs.getDouble("unit_price"));
                        System.out.println("Status: " + updatedRs.getString("status"));
                    }
                }

            } else {
                System.out.println("Order does not exist.");
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void calculateAndDelete(Scanner sc) {

        String totalSql = "SELECT SUM(quantity * unit_price) AS total FROM Orders";

        try (Connection con = getConnection();
             PreparedStatement totalPs = con.prepareStatement(totalSql);
             ResultSet totalRs = totalPs.executeQuery()) {

            if (totalRs.next()) {
                System.out.println("Total Order Value: " + totalRs.getDouble("total"));
            }

            System.out.print("Enter Order ID to delete: ");
            int id = sc.nextInt();

            String checkSql = "SELECT status FROM Orders WHERE order_id = ?";

            try (PreparedStatement checkPs = con.prepareStatement(checkSql)) {

                checkPs.setInt(1, id);

                ResultSet rs = checkPs.executeQuery();

                if (rs.next()) {

                    String status = rs.getString("status");

                    if (status.equalsIgnoreCase("Cancelled")) {

                        String deleteSql = "DELETE FROM Orders WHERE order_id = ?";

                        try (PreparedStatement deletePs = con.prepareStatement(deleteSql)) {

                            deletePs.setInt(1, id);
                            deletePs.executeUpdate();

                            System.out.println("Order deleted successfully!");
                        }

                    } else {
                        System.out.println("Order cannot be deleted because its status is " + status);
                    }

                } else {
                    System.out.println("Order does not exist.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        while (true) {

            System.out.println("\n========== ORDER MANAGEMENT SYSTEM ==========");
            System.out.println("1. Insert Three Orders");
            System.out.println("2. Display Orders By Status");
            System.out.println("3. Increase Order Quantity");
            System.out.println("4. Update Order Status");
            System.out.println("5. Display Total Value and Delete Cancelled Order");
            System.out.println("0. Exit");

            System.out.print("Enter your choice: ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {

                case 1:
                    insertOrders();
                    break;

                case 2:
                    displayOrders(sc);
                    break;

                case 3:
                    updateQuantity(sc);
                    break;

                case 4:
                    updateStatus(sc);
                    break;

                case 5:
                    calculateAndDelete(sc);
                    break;

                case 0:
                    System.out.println("Application Closed.");
                    sc.close();
                    return;

                default:
                    System.out.println("Invalid Choice!");
            }
        }
    }
}