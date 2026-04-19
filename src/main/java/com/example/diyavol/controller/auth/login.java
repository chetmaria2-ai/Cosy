package com.example.diyavol.controller.auth;

import com.example.diyavol.db.DatabaseConnection;
import com.example.diyavol.service.*;
import com.example.diyavol.util.SharedCart;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;

public class login {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Button forgotPasswordButton;
    @FXML private Button guestButton;

    private static UserService.User currentUser;

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Ошибка", "Заполните все поля");
            return;
        }

        UserService.User user = UserService.loginUser(username, password);

        if (user != null) {
            currentUser = user;
            if (user.isAdmin()) {
                showAlert("Успех", "Добро пожаловать, Администратор!");
                openWindow("/com/example/diyavol/fxml/admin/admin-panel.fxml", "CosyHome - Панель администратора", 1200, 800, event);
            } else {
                showAlert("Успех", "Добро пожаловать, " + username + "!");
                openWindow("/com/example/diyavol/fxml/menu/MainMenu.fxml", "CosyHome - Главное меню", 1000, 700, event);
            }
        } else {
            showAlert("Ошибка", "Неверный логин или пароль.\nПароль должен быть минимум 3 символа.");
        }
    }

    @FXML
    private void handleGoToRegister(ActionEvent event) {
        openWindow("/com/example/diyavol/fxml/auth/register.fxml", "CosyHome - Регистрация", 500, 650, event);
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        showAlert("Восстановление пароля", "Функция в разработке");
    }

    @FXML
    private void handleGuestMode(ActionEvent event) {
        openWindow("/com/example/diyavol/fxml/menu/guest-menu.fxml", "CosyHome - Гостевой режим", 1000, 700, event);
    }

    private void openWindow(String fxmlFile, String title, int w, int h, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root, w, h));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось открыть окно: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static UserService.User getCurrentUser() {
        return currentUser;
    }

    public static boolean isAdminLoggedIn() {
        return currentUser != null && currentUser.isAdmin();
    }
}
