package com.example.clippyfx;

import HelperMethods.VideoChecks;
import HelperMethods.SettingsWrapper;
import Interfaces.PopOut;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

public class SettingsView implements PopOut {

    public Button saveButton;
    public Button resetButton;
    public Button cancelButton;
    public AnchorPane pain;
    public TabPane settingsTabs;
    public Tab focusedTab;

    private boolean isAlive = true;
    private final ArrayList<OptionWrapper> options = new ArrayList<>();
    private final Map<String, tabWrapper> tabs = new java.util.HashMap<>();

    public void keyPressed(KeyEvent keyEvent) {
        switch (keyEvent.getCode()) {
            case ESCAPE -> close();
            case ENTER -> onSavePressed(null);
            case F5 -> build();
        }
    }

    public void mouseScroll(ScrollEvent scrollEvent) {
//        for (Node node : focusedTab.gry.getChildren()) {
//            node.setTranslateY(node.getTranslateY() + scrollEvent.getDeltaY());
//            // Get if the node is out of bounds and if so, make it hidden
//            node.setVisible(!(node.getLayoutY() + node.getTranslateY() > 320));
//        }
        scrollEvent.consume();
    }

    public void changeTab(MouseEvent mouseEvent) {
    }


    static class OptionWrapper {
        String name;
        String startValue;
        String type;
        TextField textField;
        CheckBox checkBox;
        ChoiceBox choiceBox;
    }

    static class tabWrapper {
        String name;
        Pane pane;
        Tab tab;
        int yOffset = 0;
    }

    public void hookHooks(){
        closeHook(this.pain);
    }

    public void build() {
        System.out.println("Initializing SettingsView");
        ArrayList<SettingsWrapper.setting> settings = SettingsWrapper.getAllSettings();
        for (SettingsWrapper.setting setting : settings) {
            if (tabs.containsKey(setting.group)) {
                tabs.get(setting.group).yOffset += 44;
                Pane tab = tabs.get(setting.group).pane;
                int yOffset = tabs.get(setting.group).yOffset;
                settingOptionBuilder(setting, yOffset, tab);
            } else {
                tabWrapper tabWrapper = new tabWrapper();
                tabWrapper.name = setting.group;
                tabWrapper.pane = new Pane();
                tabWrapper.tab = new Tab(setting.group);
                tabWrapper.tab.setContent(tabWrapper.pane);
                tabWrapper.yOffset = 24;
                tabs.put(setting.group, tabWrapper);
                settingOptionBuilder(setting, tabWrapper.yOffset, tabWrapper.pane);
            }

        }
        for (Map.Entry<String, tabWrapper> entry : tabs.entrySet()) {
            settingsTabs.getTabs().add(entry.getValue().tab);
        }
    }

    public void settingOptionBuilder(SettingsWrapper.setting setting, int yOffset, Pane parent) {
        Text fieldName = new Text(setting.name);
        fieldName.setText(setting.name + ": " + setting.description);
        fieldName.setLayoutY(yOffset);
        OptionWrapper option = new OptionWrapper();
        option.name = setting.name;
        option.startValue = setting.value;
        switch (setting.type) {
            case "filePath" -> {
                TextField textField = new TextField();
                Button button = new Button("Choose");
                button.setOnMouseClicked(mouseEvent -> makeDirectorySelector(textField, setting.value));
                textField.setText(setting.value);
                textField.setLayoutY(yOffset + 5);
                textField.setPrefWidth(350);
                button.setLayoutX(textField.getLayoutX() + textField.getPrefWidth() + 5);
                button.setLayoutY(yOffset + 5);
                textField.setDisable(false);
                option.textField = textField;
                option.type = "textField";
                parent.getChildren().addAll(fieldName, textField, button);
            }
            case "toggle" -> {
                CheckBox checkBox = new CheckBox();
                checkBox.setSelected(setting.bool());
                checkBox.setLayoutY(yOffset + 5);
                parent.getChildren().addAll(fieldName, checkBox);
                option.checkBox = checkBox;
                option.type = "checkBox";
            }
            case "sizeEnum" -> {
                ChoiceBox<String> choiceBox = new ChoiceBox<>();
                choiceBox.getItems().addAll(VideoChecks.getSizesString());
                choiceBox.setValue(setting.value);
                choiceBox.setLayoutY(yOffset + 5);
                parent.getChildren().addAll(fieldName, choiceBox);
                option.choiceBox = choiceBox;
                option.type = "choiceBox";
            }
            case "encoderEnum" -> {
                ChoiceBox<String> choiceBox = new ChoiceBox<>();
                choiceBox.getItems().addAll(VideoChecks.getEncodersString());
                choiceBox.setValue(setting.value);
                choiceBox.setLayoutY(yOffset + 5);
                parent.getChildren().addAll(fieldName, choiceBox);
                option.choiceBox = choiceBox;
                option.type = "choiceBox";
            }
            case "text" -> {
                TextField textField = new TextField();
                textField.setText(setting.value);
                textField.setLayoutY(yOffset + 5);
                textField.setPrefWidth(350);
                option.textField = textField;
                option.type = "text";
                parent.getChildren().addAll(fieldName, textField);
            }
        }
        options.add(option);
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
        System.out.println("Saving");
        for (OptionWrapper option : options) {
            switch (option.type) {
                case "textField" -> {
                    JSONObject object = SettingsWrapper.getRawObject(option.name).put("value", option.textField.getText());
                    SettingsWrapper.updateSetting(option.name, object);
                }
                case "checkBox" -> {
                    String value = option.checkBox.isSelected() ? "true" : "false";
                    JSONObject object = SettingsWrapper.getRawObject(option.name).put("value", value);
                    SettingsWrapper.updateSetting(option.name, object);
                }
                case "choiceBox" -> {
                    JSONObject object = SettingsWrapper.getRawObject(option.name).put("value", option.choiceBox.getValue());
                    SettingsWrapper.updateSetting(option.name, object);
                }
            }
        }
        SettingsWrapper.saveSettings();
        options.clear();
        build();
    }

    public void onResetPressed(MouseEvent mouseEvent) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reset Settings");
        alert.setHeaderText("Are you sure you want to reset all settings?");
        alert.setContentText("This will reset all settings to their default values.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            alert.close();
            SettingsWrapper.resetSettings();
            options.clear();
            build();
        } else {
            alert.close();
        }
    }

    private boolean checkUnsaved(){
        boolean changed = false;
        for (OptionWrapper option : options) {
            switch (option.type) {
                case "textField", "text" -> {if (!option.textField.getText().equals(option.startValue)) changed = true;}
                case "checkBox" -> {
                    String value = option.checkBox.isSelected() ? "true" : "false";
                    if (!value.equals(option.startValue)) changed = true;
                }
                case "choiceBox" -> {if (!option.choiceBox.getValue().equals(option.startValue)) changed = true;}
            }
            if (changed) break;
        }
        if (changed) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Cancel");
            alert.setHeaderText("Are you sure you want to cancel?");
            alert.setContentText("All unsaved changes will be lost.");
            Optional<ButtonType> result = alert.showAndWait();
            alert.close();
            return result.isPresent() && result.get() == ButtonType.OK;
        } else {
            return true;
        }
    }

    public void onCancelPressed(MouseEvent mouseEvent) {
        if(checkUnsaved()) close();
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
        if (!checkUnsaved()){
            event.consume();
        }
        System.out.println("Window closed.");
        this.isAlive = false;
    }

    private void closeHook(AnchorPane pain){
        pain.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::onClose);
    }

}
