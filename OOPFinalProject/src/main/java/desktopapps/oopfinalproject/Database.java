package desktopapps.oopfinalproject;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static final String  URL = "jdbc:sqlite:expenses.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }
    public static void initialize() {
        String usersTable = """
                CREATE TABLE IF NOT EXISTS users (
                username TEXT PRIMARY KEY,
                password TEXT NOT NULL
                );
                """;
    
    String expensesTable = """
            CREATE TABLE IF NOT EXISTS expenses (
            id INTEGER PRIMARY KEY,
            username TEXT NOT NULL,
            description TEXT NOT NULL,
            amount REAL NOT NULL,
            date TEXT NOT NULL,
            category TEXT NOT NULL,
            FOREIGN KEY (username) REFERENCES users(username)
            );
            """;
    try (Connection conn = connect();
        Statement stmt = conn.createStatement()) {
            stmt.execute(usersTable);
            stmt.execute(expensesTable);
            System.out.println("Database initialized successfully.");
    try {
        stmt.execute("ALTER TABLE users ADD COLUMN monthly_income REAL");
    } catch (SQLException e) {
        System.out.println("Column already exists or error: " + e.getMessage());
    }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        }
    public static boolean registerUser(String username, String password) {
        String sql = "Insert INTO users (username,password) VALUES (?,?)";
        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, username);
                    pstmt.setString(2,PasswordUtil.hashPassword(password));
                    pstmt.executeUpdate();
                    return true;
                }
                catch (SQLException e) {
                    return false; }
                }
    public static boolean validateLogin(String username, String password) {
        String sql = "SELECT password FROM users WHERE username = ?";
        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1,username);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            String storedPassword = rs.getString("password");
                            return PasswordUtil.verifyPassword(password, storedPassword);
                        }
                        return false;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }        
    }
    public static boolean userExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean addExpense(String username, String description, double amount, String date, String category) {
        String sql = "INSERT INTO expenses (username, description, amount, date, category) VALUES (?,?,?,?,?)";
        try (Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1,username);
                pstmt.setString(2,description);
                pstmt.setDouble(3,amount);
                pstmt.setString(4,date);
                pstmt.setString(5, category);
                pstmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    public static List<ExpensesTracker.Expense> getExpenses(String username) {
        List<ExpensesTracker.Expense> expenses = new ArrayList<>();
        String sql = "SELECT id, description, amount, date, category FROM expenses WHERE username = ?";
        try (Connection conn = connect();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ExpensesTracker.Expense e = new ExpensesTracker.Expense(rs.getString("description"), rs.getDouble("amount"), LocalDate.parse(rs.getString("date")),rs.getString("category")
                );
                e.setId(rs.getInt("id"));
                expenses.add(e);
            }
        } 
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return expenses;
                }
    public static boolean updateExpense(ExpensesTracker.Expense expense) {
        String sql = "UPDATE expenses SET description = ?, amount = ?. date = ?, category = ? WHERE id + ?";
        try (Connection conn = connect();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, expense.getDescription());
            pstmt.setDouble(2, expense.getAmount());
            pstmt.setString(3, expense.getDate().toString());
            pstmt.setString(4, expense.getCategory());
            pstmt.setInt(5, expense.getId());
            pstmt.executeUpdate();
        return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean deleteExpense(int id) {
        String sql = "DELETE FROM expenses WHERE id = ?";
        try (Connection conn = connect();
    PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1,id);
        pstmt.executeUpdate();
        return true;
        } catch (SQLException e) {
        e.printStackTrace();
        return false;
        }
    }
 public static Double getMonthlyIncome(String username) {
        String sql = "SELECT monthly_income FROM users WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double value = rs.getDouble("monthly_income");
                    return rs.wasNull() ? null : value;
                }
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setMonthlyIncome(String username, double income) {
        String sql = "UPDATE users SET monthly_income = ? WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, income);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    }
        
    
            
    


    
    


