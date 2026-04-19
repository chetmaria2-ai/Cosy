package com.example.diyavol.service;

import com.example.diyavol.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.List;

public class ReviewService {

    public static class Review {
        private int id;
        private int userId;
        private int productId;
        private String userName;
        private String productName;
        private int rating;
        private String comment;
        private Date createdAt;
        private boolean isApproved;

        public Review(int id, int userId, int productId, String userName, String productName,
                      int rating, String comment, Date createdAt, boolean isApproved) {
            this.id = id;
            this.userId = userId;
            this.productId = productId;
            this.userName = userName;
            this.productName = productName;
            this.rating = rating;
            this.comment = comment;
            this.createdAt = createdAt;
            this.isApproved = isApproved;
        }

        // Геттеры
        public int getId() { return id; }
        public int getUserId() { return userId; }
        public int getProductId() { return productId; }
        public String getUserName() { return userName; }
        public String getProductName() { return productName; }
        public int getRating() { return rating; }
        public String getComment() { return comment; }
        public Date getCreatedAt() { return createdAt; }
        public boolean isApproved() { return isApproved; }

        // Для TableView нужны property-методы
        public javafx.beans.property.SimpleIntegerProperty idProperty() {
            return new javafx.beans.property.SimpleIntegerProperty(id);
        }

        public javafx.beans.property.SimpleStringProperty userNameProperty() {
            return new javafx.beans.property.SimpleStringProperty(userName);
        }

        public javafx.beans.property.SimpleStringProperty productNameProperty() {
            return new javafx.beans.property.SimpleStringProperty(productName);
        }

        public javafx.beans.property.SimpleIntegerProperty ratingProperty() {
            return new javafx.beans.property.SimpleIntegerProperty(rating);
        }

        public javafx.beans.property.SimpleStringProperty commentProperty() {
            return new javafx.beans.property.SimpleStringProperty(comment);
        }

        public javafx.beans.property.SimpleObjectProperty<Date> createdAtProperty() {
            return new javafx.beans.property.SimpleObjectProperty<>(createdAt);
        }

        public javafx.beans.property.SimpleBooleanProperty approvedProperty() {
            return new javafx.beans.property.SimpleBooleanProperty(isApproved);
        }
    }

    // Получить все отзывы
    public static List<Review> getAllReviews() {
        List<Review> reviews = new ArrayList<>();

        // Временно используем тестовые данные, пока нет реальной БД
        if (!isDatabaseAvailable()) {
            return getTestReviews();
        }

        // Запрос без JOIN products — таблица может не существовать
        String sql = "SELECT r.id, " +
                "COALESCE(r.user_id, 0) as user_id, " +
                "COALESCE(r.product_id, 0) as product_id, " +
                "COALESCE(u.username, 'Аноним') as user_name, " +
                "COALESCE(CAST(r.product_id AS VARCHAR), 'Товар') as product_name, " +
                "COALESCE(r.rating, 5) as rating, " +
                "COALESCE(r.comment, '') as comment, " +
                "r.created_at, " +
                "COALESCE(r.is_approved, false) as is_approved " +
                "FROM reviews r " +
                "LEFT JOIN users u ON r.user_id = u.id " +
                "ORDER BY r.created_at DESC";

        try (Connection conn = com.example.diyavol.db.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Review review = new Review(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("product_id"),
                        rs.getString("user_name"),
                        rs.getString("product_name"),
                        rs.getInt("rating"),
                        rs.getString("comment"),
                        rs.getDate("created_at"),
                        rs.getBoolean("is_approved")
                );
                reviews.add(review);
            }

        } catch (SQLException e) {
            System.out.println("❌ Ошибка получения отзывов: " + e.getMessage());
            e.printStackTrace();
            // Возвращаем тестовые данные в случае ошибки
            return getTestReviews();
        }
        return reviews;
    }

    // Удалить отзыв
    public static boolean deleteReview(int reviewId) {
        // Если БД не доступна, просто возвращаем true для тестирования
        if (!isDatabaseAvailable()) {
            System.out.println("✅ Отзыв удален (тестовый режим): " + reviewId);
            return true;
        }

        String sql = "DELETE FROM reviews WHERE id = ?";

        try (Connection conn = com.example.diyavol.db.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, reviewId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.out.println("❌ Ошибка удаления отзыва: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Одобрить/заблокировать отзыв
    public static boolean toggleReviewApproval(int reviewId, boolean isApproved) {
        // Если БД не доступна, просто возвращаем true для тестирования
        if (!isDatabaseAvailable()) {
            System.out.println("✅ Статус отзыва изменен (тестовый режим): " + reviewId + " -> " + isApproved);
            return true;
        }

        String sql = "UPDATE reviews SET is_approved = ? WHERE id = ?"; // колонка добавлена через setup_admin.sql

        try (Connection conn = com.example.diyavol.db.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, isApproved);
            pstmt.setInt(2, reviewId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.out.println("❌ Ошибка изменения статуса отзыва: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Проверка доступности БД
    private static boolean isDatabaseAvailable() {
        try (Connection conn = com.example.diyavol.db.DatabaseConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                // Проверяем существование таблицы reviews
                DatabaseMetaData dbm = conn.getMetaData();
                ResultSet tables = dbm.getTables(null, null, "reviews", new String[]{"TABLE"});
                boolean tableExists = tables.next();
                tables.close();
                return tableExists;
            }
        } catch (SQLException e) {
            System.out.println("⚠️ База данных не доступна, используем тестовые данные: " + e.getMessage());
        }
        return false;
    }

    // Тестовые данные для отзывов
    private static List<Review> getTestReviews() {
        List<Review> testReviews = new ArrayList<>();

        testReviews.add(new Review(
                1, 1, 1, "Анна Петрова", "Умная лампа SmartLight Pro",
                5, "Отличная лампа! Очень удобное управление через приложение. Свет мягкий и приятный.",
                new Date(System.currentTimeMillis() - 86400000L), true
        ));

        testReviews.add(new Review(
                2, 2, 2, "Мария Иванова", "Шелковая пижама 'Комфорт'",
                4, "Качественный шелк, приятная к телу. Но размер действительно маловат, рекомендую брать на размер больше.",
                new Date(System.currentTimeMillis() - 172800000L), true
        ));

        testReviews.add(new Review(
                3, 3, 3, "Алексей Смирнов", "Аромалампа с таймером",
                3, "Нормальная лампа, дизайн симпатичный. Но таймер иногда сбоит, приходится перезагружать.",
                new Date(System.currentTimeMillis() - 259200000L), false
        ));

        testReviews.add(new Review(
                4, 4, 4, "Ольга Петрова", "Бамбуковый халат Premium",
                5, "Очень мягкий и приятный к телю! Отлично впитывает влагу. Рекомендую всем!",
                new Date(System.currentTimeMillis() - 345600000L), true
        ));

        testReviews.add(new Review(
                5, 5, 5, "Дмитрий Волков", "Умный будильник Sunrise",
                2, "Не понравился. Свет действительно слишком яркий утром, будит резко. Настройки не помогают.",
                new Date(System.currentTimeMillis() - 432000000L), false
        ));

        testReviews.add(new Review(
                6, 6, 6, "Екатерина Сидорова", "Плед 'Кашемировый уют'",
                5, "Невероятно мягкий и теплый плед! Идеально для холодных вечеров. Качество на высоте!",
                new Date(System.currentTimeMillis() - 518400000L), true
        ));

        return testReviews;
    }

    // Добавить новый отзыв
    public static boolean addReview(int userId, int productId, int rating, String comment) {
        // Валидация входных данных
        if (rating < 1 || rating > 5) {
            System.out.println("❌ Ошибка: рейтинг должен быть от 1 до 5");
            return false;
        }

        if (comment == null || comment.trim().isEmpty()) {
            System.out.println("❌ Ошибка: комментарий не может быть пустым");
            return false;
        }

        if (!isDatabaseAvailable()) {
            System.out.println("✅ Отзыв добавлен (тестовый режим)");
            return true;
        }

        String sql = "INSERT INTO reviews (user_id, product_id, rating, comment, created_at, is_approved) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, false)";

        try (Connection conn = com.example.diyavol.db.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, productId);
            pstmt.setInt(3, rating);
            pstmt.setString(4, comment.trim());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.out.println("❌ Ошибка добавления отзыва: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Получить отзывы по продукту
    public static List<Review> getReviewsByProduct(int productId) {
        List<Review> reviews = new ArrayList<>();

        if (!isDatabaseAvailable()) {
            // Фильтруем тестовые данные по productId
            for (Review review : getTestReviews()) {
                if (review.getProductId() == productId && review.isApproved()) {
                    reviews.add(review);
                }
            }
            return reviews;
        }

        String sql = "SELECT r.id, COALESCE(r.user_id,0) as user_id, COALESCE(r.product_id,0) as product_id, " +
                "COALESCE(u.username,'Аноним') as user_name, " +
                "COALESCE(CAST(r.product_id AS VARCHAR),'Товар') as product_name, " +
                "COALESCE(r.rating,5) as rating, COALESCE(r.comment,'') as comment, " +
                "r.created_at, COALESCE(r.is_approved, false) as is_approved " +
                "FROM reviews r " +
                "LEFT JOIN users u ON r.user_id = u.id " +
                "WHERE r.product_id = ? AND COALESCE(r.is_approved, false) = true " +
                "ORDER BY r.created_at DESC";

        try (Connection conn = com.example.diyavol.db.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Review review = new Review(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getInt("product_id"),
                            rs.getString("user_name"),
                            rs.getString("product_name"),
                            rs.getInt("rating"),
                            rs.getString("comment"),
                            rs.getDate("created_at"),
                            rs.getBoolean("is_approved")
                    );
                    reviews.add(review);
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ Ошибка получения отзывов по продукту: " + e.getMessage());
            e.printStackTrace();
        }
        return reviews;
    }

    // Получить средний рейтинг продукта
    public static double getAverageRating(int productId) {
        if (!isDatabaseAvailable()) {
            // Вычисляем средний рейтинг из тестовых данных
            List<Review> productReviews = getReviewsByProduct(productId);
            if (productReviews.isEmpty()) return 0.0;

            double sum = 0;
            for (Review review : productReviews) {
                sum += review.getRating();
            }
            return sum / productReviews.size();
        }

        String sql = "SELECT AVG(rating) as avg_rating FROM reviews WHERE product_id = ? AND COALESCE(is_approved, false) = true";

        try (Connection conn = com.example.diyavol.db.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("avg_rating");
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ Ошибка получения среднего рейтинга: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }

    // Получить количество отзывов по продукту
    public static int getReviewCount(int productId) {
        if (!isDatabaseAvailable()) {
            return (int) getTestReviews().stream()
                    .filter(review -> review.getProductId() == productId && review.isApproved())
                    .count();
        }

        String sql = "SELECT COUNT(*) as review_count FROM reviews WHERE product_id = ? AND COALESCE(is_approved, false) = true";

        try (Connection conn = com.example.diyavol.db.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("review_count");
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ Ошибка получения количества отзывов: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
}