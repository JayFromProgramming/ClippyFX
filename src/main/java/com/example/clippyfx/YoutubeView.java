package com.example.clippyfx;

import EncodingMagic.YoutubePegGenerator;
import Interfaces.Method;
import Interfaces.PegGenerator;
import Interfaces.PopOut;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.text.NumberFormat;
import java.text.ParseException;

public class YoutubeView implements PopOut {

    public ImageView thumbnailViewer;
    public TextField youtubeLinkBox;
    public Button submitButton;
    public TextField videoURI;
    public AnchorPane pane;
    public Text videoTitle;
    public Button findVideoButton;
    public TextArea videoInfo;
    public ProgressIndicator progressBar;

    private boolean isAlive = true;
    private boolean noWait = false;
    private String mainURI;
    private JSONObject json;
    private String thumbnailURI;
    private Method finishMethod;

    private PegGenerator getPegGenerator(){
      YoutubePegGenerator pegGenerator = new YoutubePegGenerator(this.json);
      return pegGenerator;
    }

    public void passObjects(TextField videoURI, Method finishMethod){ // Behaves like a constructor
        this.videoURI = videoURI;
        this.finishMethod = finishMethod;
        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        this.closeHook(this.pane);
    }

    public void autoLoad(TextField videoURI, Method finishMethod, String link) throws IOException {
        this.videoURI = videoURI;
        this.finishMethod = finishMethod;
        this.youtubeLinkBox.setText(link);
        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        noWait = true;
        this.findVideo(null);
    }

    public void findVideo(MouseEvent mouseEvent) throws IOException {
        System.out.println("Finding video");
        findVideoButton.setDisable(true);
        videoTitle.setText("Loading...");
        new Thread(() -> {
            try {
                this.getVideoInfo();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        /* Start animation timer */
        AnimationTimer animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (isAlive) {
                    if (!findVideoButton.isDisabled()) {
                        try {
                            submitURI(null);
                            stop();
                        } catch (IOException e) {
                            e.printStackTrace();
                            stop();
                        }
                    }
                }
            }
        };
        if(noWait) animationTimer.start();
    }

    private void getVideoInfo() throws IOException {
        progressBar.setVisible(true);
        String link = youtubeLinkBox.getText();
        System.out.println(link);
        String command = "yt-dlp -j " + link;
        System.out.println(command);
        Process frGetter = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(frGetter.getInputStream()));
        String line = reader.readLine();
//        System.out.println(line);
        this.json = new JSONObject(line);
        this.videoTitle.setText(this.json.getString("title"));
        this.updateThumbnail();
        this.fillTable();
        findVideoButton.setDisable(false);
        submitButton.setDisable(false);
        JSONArray formats = this.json.getJSONArray("formats");
        for (int i = formats.length() - 1; i >= 0; i--){
            if (!formats.getJSONObject(i).getString("vcodec").equals("none") &&
                    !formats.getJSONObject(i).getString("acodec").equals("none")){
                mainURI = formats.getJSONObject(i).getString("url");
                break;
            }
        }
        progressBar.setVisible(false);
    }

    private void updateThumbnail(){
        JSONArray thumbs = this.json.getJSONArray("thumbnails");

        for (int i = thumbs.length() - 1; i > 0; i--){
            if (StringUtils.substringAfterLast(thumbs.getJSONObject(i).getString("url"), ".").equals("jpg")){
                this.thumbnailURI = thumbs.getJSONObject(i).getString("url");
                System.out.println(this.thumbnailURI);
                break;
            }
        }
        thumbnailViewer.setImage(new javafx.scene.image.Image(thumbnailURI));
    }

    private void fillTable(){
        NumberFormat fm = NumberFormat.getInstance();
        String date = this.json.getString("upload_date");
        date = date.substring(4, 6) + "/" + date.substring(5, 7) + "/" + date.substring(0, 4);
        int totalSecs = this.json.getInt("duration");
        int hours = totalSecs / 3600;
        int minutes = (totalSecs % 3600) / 60;
        int seconds = totalSecs % 60;
        String info = String.format("Resolution: %s\nFramerate: %s fps\nUploaded by: %s\nUploaded on: %s\nLength: %s\n" +
                        "View Count: %s\nLike count: %s\nDislike count: At least 1\nAge limit: %s\n\nDescription: %s",
                this.json.getString("resolution"),
                this.json.getInt("fps"),
                this.json.getString("uploader"),
                date,
                String.format("%02d:%02d:%02d", hours, minutes, seconds),
                fm.format(this.json.getInt("view_count")),
                fm.format(this.json.getInt("like_count")),
                this.json.getInt("age_limit"),
                this.json.getString("description"));
        videoInfo.setText(info);
    }

    public void submitURI(MouseEvent mouseEvent) throws IOException {
        videoURI.setText(mainURI);
        this.isAlive = false;
        finishMethod.execute(this.getPegGenerator());
        ((Stage) pane.getScene().getWindow()).close();
    }

    @Override
    public Window getWindow() {
        return pane.getScene().getWindow();
    }

    @Override
    public popOutType getType() {
        return popOutType.YoutubeFinderView;
    }

    @Override
    public boolean close() {
        isAlive = false;
        ((Stage) pane.getScene().getWindow()).close();
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
