package EncodingMagic;

import HelperMethods.EncoderCheck;
import Interfaces.PegGenerator;
import javafx.scene.media.MediaPlayer;
import org.json.JSONObject;

import java.io.IOException;

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
    public void setVideo(String uri) {
        throw new UnsupportedOperationException("This method is not supported by the YoutubePegGenerator");
    }

    @Override
    public void setVideo(JSONObject youtubeData) {

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
    public String buildPeg(EncoderCheck.Encoders encoder, EncoderCheck.Sizes dimensions,
                           boolean allow100MB, double fps, String saveName) throws IOException {
        return null;
    }
}
