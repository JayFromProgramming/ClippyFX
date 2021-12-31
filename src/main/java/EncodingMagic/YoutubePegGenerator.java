package EncodingMagic;

import HelperMethods.VideoChecks;
import Interfaces.PegGenerator;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class YoutubePegGenerator implements PegGenerator {


    @Override
    public PegType getType() {
        return PegType.Youtube;
    }

    @Override
    public double getStartTime() {
        return 0;
    }

    @Override
    public double getEndTime() {
        return 0;
    }

    @Override
    public double getFPS() {
        return 0;
    }

    @Override
    public ArrayList<String> getEncoders() {
        ArrayList<String> list = new ArrayList<>();
        list.add("Not implemented");
        return list;
    }

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
    public void setVideoYT(JSONObject youtubeData) {

    }

    @Override
    public void passMetaData(double source_fps, double source_duration) {

    }

    @Override
    public void loadClipBounds(double start, double end) {

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
