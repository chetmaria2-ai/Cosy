package com.example.diyavol.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(
            getClass().getResource("/com/example/diyavol/fxml/auth/register.fxml"));
        primaryStage.setTitle("CosyHome - Регистрация");
        primaryStage.setScene(new Scene(root, 500, 650));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
