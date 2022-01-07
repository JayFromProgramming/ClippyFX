package HelperMethods;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;


public class SettingsWrapper {
    private static final String SETTINGS_FILE_NAME = "settings/settings.json";
    private static final String TEMPLATE_FILE_PATH = "resources/settingsTemplates/settings.json";

    private static JSONObject settingsJSON;
    private static JSONObject templateJSON;
    static File settingsFile = new File(SETTINGS_FILE_NAME);
    static File templateFile = new File(TEMPLATE_FILE_PATH);


    public static class setting{
        public String name;
        public String value;
        public String type;
        public String description;
        public String group;

        setting(JSONObject jsonObject){
            try {
                this.name = jsonObject.getString("name");
                this.value = jsonObject.getString("value");
                this.type = jsonObject.getString("type");
                this.description = jsonObject.getString("description");
                this.group = jsonObject.getString("group");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public boolean bool(){
            return Boolean.parseBoolean(value);
        }
    }

    public static void loadSettings() throws IOException {
        settingsJSON = new JSONObject();
        if (settingsFile.exists()) {
            FileReader reader = new FileReader(settingsFile);
            StringBuilder jsonString = new StringBuilder();
            for (int i = 0; reader.ready(); i++) {
                jsonString.append((char) reader.read());
            }
            settingsJSON = new JSONObject(jsonString.toString());
        }
        FileReader reader = new FileReader(templateFile);
        StringBuilder jsonString = new StringBuilder();
        for (int i = 0; reader.ready(); i++) {
            jsonString.append((char) reader.read());
        }
        templateJSON = new JSONObject(jsonString.toString());
    }

    public static void resetSettings() throws IOException {
        /* Copy template file to settings file */
        FileWriter writer = new FileWriter(settingsFile);
        FileReader reader = new FileReader(templateFile);
        StringBuilder jsonString = new StringBuilder();
        for (int i = 0; reader.ready(); i++) {
            jsonString.append((char) reader.read());
        }
        writer.write(jsonString.toString());
        writer.close();
        reader.close();
        loadSettings();
    }

    public static void saveSettings() {
        try {
            FileWriter writer = new FileWriter(settingsFile);
            writer.write(settingsJSON.toString(2));
            writer.close();
        }catch (IOException ignored){}
    }


    public static void updateSetting(String key, JSONObject value) {
        settingsJSON.put(key, value);
//        saveSettings();
    }

    public static JSONObject getRawObject(String key) {
        return settingsJSON.getJSONObject(key);
    }

    public static ArrayList<setting> getAllSettings() {
        ArrayList<setting> settings = new ArrayList<>();
        for (String key : templateJSON.keySet()) {
            try {
                settings.add(new setting(settingsJSON.getJSONObject(key)));
            } catch (JSONException e) {
                settings.add(repairSetting(key));
            }
        }
        return settings;
    }

    private static setting repairSetting(String badKey){
        settingsJSON.put(badKey, templateJSON.getJSONObject(badKey));
        return new setting(templateJSON.getJSONObject(badKey));
    }

    public static setting getSetting(String key) {
        try {
            return new setting(settingsJSON.getJSONObject(key));
        } catch (JSONException e) {
            return repairSetting(key);
        }
    }

    public static KeyCode keyBind(String key){
        try {
            return KeyCode.valueOf(settingsJSON.getJSONObject(key).getString("value"));
        } catch (JSONException e) {
            repairSetting(key);
            return KeyCode.valueOf(settingsJSON.getJSONObject(key).getString("value"));
        }
    }
}
