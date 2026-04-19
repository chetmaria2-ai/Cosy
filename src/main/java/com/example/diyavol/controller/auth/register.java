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

public class register {

    @FXML private TextField nameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registerButton;
    @FXML private Button loginButton;
    @FXML private Button guestButton;

    @FXML
    private void handleRegister(ActionEvent event) {
        String name = nameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showAlert("Ошибка", "Заполните все поля");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showAlert("Ошибка", "Пароли не совпадают");
            return;
        }
        if (password.length() < 3) {
            showAlert("Ошибка", "Пароль должен быть минимум 3 символа");
            return;
        }

        boolean success = UserService.registerUser(name, username, password, username + "@cosyhome.ru");
        if (success) {
            System.out.println("✅ Регистрация: " + username);
            showAlert("Успех", "Регистрация прошла успешно!\nТеперь вы можете войти.");
            handleGoToLogin(event);
        } else {
            showAlert("Ошибка", "Не удалось зарегистрироваться. Попробуйте снова.");
        }
    }

    @FXML
    private void handleGoToLogin(ActionEvent event) {
        openWindow("/com/example/diyavol/fxml/auth/login.fxml", "CosyHome - Авторизация", 500, 620, event);
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
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
