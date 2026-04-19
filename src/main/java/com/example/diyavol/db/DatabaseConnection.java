package com.example.diyavol.db;

import java.sql.*;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/prilo";
    private static final String USER = "postgres";
    private static final String PASSWORD = "1";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("Подключение к БД успешно!");
        } catch (SQLException e) {
            System.out.println("Ошибка подключения: " + e.getMessage());
        }
    }
}