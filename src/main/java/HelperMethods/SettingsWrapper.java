package HelperMethods;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class SettingsWrapper {
    private static final String SETTINGS_FILE_NAME = "settings/settings.json";
    private static final String TEMPLATE_FILE_PATH = "src/main/resources/settingsTemplates/settings.json";

    private static JSONObject settingsJSON;
    private static JSONObject templateJSON;
    static File settingsFile = new File(SETTINGS_FILE_NAME);
    static File templateFile = new File(TEMPLATE_FILE_PATH);

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

    private static String repairSettingsString(String badKey){
        settingsJSON.put(badKey, templateJSON.getString(badKey));
        return templateJSON.getString(badKey);
    }

    private static boolean repairSettingsBoolean(String badKey){
        settingsJSON.put(badKey, templateJSON.getBoolean(badKey));
        return templateJSON.getBoolean(badKey);
    }

    public static String getSettingsValue(String key) {
        try {
            return settingsJSON.getString(key);
        } catch (JSONException e) {
            return repairSettingsString(key);
        }
    }

    public static boolean getSettingsBoolean(String key) {
        try {
            return settingsJSON.getBoolean(key);
        } catch (JSONException e) {
            return repairSettingsBoolean(key);
        }
    }

    public static String getBasicLoadPath(){
        try{
            return settingsJSON.getString("defaultBasicLoadPath");
        }
        catch (JSONException e){
            return repairSettingsString("defaultBasicLoadPath");
        }
    }

    public static String getBasicSavePath(){
        try{
            return settingsJSON.getString("defaultBasicSavePath");
        }
        catch (JSONException e){
            return repairSettingsString("defaultBasicSavePath");
        }
    }

    public static String getAdvancedLoadPath(){
        try{
            return settingsJSON.getString("defaultAdvancedLoadPath");
        }
        catch (JSONException e){
            return repairSettingsString("defaultAdvancedLoadPath");
        }
    }

    public static String getAdvancedSavePath(){
        try{
            return settingsJSON.getString("defaultAdvancedSavePath");
        }
        catch (JSONException e){
            return repairSettingsString("defaultAdvancedSavePath");
        }
    }

    public static String getYoutubeSavePath(){
        try{
            return settingsJSON.getString("defaultYoutubeSavePath");
        }
        catch (JSONException e){
            return repairSettingsString("defaultYoutubeSavePath");
        }
    }
}
