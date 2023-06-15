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
        if (line == null){
            this.videoTitle.setText("Error: Video not found");
            findVideoButton.setDisable(false);
            progressBar.setVisible(false);
            this.videoInfo.setText("yt-dlp returned null...\n" +
                    "Make sure yt-dlp is up to date (yt-dlp -U)");
            return;
        }
        this.json = new JSONObject(line);
        this.videoTitle.setText(this.json.optString("title"));
        this.updateThumbnail();
        this.fillTable();
        findVideoButton.setDisable(false);
        submitButton.setDisable(false);
        JSONArray formats = this.json.getJSONArray("formats");
        for (int i = formats.length() - 1; i >= 0; i--){
            if (!formats.getJSONObject(i).optString("vcodec").equals("none") &&
                    !formats.getJSONObject(i).optString("acodec").equals("none")){
                mainURI = formats.getJSONObject(i).optString("url");
                break;
            }
        }
        progressBar.setVisible(false);
    }

    private void updateThumbnail(){
        JSONArray thumbs = this.json.getJSONArray("thumbnails");

        for (int i = thumbs.length() - 1; i > 0; i--){
            // Check if the url contains somewhere in it .jpg
            if (thumbs.getJSONObject(i).optString("url").contains(".jpg")){
                this.thumbnailURI = thumbs.getJSONObject(i).optString("url");
                System.out.println(this.thumbnailURI);
                break;
            }
//            if (StringUtils.substringAfterLast(thumbs.getJSONObject(i).getString("url"), ".").equals("jpg")){
//                this.thumbnailURI = thumbs.getJSONObject(i).getString("url");
//                System.out.println(this.thumbnailURI);
//                break;
//            }
        }
        if (this.thumbnailURI == null){
            return;
        }
        thumbnailViewer.setImage(new javafx.scene.image.Image(thumbnailURI));
    }

    private void fillTable(){
        System.out.println(this.json.toString(4));
        NumberFormat fm = NumberFormat.getInstance();
        String date = this.json.optString("upload_date");
        if (date.length() != 8) {
            System.out.println("Warning: upload date is not 8 characters");
        } else {
            date = date.substring(4, 6) + "/" + date.substring(5, 7) + "/" + date.substring(0, 4);
        }
        float totalSecs = this.json.optFloat("duration");
        if (totalSecs == 0) {
            System.out.println("Warning: duration is 0");
        }
        int hours = (int) (totalSecs / 3600);
        int minutes = (int) ((totalSecs % 3600) / 60);
        int seconds = (int) (totalSecs % 60);
        int millis = (int) ((totalSecs - (int) totalSecs) * 1000);

        String website = this.json.optString("webpage_url");
        // Convert to just the top level domain
        website = website.substring(website.indexOf("://") + 3);
        website = website.substring(0, website.indexOf("/"));


        String info = String.format("""
                        Website: %s
                        Extractor: %s
                        Resolution: %s
                        Framerate: %s fps
                        Uploaded by: %s
                        Uploaded on: %s
                        Length: %s
                        View Count: %s
                        Age limit: %s

                        Description: %s""",
                website,
                this.json.optString("extractor"),
                this.json.optString("resolution"),
                this.json.optInt("fps"),
                this.json.optString("uploader"),
                date,
                String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis),
                fm.format(this.json.optInt("view_count")),
//                fm.format(this.json.getInt("like_count")),
                this.json.optInt("age_limit"),
                this.json.optString("description"));
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
