package com.example.clippyfx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.animation.AnimationTimer;
import javafx.util.Duration;
import javafx.stage.Stage;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class MainController {
    public TextField VideoURI;
    public AnchorPane VideoPain;
    public AnchorPane Pain;
    public MediaPlayer mediaPlayer;


    public Button LoadFileButton;
    public Button goButton;
    public FileChooser fileChooser;
    public Button LoadLink;
    public AnchorPane LoadPane;
    public AnimationTimer timer;
    public Slider clipStart;
    public Slider scrubBar;
    public Button playPauseButton;
    public Button endInsert;
    public Button startInsert;
    public Slider clipEnd;
    public Text clipStartText;
    public Text scrubBarText;
    public Text clipEndText;
    public Button clipItButton;
    public Button vp9LoadButton;
    public Button ejectButton;
    public TextArea youtubeData;

    private boolean isPlaying = false;
    private boolean scrubbing = false;
    private float fps = 30;
    public JSONArray videoURIs;

    @FXML
    private Label welcomeText;

    @FXML
    protected void onMediaLoad() throws IOException {
        System.out.println("Media loading...");
        Media media = new Media(VideoURI.getText());
        mediaPlayer = new MediaPlayer(media);
        clipEnd.setValue(100);
        mediaPlayer.setAutoPlay(false);
        mediaPlayer.setCycleCount(1);
        MediaView mediaView = new MediaView(mediaPlayer);
        mediaView.fitWidthProperty().bind(VideoPain.widthProperty());
        mediaView.fitHeightProperty().bind(VideoPain.heightProperty());
        mediaView.setPreserveRatio(true);
        VideoPain.getChildren().add(mediaView);
        mediaPlayer.setVolume(0.5);
        LoadPane.setVisible(false);
        isPlaying = true;
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if(!scrubbing) scrubBar.setValue((mediaPlayer.getCurrentTime().toSeconds()
                            / mediaPlayer.getTotalDuration().toSeconds()) * 100);
                clipStartText.setText(String.format("%.2f/%.2f",
                        clipStart.getValue() / 100 * mediaPlayer.getTotalDuration().toSeconds(),
                        mediaPlayer.getTotalDuration().toSeconds()));
                scrubBarText.setText(String.format("%.2f/%.2f",
                        scrubBar.getValue() / 100 * mediaPlayer.getTotalDuration().toSeconds(),
                        mediaPlayer.getTotalDuration().toSeconds()));
                clipEndText.setText(String.format("%.2f/%.2f",
                        clipEnd.getValue() / 100 * mediaPlayer.getTotalDuration().toSeconds(),
                        mediaPlayer.getTotalDuration().toSeconds()));
            }
        };
        timer.start();
        String command = "ffprobe -v error -select_streams v -of default=noprint_wrappers=1:nokey=1 -show_entries stream=r_frame_rate \"" + VideoURI.getText().substring(6, VideoURI.getText().length()) + "\"";
        System.out.println(command);
        Process frGetter = Runtime.getRuntime().exec(command);
        while(frGetter.isAlive()) {
            int i = 0;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(frGetter.getInputStream()));
        String line = reader.readLine();

        fps = (float) calcFrameRate(line);


        System.out.println("FPS: " + fps);
        setEnabledUI(true);
        System.out.println("Media loaded.");
    }


    private void setEnabledUI(boolean enabled) {
        clipStart.setDisable(!enabled);
        clipEnd.setDisable(!enabled);
        clipItButton.setDisable(!enabled);
        playPauseButton.setDisable(!enabled);
        startInsert.setDisable(!enabled);
        endInsert.setDisable(!enabled);
        scrubBar.setDisable(!enabled);
        ejectButton.setDisable(!enabled);
    }

    public void ejectMedia(MouseEvent mouseEvent) {
        System.out.println("Ejecting media...");
        mediaPlayer.stop();
        mediaPlayer.dispose();
        timer.stop();
        VideoPain.getChildren().remove(1);
        setEnabledUI(false);
        LoadPane.setVisible(true);
    }

    protected double calcFrameRate(String ffprobeOutput) {
        if (ffprobeOutput == null) return 30f;
        String[] sections = ffprobeOutput.split("/");
        return (double) Integer.parseInt(sections[0]) / (double) Integer.parseInt(sections[1]);
    }

    public void playPauseVideo(MouseEvent mouseEvent) {
        if(mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
            playPauseButton.setText("Play");
            isPlaying = false;
        } else {
            mediaPlayer.play();
            playPauseButton.setText("Pause");
            isPlaying = true;
        }
    }


    public void loadFileOption(MouseEvent mouseEvent) {
        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video Files",
                "*.mp4", "*.avi", "*.mov"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MP4 Files", "*.mp4"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("AVI Files", "*.avi"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MOV Files", "*.mov"));
        fileChooser.setTitle("Choose a file to clip");
        // TODO: Make this a configurable option
        fileChooser.setInitialDirectory(new java.io.File("C:\\Users\\Public\\Videos"));
        java.io.File file = fileChooser.showOpenDialog(Pain.getScene().getWindow());
        if (file != null) {
            VideoURI.setText(file.toURI().toString());
        }
    }

    public void loadLinkOption(MouseEvent mouseEvent) {
        String link = VideoURI.getText();
        // Get URI from ffmpeg <--- look at this duuuude

    }

    public void scrubPressed(MouseEvent mouseEvent) {
        mediaPlayer.pause();
        scrubbing = true;
    }

    public void scrubReleased(MouseEvent mouseEvent) {
        new Thread(() -> mediaPlayer.seek(mediaPlayer.getMedia().getDuration().multiply(scrubBar.getValue() / 100))).start();
        if (isPlaying) {
            mediaPlayer.play();
        }else{
            mediaPlayer.pause();
        }
        scrubbing = false;
    }

    public void setStart(MouseEvent mouseEvent) {
        clipStart.setValue(mediaPlayer.getCurrentTime().toSeconds() / mediaPlayer.getTotalDuration().toSeconds() * 100);
    }

    public void setEnd(MouseEvent mouseEvent) {
        clipEnd.setValue(mediaPlayer.getCurrentTime().toSeconds() / mediaPlayer.getTotalDuration().toSeconds() * 100);
    }

    public void barScrubbed(MouseEvent mouseEvent) {
    }

    public void keyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.COMMA) {
            // Skip back one frame
            new Thread(() -> mediaPlayer.seek(Duration.seconds(mediaPlayer.getCurrentTime().toSeconds() - 1 / fps))).start();
        } else if (keyEvent.getCode() == KeyCode.PERIOD) {
            new Thread(() -> mediaPlayer.seek(Duration.seconds(mediaPlayer.getCurrentTime().toSeconds() + 1 / fps))).start();
        }
    }

    public void clipIt(MouseEvent mouseEvent) throws IOException, InterruptedException {
        // ffmpeg -ss {startTime} -i '{file/URI}' -c:v libvpx-vp9
        System.out.println(youtubeData);
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("clipping-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        Stage stage = new Stage();
        stage.setTitle("ClippyFX Clipping");
        stage.setScene(scene);
        stage.show();
        ClippingView clippingProgressWindow = fxmlLoader.getController();
        clippingProgressWindow.passObjects(mediaPlayer, clipStart, clipEnd, VideoURI, fps, youtubeData, videoURIs);

    }

    public void loadVP9(MouseEvent mouseEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("compatablityator-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        Stage stage = new Stage();
        stage.setTitle("ClippyFX: Advanced video loader");
        stage.setScene(scene);
        stage.show();
        CompatabilityatorView compat  = fxmlLoader.getController();
        compat.passObjects(VideoURI);
    }

    public void loadYoutube(MouseEvent mouseEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("youtube-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 213);
        Stage stage = new Stage();
        stage.setTitle("ClippyFX: Youtube video loader");
        stage.setScene(scene);
        stage.show();
        YoutubeView youtubeSelectView = fxmlLoader.getController();
        youtubeSelectView.passObjects(VideoURI, videoURIs, youtubeData);
    }
}