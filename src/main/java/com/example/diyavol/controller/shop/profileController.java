package com.example.diyavol.controller.shop;

import com.example.diyavol.controller.auth.login;
import com.example.diyavol.db.DatabaseConnection;
import com.example.diyavol.service.*;
import com.example.diyavol.util.SharedCart;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;

public class profileController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private ListView<String> orderHistory;
    @FXML private ListView<String> favoritesList;
    @FXML private ListView<String> promotionsList;
    @FXML private Label loyaltyPoints;
    @FXML private Label usernameLabel;
    @FXML private Label loyaltyLevel;
    @FXML private ProgressBar loyaltyProgress;
    @FXML private Button saveButton;
    @FXML private Button backButton;
    @FXML private Button logoutButton;

    private int currentPoints = 150;
    private final int[] LEVEL_THRESHOLDS = {100, 300, 500, 1000};
    private final String[] LEVEL_NAMES = {"Новичок", "Постоянный клиент", "VIP клиент", "Премиум"};

    @FXML
    public void initialize() {
        // Загружаем данные текущего пользователя
        UserService.User currentUser = login.getCurrentUser();
        if (currentUser != null) {
            usernameLabel.setText(currentUser.getFullName() + " (@" + currentUser.getUsername() + ")");
            nameField.setText(currentUser.getFullName());
            emailField.setText(currentUser.getEmail());
            phoneField.setText(""); // телефон пока не хранится
            currentPoints = currentUser.getLoyaltyPoints();
            System.out.println("✅ Профиль загружен для: " + currentUser.getUsername());
        } else {
            usernameLabel.setText("Гость");
            nameField.setText("");
            emailField.setText("");
            phoneField.setText("");
        }

        // Инициализируем систему лояльности
        initializeLoyaltySystem();

        // Заполняем историю заказов
        orderHistory.setItems(FXCollections.observableArrayList(
                "🛍️ Заказ #001 от 15.01.2024 - 5 998 руб\n   Умная лампа SmartLight Pro x2",
                "🛍️ Заказ #002 от 20.01.2024 - 9 198 руб\n   Шелковая пижама 'Комфорт', Аромалампа",
                "🛍️ Заказ #003 от 25.01.2024 - 3 999 руб\n   Плед 'Кашемировый уют'",
                "🛍️ Заказ #004 от 02.02.2024 - 7 598 руб\n   Бамбуковый халат Premium",
                "🛍️ Заказ #005 от 10.02.2024 - 4 299 руб\n   Набор полотенец 'Бамбук'"
        ));

        // Заполняем избранное
        favoritesList.setItems(FXCollections.observableArrayList(
                "❤️ Бамбуковый халат Premium",
                "❤️ Аромалампа с таймером",
                "❤️ Комплект постельного белья 'Нежность'",
                "❤️ Органайзер для косметики",
                "❤️ Умная лампа SmartLight Pro",
                "❤️ Плед 'Кашемировый уют'"
        ));

        // Заполняем акции и специальные предложения
        promotionsList.setItems(FXCollections.observableArrayList(
                "🎁 Скидка 15% на первую покупку",
                "⭐ +50 баллов за отзыв о товаре",
                "🚚 Бесплатная доставка от 3000 руб",
                "💎 Скидка 20% в день рождения",
                "🎯 2x баллов за покупки в выходные",
                "📦 -10% на все товары категории 'Текстиль'",
                "🌟 Подарок при заказе от 5000 руб",
                "🔥 Скидка 25% на все умные устройства"
        ));

        // Настраиваем внешний вид ListView
        setupListViews();
    }

    private void initializeLoyaltySystem() {
        updateLoyaltyDisplay();
    }

    private void updateLoyaltyDisplay() {
        loyaltyPoints.setText("🌟 " + currentPoints + " баллов");

        // Определяем текущий уровень
        int currentLevel = 0;
        for (int i = 0; i < LEVEL_THRESHOLDS.length; i++) {
            if (currentPoints >= LEVEL_THRESHOLDS[i]) {
                currentLevel = i;
            }
        }

        loyaltyLevel.setText("Уровень: " + LEVEL_NAMES[currentLevel]);

        // Обновляем прогресс-бар
        if (currentLevel < LEVEL_THRESHOLDS.length - 1) {
            int nextLevelThreshold = LEVEL_THRESHOLDS[currentLevel + 1];
            int currentLevelThreshold = currentLevel == 0 ? 0 : LEVEL_THRESHOLDS[currentLevel];
            double progress = (double)(currentPoints - currentLevelThreshold) /
                    (nextLevelThreshold - currentLevelThreshold);
            loyaltyProgress.setProgress(progress);
        } else {
            loyaltyProgress.setProgress(1.0);
        }
    }

    private void setupListViews() {
        // Стили для истории заказов
        orderHistory.setStyle("-fx-background-color: rgba(245,245,220,0.9); -fx-background-radius: 10; -fx-border-radius: 10;");

        // Стили для избранного
        favoritesList.setStyle("-fx-background-color: rgba(245,245,220,0.9); -fx-background-radius: 10; -fx-border-radius: 10;");

        // Стили для акций
        promotionsList.setStyle("-fx-background-color: rgba(245,245,220,0.9); -fx-background-radius: 10; -fx-border-radius: 10;");
    }

    @FXML
    private void handleSaveProfile(ActionEvent event) {
        if (validateFields()) {
            showAlert("Успех", "✅ Профиль успешно сохранен!");
            // Здесь можно добавить логику сохранения в БД
        }
    }

    @FXML
    private void handleViewOrder(ActionEvent event) {
        String selectedOrder = orderHistory.getSelectionModel().getSelectedItem();
        if (selectedOrder != null) {
            showAlert("Детали заказа", "📦 " + selectedOrder + "\n\n🚚 Статус: Доставлен\n💳 Способ оплаты: Карта\n🏠 Адрес доставки: ул. Примерная, д. 123");
        } else {
            showAlert("Ошибка", "❌ Выберите заказ для просмотра");
        }
    }

    @FXML
    private void handleRemoveFavorite(ActionEvent event) {
        String selectedFavorite = favoritesList.getSelectionModel().getSelectedItem();
        if (selectedFavorite != null) {
            favoritesList.getItems().remove(selectedFavorite);
            showAlert("Избранное", "🗑️ Товар удален из избранного: " + selectedFavorite);
        } else {
            showAlert("Ошибка", "❌ Выберите товар для удаления из избранного");
        }
    }

    @FXML
    private void handleActivatePromotion(ActionEvent event) {
        String selectedPromotion = promotionsList.getSelectionModel().getSelectedItem();
        if (selectedPromotion != null) {
            // Добавляем баллы за активацию акции
            currentPoints += 10;
            updateLoyaltyDisplay();

            showAlert("Акция активирована", "✅ " + selectedPromotion + "\n\n+10 баллов за участие в акции!");
        } else {
            showAlert("Ошибка", "❌ Выберите акцию для активации");
        }
    }

    @FXML
    private void handleBackToMain(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/diyavol/fxml/menu/MainMenu.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("CosyHome - Главное меню");
            stage.setScene(new Scene(root, 1000, 700));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private boolean validateFields() {
        if (nameField.getText().isEmpty()) {
            showAlert("Ошибка", "❌ Введите имя");
            return false;
        }
        if (emailField.getText().isEmpty() || !emailField.getText().contains("@")) {
            showAlert("Ошибка", "❌ Введите корректный email");
            return false;
        }
        if (phoneField.getText().isEmpty()) {
            showAlert("Ошибка", "❌ Введите телефон");
            return false;
        }
        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}