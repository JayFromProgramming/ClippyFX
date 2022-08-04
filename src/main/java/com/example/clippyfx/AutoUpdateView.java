package com.example.clippyfx;


import Interfaces.PopOut;
import javafx.event.Event;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Window;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * Used for checking GitHub for newer releases of the application, and updating the application if one is found.
 */
public class AutoUpdateView implements PopOut {

    public AnchorPane pane;
    public Label version_bar;
    public Hyperlink info_link;
    public Button update_cancel;
    public Button update_confirm;


    private boolean isAlive = true;
    private final String repo_url;

    public AutoUpdateView(String source_repo) {
        repo_url = source_repo;
    }

    public void cancel_clicked(MouseEvent mouseEvent) {
    }

    public void update_clicked(MouseEvent mouseEvent) {
    }

    public boolean new_version_available(){
        return !this.getCurrentVersion().equals(this.getLatestVersion());
    }

    /**
     * Makes the window visible
     */
    public void show(){

    }

    /**
     * This method gets what version of the application is currently installed
     * @return The version of the application installed
     */
    public String getCurrentVersion(){
        // Version is in the form of "v1.2.3" and is stored as a text file in the settings folder as "version.txt"
        String version = "";
        File version_file;
        FileReader fr;
        try {
            version_file = new File("./settings/version.txt");
            try {
                fr = new FileReader(version_file);
            } catch (FileNotFoundException e) { // If the version file doesn't exist, create it
                makeVersionFile("v0.0.0");
                fr = new FileReader(version_file);
            }
            char[] chars = new char[(int) version_file.length()];
            int read = fr.read(chars);
            if (read > 0) {
                version = new String(chars, 0, read);
            } else {
                version = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            version = "unknown";
        }
        return version;
    }

    /**
     * This method gets the latest release version of the application from GitHub
     * @return The latest release version of the application as a string
     */
    public String getLatestVersion() {
        // The latest version is gotten from the GitHub repo under the "releases" section
        String latest_version = "";
        try {
            System.out.printf("Getting latest version from %s/releases/latest\n", repo_url);
            URLConnection conn = new URL(repo_url + "/releases/latest").openConnection();
            long length = conn.getContentLengthLong();
            InputStream connection = conn.getInputStream();
            byte[] read = connection.readAllBytes();
            String api_data;
            if (read.length > 0) {
                api_data = new String(read, 0, read.length);
            } else {
                api_data = "";
            }
            connection.close();
            System.out.printf("%s\n", api_data);
            latest_version = new JSONObject(api_data).getString("tag_name");
        } catch (Exception e) {
            e.printStackTrace();
            return "v0.0.0";
        }
        return latest_version;
    }

    /**
     * This method updates the version.txt file with the version value being that of the passed in version
     * @param version The version to update the version.txt file with ex: "v1.2.3"
     */
    private void makeVersionFile(String version){
        File version_file = new File("./settings/version.txt");
        try {
            version_file.createNewFile();
            FileWriter writer = new FileWriter(version_file);
            writer.write(version);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Window getWindow() {
        return pane.getScene().getWindow();
    }

    @Override
    public popOutType getType() {
        return popOutType.AutoUpdateView;
    }

    @Override
    public boolean close() {
        isAlive = false;
        return true;
    }

    @Override
    public boolean isAlive() {
        return isAlive;
    }

    public <T extends Event> void onClose(T t) {
        close();
    }
}
