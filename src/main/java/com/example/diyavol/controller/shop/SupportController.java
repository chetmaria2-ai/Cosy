package com.example.diyavol.controller.shop;

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
import javafx.scene.layout.VBox;

public class SupportController {

    @FXML private TextArea questionArea;
    @FXML private TextField emailField;
    @FXML private ChoiceBox<String> topicChoiceBox;
    @FXML private TextArea responseArea;
    @FXML private Button sendButton;
    @FXML private Button backButton;
    @FXML private VBox responseContainer;

    @FXML
    public void initialize() {
        // Заполняем выбор темы
        topicChoiceBox.getItems().addAll(
                "Вопрос по заказу",
                "Техническая поддержка",
                "Возврат товара",
                "Сотрудничество",
                "Жалоба",
                "Предложение",
                "Другое"
        );
        topicChoiceBox.setValue("Вопрос по заказу");

        // Скрываем область ответа до отправки
        responseContainer.setVisible(false);

        // Заполняем поле email примером
        emailField.setText("ваш-email@example.com");
        questionArea.setPromptText("Опишите ваш вопрос подробно...");
    }

    @FXML
    private void handleSendQuestion(ActionEvent event) {
        String question = questionArea.getText().trim();
        String topic = topicChoiceBox.getValue();
        String email = emailField.getText().trim();

        if (validateInput(question, topic, email)) {
            // Показываем область ответа
            responseContainer.setVisible(true);

            // Генерируем ответ
            String response = generateSupportResponse(topic, question);
            responseArea.setText(response);

            // Показываем подтверждение
            showAlert("Успех", "✅ Ваш вопрос отправлен! Мы ответим вам на email в течение 24 часов.");
        }
    }

    @FXML
    private void handleClearForm(ActionEvent event) {
        questionArea.clear();
        emailField.clear();
        topicChoiceBox.setValue("Вопрос по заказу");
        responseContainer.setVisible(false);
        showAlert("Форма очищена", "Все поля формы были очищены. Вы можете заполнить их заново.");
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
            showAlert("Ошибка", "❌ Не удалось вернуться в главное меню: " + e.getMessage());
        }
    }

    private boolean validateInput(String question, String topic, String email) {
        if (question == null || question.isEmpty()) {
            showAlert("Ошибка", "❌ Пожалуйста, опишите ваш вопрос");
            return false;
        }
        if (question.length() < 10) {
            showAlert("Ошибка", "❌ Описание вопроса должно содержать не менее 10 символов");
            return false;
        }
        if (topic == null || topic.isEmpty()) {
            showAlert("Ошибка", "❌ Выберите тему вопроса");
            return false;
        }
        if (email == null || email.isEmpty() || !email.contains("@")) {
            showAlert("Ошибка", "❌ Введите корректный email адрес");
            return false;
        }
        return true;
    }

    private String generateSupportResponse(String topic, String question) {
        String baseResponse = "✅ Ваш вопрос принят в обработку!\n\n" +
                "Тема: " + topic + "\n" +
                "Время обращения: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) + "\n" +
                "Номер обращения: #" + (1000 + (int)(Math.random() * 9000)) + "\n\n" +
                "Ваш вопрос:\n" + question + "\n\n" +
                "📋 Статус: Принято в работу\n" +
                "⏱️ Ожидаемое время ответа: 24 часа\n" +
                "📧 Ответ будет отправлен на указанный email\n\n" +
                "Благодарим за обращение! ❤️";

        // Добавляем специфические ответы в зависимости от темы
        switch (topic) {
            case "Вопрос по заказу":
                baseResponse += "\n\n💡 Для ускорения решения вопроса укажите номер вашего заказа.";
                break;
            case "Возврат товара":
                baseResponse += "\n\n💡 К письму приложите фото товара и чек для ускорения обработки возврата.";
                break;
            case "Техническая поддержка":
                baseResponse += "\n\n💡 Наш технический специалист свяжется с вами в приоритетном порядке.";
                break;
        }

        return baseResponse;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #F5F5DC; -fx-border-color: #8B0000; -fx-border-width: 2; -fx-border-radius: 10;");

        alert.showAndWait();
    }
}