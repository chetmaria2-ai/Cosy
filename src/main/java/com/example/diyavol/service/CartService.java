package com.example.diyavol.service;

import com.example.diyavol.db.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartService {

    public static class CartEntry {
        private final int productId;
        private final String productName;
        private final double price;
        private int quantity;

        public CartEntry(int productId, String productName, double price, int quantity) {
            this.productId = productId;
            this.productName = productName;
            this.price = price;
            this.quantity = quantity;
        }

        public int getProductId()    { return productId; }
        public String getProductName() { return productName; }
        public double getPrice()     { return price; }
        public int getQuantity()     { return quantity; }
    }

    public static void addToCart(int userId, int productId, int quantity) {
        String sql = "INSERT INTO cart (user_id, product_id, quantity) VALUES (?, ?, ?) " +
                     "ON CONFLICT (user_id, product_id) " +
                     "DO UPDATE SET quantity = cart.quantity + EXCLUDED.quantity";
        try (Connection conn = com.example.diyavol.db.DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            ps.executeUpdate();
            System.out.println("✅ Товар добавлен в корзину БД: productId=" + productId);
        } catch (SQLException e) {
            System.out.println("❌ Ошибка добавления в корзину: " + e.getMessage());
        }
    }

    public static void removeFromCart(int userId, int productId) {
        String sql = "DELETE FROM cart WHERE user_id = ? AND product_id = ?";
        try (Connection conn = com.example.diyavol.db.DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("❌ Ошибка удаления из корзины: " + e.getMessage());
        }
    }

    public static void updateQuantity(int userId, int productId, int quantity) {
        if (quantity <= 0) {
            removeFromCart(userId, productId);
            return;
        }
        String sql = "UPDATE cart SET quantity = ? WHERE user_id = ? AND product_id = ?";
        try (Connection conn = com.example.diyavol.db.DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, userId);
            ps.setInt(3, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("❌ Ошибка обновления количества: " + e.getMessage());
        }
    }

    public static List<CartEntry> loadCart(int userId) {
        List<CartEntry> result = new ArrayList<>();
        String sql = "SELECT c.product_id, p.name, p.price, c.quantity " +
                     "FROM cart c JOIN products p ON c.product_id = p.id " +
                     "WHERE c.user_id = ? ORDER BY c.added_at";
        try (Connection conn = com.example.diyavol.db.DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(new CartEntry(
                    rs.getInt("product_id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getInt("quantity")
                ));
            }
            System.out.println("✅ Корзина загружена из БД: " + result.size() + " товаров");
        } catch (SQLException e) {
            System.out.println("❌ Ошибка загрузки корзины: " + e.getMessage());
        }
        return result;
    }

    public static void clearCart(int userId) {
        String sql = "DELETE FROM cart WHERE user_id = ?";
        try (Connection conn = com.example.diyavol.db.DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
            System.out.println("✅ Корзина очищена в БД для userId=" + userId);
        } catch (SQLException e) {
            System.out.println("❌ Ошибка очистки корзины: " + e.getMessage());
        }
    }
}
