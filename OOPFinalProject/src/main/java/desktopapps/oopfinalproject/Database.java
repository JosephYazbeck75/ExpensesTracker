package desktopapps.oopfinalproject;

import java.sql.*;

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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        }
    public static boolean registerUser(String username, String password) {
        String sql = "Insert INTO users (username,password) VALUES (?,?)";
        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, username);
                    pstmt.setString(2,password);
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
                            return storedPassword.equals(password);
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
}

    
    


