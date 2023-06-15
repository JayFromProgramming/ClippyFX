package com.example.clippyfx;

import HelperMethods.StreamedCommand;
import HelperMethods.VideoChecks;
import HelperMethods.SettingsWrapper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;


public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException, InterruptedException {
        int num = 02635; // Just a random number
        System.out.println(num);
        String check = StreamedCommand.getCommandOutput("ffmpeg -version");
        if (check.contains("ffmpeg version")) {
            System.out.println("FFmpeg is installed");
        } else {
            System.out.println("FFmpeg is not installed");
            System.exit(-27);
        }
//        if (runUpdateCheck()){
//            System.out.println("New version found");
//        } else {
//            System.out.println("No new version found");
//        }
        new Thread(VideoChecks::checkEncoders).start();
        // URL to the FXML file
        URL view = HelloApplication.class.getResource("/hello-view.fxml");
        if (view == null) {
            System.out.println("View object is null");
            System.exit(-1);
        }
        FXMLLoader fxmlLoader = new FXMLLoader(view);
        Scene scene = new Scene(fxmlLoader.load(), 949, 686);
        stage.setTitle("ClippyFX");
        stage.setScene(scene);
        stage.getIcons().add(new Image("file:resources/videoResources/Icon.png"));
        stage.show();
        MainController controller = fxmlLoader.getController();
        controller.Pain.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, controller::onClose);
        controller.Pain.setOnDragOver(controller::onDragOver);
        controller.Pain.setOnDragDropped(controller::onDragNDrop);
        controller.startAnimations();
        Thread.setDefaultUncaughtExceptionHandler(controller::onUncaughtException);
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
    }

    private boolean runUpdateCheck() throws IOException, InterruptedException {
        AutoUpdateView updateView = new AutoUpdateView(
                "https://api.github.com/repos/JayFromProgramming/ClippyFX");
         if (updateView.new_version_available()) {
             FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/update-view.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 949, 686);
                Stage stage = new Stage();
                stage.setTitle("ClippyFX Update");
                stage.setScene(scene);
                stage.getIcons().add(new Image("file:resources/videoResources/Icon.png"));
                stage.show();
                AutoUpdateView controller = fxmlLoader.getController();
                controller.pane.getScene().getWindow().addEventFilter(
                        WindowEvent.WINDOW_CLOSE_REQUEST, controller::onClose);
                // Wait for the window to close before continuing
                while (stage.isShowing()) {
                    Thread.sleep(100);
                }
         }
        return false;
    }

    public static void main(String[] args) throws IOException {
        /* Check if a settings directory exists */
        File settingsDir = new File("settings");
        if (!settingsDir.exists()) {
            System.out.println("Creating settings directory");
            settingsDir.mkdir();
        }
        /* Check if a settings file exists */
        File settingsFile = new File("settings/settings.json");
        if (!settingsFile.exists()) {
            File settingsFileTemplate = new File("resources/settingsTemplates/settings.json");
            // Copy the settings file to the settings directory
            Files.copy(Path.of(settingsFileTemplate.getAbsolutePath()), Path.of(settingsFile.getAbsolutePath()));
        }
        SettingsWrapper.loadSettings();
        File tempFile = new File("resources/videoResources/TempWorkingFile.mp4");
        if (tempFile.exists()) tempFile.delete();
        launch();
    }
}