package com.example.clippyfx;

import HelperMethods.EncoderCheck;
import HelperMethods.SettingsWrapper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("ClippyFX");
        alert.setHeaderText("Checking for encoders");
        alert.setContentText("Checking for encoders...");
        alert.show();
        EncoderCheck.checkEncoders();
        alert.close();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 949, 686);
        stage.setTitle("ClippyFX");
        stage.setScene(scene);
        stage.show();
        MainController controller = fxmlLoader.getController();
        controller.Pain.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, controller::onClose);
        Thread.setDefaultUncaughtExceptionHandler(controller::onUncaughtException);
    }

    public static void main(String[] args) throws IOException {
        /* Check if a settings directory exists */
        File settingsDir = new File("settings");
        if (!settingsDir.exists()) {
            settingsDir.mkdir();
        }
        /* Check if a settings file exists */
        File settingsFile = new File("settings/settings.json");
        if (!settingsFile.exists()) {
            File settingsFileTemplate = new File("src/main/resources/settingsTemplates/settings.json");
            // Copy the settings file to the settings directory
            Files.copy(Path.of(settingsFileTemplate.getAbsolutePath()), Path.of(settingsFile.getAbsolutePath()));
        }
        SettingsWrapper.loadSettings();
        launch();
    }
}