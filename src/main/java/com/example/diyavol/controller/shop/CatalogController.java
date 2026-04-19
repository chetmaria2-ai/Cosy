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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.util.List;

public class CatalogController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> priceComboBox;
    @FXML private ScrollPane productsScrollPane;
    @FXML private TextArea productDescription;
    @FXML private Label productPrice;
    @FXML private Label productStock;
    @FXML private Label productRating;
    @FXML private Label productCategory;
    @FXML private Button addToCartBtn;
    @FXML private Button addToFavoritesBtn;
    @FXML private ImageView productImageView;

    private List<ProductService.Product> allProducts;
    private ProductService.Product selectedProduct;

    @FXML
    public void initialize() {
        // Загружаем товары
        loadProductsFromDatabase();

        // Заполняем категории
        categoryComboBox.setItems(FXCollections.observableArrayList(
                "Все категории", "Электроника", "Пижамы и халаты",
                "Декор и освещение", "Текстиль", "Органайзеры"
        ));

        priceComboBox.setItems(FXCollections.observableArrayList(
                "Любая цена", "До 2000 руб", "2000-4000 руб", "4000-6000 руб", "Свыше 6000 руб"
        ));

        displayProductCards();

        // Деактивируем кнопки до выбора товара
        addToCartBtn.setDisable(true);
        addToFavoritesBtn.setDisable(true);

        // Если пользователь не авторизован — скрываем кнопку корзины
        if (login.getCurrentUser() == null) {
            addToCartBtn.setText("Войдите, чтобы добавить в корзину");
            addToCartBtn.setStyle("-fx-background-color: #AAAAAA; -fx-text-fill: #F5F5DC; -fx-font-weight: bold; " +
                    "-fx-background-radius: 15; -fx-padding: 8 15;");
        }
    }

    private void loadProductsFromDatabase() {
        allProducts = ProductService.getAllProducts();
    }

    private void displayProductCards() {
        GridPane productsGrid = new GridPane();
        productsGrid.setPadding(new Insets(20));
        productsGrid.setHgap(20);
        productsGrid.setVgap(20);
        productsGrid.setAlignment(Pos.TOP_CENTER);

        int column = 0;
        int row = 0;
        int columnsCount = 3;

        for (ProductService.Product product : allProducts) {
            VBox productCard = createProductCard(product);
            productsGrid.add(productCard, column, row);

            column++;
            if (column >= columnsCount) {
                column = 0;
                row++;
            }
        }

        productsScrollPane.setContent(productsGrid);
        productsScrollPane.setFitToWidth(true);
        productsScrollPane.setStyle("-fx-background: transparent; -fx-border-color: transparent;");
    }

    private VBox createProductCard(ProductService.Product product) {
        VBox card = new VBox();
        card.setAlignment(Pos.TOP_CENTER);
        card.setSpacing(10);
        card.setPadding(new Insets(15));
        card.setPrefWidth(250);
        card.setPrefHeight(350);

        // Стиль карточки
        card.setStyle("-fx-background-color: rgba(245,245,220,0.95); " +
                "-fx-background-radius: 20; " +
                "-fx-border-radius: 20; " +
                "-fx-border-color: #8B0000; " +
                "-fx-border-width: 2; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);");

        // Эффекты при наведении
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: rgba(245,245,220,0.98); " +
                    "-fx-background-radius: 20; " +
                    "-fx-border-radius: 20; " +
                    "-fx-border-color: #A52A2A; " +
                    "-fx-border-width: 3; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 20, 0, 0, 7);");
        });

        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: rgba(245,245,220,0.95); " +
                    "-fx-background-radius: 20; " +
                    "-fx-border-radius: 20; " +
                    "-fx-border-color: #8B0000; " +
                    "-fx-border-width: 2; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);");
        });

        // Контейнер для изображения
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(180, 180);
        imageContainer.setStyle("-fx-background-color: #D2B48C; -fx-background-radius: 15;");

        try {
            String imagePath = product.getImageUrl();
            Image image;

            if (imagePath != null && !imagePath.isEmpty()) {
                if (imagePath.startsWith("http")) {
                    // Загружаем из URL
                    image = new Image(imagePath, 160, 160, true, true, false);
                } else {
                    // Загружаем из ресурсов
                    image = new Image(getClass().getResourceAsStream(imagePath));
                }
            } else {
                // Если путь не указан, используем эмодзи
                throw new Exception("No image path");
            }

            if (image != null && !image.isError()) {
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(160);
                imageView.setFitHeight(160);
                imageView.setPreserveRatio(true);
                imageView.setStyle("-fx-background-radius: 12;");

                // Эффект при наведении на изображение
                imageView.setOnMouseEntered(ev -> {
                    imageView.setFitWidth(165);
                    imageView.setFitHeight(165);
                });

                imageView.setOnMouseExited(ev -> {
                    imageView.setFitWidth(160);
                    imageView.setFitHeight(160);
                });

                imageContainer.getChildren().add(imageView);
            } else {
                throw new Exception("Image loading failed");
            }

        } catch (Exception e) {
            // Если изображение не найдено, показываем эмодзи-заглушку
            Label placeholder = new Label(getEmojiForCategory(product.getCategoryName()));
            placeholder.setStyle("-fx-font-size: 48px; -fx-text-fill: #8B0000;");
            imageContainer.getChildren().add(placeholder);
        }

        // Название товара
        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #8B0000; -fx-wrap-text: true;");
        nameLabel.setMaxWidth(200);
        nameLabel.setAlignment(Pos.CENTER);

        // Цена
        Label priceLabel = new Label(String.format("💰 %.0f руб", product.getPrice()));
        priceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2E8B57;");

        // Рейтинг и количество
        HBox ratingBox = new HBox(5);
        ratingBox.setAlignment(Pos.CENTER);

        Label ratingLabel = new Label(String.format("⭐ %.1f", product.getRating()));
        ratingLabel.setStyle("-fx-text-fill: #FF8C00; -fx-font-weight: bold;");

        Label stockLabel = new Label("📦 " + product.getStockQuantity() + " шт.");
        stockLabel.setStyle("-fx-text-fill: #5D4037; -fx-font-size: 12px;");

        ratingBox.getChildren().addAll(ratingLabel, stockLabel);

        // Кнопка выбора
        Button selectButton = new Button("Выбрать");
        selectButton.setStyle("-fx-background-color: #8B0000; -fx-text-fill: #F5F5DC; -fx-font-weight: bold; " +
                "-fx-background-radius: 15; -fx-padding: 8 15;");
        selectButton.setOnAction(event -> showProductDetails(product));

        // Анимация кнопки
        selectButton.setOnMouseEntered(event -> {
            selectButton.setStyle("-fx-background-color: #A52A2A; -fx-text-fill: #F5F5DC; -fx-font-weight: bold; " +
                    "-fx-background-radius: 15; -fx-padding: 8 15;");
        });

        selectButton.setOnMouseExited(event -> {
            selectButton.setStyle("-fx-background-color: #8B0000; -fx-text-fill: #F5F5DC; -fx-font-weight: bold; " +
                    "-fx-background-radius: 15; -fx-padding: 8 15;");
        });

        card.getChildren().addAll(imageContainer, nameLabel, priceLabel, ratingBox, selectButton);
        return card;
    }

    private String getEmojiForCategory(String category) {
        switch (category) {
            case "Электроника": return "💡";
            case "Пижамы и халаты": return "👘";
            case "Декор и освещение": return "🏮";
            case "Текстиль": return "🛏️";
            case "Органайзеры": return "🗃️";
            default: return "🛍️";
        }
    }

    private void showProductDetails(ProductService.Product product) {
        this.selectedProduct = product;

        // Обновляем изображение
        try {
            String imagePath = product.getImageUrl();
            Image image;

            if (imagePath != null && !imagePath.isEmpty()) {
                if (imagePath.startsWith("http")) {
                    image = new Image(imagePath, 200, 200, true, true, false);
                } else {
                    image = new Image(getClass().getResourceAsStream(imagePath));
                }
            } else {
                throw new Exception("No image path");
            }

            productImageView.setImage(image);
            productImageView.setFitWidth(200);
            productImageView.setFitHeight(200);
            productImageView.setPreserveRatio(true);
        } catch (Exception e) {
            productImageView.setImage(null);
        }

        productDescription.setText("📦 " + product.getName() +
                "\n\n📋 Описание:\n" + product.getDescription() +
                "\n\n⭐ Особенности:\n• Высокое качество материалов\n• Быстрая доставка 1-3 дня\n• Гарантия 1 год");
        productPrice.setText("💰 Цена: " + String.format("%.0f", product.getPrice()) + " руб");
        productStock.setText("📦 В наличии: " + product.getStockQuantity() + " шт.");
        productRating.setText("⭐ Рейтинг: " + product.getRating() + "/5.0");
        productCategory.setText("📁 Категория: " + product.getCategoryName());

        // Активируем кнопки (кнопку корзины — только для авторизованных)
        if (login.getCurrentUser() != null) {
            addToCartBtn.setDisable(false);
        }
        addToFavoritesBtn.setDisable(false);
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim().toLowerCase();
        if (!query.isEmpty()) {
            GridPane productsGrid = new GridPane();
            productsGrid.setPadding(new Insets(20));
            productsGrid.setHgap(20);
            productsGrid.setVgap(20);
            productsGrid.setAlignment(Pos.TOP_CENTER);

            int column = 0;
            int row = 0;
            int columnsCount = 3;

            for (ProductService.Product product : allProducts) {
                if (product.getName().toLowerCase().contains(query)) {
                    VBox productCard = createProductCard(product);
                    productsGrid.add(productCard, column, row);

                    column++;
                    if (column >= columnsCount) {
                        column = 0;
                        row++;
                    }
                }
            }
            productsScrollPane.setContent(productsGrid);
        } else {
            displayProductCards();
        }
    }

    @FXML
    private void handleCategoryFilter() {
        String selectedCategory = categoryComboBox.getValue();
        if (selectedCategory != null && !selectedCategory.equals("Все категории")) {
            GridPane productsGrid = new GridPane();
            productsGrid.setPadding(new Insets(20));
            productsGrid.setHgap(20);
            productsGrid.setVgap(20);
            productsGrid.setAlignment(Pos.TOP_CENTER);

            int column = 0;
            int row = 0;
            int columnsCount = 3;

            for (ProductService.Product product : allProducts) {
                if (product.getCategoryName().equals(selectedCategory)) {
                    VBox productCard = createProductCard(product);
                    productsGrid.add(productCard, column, row);

                    column++;
                    if (column >= columnsCount) {
                        column = 0;
                        row++;
                    }
                }
            }
            productsScrollPane.setContent(productsGrid);
        } else {
            displayProductCards();
        }
    }

    @FXML
    private void handlePriceFilter() {
        String selectedPrice = priceComboBox.getValue();
        if (selectedPrice != null && !selectedPrice.equals("Любая цена")) {
            GridPane productsGrid = new GridPane();
            productsGrid.setPadding(new Insets(20));
            productsGrid.setHgap(20);
            productsGrid.setVgap(20);
            productsGrid.setAlignment(Pos.TOP_CENTER);

            int column = 0;
            int row = 0;
            int columnsCount = 3;

            for (ProductService.Product product : allProducts) {
                double price = product.getPrice();
                boolean showProduct = false;

                switch (selectedPrice) {
                    case "До 2000 руб":
                        showProduct = price <= 2000;
                        break;
                    case "2000-4000 руб":
                        showProduct = price >= 2000 && price <= 4000;
                        break;
                    case "4000-6000 руб":
                        showProduct = price >= 4000 && price <= 6000;
                        break;
                    case "Свыше 6000 руб":
                        showProduct = price > 6000;
                        break;
                    default:
                        showProduct = true;
                }

                if (showProduct) {
                    VBox productCard = createProductCard(product);
                    productsGrid.add(productCard, column, row);

                    column++;
                    if (column >= columnsCount) {
                        column = 0;
                        row++;
                    }
                }
            }
            productsScrollPane.setContent(productsGrid);
        } else {
            displayProductCards();
        }
    }

    @FXML
    private void handleAddToCart() {
        if (login.getCurrentUser() == null) {
            showAlert("Доступ запрещён", "❌ Для добавления товара в корзину необходимо войти в аккаунт.");
            return;
        }

        if (selectedProduct != null) {
            SharedCart.addItem(selectedProduct.getName(), selectedProduct.getPrice(), selectedProduct.getId());
            int total = SharedCart.getItemCount();
            showAlert("✅ Добавлено в корзину",
                selectedProduct.getName() + "\n" +
                "Цена: " + String.format("%.2f", selectedProduct.getPrice()) + " руб.\n" +
                "Всего товаров в корзине: " + total);
        } else {
            showAlert("Ошибка", "❌ Выберите товар для добавления в корзину");
        }
    }

    @FXML
    private void handleAddToFavorites() {
        if (selectedProduct != null) {
            showAlert("Успешно", "❤️ " + selectedProduct.getName() + " добавлен в избранное!");
        } else {
            showAlert("Ошибка", "❌ Выберите товар для добавления в избранное");
        }
    }

    @FXML
    private void handleClearFilters() {
        categoryComboBox.setValue("Все категории");
        priceComboBox.setValue("Любая цена");
        searchField.clear();
        displayProductCards();
    }

    @FXML
    private void handleBackToMain(ActionEvent event) {
        // Показываем диалог выбора куда вернуться
        if (login.getCurrentUser() != null) {
            // Авторизован — сразу в главное меню
            openWindow("/com/example/diyavol/fxml/menu/MainMenu.fxml", "CosyHome - Главное меню", 1000, 700, event);
        } else {
            // Гость — предлагаем выбор
            javafx.scene.control.Alert choiceAlert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION);
            choiceAlert.setTitle("Куда вернуться?");
            choiceAlert.setHeaderText(null);
            choiceAlert.setContentText("Выберите действие:");

            javafx.scene.control.ButtonType btnGuest =
                new javafx.scene.control.ButtonType("👤 Гостевое меню");
            javafx.scene.control.ButtonType btnLogin =
                new javafx.scene.control.ButtonType("🔐 Войти / Регистрация");
            javafx.scene.control.ButtonType btnCancel =
                new javafx.scene.control.ButtonType("Остаться", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);

            choiceAlert.getButtonTypes().setAll(btnGuest, btnLogin, btnCancel);
            choiceAlert.showAndWait().ifPresent(choice -> {
                if (choice == btnGuest) {
                    openWindow("/com/example/diyavol/fxml/menu/guest-menu.fxml", "CosyHome - Гостевой режим", 1000, 700, event);
                } else if (choice == btnLogin) {
                    openWindow("/com/example/diyavol/fxml/auth/register.fxml", "CosyHome - Регистрация", 500, 650, event);
                }
            });
        }
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