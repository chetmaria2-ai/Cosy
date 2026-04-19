package com.example.diyavol.controller.shop;

import com.example.diyavol.db.DatabaseConnection;
import com.example.diyavol.service.*;
import com.example.diyavol.util.SharedCart;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;

public class GuestController {

    @FXML
    public void initialize() {
        System.out.println("👤 Гостевой режим активен");
    }

    @FXML
    private void openCatalog(ActionEvent event) {
        openWindow("/com/example/diyavol/fxml/shop/catalog.fxml", "CosyHome - Каталог товаров", 1000, 700, event);
    }

    @FXML
    private void openSupport(ActionEvent event) {
        openWindow("/com/example/diyavol/fxml/shop/support.fxml", "CosyHome - Поддержка", 700, 600, event);
    }

    @FXML
    private void goToLogin(ActionEvent event) {
        openWindow("/com/example/diyavol/fxml/auth/login.fxml", "CosyHome - Авторизация", 500, 600, event);
    }

    @FXML
    private void goToRegister(ActionEvent event) {
        openWindow("/com/example/diyavol/fxml/auth/register.fxml", "CosyHome - Регистрация", 500, 650, event);
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
}
