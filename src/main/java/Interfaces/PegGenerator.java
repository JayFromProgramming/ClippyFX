package Interfaces;

import HelperMethods.PegArgument;
import HelperMethods.VideoChecks;
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

    ArrayList<String> getEncoders();

    ArrayList<String> getSizes();

    /**
     * Used if the type of video is a local file
     * @param uri The uri of the file
     */
    void setVideoFile(String uri);

    /**
     * Used if the type of video is a youtube video
     * @param youtubeData The data of the youtube video
     */
    void setVideoYT(JSONObject youtubeData);

    void passMetaData(double source_fps, double source_duration);

    void loadClipBounds(double start, double end, double speed, double volume);

    double getTotalFrames();

    String getPreferredSaveLocation();

    /**
     * @return A FFMPEG command to encode the video
     * @throws IOException Thrown if the FFMPEG command cannot be generated
     */
    String buildPeg(String savePath, PegArgument args) throws IOException;

}
