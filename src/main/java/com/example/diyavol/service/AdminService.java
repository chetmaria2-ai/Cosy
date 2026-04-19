package com.example.diyavol.service;

import com.example.diyavol.db.DatabaseConnection;

import java.sql.*;
import java.util.*;

public class AdminService {

    public static class Admin {
        private int id;
        private int userId;
        private String username;
        private String role;
        private List<String> permissions;
        private Timestamp createdAt;

        public Admin(int id, int userId, String username, String role, List<String> permissions, Timestamp createdAt) {
            this.id = id;
            this.userId = userId;
            this.username = username;
            this.role = role;
            this.permissions = permissions;
            this.createdAt = createdAt;
        }

        // Геттеры
        public int getId() { return id; }
        public int getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getRole() { return role; }
        public List<String> getPermissions() { return permissions; }
        public Timestamp getCreatedAt() { return createdAt; }

        public boolean hasPermission(String permission) {
            return permissions.contains(permission) || permissions.contains("all");
        }
    }

    public static class AdminAction {
        private int id;
        private int adminId;
        private String adminName;
        private String actionType;
        private String targetType;
        private Integer targetId;
        private String description;
        private Timestamp createdAt;

        public AdminAction(int id, int adminId, String adminName, String actionType,
                           String targetType, Integer targetId, String description, Timestamp createdAt) {
            this.id = id;
            this.adminId = adminId;
            this.adminName = adminName;
            this.actionType = actionType;
            this.targetType = targetType;
            this.targetId = targetId;
            this.description = description;
            this.createdAt = createdAt;
        }

        // Геттеры
        public int getId() { return id; }
        public int getAdminId() { return adminId; }
        public String getAdminName() { return adminName; }
        public String getActionType() { return actionType; }
        public String getTargetType() { return targetType; }
        public Integer getTargetId() { return targetId; }
        public String getDescription() { return description; }
        public Timestamp getCreatedAt() { return createdAt; }
    }

    // Проверяем, является ли пользователь администратором
    public static boolean isUserAdmin(int userId) {
        String sql = "SELECT COUNT(*) FROM admins WHERE user_id = ?";

        try (Connection conn = com.example.diyavol.db.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            return rs.next() && rs.getInt(1) > 0;

        } catch (SQLException e) {
            System.out.println("❌ Ошибка проверки прав администратора: " + e.getMessage());
            return false;
        }
    }

    // Получаем информацию об администраторе
    public static Admin getAdminByUserId(int userId) {
        String sql = "SELECT a.id, a.user_id, u.username, a.role, a.permissions, a.created_at " +
                "FROM admins a " +
                "JOIN users u ON a.user_id = u.id " +
                "WHERE a.user_id = ?";

        try (Connection conn = com.example.diyavol.db.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Преобразуем массив PostgreSQL в List<String>
                Array permissionsArray = rs.getArray("permissions");
                List<String> permissions = new ArrayList<>();
                if (permissionsArray != null) {
                    String[] permArray = (String[]) permissionsArray.getArray();
                    permissions = Arrays.asList(permArray);
                }

                return new Admin(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        permissions,
                        rs.getTimestamp("created_at")
                );
            }

        } catch (SQLException e) {
            System.out.println("❌ Ошибка получения данных администратора: " + e.getMessage());
        }
        return null;
    }

    // Делаем пользователя администратором
    public static boolean makeUserAdmin(int userId, String role, List<String> permissions) {
        String sql = "INSERT INTO admins (user_id, role, permissions) VALUES (?, ?, ?) " +
                "ON CONFLICT (user_id) DO UPDATE SET role = EXCLUDED.role, permissions = EXCLUDED.permissions, updated_at = CURRENT_TIMESTAMP";

        try (Connection conn = com.example.diyavol.db.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, role);

            // Преобразуем List в массив для PostgreSQL
            Array permissionsArray = conn.createArrayOf("VARCHAR", permissions.toArray());
            pstmt.setArray(3, permissionsArray);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Логируем действие
                logAdminAction(userId, "make_admin", "user", userId,
                        "Назначен администратором с ролью: " + role);
                return true;
            }

        } catch (SQLException e) {
            System.out.println("❌ Ошибка назначения администратора: " + e.getMessage());
        }
        return false;
    }

    // Убираем права администратора
    public static boolean removeAdmin(int userId) {
        String sql = "DELETE FROM admins WHERE user_id = ?";

        try (Connection conn = com.example.diyavol.db.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Логируем действие
                logAdminAction(userId, "remove_admin", "user", userId,
                        "Удален из администраторов");
                return true;
            }

        } catch (SQLException e) {
            System.out.println("❌ Ошибка удаления администратора: " + e.getMessage());
        }
        return false;
    }

    // Получаем всех администраторов
    public static List<Admin> getAllAdmins() {
        List<Admin> admins = new ArrayList<>();
        String sql = "SELECT a.id, a.user_id, u.username, a.role, a.permissions, a.created_at " +
                "FROM admins a " +
                "JOIN users u ON a.user_id = u.id " +
                "ORDER BY a.created_at DESC";

        try (Connection conn = com.example.diyavol.db.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Array permissionsArray = rs.getArray("permissions");
                List<String> permissions = new ArrayList<>();
                if (permissionsArray != null) {
                    String[] permArray = (String[]) permissionsArray.getArray();
                    permissions = Arrays.asList(permArray);
                }

                admins.add(new Admin(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        permissions,
                        rs.getTimestamp("created_at")
                ));
            }

        } catch (SQLException e) {
            System.out.println("❌ Ошибка получения списка администраторов: " + e.getMessage());
        }
        return admins;
    }

    // Логирование действий администратора
    public static void logAdminAction(int adminId, String actionType, String targetType,
                                      Integer targetId, String description) {
        String sql = "INSERT INTO admin_actions (admin_id, action_type, target_type, target_id, description) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = com.example.diyavol.db.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, adminId);
            pstmt.setString(2, actionType);
            pstmt.setString(3, targetType);
            if (targetId != null) {
                pstmt.setInt(4, targetId);
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            pstmt.setString(5, description);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("❌ Ошибка логирования действия администратора: " + e.getMessage());
        }
    }

    // Получаем историю действий администратора
    public static List<AdminAction> getAdminActions(int limit) {
        List<AdminAction> actions = new ArrayList<>();
        String sql = "SELECT aa.id, aa.admin_id, u.username as admin_name, aa.action_type, " +
                "aa.target_type, aa.target_id, aa.description, aa.created_at " +
                "FROM admin_actions aa " +
                "JOIN admins a ON aa.admin_id = a.id " +
                "JOIN users u ON a.user_id = u.id " +
                "ORDER BY aa.created_at DESC " +
                "LIMIT ?";

        try (Connection conn = com.example.diyavol.db.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                actions.add(new AdminAction(
                        rs.getInt("id"),
                        rs.getInt("admin_id"),
                        rs.getString("admin_name"),
                        rs.getString("action_type"),
                        rs.getString("target_type"),
                        rs.getInt("target_id"),
                        rs.getString("description"),
                        rs.getTimestamp("created_at")
                ));
            }

        } catch (SQLException e) {
            System.out.println("❌ Ошибка получения истории действий: " + e.getMessage());
        }
        return actions;
    }

    // Получаем базовые разрешения для роли
    public static List<String> getDefaultPermissions(String role) {
        switch (role.toLowerCase()) {
            case "super_admin":
                return Arrays.asList("all");
            case "content_manager":
                return Arrays.asList("manage_products", "manage_reviews", "view_users");
            case "user_manager":
                return Arrays.asList("manage_users", "view_products", "view_reviews");
            case "moderator":
                return Arrays.asList("manage_reviews", "view_users", "view_products");
            default:
                return Arrays.asList("view_users", "view_products", "view_reviews");
        }
    }
}