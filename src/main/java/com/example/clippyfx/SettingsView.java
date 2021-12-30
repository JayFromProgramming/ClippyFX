package com.example.clippyfx;

import HelperMethods.VideoChecks;
import HelperMethods.SettingsWrapper;
import Interfaces.PopOut;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import java.util.ArrayList;

public class SettingsView implements PopOut {

    public Button saveButton;
    public Button resetButton;
    public Button cancelButton;
    public ScrollPane scrollPane;
    public AnchorPane pain;

    private boolean isAlive = true;

    public void initialize() {
        ArrayList<SettingsWrapper.setting> settings = SettingsWrapper.getAllSettings();
        ArrayList<Pane> panes = new ArrayList<>();
        Pane backPain = new Pane();
        int yOffset = 10;
        for (SettingsWrapper.setting setting : settings) {
            panes.add(settingOptionBuilder(setting, yOffset));
            yOffset += 50;
        }
        backPain.getChildren().addAll(panes);
        scrollPane.setContent(backPain);
    }

    public Pane settingOptionBuilder(SettingsWrapper.setting setting, int yOffset) {
        Pane pane = new Pane();
        Text fieldName = new Text(setting.name);
        fieldName.setText(setting.name);
        fieldName.setLayoutY(yOffset);
        switch (setting.type) {
            case "filePath" -> {
                TextField textField = new TextField();
                textField.setText(setting.value);
                textField.setLayoutY(yOffset + 5);
                textField.setPrefWidth(200);
                textField.setOnMouseClicked(mouseEvent -> this.makeDirectorySelector(textField, setting.value));
                pane.getChildren().addAll(fieldName, textField);
            }
            case "toggle" -> {
                CheckBox checkBox = new CheckBox();
                checkBox.setSelected(setting.bool());
                checkBox.setLayoutY(yOffset + 5);
                pane.getChildren().addAll(fieldName, checkBox);
            }
            case "sizeEnum" -> {
                ChoiceBox<String> choiceBox = new ChoiceBox<>();
                choiceBox.getItems().addAll(VideoChecks.getSizesString());
                choiceBox.setValue(setting.value);
                choiceBox.setLayoutY(yOffset + 5);
                pane.getChildren().addAll(fieldName, choiceBox);
            }
            case "encoderEnum" -> {
                ChoiceBox<String> choiceBox = new ChoiceBox<>();
                choiceBox.getItems().addAll(VideoChecks.getEncodersString());
                choiceBox.setValue(setting.value);
                choiceBox.setLayoutY(yOffset + 5);
                pane.getChildren().addAll(fieldName, choiceBox);
            }
        }
        return pane;
    }

    private void makeDirectorySelector(TextField textField, String directory) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new java.io.File(directory));
        java.io.File selectedDirectory = directoryChooser.showDialog(getWindow());
        if (selectedDirectory != null) {
            textField.setText(selectedDirectory.getAbsolutePath());
        }
    }

    public void onSavePressed(MouseEvent mouseEvent) {
    }

    public void onResetPressed(MouseEvent mouseEvent) {
    }

    public void onCancelPressed(MouseEvent mouseEvent) {
    }


    @Override
    public Window getWindow() {
        return pain.getScene().getWindow();
    }

    @Override
    public popOutType getType() {
        return popOutType.SettingsView;
    }

    @Override
    public boolean close() {
        isAlive = false;
        ((Stage) pain.getScene().getWindow()).close();
        return true;
    }

    @Override
    public boolean isAlive() {
        return isAlive;
    }

    private void onClose(WindowEvent event) {
        System.out.println("Window closed.");
        this.isAlive = false;
    }

    private void closeHook(AnchorPane pain){
        pain.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::onClose);
    }

}
