package com.example.clippyfx;

import HelperMethods.PegArgument;
import HelperMethods.VideoChecks;
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
    public CheckBox open_toggle;

    private AnimationTimer timer;
    public double clipStart;
    public MediaPlayer mediaPlayer;
    public TextField VideoURI;
    public double clipEnd;

    private float clippingRate = 0.0f;
    private int totalFrames = 0;
    private String currentSize = "";
    private float fps = 0.0f;
    private ArrayList<String> splitList;
    private boolean clipping = false;
    private boolean isAlive = true;
    private PegGenerator pegGenerator;
    private Process clipper;

    public ClippingView() {

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
            try {
                if (reader.ready()) line = reader.readLine();
            }catch (IOException e){
                e.printStackTrace();
            }
            if (line != null) {
                ffmpegOutput.appendText(line + "\n");
                progressText.setText(FFmpegWrapper.getFFMPEGProgress(line, clipEnd-clipStart,
                        fps, totalFrames, progressBar));
                double pos = FFmpegWrapper.getPlaybackPercent((int) (clipStart*fps),
                        (int) ((int) mediaPlayer.getTotalDuration().toSeconds() * fps));
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
                    progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
                } else {
                    ffmpegOutput.appendText("Clipping successful.");
                    System.out.println("Clipping successful.");
                    progressBar.setProgress(1);
                    // Open the file in file explorer
                    try {
                        if (open_toggle.isSelected()) {
                            Runtime.getRuntime().exec("explorer " + pathBox.getText());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                clipItButton.setDisable(true);
                stop();
            }
        }
    }

    private VideoChecks.Encoders getEncoder() {
        return VideoChecks.Encoders.valueOf(presetBox.getValue().toString());
    }

    /**
     * Gets the size of the video to be clipped.
     * @return The size of the video to be clipped.
     */
    private VideoChecks.Sizes getSize() {
        if (videoSizeSelect.getValue().toString().equals("Source")) {
            return VideoChecks.Sizes.Source;
        }else{
            return VideoChecks.Sizes.valueOf("x" + videoSizeSelect.getValue().toString());
        }
    }

    /**
     * Called when the user clicks the clipIt button.
     * @param mouseEvent The mouse event that triggered the method. (Not used)
     * @throws IOException If the file cannot be found.
     * @throws InterruptedException If the process is interrupted.
     */
    public void clipIt(MouseEvent mouseEvent) throws IOException, InterruptedException {
        if (clipping){
            FFmpegWrapper.killProcess(clipper);
            clipping = false;
            return;
        }
        if (nameBox.getText().equals("")) nameBox.setText("clip");
        mediaPlayer.pause();
        System.out.println("Clipping...");
        swapVisibility();
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        String fileSaveName = pathBox.getText() + "\\" + nameBox.getText();
        if (clipStart > clipEnd) throw new IllegalArgumentException("Start time cannot be greater than end time.");
        PegArgument args = new PegArgument(this.getEncoder(), this.getSize(), sizeCap.isSelected(),
                Double.parseDouble(fpsSelect.getText()));
        String command = pegGenerator.buildPeg(fileSaveName, args);
        System.out.println(command);
        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
        builder.redirectErrorStream(true);
        this.clipper = builder.start();
        new progressUpdater(clipper).start();
        clipping = true;
        clipItButton.setText("Abort");
        // TODO: GIANT GREEN CHECK MARK BABYYY WHOOOOOOOOOOO - Nick
    }

    /**
     * The method used to pass the media player and peg generator to this class.
     * @param mediaPlayer  The media player to be used.
     * @param pegGenerator The peg generator to be used.
     */
    @SuppressWarnings("unchecked")
    public void passObjects(MediaPlayer mediaPlayer, PegGenerator pegGenerator) {
        closeHook(this.pain);

        this.mediaPlayer = mediaPlayer;
        this.clipStart = pegGenerator.getStartTime();
        this.clipEnd = pegGenerator.getEndTime();
        this.fps = (float) pegGenerator.getFPS();
        this.pegGenerator = pegGenerator;
        this.totalFrames = (int) pegGenerator.getTotalFrames();
        this.pathBox.setText(pegGenerator.getPreferredSaveLocation());

        this.sizeCap.setSelected(SettingsWrapper.getSetting("defaultAllow100MB").bool());

        presetBox.setItems(FXCollections.observableArrayList(pegGenerator.getEncoders()));
        videoSizeSelect.setItems(FXCollections.observableArrayList(pegGenerator.getSizes()));
        if (FXCollections.observableArrayList(pegGenerator.getEncoders()). // Make sure the preferred value is actually
                contains(SettingsWrapper.getSetting("preferredOutputEncoder").value)){ // available.
            this.presetBox.setValue(SettingsWrapper.getSetting("preferredOutputEncoder").value);
        }else this.presetBox.setValue(pegGenerator.getEncoders().get(0));

        if (FXCollections.observableArrayList(pegGenerator.getSizes()). // Make sure the preferred value is actually
                contains(SettingsWrapper.getSetting("preferredVideoSize").value)){ // available.
            this.videoSizeSelect.setValue(SettingsWrapper.getSetting("preferredVideoSize").value);
        }else this.videoSizeSelect.setValue(pegGenerator.getSizes().get(0));

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
        if (clipper != null && clipper.isAlive()) {
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
        if (clipper != null && clipper.isAlive()) {
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
