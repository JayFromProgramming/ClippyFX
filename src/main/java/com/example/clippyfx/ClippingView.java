package com.example.clippyfx;

import HelperMethods.EncoderCheck;
import HelperMethods.FFmpegWrapper;
import HelperMethods.SettingsWrapper;
import Interfaces.PopOut;
import Interfaces.PegGenerator;
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

public class ClippingView implements PopOut {

    public ProgressBar progressBar;
    public TextArea ffmpegOutput;
    public TextField nameBox;
  
    public TextField pathBox;
    public TextField uri;
    public Button clipItButton;
    public Text progressText;

    public ChoiceBox videoSizeSelect;
    public ChoiceBox presetBox;
    public CheckBox sizeCap;
    public AnchorPane pain;
    public TextField fpsSelect;

    private AnimationTimer timer;
    public Slider clipStart;
    public MediaPlayer mediaPlayer;
    public TextField VideoURI;
    public Slider clipEnd;

    private float clippingRate = 0.0f;
    private int totalFrames = 0;
    private String currentSize = "";
    private float fps = 0.0f;
    private ArrayList<String> splitList;
    private boolean clipping = false;
    private boolean isAlive = true;
    private PegGenerator presetSelector;
    private Process clipper;
    private double startTime;
    private double endTime;

    public ClippingView() {

    }

    public void setProgress(double progress) {
        progressBar.setProgress(progress);
    }


    public void openExplorer(MouseEvent mouseEvent) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select a folder to save the clip to.");
        chooser.setInitialDirectory(new File(pathBox.getText()));
        File selectedDirectory = chooser.showDialog(pain.getScene().getWindow());
        if (selectedDirectory != null) {
            pathBox.setText(selectedDirectory.getAbsolutePath());
        }
    }

    private void swapVisibility() {
        pathBox.setDisable(true);
        nameBox.setDisable(true);
        sizeCap.setDisable(true);
        videoSizeSelect.setDisable(true);
        presetBox.setDisable(true);
        fpsSelect.setDisable(true);
        progressBar.setVisible(true);
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
                progressText.setText(FFmpegWrapper.getFFMPEGProgress(line, endTime-startTime,
                        fps, totalFrames, progressBar));
                double pos = FFmpegWrapper.getPlaybackPercent((int) (startTime*fps),
                        (int) ((int) mediaPlayer.getTotalDuration().toSeconds() * fps));
                System.out.println(pos);
                new Thread(() -> mediaPlayer.seek(mediaPlayer.getMedia().getDuration().multiply(pos))).start();

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

    public void clipIt(MouseEvent mouseEvent) throws IOException, InterruptedException {
        if (clipping){
            FFmpegWrapper.killProcess(clipper);
            clipping = false;
            return;
        }
        if (Double.parseDouble(fpsSelect.getText()) > fps) fpsSelect.setText(String.valueOf(fps));
        if (nameBox.getText().equals("")) nameBox.setText("clip");
        mediaPlayer.pause();
        System.out.println("Clipping...");
        swapVisibility();
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        startTime = clipStart.getValue() / 100 * mediaPlayer.getTotalDuration().toSeconds();
        endTime = clipEnd.getValue() / 100 * mediaPlayer.getTotalDuration().toSeconds();
        String fileName = uri.getText();
        if (fileName.contains("file:")) {
            fileName = fileName.substring(6);
        }
        String fileSaveName = pathBox.getText() + "\\" + nameBox.getText();
        if (startTime > endTime) throw new IllegalArgumentException("Start time cannot be greater than end time.");
        String command = String.format("ffmpeg -ss %.2f -i \"%s\" -to %.2f -c:v libvpx-vp9 -c:a libopus -b:v 1500k -b:a 128k -y \"%s.webm\"",
                startTime, fileName, endTime - startTime, fileSaveName);
        System.out.println(command);
        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
        builder.redirectErrorStream(true);
        this.clipper = builder.start();
        new progressUpdater(clipper).start();
        clipping = true;
        clipItButton.setText("Abort");
        // TODO: GIANT GREEN CHECK MARK BABYYY WHOOOOOOOOOOO - Nick
    }

    @SuppressWarnings("unchecked")
    public void passObjects(MediaPlayer mediaPlayer, Slider clipStart, Slider clipEnd, TextField VideoURI, float fps,
                            PegGenerator presetSelector, TextField videoURI) {
        closeHook(this.pain);
        this.mediaPlayer = mediaPlayer;
        this.clipStart = clipStart;
        this.clipEnd = clipEnd;
        this.VideoURI = VideoURI;
        this.fps = fps;
        this.presetSelector = presetSelector;
        this.uri = videoURI;
        this.pathBox.setText(SettingsWrapper.getBasicSavePath());

        this.totalFrames = (int) ((clipEnd.getValue() / 100 * mediaPlayer.getTotalDuration().toSeconds() -
                clipStart.getValue() / 100 * mediaPlayer.getTotalDuration().toSeconds()) * fps);

        this.sizeCap.setSelected(SettingsWrapper.getSettingsBoolean("defaultAllow100MB"));
        presetBox.setItems(FXCollections.observableArrayList(EncoderCheck.getEncodersString()));
        videoSizeSelect.setItems(FXCollections.observableArrayList(EncoderCheck.getAllowedSizesString()));
        presetBox.setValue(SettingsWrapper.getSettingsString("preferredOutputEncoder"));
        videoSizeSelect.setValue(SettingsWrapper.getSettingsString("preferredVideoSize"));
        fpsSelect.setText(String.valueOf(fps));
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
    public boolean close() {
        if (clipper.isAlive()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Clip in progress");
            alert.setHeaderText("Can't close while clipping is in progress.");
            alert.setContentText("Please wait until conversion is complete.");
            alert.show();
            return false;
        }
        ((Stage) pain.getScene().getWindow()).close();
        isAlive = false;
        return true;
    }

    @Override
    public boolean isAlive() {
        return isAlive;
    }

    private void onClose(WindowEvent event) {
        if (clipper.isAlive()) {
            event.consume();
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Clip in progress");
            alert.setHeaderText("Can't close while clipping is in progress.");
            alert.setContentText("Please wait until conversion is complete.");
            alert.showAndWait();
            return;
        }
        System.out.println("Window closed.");
        this.isAlive = false;
    }

    private void closeHook(AnchorPane pain){
        pain.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::onClose);
    }

}
