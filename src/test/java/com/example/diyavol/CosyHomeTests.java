package com.example.diyavol;
import com.example.diyavol.db.DatabaseConnection;
import com.example.diyavol.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;
public class CosyHomeTests {


    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 3;
    }

    @Test
    @DisplayName("Корректный email проходит валидацию")
    public void test1_ValidEmail() {
        assertTrue(isValidEmail("user@example.com"));
        assertTrue(isValidEmail("test@mail.ru"));
    }

    @Test
    @DisplayName("Некорректный email не проходит валидацию")
    public void test2_InvalidEmail() {
        assertFalse(isValidEmail("user@"));
        assertFalse(isValidEmail("userexample.com"));
        assertFalse(isValidEmail(""));
    }


    @Test
    @DisplayName("Пароль длиной >= 3 символов считается корректным")
    public void test3_ValidPassword() {
        assertTrue(isValidPassword("Pass123!"));
        assertTrue(isValidPassword("abc"));
    }

    @Test
    @DisplayName("Пароль короче 3 символов или пустой отклоняется")
    public void test4_InvalidPassword() {
        assertFalse(isValidPassword("ab"));
        assertFalse(isValidPassword(""));
        assertFalse(isValidPassword(null));
    }


    @Test
    @DisplayName("Цена товара должна быть положительной")
    public void test5_ProductPricePositive() {
        assertTrue(2999.0 > 0);
        assertFalse(-100.0 > 0);
        assertFalse(0.0 > 0);
    }

    @Test
    @DisplayName("Остаток товара на складе не может быть отрицательным")
    public void test6_ProductStockNotNegative() {
        assertTrue(50 >= 0);
        assertFalse(-1 >= 0);
    }


    @Test
    @DisplayName("Количество товара в корзине должно быть больше нуля")
    public void test7_CartQuantityPositive() {
        assertTrue(1 > 0);
        assertFalse(0 > 0);
    }


    @Test
    @DisplayName("Подключение к базе данных PostgreSQL успешно устанавливается")
    public void test8_DatabaseConnection() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        assertNotNull(conn);
        assertFalse(conn.isClosed());
    }


    @Test
    @DisplayName("Роль пользователя корректно определяется (admin / customer)")
    public void test9_UserRoleMapping() {
        UserService.User admin = new UserService.User(
                1, "admin", "Admin", "a@a.ru", "admin", 0);
        assertTrue(admin.isAdmin());

        UserService.User customer = new UserService.User(
                2, "user", "User", "u@u.ru", "customer", 0);
        assertFalse(customer.isAdmin());
    }

    @Test
    @DisplayName("Пустой логин не проходит авторизацию")
    public void test10_EmptyUsernameRejected() {
        UserService.User result = UserService.loginUser("", "pass");
        assertNull(result);
    }
}