package com.example.diyavol.controller.admin;

import com.example.diyavol.controller.auth.login;
import com.example.diyavol.db.DatabaseConnection;
import com.example.diyavol.service.*;
import com.example.diyavol.util.SharedCart;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;

import java.util.List;
import java.util.Optional;

public class AdminPanelController {

    @FXML private TableView<UserService.User> usersTable;
    @FXML private TableColumn<UserService.User, String> userIdColumn;
    @FXML private TableColumn<UserService.User, String> userNameColumn;
    @FXML private TableColumn<UserService.User, String> userEmailColumn;
    @FXML private TableColumn<UserService.User, Boolean> userAdminColumn;

    @FXML private TableView<ReviewService.Review> reviewsTable;
    @FXML private TableColumn<ReviewService.Review, String> reviewIdColumn;
    @FXML private TableColumn<ReviewService.Review, String> reviewUserColumn;
    @FXML private TableColumn<ReviewService.Review, String> reviewProductColumn;
    @FXML private TableColumn<ReviewService.Review, Integer> reviewRatingColumn;
    @FXML private TableColumn<ReviewService.Review, String> reviewCommentColumn;
    @FXML private TableColumn<ReviewService.Review, Boolean> reviewApprovedColumn;

    @FXML private TableView<ProductService.Product> productsTable;
    @FXML private TableColumn<ProductService.Product, Integer> productIdColumn;
    @FXML private TableColumn<ProductService.Product, String> productNameColumn;
    @FXML private TableColumn<ProductService.Product, String> productCategoryColumn;
    @FXML private TableColumn<ProductService.Product, Double> productPriceColumn;
    @FXML private TableColumn<ProductService.Product, Integer> productStockColumn;
    @FXML private TableColumn<ProductService.Product, String> productImageColumn;

    @FXML private TabPane adminTabs;
    @FXML private Button makeAdminButton;
    @FXML private Button removeAdminButton;
    @FXML private Button deleteReviewButton;
    @FXML private Button toggleReviewButton;
    @FXML private Button addProductButton;
    @FXML private Button editProductButton;
    @FXML private Button deleteProductButton;
    @FXML private Button backButton;

    private ObservableList<UserService.User> usersList = FXCollections.observableArrayList();
    private ObservableList<ReviewService.Review> reviewsList = FXCollections.observableArrayList();
    private ObservableList<ProductService.Product> productsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (!login.isAdminLoggedIn()) {
            showAlert("Ошибка доступа", "У вас нет прав администратора");
            return;
        }
        initializeUsersTable();
        initializeReviewsTable();
        initializeProductsTable();
        loadUsers();
        loadReviews();
        loadProducts();
    }


    private void initializeUsersTable() {
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        userEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        userAdminColumn.setCellValueFactory(new PropertyValueFactory<>("admin"));
        usersTable.setItems(usersList);
    }

    private void loadUsers() {
        usersList.clear();
        usersList.addAll(UserService.getAllUsers());
    }

    @FXML
    private void handleMakeAdmin(ActionEvent event) {
        UserService.User u = usersTable.getSelectionModel().getSelectedItem();
        if (u == null) { showAlert("Ошибка", "Выберите пользователя"); return; }
        if (UserService.makeAdmin(u.getId(), true)) {
            showAlert("Успех", u.getUsername() + " теперь администратор");
            loadUsers();
        } else showAlert("Ошибка", "Не удалось назначить администратора");
    }

    @FXML
    private void handleRemoveAdmin(ActionEvent event) {
        UserService.User u = usersTable.getSelectionModel().getSelectedItem();
        if (u == null) { showAlert("Ошибка", "Выберите пользователя"); return; }
        if (u.getId() == login.getCurrentUser().getId()) {
            showAlert("Ошибка", "Нельзя снять права с самого себя"); return;
        }
        if (UserService.makeAdmin(u.getId(), false)) {
            showAlert("Успех", u.getUsername() + " больше не администратор");
            loadUsers();
        } else showAlert("Ошибка", "Не удалось снять права");
    }


    private void initializeReviewsTable() {
        reviewIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        reviewUserColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        reviewProductColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        reviewRatingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        reviewCommentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
        reviewApprovedColumn.setCellValueFactory(new PropertyValueFactory<>("approved"));
        reviewsTable.setItems(reviewsList);
    }

    private void loadReviews() {
        reviewsList.clear();
        reviewsList.addAll(ReviewService.getAllReviews());
    }

    @FXML
    private void handleDeleteReview(ActionEvent event) {
        ReviewService.Review r = reviewsTable.getSelectionModel().getSelectedItem();
        if (r == null) { showAlert("Ошибка", "Выберите отзыв"); return; }
        if (ReviewService.deleteReview(r.getId())) {
            showAlert("Успех", "Отзыв удалён");
            loadReviews();
        } else showAlert("Ошибка", "Не удалось удалить отзыв");
    }

    @FXML
    private void handleToggleReview(ActionEvent event) {
        ReviewService.Review r = reviewsTable.getSelectionModel().getSelectedItem();
        if (r == null) { showAlert("Ошибка", "Выберите отзыв"); return; }
        boolean newStatus = !r.isApproved();
        if (ReviewService.toggleReviewApproval(r.getId(), newStatus)) {
            showAlert("Успех", "Отзыв " + (newStatus ? "одобрен" : "заблокирован"));
            loadReviews();
        } else showAlert("Ошибка", "Не удалось изменить статус");
    }


    private void initializeProductsTable() {
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        productCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        productPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        productStockColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        // Колонка с превью картинки
        productImageColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getImageUrl() != null ? data.getValue().getImageUrl() : "—"));
        productImageColumn.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(50);
                imageView.setFitHeight(50);
                imageView.setPreserveRatio(true);
            }
            @Override
            protected void updateItem(String url, boolean empty) {
                super.updateItem(url, empty);
                if (empty || url == null || url.isBlank() || url.equals("—")) {
                    setGraphic(null);
                    setText("—");
                } else {
                    try {
                        var stream = getClass().getResourceAsStream("/com/example/diyavol/images/" +
                                url.replaceAll(".*/", ""));
                        if (stream != null) {
                            imageView.setImage(new Image(stream));
                        } else {
                            imageView.setImage(new Image(url, true));
                        }
                        setGraphic(imageView);
                        setText(null);
                    } catch (Exception e) {
                        setGraphic(null);
                        setText("❌ URL");
                    }
                }
            }
        });

        productsTable.setItems(productsList);
        productsTable.setRowFactory(tv -> {
            TableRow<ProductService.Product> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    showProductDialog(row.getItem());
                }
            });
            return row;
        });
    }

    private void loadProducts() {
        productsList.clear();
        productsList.addAll(ProductService.getAllProductsFromDB());
    }

    @FXML
    private void handleAddProduct(ActionEvent event) {
        showProductDialog(null);
    }

    @FXML
    private void handleEditProduct(ActionEvent event) {
        ProductService.Product selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Ошибка", "Выберите товар для редактирования"); return; }
        showProductDialog(selected);
    }

    @FXML
    private void handleDeleteProduct(ActionEvent event) {
        ProductService.Product selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Ошибка", "Выберите товар для удаления"); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Удаление товара");
        confirm.setHeaderText(null);
        confirm.setContentText("Удалить товар \"" + selected.getName() + "\"?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                if (ProductService.deleteProduct(selected.getId())) {
                    showAlert("Успех", "Товар удалён");
                    loadProducts();
                } else {
                    showAlert("Ошибка", "Не удалось удалить товар");
                }
            }
        });
    }

    private void showProductDialog(ProductService.Product existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "➕ Добавить товар" : "✏️ Редактировать товар");
        dialog.setHeaderText(null);

        ButtonType saveBtn = new ButtonType("💾 Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);
        dialog.getDialogPane().setMinWidth(550);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField(existing != null ? existing.getName() : "");
        nameField.setPromptText("Название товара");
        nameField.setPrefWidth(350);

        TextArea descField = new TextArea(existing != null ? existing.getDescription() : "");
        descField.setPromptText("Описание");
        descField.setPrefRowCount(3);
        descField.setPrefWidth(350);

        TextField priceField = new TextField(existing != null ? String.valueOf(existing.getPrice()) : "");
        priceField.setPromptText("Цена (руб.)");

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("Электроника", "Пижамы и халаты",
                "Декор и освещение", "Текстиль", "Органайзеры");
        categoryBox.setValue(existing != null ? existing.getCategoryName() : "Электроника");

        TextField stockField = new TextField(existing != null ? String.valueOf(existing.getStockQuantity()) : "0");
        stockField.setPromptText("Количество на складе");

        TextField ratingField = new TextField(existing != null ? String.valueOf(existing.getRating()) : "5.0");
        ratingField.setPromptText("Рейтинг (0.0 - 5.0)");

        // Поле URL картинки
        TextField imageUrlField = new TextField(existing != null && existing.getImageUrl() != null
                ? existing.getImageUrl() : "");
        imageUrlField.setPromptText("URL картинки или имя файла из /images/");
        imageUrlField.setPrefWidth(350);

        // Превью картинки
        ImageView preview = new ImageView();
        preview.setFitWidth(120);
        preview.setFitHeight(120);
        preview.setPreserveRatio(true);
        preview.setStyle("-fx-border-color: #8B0000; -fx-border-width: 1;");

        // Обновляем превью при изменении URL
        imageUrlField.textProperty().addListener((obs, old, val) -> updatePreview(preview, val));
        if (existing != null) updatePreview(preview, existing.getImageUrl());

        Label previewLabel = new Label("Превью:");
        previewLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #8B0000;");

        // Подсказка про папку images
        Label hint = new Label("💡 Положи картинку в: src/main/resources/com/example/diyavol/images/\n" +
                "   и введи просто имя файла, например: lamp.jpg\n" +
                "   Или вставь полный https:// URL");
        hint.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666; -fx-wrap-text: true;");
        hint.setMaxWidth(350);

        int row = 0;
        grid.add(new Label("Название:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Описание:"), 0, row); grid.add(descField, 1, row++);
        grid.add(new Label("Цена (руб.):"), 0, row); grid.add(priceField, 1, row++);
        grid.add(new Label("Категория:"), 0, row); grid.add(categoryBox, 1, row++);
        grid.add(new Label("На складе:"), 0, row); grid.add(stockField, 1, row++);
        grid.add(new Label("Рейтинг:"), 0, row); grid.add(ratingField, 1, row++);
        grid.add(new Label("Картинка:"), 0, row); grid.add(imageUrlField, 1, row++);
        grid.add(new Label(""), 0, row); grid.add(hint, 1, row++);
        grid.add(previewLabel, 0, row); grid.add(preview, 1, row++);

        // Стили
        for (Node n : grid.getChildren()) {
            if (n instanceof Label) n.setStyle("-fx-font-weight: bold; -fx-text-fill: #2F4F4F;");
        }
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setStyle("-fx-background-color: #F5F5DC;");

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == saveBtn) {
            // Валидация
            if (nameField.getText().isBlank()) { showAlert("Ошибка", "Введите название товара"); return; }
            double price;
            try { price = Double.parseDouble(priceField.getText().replace(",", ".")); }
            catch (NumberFormatException e) { showAlert("Ошибка", "Неверный формат цены"); return; }
            int stock;
            try { stock = Integer.parseInt(stockField.getText()); }
            catch (NumberFormatException e) { showAlert("Ошибка", "Неверное количество"); return; }
            double rating;
            try { rating = Double.parseDouble(ratingField.getText().replace(",", ".")); }
            catch (NumberFormatException e) { rating = 5.0; }

            ProductService.Product p = new ProductService.Product(
                    existing != null ? existing.getId() : 0,
                    nameField.getText().trim(),
                    descField.getText().trim(),
                    price,
                    categoryBox.getValue(),
                    stock,
                    rating,
                    imageUrlField.getText().trim()
            );

            boolean ok = existing == null ? ProductService.addProduct(p) : ProductService.updateProduct(p);
            if (ok) {
                showAlert("Успех", existing == null ? "✅ Товар добавлен!" : "✅ Товар обновлён!");
                loadProducts();
            } else {
                showAlert("Ошибка", "Не удалось сохранить товар.\nПроверьте подключение к БД.");
            }
        }
    }

    private void updatePreview(ImageView view, String url) {
        if (url == null || url.isBlank()) { view.setImage(null); return; }
        try {
            var stream = getClass().getResourceAsStream(
                    "/com/example/diyavol/images/" + url.replaceAll(".*/", ""));
            if (stream != null) {
                view.setImage(new Image(stream));
            } else if (url.startsWith("http")) {
                view.setImage(new Image(url, true));
            } else {
                view.setImage(null);
            }
        } catch (Exception e) {
            view.setImage(null);
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadUsers();
        loadReviews();
        loadProducts();
        showAlert("Обновление", "Данные обновлены");
    }

    @FXML
    private void handleBackToMain(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/diyavol/fxml/auth/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("CosyHome - Авторизация");
            stage.setScene(new Scene(root, 500, 620));
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
