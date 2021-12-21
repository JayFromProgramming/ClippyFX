package com.example.clippyfx;

import UsefulThings.StreamedCommand;
import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class CompatabilityatorView {

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

    private final float clippingRate = 0.0f;
    private int totalFrames = 0;
    private final float fps = 0.0f;
    private ArrayList<String> splitList;
    private boolean clipping = false;
    private Process clipper;
    private TextArea youtubeData;
    private JSONArray videoURIs;

    public CompatabilityatorView() {

    }

    public void setProgress(double progress) {
        progressBar.setProgress(progress);
    }


    private void swapVisibility() {
        pathBox.setDisable(true);
        nameBox.setDisable(true);
        typeBox.setDisable(true);
        enableMP4.setDisable(true);
        enableMP4.setSelected(true);
        progressBar.setVisible(true);
        clipItButton.setDisable(true);
    }

    private String getFFMPEGProgress(String line) {
        if (line.contains("frame=")) {
            int currentFrame = Integer.parseInt(StringUtils.substringBetween(line, "frame=", "fps=").replaceAll("\\s", ""));
            float currentfps = Float.parseFloat(StringUtils.substringBetween(line, "fps=", "q=").replaceAll("\\s", ""));
            String currentSize = StringUtils.substringBetween(line, "size=", "time=").replaceAll("\\s", "");
            String currentBitrate = StringUtils.substringBetween(line, "bitrate=", "speed=").replaceAll("\\s", "");
            String currentSpeed = StringUtils.substringAfter(line, "speed=").replaceAll("\\s", "");
//            this.progressBar.setProgress((float) currentFrame / totalFrames);
            line = String.format("Frame %s of %s | %s, %s speed", currentFrame, totalFrames, currentBitrate, currentSpeed);
            return line;
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
            try {
                if (reader.ready()) {
                    line = reader.readLine();
                    System.out.println(line);
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
                    stop();
                    File temp = new File("TempWorkingFile.mp4");
                    if (temp.exists()) {
                        VideoURI.setText(temp.toURI().toString());
                        temp.deleteOnExit(); // Clean up after yourself
                    }
                    ((Stage) pain.getScene().getWindow()).close();
                }
                stop();
            }
        }
    }

    private String checkHWACCEL(String fileName) throws IOException {
        Process hwaccel = StreamedCommand.runCommand("ffmpeg -i \"" + fileName + "\" -c:v h264_nvenc -frames 1 -f null NUL");
        int resultCode = StreamedCommand.waitForExit(hwaccel);
        String command;
        if (resultCode == 0) {
            command = "ffmpeg -i \"%s\" -c:v h264_nvenc -c:a aac -preset:v p2 -cq:v 23 -b:a 128k -y \"%s\"";
        } else{
            hwaccel = StreamedCommand.runCommand("ffmpeg -i \"" + fileName + "\" -c:v h264_amf -frames 1 -f null NUL");
            resultCode = StreamedCommand.waitForExit(hwaccel);
            if (resultCode == 0){
                command = "ffmpeg -i \"%s\" -c:v h264_amf -c:a aac -quality speed -b:a 128k -y \"%s\"";
            } else{
                command = "ffmpeg -i \"%s\" -c:v libx264 -c:a aac -crf:v 25 -b:a 128k -y \"%s\"";
            }
        }
        return command;
    }

    public void clipIt() throws IOException {
        if (clipping){
            return;
        }
        System.out.println("Making clip shit n stuff...");
        swapVisibility();
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        String fileName = pathBox.getText();
        String fileSaveName = nameBox.getText();
        String command = String.format(checkHWACCEL(fileName),
                fileName, fileSaveName);
        clipper = StreamedCommand.runCommand(command);
        new progressUpdater(clipper).start();
        clipping = true;
        clipItButton.setText("Abort");
        // TODO: GIANT GREEN CHECK MARK BABYYY WHOOOOOOOOOOO - Nick
    }

    public void passObjects(TextField VideoURI) throws IOException {

        this.VideoURI = VideoURI;
        pathBox.setDisable(true);
        nameBox.setDisable(true);
        typeBox.setDisable(true);
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Advanced Formats",
                "*.webm", "*.mkv", "*.mov"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Supported Formats",
                "*.mp4", "*.avi", "*.mov"));
        fileChooser.setInitialDirectory(new java.io.File("C:\\Users\\Public\\Videos"));
        java.io.File file = fileChooser.showOpenDialog(pain.getScene().getWindow());
        if (file != null) {
            pathBox.setText(file.getAbsolutePath());
            nameBox.setText("TempWorkingFile.mp4");
        }
        clipIt();
    }
}
