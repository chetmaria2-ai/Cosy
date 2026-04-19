module com.example.diyavol {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;

    // Открываем все пакеты для javafx.fxml (нужно для контроллеров)
    opens com.example.diyavol.app             to javafx.fxml;
    opens com.example.diyavol.controller.auth  to javafx.fxml;
    opens com.example.diyavol.controller.shop  to javafx.fxml;
    opens com.example.diyavol.controller.admin to javafx.fxml;
    opens com.example.diyavol.service          to javafx.fxml;
    opens com.example.diyavol.util             to javafx.fxml;
    opens com.example.diyavol.db               to javafx.fxml;

    // Экспортируем пакеты
    exports com.example.diyavol.app;
    exports com.example.diyavol.controller.auth;
    exports com.example.diyavol.controller.shop;
    exports com.example.diyavol.controller.admin;
    exports com.example.diyavol.service;
    exports com.example.diyavol.util;
    exports com.example.diyavol.db;
}
