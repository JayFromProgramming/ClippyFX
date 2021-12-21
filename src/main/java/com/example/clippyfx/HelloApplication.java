package com.example.clippyfx;

import HelperMethods.SettingsWrapper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 949, 686);
        stage.setTitle("ClippyFX");
        stage.setScene(scene);
        stage.show();
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