package com.example.clippyfx;

import EncodingMagic.FilePegGenerator;
import HelperMethods.StreamedCommand;
import Interfaces.Method;
import Interfaces.PegGenerator;
import Interfaces.PopOut;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.animation.AnimationTimer;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class MainController {
    public TextField VideoURI;
    public AnchorPane VideoPain;
    public AnchorPane Pain;
    public MediaPlayer mediaPlayer;


    public Button LoadFileButton;
    public Button goButton;
    public FileChooser fileChooser;
    public Button youtubeButton;
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
    public Button settingsButton;

    private boolean isPlaying = false;
    private boolean scrubbing = false;
    private float fps = 30;
    private ArrayList<PopOut> popOuts = new ArrayList<>();
    private PegGenerator pegGenerator;

    public void onClose(WindowEvent event) {
        for (PopOut popOut : popOuts) {
            if(!popOut.close()) {
                System.out.println("Close cancelled because " + popOut.getType() + " is open");
                event.consume();
                popOut.getWindow().requestFocus();
            }
        }
    }

    protected void onDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    protected void onDragNDrop(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            File file = event.getDragboard().getFiles().get(0);
            try {
                loadFileDirect(file);
            } catch (IOException | InterruptedException e) {
                System.out.println("Drag n drop failed");
                e.printStackTrace();
            }
        }
        event.consume();
    }

    @FXML
    protected void onMediaLoad(MouseEvent ignored) throws IOException {
        loadMedia(new FilePegGenerator());
    }

    protected void loadMedia(PegGenerator pegGenerator) throws IOException {
        System.out.println("Media loading...");
        setEnabledUI(true);
        this.pegGenerator = pegGenerator;
        Media media = new Media(VideoURI.getText());
        mediaPlayer = new MediaPlayer(media);
        clipEnd.setValue(100);
        mediaPlayer.setAutoPlay(true);
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
        if (pegGenerator.getType() == PegGenerator.PegType.Youtube) {
            fps = 30;
        } else {
            String command = "ffprobe -v error -select_streams v -of default=noprint_wrappers=1:nokey=1 -show_entries stream=r_frame_rate \"" + VideoURI.getText().substring(6) + "\"";
            fps = (float) calcFrameRate(StreamedCommand.getCommandOutput(command));
        }
        System.out.println("FPS: " + fps);
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

    public void keyPressed(KeyEvent keyEvent) throws IOException, InterruptedException {
        if (keyEvent.getCode() == KeyCode.COMMA) {
            new Thread(() -> mediaPlayer.seek(Duration.seconds(mediaPlayer.getCurrentTime().toSeconds() - 1 / fps))).start();
        } else if (keyEvent.getCode() == KeyCode.PERIOD) {
            new Thread(() -> mediaPlayer.seek(Duration.seconds(mediaPlayer.getCurrentTime().toSeconds() + 1 / fps))).start();
        } else if (keyEvent.getCode() == KeyCode.B) {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                playPauseButton.setText("Play");
            } else {
                mediaPlayer.play();
                playPauseButton.setText("Pause");
            }
        } else if (keyEvent.getCode() == KeyCode.C) {
            clipIt();
        } else if (keyEvent.getCode() == KeyCode.SEMICOLON) {
            clipStart.setValue(mediaPlayer.getCurrentTime().toSeconds() / mediaPlayer.getTotalDuration().toSeconds() * 100);
        } else if (keyEvent.getCode() == KeyCode.QUOTE) {
            clipEnd.setValue(mediaPlayer.getCurrentTime().toSeconds() / mediaPlayer.getTotalDuration().toSeconds() * 100);
        }
    }

    public void clipIt(MouseEvent mouseEvent) throws IOException, InterruptedException {
        clipIt();
    }

    public void clipIt() throws IOException, InterruptedException {
        popOuts.removeIf(popOut -> !popOut.isAlive());
        for (PopOut popOut : popOuts) {
            if (popOut.getType() == PopOut.popOutType.ClippingView) {
                if (popOut.isAlive()) {
                    popOut.getWindow().requestFocus();
                    return;
                }
            }
        }
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("clipping-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        Stage stage = new Stage();
        stage.setTitle("ClippyFX Clipping");
        stage.setScene(scene);
        stage.show();
        double start = (clipStart.getValue() / 100) * mediaPlayer.getTotalDuration().toSeconds();
        double end = (clipEnd.getValue() / 100) * mediaPlayer.getTotalDuration().toSeconds();
        System.out.println("Start: " + start);
        System.out.println("End: " + end);
        this.pegGenerator.loadClipBounds(start, end);
        ClippingView clippingProgressWindow = fxmlLoader.getController();
        this.pegGenerator.passMetaData(this.fps, this.mediaPlayer.getTotalDuration().toSeconds());
        clippingProgressWindow.passObjects(mediaPlayer, this.pegGenerator);
        popOuts.add(clippingProgressWindow);
    }

    public void loadFileDirect(File file) throws IOException, InterruptedException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("compatablityator-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        Stage stage = new Stage();
        stage.setTitle("ClippyFX: Advanced video loader");
        stage.setScene(scene);
        stage.show();
        ImporterView compat = fxmlLoader.getController();
        popOuts.add(compat);
        compat.bypassFileChooser(file, new go());
    }

    public void loadFile(MouseEvent mouseEvent) throws IOException, InterruptedException {
        popOuts.removeIf(popOut -> !popOut.isAlive());
        for (PopOut popOut : popOuts) {
            if (popOut.getType() == PopOut.popOutType.ConverterView) {
                if (popOut.isAlive()) {
                    popOut.getWindow().requestFocus();
                    return;
                }
            }
        }
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("compatablityator-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        Stage stage = new Stage();
        stage.setTitle("ClippyFX: Advanced video loader");
        stage.setScene(scene);
        stage.show();
        ImporterView compat = fxmlLoader.getController();
        popOuts.add(compat);
        compat.passObjects(VideoURI, new go());
    }

    public void loadYoutube(MouseEvent mouseEvent) throws IOException {
        popOuts.removeIf(popOut -> !popOut.isAlive());
        for (PopOut popOut : popOuts) {
            if (popOut.getType() == PopOut.popOutType.YoutubeFinderView) {
                if (popOut.isAlive()) {
                    popOut.getWindow().requestFocus();
                    return;
                }
            }
        }
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("youtube-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 213);
        Stage stage = new Stage();
        stage.setTitle("ClippyFX: Youtube video loader");
        stage.setScene(scene);
        stage.show();
        YoutubeView youtubeSelectView = fxmlLoader.getController();
        popOuts.add(youtubeSelectView);
        youtubeSelectView.passObjects(VideoURI, new go());
    }

    public void onUncaughtException(Thread thread, Throwable throwable) {
        System.out.println("Uncaught exception: " + throwable.getClass().getName());
        System.out.println("Thread: " + thread.getName());
        StringBuilder cause = new StringBuilder();
        Throwable lastCause = throwable;
        while (lastCause.getCause() != null) {
            System.out.println("Caused by: " + throwable.getCause().getClass().getName());
            cause.append("Which caused: ").append(throwable.getCause().getClass().getName()).append("\n");
            lastCause = lastCause.getCause();
        }
        System.out.println("Stack trace: ");
        System.out.println("Message: " + lastCause.getMessage());
        throwable.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("\"" + thread.getName() + "\" has encountered an error");
        alert.setHeaderText("Type: " + lastCause.getClass().getName() + "\n" + cause);
        alert.setResizable(true);
        StringBuilder sb = new StringBuilder();
        int lines = 0;
        for (StackTraceElement element : throwable.getStackTrace()) {
            if (lines++ > 10) {
                sb.append("Plus ").append(throwable.getStackTrace().length - lines).append(" more lines\n");
                break;
            };
            sb.append(element.toString()).append("\n");
        }
        alert.setContentText(sb.toString());
        alert.showAndWait();
    }

    @FXML
    public void openSettings(MouseEvent mouseEvent) throws IOException {
        popOuts.removeIf(popOut -> !popOut.isAlive());
        for (PopOut popOut : popOuts) {
            if (popOut.getType() == PopOut.popOutType.SettingsView) {
                if (popOut.isAlive()) {
                    popOut.getWindow().requestFocus();
                    return;
                }
            }
        }
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("settings-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        Stage stage = new Stage();
        stage.setTitle("ClippyFX: Open settings");
        stage.setScene(scene);
        stage.show();
        SettingsView controller = fxmlLoader.getController();
        popOuts.add(controller);
        controller.build();
        controller.hookHooks();
    }

    class go implements Method {

        @Override
        public void execute(Object data) throws IOException {
            Pain.getScene().getWindow().requestFocus();
            loadMedia((PegGenerator) data);
        }

    }


}