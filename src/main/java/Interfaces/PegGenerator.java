package Interfaces;

import HelperMethods.EncoderCheck;
import javafx.scene.media.MediaPlayer;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public interface PegGenerator {

    enum PegType {
        Youtube,
        File,
        URI,
        UnDeclared
    }

    PegType getType();

    double getStartTime();
    double getEndTime();
    double getFPS();

    /**
     * Used if the type of video is a local file
     * @param uri The uri of the file
     */
    void setVideo(String uri);

    /**
     * Used if the type of video is a youtube video
     * @param youtubeData The data of the youtube video
     */
    void setVideo(JSONObject youtubeData);

    void passMetaData(double source_fps, double source_duration);

    void loadClipBounds(double start, double end);

    double getTotalClipFrames();

    /**
     * @param encoder The type of encoding to use
     * @param dimensions The output dimensions of the video
     * @param allow100MB Used to allow files to be up to 100MB in size
     * @param fps The output fps of the video
     * @param saveName The name of the file to save the video as
     * @return A FFMPEG command to encode the video
     * @throws IOException Thrown if the FFMPEG command cannot be generated
     */
    String buildPeg(EncoderCheck.Encoders encoder, EncoderCheck.Sizes dimensions,
                    boolean allow100MB, double fps, String saveName) throws IOException;

}
