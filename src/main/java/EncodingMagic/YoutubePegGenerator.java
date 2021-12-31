package EncodingMagic;

import HelperMethods.VideoChecks;
import Interfaces.PegGenerator;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class YoutubePegGenerator implements PegGenerator {


    private final JSONObject youtubeData;
    private double START_TIME;
    private double END_TIME;
    private double sourceFrameRate;
    private int sourceHeight;
    private int sourceDuration;


    public YoutubePegGenerator(JSONObject youtubeData) {
        this.youtubeData = youtubeData;
    }


    @Override
    public PegType getType() {
        return PegType.Youtube;
    }

    @Override
    public double getStartTime() {
        return START_TIME;
    }

    @Override
    public double getEndTime() {
        return END_TIME;
    }

    @Override
    public double getFPS() {
        return sourceFrameRate;
    }

    @Override
    public ArrayList<String> getEncoders() {return VideoChecks.getEncodersString();}

    @Override
    public ArrayList<String> getSizes() {
        ArrayList<String> list = new ArrayList<>();
        list.add("Not implemented");
        return list;
    }

    @Override
    public void setVideoFile(String uri) {
        throw new UnsupportedOperationException("This method is not supported by the YoutubePegGenerator");
    }

    @Override
    @Deprecated
    public void setVideoYT(JSONObject youtubeData) {
        // lmao I'm lazy as fuck
    }

    @Override
    public void passMetaData(double source_fps, double source_duration) {
       this.sourceDuration = this.youtubeData.getInt("duration");
       this.sourceFrameRate = this.youtubeData.getInt("fps");
    }

    @Override
    public void loadClipBounds(double start, double end) {
        this.START_TIME = start;
        this.END_TIME = end;
    }

    @Override
    public double getTotalClipFrames() {
        return 0;
    }

    @Override
    public String buildPeg(VideoChecks.Encoders encoder, VideoChecks.Sizes dimensions,
                           boolean allow100MB, double fps, String saveName) throws IOException {
        return null;
    }
}
