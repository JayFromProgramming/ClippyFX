package com.example.clippyfx;

import EncodingMagic.FilePegGenerator;
import HelperMethods.VideoChecks;
import HelperMethods.FFmpegWrapper;
import HelperMethods.SettingsWrapper;
import HelperMethods.StreamedCommand;
import Interfaces.Method;
import Interfaces.PopOut;
import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ImporterView implements PopOut {

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

    private boolean isAlive = true;
    private final float clippingRate = 0.0f;
    private int totalFrames = 0;
    private double totalDuration = 0;
    private float fps = 0.0f;
    private ArrayList<String> splitList;
    private boolean clipping = false;
    private Process clipper;
    private FileChooser fileChooser;
    private Method finishMethod;
    private FilePegGenerator pegGenerator;

    public ImporterView() {

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
//        clipItButton.setDisable(true);
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
            }catch (IOException e){
                e.printStackTrace();
            }
            if (line != null) {
                ffmpegOutput.appendText(line + "\n");
                progressText.setText(FFmpegWrapper.getFFMPEGProgress(line, totalDuration, fps, totalFrames, progressBar));
            }
            if (!clipper.isAlive()) {
                if (clipper.exitValue() != 0) {
                    System.out.println("Conversion failed.");
                    progressText.setText("FATAL: Conversion failed.");
                    ffmpegOutput.setText("");
                    try {
                        while ((line = reader.readLine()) != null) {
                            System.out.println(line);
                            ffmpegOutput.appendText(line + "\n");
                        }
                    } catch (IOException ignored) {}
                    clipping=false;
                    clipItButton.setDisable(false);
                    clipItButton.setText("Convert");
                    typeBox.setDisable(false);
                    stop();
                } else {
                    stop();
                    try {
                        finish();
                    } catch (IOException ignored) {}
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void checkHWACCEL() {
        ArrayList<VideoChecks.Encoders> encoders = VideoChecks.getEncoders();
        if (encoders.contains(VideoChecks.Encoders.h264_nvenc)) {
            typeBox.getSelectionModel().select("h264_nvenc");
        } else if (encoders.contains(VideoChecks.Encoders.h264_amf)) {
            typeBox.getSelectionModel().select("h264_amf");
        }else if (encoders.contains(VideoChecks.Encoders.libx264)) {
            typeBox.getSelectionModel().select("libx264");
        } else {
            throw new IllegalStateException("No encoder found.");
        }
    }

    private String selectHWACCEL(){
        return switch (typeBox.getSelectionModel().getSelectedItem().toString()) {
            case "h264_nvenc" -> "ffmpeg -i \"%s\" -c:v h264_nvenc -c:a aac -preset:v p2 -cq:v 23 -b:a 128k -y \"%s\"";
            case "h264_amf" -> "ffmpeg -i \"%s\" -c:v h264_amf -c:a aac -preset:v p2 -cq:v 23 -b:a 128k -y \"%s\"";
            case "libx264" -> "ffmpeg -i \"%s\" -c:v libx264 -c:a aac -preset:v p2 -cq:v 23 -b:a 128k -y \"%s\"";
            default -> throw new IllegalStateException("Unexpected value: " + typeBox.getSelectionModel().getSelectedItem());
        };
    }

    protected double calcFrameRate(String ffprobeOutput) {
        if (ffprobeOutput == null) return 30f;
        String[] sections = ffprobeOutput.split("/");
        return (double) Integer.parseInt(sections[0]) / (double) Integer.parseInt(sections[1]);
    }

    public void convertIt() throws IOException, InterruptedException {
        if (clipping){
            FFmpegWrapper.killProcess(clipper);
            clipping=false;
            return;
        }
        swapVisibility();
        String fileName = pathBox.getText();
        String fileSaveName = nameBox.getText();

        ffmpegOutput.appendText("Starting conversion...\nCalculating duration and framerate...\n");
        String probe_command = "ffprobe -v error -select_streams v -of default=noprint_wrappers=1:nokey=1 -show_entries stream=r_frame_rate \"" + fileName + "\"";
        fps = (float) calcFrameRate(StreamedCommand.getCommandOutput(probe_command));
        ffmpegOutput.appendText("Calculated framerate: " + fps + "\n");
        probe_command = "ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 \"" + fileName + "\"";
        this.totalDuration = Double.parseDouble(StreamedCommand.getCommandOutput(probe_command));
        ffmpegOutput.appendText("Calculated duration: " + totalDuration + "\n");
        this.totalFrames = (int) (fps * totalDuration);
        ffmpegOutput.appendText("Calculated total frames: " + totalFrames + "\n");
        String command = String.format(selectHWACCEL(), fileName, fileSaveName);
        ffmpegOutput.appendText("Conversion command: " + command + "\n");
        clipper = StreamedCommand.runCommand(command);
        ffmpegOutput.appendText("Command loaded, starting conversion.\n");
        new progressUpdater(clipper).start();
        clipping = true;
        clipItButton.setText("Abort");
    }


    private void finish() throws IOException {
        clipping = false;
        progressBar.setProgress(1);
        File temp = new File(nameBox.getText());
        if (temp.exists()) {
            ffmpegOutput.appendText("Conversion successful.");
            System.out.println("Conversion successful.");
            VideoURI.setText(temp.toURI().toString());
            temp.deleteOnExit(); // Delete the temp file after normal termination of the JVM
            System.out.println("Temp file marked for deletion on exit.");
            isAlive = false;
            this.pegGenerator.setTempFile(temp.getAbsolutePath());
            ((Stage) pain.getScene().getWindow()).close();
            this.finishMethod.execute(this.pegGenerator);
        } else {
            ffmpegOutput.appendText("Conversion failed unable to find output file.");
            System.out.println("Conversion failed unable to find output file.");
        }
    }

    public void bypassFileChooser(File file, Method execute) throws IOException, InterruptedException {
        this.finishMethod = execute;
        this.closeHook(this.pain);
        pathBox.setDisable(true);
        nameBox.setDisable(true);
        typeBox.setDisable(true);
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        checkHWACCEL();
        preformImport(file);
    }

    @SuppressWarnings("unchecked")
    public void passObjects(TextField VideoURI, Method execute) throws IOException, InterruptedException {

        this.VideoURI = VideoURI;
        this.finishMethod = execute;
        this.closeHook(this.pain);
        pathBox.setDisable(true);
        nameBox.setDisable(true);
        typeBox.setDisable(true);
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        checkHWACCEL();
        typeBox.setItems(FXCollections.observableArrayList("h264_nvenc", "h264_amf", "libx264"));
        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video Files",
                "*.mp4", "*.avi", "*.mkv", "*.mov", "*.webm", "*.flv", "*.wmv", "*.mpg", "*.mpeg", "*.m4v",
                "*.mxf", "*.rm", "*.rmvb", "*.ogv", "*.ogm", "*.3gp", "*.3g2", "*.m2ts", "*.mts", "*.ts"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Advanced Formats",
                "*.webm", "*.mkv", "*.mov"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Basic Formats",
                "*.mp4", "*.avi", "*.mov"));
        fileChooser.setInitialDirectory(new File(SettingsWrapper.getSetting("defaultAdvancedLoadPath").value));
        java.io.File file = fileChooser.showOpenDialog(pain.getScene().getWindow());
        this.pegGenerator = new FilePegGenerator(file.toURI().toString());
        fileChooser = null;
        preformImport(file);
    }


    private void preformImport(File file) throws IOException, InterruptedException {
        if (file != null) {
            ffmpegOutput.appendText("Loading file: " + file.getAbsolutePath() + "\n");
            ffmpegOutput.appendText("Determining encoding type\n");
            VideoChecks.checkAllowedSizes(file);
            // Check if the file is a supported encoding
            String encoding = StreamedCommand.getCommandOutput("ffprobe -v error -select_streams v -of default=noprint_wrappers=1:nokey=1 -show_entries stream=codec_name \"" + file.getAbsolutePath() + "\"");
            ffmpegOutput.appendText("Encoding type: " + encoding + "\n");
            System.out.println("Encoding type: " + encoding);
            if (encoding.equals("h264") && file.getName().endsWith(".mp4")) {
                // If it is, bypass the file conversion and just set the video URI
                System.out.println("Encoding type is h264, bypassing conversion.");
                VideoURI.setText(file.toURI().toString());
                isAlive = false;
                ((Stage) pain.getScene().getWindow()).close();
                this.finishMethod.execute(this.pegGenerator);
            }else{
                // If not, convert it to a supported encoding
                pathBox.setText(file.getAbsolutePath());
                nameBox.setText("resources/videoResources/TempWorkingFile.mp4");
                convertIt();
            }
        } else {
            ffmpegOutput.appendText("No file selected.\n");
            ((Stage) pain.getScene().getWindow()).close();
        }
    }

    @Override
    public Window getWindow() {
        return pain.getScene().getWindow();
    }

    @Override
    public popOutType getType() {
        return popOutType.ConverterView;
    }

    @Override
    public boolean close() {
        if (clipper != null && clipper.isAlive()) {
            try{FFmpegWrapper.killProcess(clipper);}catch (Exception ignored){}
            if(clipper.isAlive()) return false;
        }
        if (fileChooser != null) {
            return false;
        }
        ((Stage) pain.getScene().getWindow()).close();
        return true;
    }

    @Override
    public boolean isAlive() {
        return isAlive;
    }

    private void onClose(WindowEvent event) throws IllegalStateException {
        if (clipper != null && clipper.isAlive()) {
            try{FFmpegWrapper.killProcess(clipper);}catch (Exception ignored){}
            event.consume();
        }
        if (fileChooser != null) {
            event.consume();
        }
        System.out.println("Window closed.");
        this.isAlive = false;
    }

    private void closeHook(AnchorPane pain){
        pain.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::onClose);
    }

}
