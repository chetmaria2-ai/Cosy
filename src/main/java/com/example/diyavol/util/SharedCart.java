package com.example.diyavol.util;

import com.example.diyavol.controller.auth.login;
import com.example.diyavol.controller.shop.CartController;
import com.example.diyavol.service.UserService;
import com.example.diyavol.service.CartService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SharedCart {

    private static final ObservableList<CartController.CartItem> items =
            FXCollections.observableArrayList();

    public static void addItem(String productName, double price) {
        addItem(productName, price, -1);
    }

    public static void addItem(String productName, double price, int productId) {
        for (int i = 0; i < items.size(); i++) {
            CartController.CartItem item = items.get(i);
            if (item.getProductName().equals(productName)) {
                item.incrementQuantity();
                items.set(i, item);
                // Синхронизируем с БД
                syncIncrement(productId, item.getQuantity());
                System.out.println("🛒 +1 к " + productName + " (итого: " + item.getQuantity() + ")");
                return;
            }
        }
        CartController.CartItem newItem = new CartController.CartItem(productName, price, 1, productId);
        items.add(newItem);
        // Сохраняем в БД
        syncAdd(productId);
        System.out.println("🛒 Добавлено: " + productName + " за " + price + " руб.");
    }

    private static void syncAdd(int productId) {
        UserService.User user = login.getCurrentUser();
        if (user != null && productId > 0) {
            CartService.addToCart(user.getId(), productId, 1);
        }
    }

    private static void syncIncrement(int productId, int newQty) {
        UserService.User user = login.getCurrentUser();
        if (user != null && productId > 0) {
            CartService.updateQuantity(user.getId(), productId, newQty);
        }
    }

    public static void loadFromDatabase() {
        UserService.User user = login.getCurrentUser();
        if (user == null) return;

        items.clear();
        for (CartService.CartEntry entry : CartService.loadCart(user.getId())) {
            items.add(new CartController.CartItem(
                entry.getProductName(),
                entry.getPrice(),
                entry.getQuantity(),
                entry.getProductId()
            ));
        }
    }

    public static void removeItem(CartController.CartItem item) {
        items.remove(item);
        UserService.User user = login.getCurrentUser();
        if (user != null && item.getProductId() > 0) {
            CartService.removeFromCart(user.getId(), item.getProductId());
        }
    }

    public static void clear() {
        UserService.User user = login.getCurrentUser();
        if (user != null) {
            CartService.clearCart(user.getId());
        }
        items.clear();
    }

    public static ObservableList<CartController.CartItem> getItems() {
        return items;
    }

    public static double getTotal() {
        return items.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();
    }

    public static int getItemCount() {
        return items.stream().mapToInt(CartController.CartItem::getQuantity).sum();
    }
}
