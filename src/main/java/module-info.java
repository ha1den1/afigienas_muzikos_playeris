module com.example.work {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    opens com.example.work to javafx.fxml;
    exports com.example.work;
}