package com.example.diyavol.controller.shop;

import com.example.diyavol.db.DatabaseConnection;
import com.example.diyavol.service.*;
import com.example.diyavol.util.SharedCart;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

public class CartController {

    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> productColumn;
    @FXML private TableColumn<CartItem, Double> priceColumn;
    @FXML private TableColumn<CartItem, Integer> quantityColumn;
    @FXML private Label totalLabel;
    @FXML private Button checkoutButton;
    @FXML private Button continueShoppingButton;
    @FXML private Button removeButton;
    @FXML private Button backButton;

    private ObservableList<CartItem> cartItems;
    private String savedCardNumber = ""; // Сохраненные данные карты

    @FXML
    public void initialize() {
        // Инициализация таблицы
        productColumn.setCellValueFactory(cellData -> cellData.getValue().productNameProperty());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());
        quantityColumn.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());

        // Загружаем корзину из БД (если пользователь авторизован)
        SharedCart.loadFromDatabase();

        // Привязываем список к таблице
        cartItems = SharedCart.getItems();
        cartTable.setItems(cartItems);
        updateTotal();

        // Обновляем итог при изменениях в корзине
        cartItems.addListener((javafx.collections.ListChangeListener<CartItem>) c -> updateTotal());
    }

    @FXML
    private void handleCheckout(ActionEvent event) {
        if (cartItems.isEmpty()) {
            showAlert("Корзина пуста", "Добавьте товары в корзину перед оформлением заказа");
            return;
        }

        // создание диалога выбора способа оплаты
        ChoiceDialog<String> paymentDialog = new ChoiceDialog<>("Онлайн картой",
                FXCollections.observableArrayList("Онлайн картой", "При получении"));

        paymentDialog.setTitle("Способ оплаты");
        paymentDialog.setHeaderText("Выберите способ оплаты");
        paymentDialog.setContentText("Способ оплаты:");

        // Вывод диалога
        paymentDialog.showAndWait().ifPresent(paymentMethod -> {
            showCardPaymentDialog(paymentMethod);
        });
    }

    private void showCardPaymentDialog(String paymentMethod) {
        // Создаем кастомный диалог для ввода данных карты
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Резервирование средств");
        dialog.setHeaderText("Введите данные банковской карты для резервирования средств\nСпособ оплаты: " + paymentMethod);

        // Устанавливаем кнопки
        String confirmButtonText = paymentMethod.equals("Онлайн картой") ? "Оплатить" : "Забронировать";
        ButtonType confirmButtonType = new ButtonType(confirmButtonText, ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        // Создаем поля для ввода
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField cardNumberField = new TextField();
        cardNumberField.setPromptText("1234 5678 9012 3456");
        cardNumberField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Форматирование номера карты (добавляем пробелы каждые 4 цифры)
            if (!newValue.matches("\\d*")) {
                cardNumberField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.length() > 16) {
                cardNumberField.setText(oldValue);
            }
            // Добавляем пробелы каждые 4 цифры
            String formatted = formatCardNumber(newValue.replaceAll(" ", ""));
            if (!formatted.equals(newValue)) {
                cardNumberField.setText(formatted);
            }
        });

        TextField expiryField = new TextField();
        expiryField.setPromptText("ММ/ГГ");
        expiryField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                expiryField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.length() == 2 && oldValue.length() == 1) {
                expiryField.setText(newValue + "/");
            }
            if (newValue.length() > 5) {
                expiryField.setText(oldValue);
            }
        });

        TextField cvvField = new TextField();
        cvvField.setPromptText("CVV");
        cvvField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                cvvField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.length() > 3) {
                cvvField.setText(oldValue);
            }
        });

        TextField cardHolderField = new TextField();
        cardHolderField.setPromptText("IVAN IVANOV");

        // Информация о способе оплаты
        Label paymentInfoLabel = new Label();
        if (paymentMethod.equals("Онлайн картой")) {
            paymentInfoLabel.setText("💳 Будет выполнено полное списание средств");
            paymentInfoLabel.setStyle("-fx-text-fill: #8B0000; -fx-font-weight: bold;");
        } else {
            paymentInfoLabel.setText("💳 Будет забронирована сумма для оплаты при получении");
            paymentInfoLabel.setStyle("-fx-text-fill: #2E8B57; -fx-font-weight: bold;");
        }

        // Если есть сохраненная карта, показываем ее
        if (!savedCardNumber.isEmpty()) {
            Button useSavedCardButton = new Button("Использовать сохраненную карту");
            useSavedCardButton.setOnAction(e -> {
                cardNumberField.setText(savedCardNumber);
                dialog.setResult(new Pair<>(savedCardNumber, "saved"));
                dialog.close();
            });
            grid.add(new Label("Сохраненная карта:"), 0, 0);
            grid.add(useSavedCardButton, 1, 0);
        }

        grid.add(paymentInfoLabel, 0, 1, 2, 1);
        grid.add(new Label("Номер карты:"), 0, 2);
        grid.add(cardNumberField, 1, 2);
        grid.add(new Label("Срок действия:"), 0, 3);
        grid.add(expiryField, 1, 3);
        grid.add(new Label("CVV:"), 0, 4);
        grid.add(cvvField, 1, 4);
        grid.add(new Label("Владелец карты:"), 0, 5);
        grid.add(cardHolderField, 1, 5);

        CheckBox saveCardCheckbox = new CheckBox("Сохранить карту для будущих покупок");
        grid.add(saveCardCheckbox, 1, 6);

        dialog.getDialogPane().setContent(grid);

        // Преобразуем результат
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                // Валидация данных
                if (cardNumberField.getText().replaceAll(" ", "").length() != 16) {
                    showAlert("Ошибка", "Номер карты должен содержать 16 цифр");
                    return null;
                }
                if (expiryField.getText().length() != 5 || !expiryField.getText().contains("/")) {
                    showAlert("Ошибка", "Неверный формат срока действия (ММ/ГГ)");
                    return null;
                }
                if (cvvField.getText().length() != 3) {
                    showAlert("Ошибка", "CVV должен содержать 3 цифры");
                    return null;
                }
                if (cardHolderField.getText().isEmpty()) {
                    showAlert("Ошибка", "Введите имя владельца карты");
                    return null;
                }

                // Сохраняем карту если выбрано
                if (saveCardCheckbox.isSelected()) {
                    savedCardNumber = cardNumberField.getText();
                    System.out.println("💳 Карта сохранена: " + maskCardNumber(savedCardNumber));
                }

                return new Pair<>(cardNumberField.getText(), cardHolderField.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(cardData -> {
            processOrder(paymentMethod, cardData.getKey());
        });
    }

    private String formatCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) return "";
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < cardNumber.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                formatted.append(" ");
            }
            formatted.append(cardNumber.charAt(i));
        }
        return formatted.toString();
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) return "****";
        String cleaned = cardNumber.replaceAll(" ", "");
        return "**** **** **** " + cleaned.substring(cleaned.length() - 4);
    }

    private void processOrder(String paymentMethod, String cardNumber) {
        double total = cartItems.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();

        String paymentInfo = "";
        if (paymentMethod.equals("Онлайн картой")) {
            String maskedCard = cardNumber != null ? maskCardNumber(cardNumber) : "****";
            paymentInfo = "\n💳 Способ оплаты: Онлайн картой" +
                    "\n📋 Карта: " + maskedCard +
                    "\n⏰ Статус: Оплачено ✅" +
                    "\n💰 Сумма списания: " + String.format("%.2f", total) + " руб" +
                    "\n📧 Чек отправлен на email";
        } else {
            String maskedCard = cardNumber != null ? maskCardNumber(cardNumber) : "****";
            paymentInfo = "\n💳 Способ оплаты: При получении" +
                    "\n📋 Карта для бронирования: " + maskedCard +
                    "\n⏰ Статус: Сумма забронирована ⚠️" +
                    "\n💰 Забронировано: " + String.format("%.2f", total) + " руб" +
                    "\n💵 К оплате при получении: " + String.format("%.2f", total) + " руб" +
                    "\nℹ️  Средства будут разблокированы после получения заказа";
        }

        String orderDetails = "🎉 Заказ успешно оформлен!\n\n" +
                "📦 Состав заказа:\n";

        for (CartItem item : cartItems) {
            orderDetails += "• " + item.getProductName() + " - " + item.getQuantity() + " шт. x " +
                    String.format("%.2f", item.getPrice()) + " руб.\n";
        }

        orderDetails += "\n💰 Общая сумма: " + String.format("%.2f", total) + " руб" +
                paymentInfo +
                "\n\n🚚 Доставка: 1-3 рабочих дня" +
                "\n📞 Номер заказа: #" + (1000 + (int)(Math.random() * 9000)) +
                "\n\n✅ Вы получите SMS с подтверждением заказа";

        // Показываем детали заказа
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Оформление заказа");
        alert.setHeaderText("Заказ принят!");
        alert.setContentText(orderDetails);

        // Стилизуем алерт
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #F5F5DC; -fx-border-color: #8B0000; -fx-border-width: 2;");

        alert.showAndWait();

        // Очищаем корзину после оформления
        SharedCart.clear();
        updateTotal();
    }

    @FXML
    private void handleContinueShopping(ActionEvent event) {
        openMainMenu(event);
    }

    @FXML
    private void handleRemoveItem(ActionEvent event) {
        CartItem selectedItem = cartTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            SharedCart.removeItem(selectedItem);
            updateTotal();
            showAlert("Удаление", "Товар удалён из корзины");
        } else {
            showAlert("Ошибка", "Выберите товар для удаления");
        }
    }

    @FXML
    private void handleBackToMain(ActionEvent event) {
        openMainMenu(event);
    }

    private void openMainMenu(ActionEvent event) {
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

    private void updateTotal() {
        double total = cartItems.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();
        totalLabel.setText(String.format("Итого: %.2f руб", total));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #F5F5DC; -fx-border-color: #8B0000; -fx-border-width: 2;");

        alert.showAndWait();
    }

    public static class CartItem {
        private final javafx.beans.property.SimpleStringProperty productName;
        private final javafx.beans.property.SimpleDoubleProperty price;
        private final javafx.beans.property.SimpleIntegerProperty quantity;
        private final int productId;

        public CartItem(String productName, double price, int quantity) {
            this(productName, price, quantity, -1);
        }

        public CartItem(String productName, double price, int quantity, int productId) {
            this.productName = new javafx.beans.property.SimpleStringProperty(productName);
            this.price = new javafx.beans.property.SimpleDoubleProperty(price);
            this.quantity = new javafx.beans.property.SimpleIntegerProperty(quantity);
            this.productId = productId;
        }

        public String getProductName() { return productName.get(); }
        public double getPrice() { return price.get(); }
        public int getQuantity() { return quantity.get(); }
        public int getProductId() { return productId; }

        public void incrementQuantity() { this.quantity.set(this.quantity.get() + 1); }
        public void decrementQuantity() { if (this.quantity.get() > 1) this.quantity.set(this.quantity.get() - 1); }

        public javafx.beans.property.SimpleStringProperty productNameProperty() {
            return productName;
        }

        public javafx.beans.property.SimpleDoubleProperty priceProperty() {
            return price;
        }

        public javafx.beans.property.SimpleIntegerProperty quantityProperty() {
            return quantity;
        }
    }
}