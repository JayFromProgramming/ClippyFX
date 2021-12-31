module com.example.clippyfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires javafx.media;
    requires org.json;
    requires org.apache.commons.lang3;
    requires java.management;

    opens com.example.clippyfx to javafx.fxml;
    exports com.example.clippyfx;
}