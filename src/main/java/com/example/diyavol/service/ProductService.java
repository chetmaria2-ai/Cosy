package com.example.diyavol.service;

import com.example.diyavol.db.DatabaseConnection;

import java.sql.*;
import java.util.*;

public class ProductService {

    public static class Product {
        private int id;
        private String name;
        private String description;
        private double price;
        private String categoryName;
        private int stockQuantity;
        private double rating;
        private String imageUrl;

        public Product(String name, String description, double price,
                       String categoryName, int stockQuantity, double rating, String imageUrl) {
            this(0, name, description, price, categoryName, stockQuantity, rating, imageUrl);
        }

        public Product(int id, String name, String description, double price,
                       String categoryName, int stockQuantity, double rating, String imageUrl) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.price = price;
            this.categoryName = categoryName;
            this.stockQuantity = stockQuantity;
            this.rating = rating;
            this.imageUrl = imageUrl;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public double getPrice() { return price; }
        public String getCategoryName() { return categoryName; }
        public int getStockQuantity() { return stockQuantity; }
        public double getRating() { return rating; }
        public String getImageUrl() { return imageUrl; }
    }

    // ==================== ЧТЕНИЕ ====================

    /** Загружает из БД, при ошибке — тестовые данные */
    public static List<Product> getAllProducts() {
        List<Product> fromDB = getAllProductsFromDB();
        return fromDB.isEmpty() ? getTestProducts() : fromDB;
    }

    // Маппинг category_id -> название
    private static String categoryName(int id) {
        switch (id) {
            case 1: return "Электроника";
            case 2: return "Пижамы и халаты";
            case 3: return "Декор и освещение";
            case 4: return "Текстиль";
            case 5: return "Органайзеры";
            default: return "Прочее";
        }
    }
    private static int categoryId(String name) {
        switch (name) {
            case "Электроника":       return 1;
            case "Пижамы и халаты":   return 2;
            case "Декор и освещение": return 3;
            case "Текстиль":          return 4;
            case "Органайзеры":       return 5;
            default:                  return 1;
        }
    }

    /** Только из БД (для панели админа) */
    public static List<Product> getAllProductsFromDB() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT id, name, description, price, category_id, " +
                     "stock_quantity, rating, image_url FROM products ORDER BY id";
        try (Connection conn = com.example.diyavol.db.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                products.add(new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDouble("price"),
                    categoryName(rs.getInt("category_id")),
                    rs.getInt("stock_quantity"),
                    rs.getDouble("rating"),
                    rs.getString("image_url")
                ));
            }
            System.out.println("✅ Загружено товаров из БД: " + products.size());
        } catch (SQLException e) {
            System.out.println("⚠️  Ошибка загрузки товаров: " + e.getMessage());
        }
        return products;
    }

    // ==================== ДОБАВЛЕНИЕ ====================

    public static boolean addProduct(Product p) {
        String sql = "INSERT INTO products (name, description, price, category_id, " +
                     "stock_quantity, rating, image_url, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, true)";
        try (Connection conn = com.example.diyavol.db.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getName());
            pstmt.setString(2, p.getDescription());
            pstmt.setDouble(3, p.getPrice());
            pstmt.setInt(4, categoryId(p.getCategoryName()));
            pstmt.setInt(5, p.getStockQuantity());
            pstmt.setDouble(6, p.getRating());
            pstmt.setString(7, p.getImageUrl());
            int rows = pstmt.executeUpdate();
            System.out.println("✅ Товар добавлен: " + p.getName());
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("❌ Ошибка добавления товара: " + e.getMessage());
            return false;
        }
    }

    // ==================== РЕДАКТИРОВАНИЕ ====================

    public static boolean updateProduct(Product p) {
        String sql = "UPDATE products SET name=?, description=?, price=?, category_id=?, " +
                     "stock_quantity=?, rating=?, image_url=? WHERE id=?";
        try (Connection conn = com.example.diyavol.db.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getName());
            pstmt.setString(2, p.getDescription());
            pstmt.setDouble(3, p.getPrice());
            pstmt.setInt(4, categoryId(p.getCategoryName()));
            pstmt.setInt(5, p.getStockQuantity());
            pstmt.setDouble(6, p.getRating());
            pstmt.setString(7, p.getImageUrl());
            pstmt.setInt(8, p.getId());
            int rows = pstmt.executeUpdate();
            System.out.println("✅ Товар обновлён: " + p.getName());
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("❌ Ошибка обновления товара: " + e.getMessage());
            return false;
        }
    }

    // ==================== УДАЛЕНИЕ ====================

    public static boolean deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = com.example.diyavol.db.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            System.out.println("✅ Товар удалён id=" + id);
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("❌ Ошибка удаления товара: " + e.getMessage());
            return false;
        }
    }

    // ==================== ТЕСТОВЫЕ ДАННЫЕ ====================

    private static List<Product> getTestProducts() {
        List<Product> p = new ArrayList<>();
        p.add(new Product(1, "Умная лампа SmartLight Pro", "Управление через приложение", 2999.0, "Электроника", 50, 4.8, "smart_lamp.jpg"));
        p.add(new Product(2, "Шелковая пижама Комфорт", "Роскошная шелковая пижама", 5599.0, "Пижамы и халаты", 40, 4.9, "silk_pajamas.jpg"));
        p.add(new Product(3, "Аромалампа с таймером", "Керамическая аромалампа", 1899.0, "Декор и освещение", 45, 4.4, "aroma_lamp.jpg"));
        p.add(new Product(4, "Плед Кашемировый уют", "Мягкий плед из шерсти", 3299.0, "Текстиль", 25, 4.7, "blanket.jpg"));
        p.add(new Product(5, "Органайзер для косметики", "Вращающийся органайзер", 4599.0, "Органайзеры", 20, 4.6, "cosmetics_organizer.jpg"));
        return p;
    }
}
