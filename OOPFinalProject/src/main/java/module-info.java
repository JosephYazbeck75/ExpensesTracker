module desktopapps.oopfinalproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;

    opens desktopapps.oopfinalproject to javafx.fxml;
    exports desktopapps.oopfinalproject;
}