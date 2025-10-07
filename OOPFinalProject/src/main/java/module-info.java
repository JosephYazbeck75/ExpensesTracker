module desktopapps.oopfinalproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens desktopapps.oopfinalproject to javafx.fxml;
    exports desktopapps.oopfinalproject;
}