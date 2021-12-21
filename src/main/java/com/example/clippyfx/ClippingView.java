package com.example.clippyfx;

import HelperMethods.SettingsWrapper;
import Interfaces.PopOut;
import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;

import java.io.*;
import java.util.ArrayList;

import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;

public class ClippingView implements PopOut {

    public ProgressBar progressBar;
    public TextArea ffmpegOutput;
    public TextField nameBox;
    public ChoiceBox typeBox;
    public TextField pathBox;
    public Button clipItButton;
    public Text progressText;
    public CheckBox enableMP4;

    private AnimationTimer timer;
    public Slider clipStart;
    public MediaPlayer mediaPlayer;
    public TextField VideoURI;
    public Slider clipEnd;
    public AnchorPane pain;

    private float clippingRate = 0.0f;
    private int currentFrame = 0;
    private int totalFrames = 0;
    private float currentSpeed = 0.0f;
    private float fps = 0.0f;
    private ArrayList<String> splitList;
    private boolean clipping = false;
    private boolean isAlive = true;
    private Process clipper;
    private TextArea youtubeData;
    private JSONArray videoURIs;

    public ClippingView() {

    }

    public void setProgress(double progress) {
        progressBar.setProgress(progress);
    }


    public void openExplorer(MouseEvent mouseEvent) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select a folder to save the clip to.");
        chooser.setInitialDirectory(new File(SettingsWrapper.getAdvancedLoadPath()));
        File selectedDirectory = chooser.showDialog(pain.getScene().getWindow());
        if (selectedDirectory != null) {
            pathBox.setText(selectedDirectory.getAbsolutePath());
        }
    }

    private void swapVisibility() {
        pathBox.setDisable(false);
        nameBox.setDisable(false);
        typeBox.setDisable(false);
//        clipItButton.setVisible(false);
        progressBar.setVisible(true);
    }

    private String getFFMPEGProgress(String line) {
        if (line.contains("frame=")) {
//            String[] split = line.split(" ");
//            splitList.clear();
//            for (String s : split) {
//                if (!s.equals("")) splitList.add(s);
//            }
//            this.currentFrame = Integer.parseInt(splitList.get(1));
//
//            for (String value: splitList) {
//                if (value.contains("fps=")) {
//                    String[] split2 = value.split("=");
//                    this.currentSpeed = Float.parseFloat(split2[1]);
//                }
//            }
//            line= "On frame " + currentFrame + " of " + totalFrames + " at " + currentSpeed + " fps.";
//            return line;

        }
        return "No progress available.";
    }

    private class progressUpdater extends AnimationTimer{

        private final BufferedReader reader;
        private final Process clipper;


        public progressUpdater(Process clipper) {
            this.clipper = clipper;
            this.reader = new BufferedReader(new InputStreamReader(clipper.getInputStream()));
            splitList = new ArrayList<>();
        }

        @Override
        public void handle(long now) {
            String line = null;
//            System.out.println("Reading");
            try {
                if (reader.ready()) {
                    line = reader.readLine();
//                    System.out.println(line);
                }
            }catch (IOException ignored){}
            if (line != null) {
                ffmpegOutput.appendText(line + "\n");
                progressText.setText(getFFMPEGProgress(line));
            }
            if (!clipper.isAlive()) {
                if (clipper.exitValue() != 0) {
                    System.out.println("Clipping failed.");
                    ffmpegOutput.setText("");
                    try {
                        while ((line = reader.readLine()) != null) {
                            System.out.println(line);
                            ffmpegOutput.appendText(line + "\n");
                        }
                    } catch (IOException ignored) {
                    }
                } else {
                    ffmpegOutput.appendText("Clipping successful.");
                    System.out.println("Clipping successful.");
                    progressBar.setProgress(1);
                }
                stop();
            }
        }
    }

    public void clipIt(MouseEvent mouseEvent) throws IOException {
        if (clipping){
//            // Cancel the current clip
//            Runtime.getRuntime().exec("taskkill /PID " + clipper.pid() + " /T /F");
//            System.out.println(clipper.pid());
//            clipper.destroyForcibly();
//            clipping = false;
//            ffmpegOutput.appendText("Clipping cancelled.\n");
            return;
        }
        System.out.println("Clipping...");
        swapVisibility();
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        double startTime = clipStart.getValue() / 100 * mediaPlayer.getTotalDuration().toSeconds();
        double endTime = clipEnd.getValue() / 100 * mediaPlayer.getTotalDuration().toSeconds();
        String fileName = VideoURI.getText().substring(6, VideoURI.getText().length());
        String fileSaveName = pathBox.getText() + "\\" + nameBox.getText();
        if (startTime > endTime) throw new IllegalArgumentException("Start time cannot be greater than end time.");
        String command = String.format("ffmpeg -ss %.2f -i \"%s\" -to %.2f -c:v libvpx-vp9 -c:a libopus -b:v 1500k -b:a 128k -y \"%s\"",
                startTime, fileName, endTime, fileSaveName);
        System.out.println(command);
        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
        builder.redirectErrorStream(true);
        clipper = builder.start();
        new progressUpdater(clipper).start();
        clipping = true;
        clipItButton.setText("Abort");
    }

    public void passObjects(MediaPlayer mediaPlayer, Slider clipStart, Slider clipEnd, TextField VideoURI, float fps,
                            TextArea isYoutube, JSONArray videoURIs) {
        closeHook(this.pain);
        this.mediaPlayer = mediaPlayer;
        this.clipStart = clipStart;
        this.clipEnd = clipEnd;
        this.VideoURI = VideoURI;
        this.fps = fps;
        this.youtubeData = isYoutube;
        this.videoURIs = videoURIs;
//        if (!this.youtubeData.getText().equals("")){
//
//        }

        int[] hs = {144, 240, 360, 480, 720, 1080, 1440, 2160};
        ArrayList<Integer> heights = new ArrayList<>();

        this.typeBox.setItems(FXCollections.observableArrayList(""));

        this.totalFrames = (int) ((clipEnd.getValue() / 100 * mediaPlayer.getTotalDuration().toSeconds() -
                clipStart.getValue() / 100 * mediaPlayer.getTotalDuration().toSeconds()) * fps);
    }

    @Override
    public Window getWindow() {
        return pain.getScene().getWindow();
    }

    @Override
    public popOutType getType() {
        return popOutType.ClippingView;
    }

    @Override
    public void close() {
        isAlive = false;
        ((Stage) pain.getScene().getWindow()).close();
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
