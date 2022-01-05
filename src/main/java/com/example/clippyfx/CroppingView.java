package com.example.clippyfx;

import Interfaces.PegGenerator;
import Interfaces.PopOut;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Window;

public class CroppingView implements PopOut {

    public Slider CropYStart;
    public Slider CropYEnd;
    public Slider CropXEnd;
    public Slider CropXStart;
    public AnchorPane videoFrame;
    public MediaView mediaView;

    private int videoDimensionX;
    private int videoDimensionY;
    private int frameDimensionX;
    private int frameDimensionY;


    public void passObjects(MediaPlayer mediaPlayer, PegGenerator pegGenerator) {
//        CropXEnd.setValue(pegGenerator.getXEnd());
//        CropYEnd.setValue(pegGenerator.getYEnd());
        mediaView = new MediaView(mediaPlayer);
        mediaView.fitWidthProperty().bind(videoFrame.widthProperty());
        mediaView.fitHeightProperty().bind(videoFrame.heightProperty());
        mediaView.setPreserveRatio(true);
        videoFrame.getChildren().add(mediaView);
        videoDimensionX = mediaPlayer.getMedia().getWidth();
        videoDimensionY = mediaPlayer.getMedia().getHeight();
        frameDimensionX = (int) videoFrame.getWidth();
        frameDimensionY = (int) videoFrame.getHeight();
    }

    @Override
    public Window getWindow() {
        return null;
    }

    @Override
    public popOutType getType() {
        return null;
    }

    @Override
    public boolean close() {
        return false;
    }

    @Override
    public boolean isAlive() {
        return false;
    }

    public void dragXEnd(MouseEvent mouseEvent) {

    }

    public void dragXStart(MouseEvent mouseEvent) {
    }
}
