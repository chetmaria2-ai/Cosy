package com.example.diyavol.service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    public static class User {
        private int id;
        private String username;
        private String fullName;
        private String email;
        private String role;
        private int loyaltyPoints;

        public User(int id, String username, String fullName, String email, String role, int loyaltyPoints) {
            this.id = id;
            this.username = username;
            this.fullName = fullName;
            this.email = email;
            this.role = role;
            this.loyaltyPoints = loyaltyPoints;
        }

        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public int getLoyaltyPoints() { return loyaltyPoints; }

        public boolean isAdmin() {
            return "admin".equals(role);
        }
    }

    public static User loginUser(String username, String password) {
        if (username == null || password == null || password.length() < 3) return null;

        try (Connection conn = com.example.diyavol.db.DatabaseConnection.getConnection()) {
            String sql = "SELECT id, username, email, password_hash FROM users WHERE username = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();
                if (!rs.next()) {
                    System.out.println("Пользователь не найден: " + username);
                    return null;
                }
                String storedPassword = rs.getString("password_hash");
                if (!password.equals(storedPassword)) {
                    System.out.println("Неверный пароль для: " + username);
                    return null;
                }
                int id = rs.getInt("id");
                String email = rs.getString("email");
            }

            String fullSql = "SELECT id, username, email, " +
                "COALESCE(full_name, username) as full_name, " +
                "COALESCE(role, 'customer') as role, " +
                "COALESCE(loyalty_points, 0) as loyalty_points " +
                "FROM users WHERE username = ? AND password_hash = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(fullSql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    System.out.println("✅ Вход через БД: " + username);
                    return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getInt("loyalty_points")
                    );
                }
            }
            return null;
        } catch (SQLException e) {
            // БД недоступна — тестовый режим (работает для всех)
            System.out.println("⚠️  БД недоступна, тестовый режим. Ошибка: " + e.getMessage());
            if (username.equals("admin")) {
                return new User(1, "admin", "Администратор", "admin@test.ru", "admin", 0);
            } else {
                return new User(2, username, "Пользователь " + username,
                               username + "@test.ru", "customer", 0);
            }
        }
    }

    // РЕГИСТРАЦИЯ — открытый пароль, записываем в БД
    public static boolean registerUser(String name, String username, String password, String email) {
        try (Connection conn = com.example.diyavol.db.DatabaseConnection.getConnection()) {
            // Проверяем, не занят ли логин
            try (PreparedStatement check = conn.prepareStatement(
                    "SELECT COUNT(*) FROM users WHERE username = ?")) {
                check.setString(1, username);
                ResultSet rs = check.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("❌ Логин занят: " + username);
                    return false;
                }
            }

            String sql = "INSERT INTO users (username, password_hash, full_name, email, role, loyalty_points) " +
                         "VALUES (?, ?, ?, ?, 'customer', 0)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);  // открытый пароль
                pstmt.setString(3, name);
                pstmt.setString(4, email);
                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    System.out.println("✅ Зарегистрирован в БД: " + username);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.out.println("⚠️  БД недоступна, тестовый режим: " + username);
            return true;
        }
        return false;
    }

    // ПОЛУЧИТЬ ВСЕХ ПОЛЬЗОВАТЕЛЕЙ
    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, full_name, email, role, loyalty_points FROM users ORDER BY id";

        try (Connection conn = com.example.diyavol.db.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                users.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getInt("loyalty_points")
                ));
            }
            System.out.println("✅ Загружено пользователей: " + users.size());

        } catch (SQLException e) {
            System.out.println("❌ Ошибка загрузки пользователей: " + e.getMessage());
        }
        return users;
    }

    // !!! НОВЫЙ МЕТОД makeAdmin !!!
    public static boolean makeAdmin(int userId, boolean isAdmin) {
        String sql = "UPDATE users SET role = ? WHERE id = ?";

        try (Connection conn = com.example.diyavol.db.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, isAdmin ? "admin" : "customer");
            pstmt.setInt(2, userId);

            int affectedRows = pstmt.executeUpdate();
            System.out.println("✅ Права пользователя " + userId + " изменены на " + (isAdmin ? "admin" : "customer"));
            return affectedRows > 0;

        } catch (SQLException e) {
            System.out.println("❌ Ошибка изменения прав: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static boolean deleteUser(int userId) {
        boolean success = UserService.deleteUser(userId);
        if (success) {
            showAlert("Успех", "Пользователь удалён.");
            loadUsers();
        }
        return success;
    }

    private static void loadUsers() {
    }

    private static void showAlert(String успех, String s) {
    }


    // ПРОВЕРИТЬ ЕСТЬ ЛИ АДМИН
    public static boolean hasAdmin() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'admin'";

        try (Connection conn = com.example.diyavol.db.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("❌ Ошибка проверки админа: " + e.getMessage());
        }
        return false;
    }
}