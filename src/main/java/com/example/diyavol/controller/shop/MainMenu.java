package com.example.diyavol.controller.shop;

import com.example.diyavol.controller.auth.login;
import com.example.diyavol.db.DatabaseConnection;
import com.example.diyavol.service.*;
import com.example.diyavol.util.SharedCart;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;

public class MainMenu {

    @FXML private Label title;
    @FXML private Label userGreeting;
    @FXML private Button catalogBtn;
    @FXML private Button cartBtn;
    @FXML private Button profileBtn;
    @FXML private Button supportBtn;
    @FXML private Button logoutBtn;

    @FXML
    public void initialize() {
        // Показываем приветствие с именем текущего пользователя
        UserService.User user = login.getCurrentUser();
        if (user != null && userGreeting != null) {
            userGreeting.setText("Добро пожаловать, " + user.getUsername() + "!");
        }
    }

    private void openWindow(String fxmlFile, String windowTitle, int width, int height, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(windowTitle);
            stage.setScene(new Scene(root, width, height));
            stage.show();
        } catch (Exception e) {
            System.out.println("❌ Ошибка открытия " + windowTitle + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void openCatalog(ActionEvent event) {
        openWindow("/com/example/diyavol/fxml/shop/catalog.fxml", "CosyHome - Каталог товаров", 1000, 700, event);
    }

    @FXML
    private void openCart(ActionEvent event) {
        openWindow("/com/example/diyavol/fxml/shop/cart.fxml", "CosyHome - Корзина", 900, 600, event);
    }

    @FXML
    private void openProfile(ActionEvent event) {
        openWindow("/com/example/diyavol/fxml/shop/profile.fxml", "CosyHome - Профиль", 800, 700, event);
    }

    @FXML
    private void openSupport(ActionEvent event) {
        openWindow("/com/example/diyavol/fxml/shop/support.fxml", "CosyHome - Поддержка", 700, 600, event);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/diyavol/fxml/auth/register.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("CosyHome - Регистрация");
            stage.setScene(new Scene(root, 500, 650));
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
