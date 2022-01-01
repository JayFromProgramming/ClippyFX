package EncodingMagic;

import HelperMethods.PegArgument;
import HelperMethods.SettingsWrapper;
import HelperMethods.VideoChecks;
import Interfaces.PegGenerator;
import org.apache.commons.lang3.NotImplementedException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class YoutubePegGenerator implements PegGenerator {


    private final JSONObject youtubeData;
    private double START_TIME;
    private double END_TIME;
    private double CLIP_SPEED;
    private double CLIP_VOLUME;
    private double sourceFrameRate;
    private int sourceHeight;
    private int sourceDuration;
    private double sourceFps;


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
        JSONArray formats = this.youtubeData.getJSONArray("formats");
        int height = 0;
        for (int i = formats.length() - 1; i >= 0; i--){
            if (!formats.getJSONObject(i).getString("vcodec").equals("none") &&
                    formats.getJSONObject(i).getString("acodec").equals("none")){
                height = formats.getJSONObject(i).getInt("height");
                break;
            }
        }
        this.sourceHeight = height;
        System.out.println("Highest quality: " + this.sourceHeight);
        VideoChecks.checkAllowedSizes(String.valueOf(height));
        return VideoChecks.getAllowedSizesString();
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
    public void loadClipBounds(double start, double end, double speed, double volume) {
        this.START_TIME = start;
        this.END_TIME = end;
        this.CLIP_SPEED = speed;
        this.CLIP_VOLUME = volume;
    }

    @Override
    public double getTotalFrames() {
        return sourceFrameRate * sourceDuration;
    }

    @Override
    public String getPreferredSaveLocation() {
        return SettingsWrapper.getSetting("defaultYoutubeSavePath").value;
    }

    @Override
    public String buildPeg(String saveName, PegArgument args) throws IOException {
        VideoChecks.Encoders encoder = args.encoder;
        VideoChecks.Sizes dimensions = args.dimensions;
        double fps = args.fps;
        boolean allow100MB = args.allow100MB;
        JSONArray formats = this.youtubeData.getJSONArray("formats");
        String videoURI = "";
        String audioURI = "";
        for (int i = formats.length() - 1; i >= 0; i--){
            if (!formats.getJSONObject(i).getString("vcodec").equals("none") &&
                    formats.getJSONObject(i).getString("acodec").equals("none")){
                videoURI = formats.getJSONObject(i).getString("url");
                break;
            }
        }
        for (int i = formats.length() - 1; i >= 0; i--){
            if (formats.getJSONObject(i).getString("vcodec").equals("none") &&
                    !formats.getJSONObject(i).getString("acodec").equals("none")){
                audioURI = formats.getJSONObject(i).getString("url");
                break;
            }
        }
        return switch (encoder) {
            case libx264 -> buildCPUX264Peg(videoURI, audioURI, dimensions, allow100MB, fps, this.START_TIME, this.END_TIME, saveName);
            case h264_nvenc -> buildNVENCX264Peg(videoURI, audioURI, dimensions, allow100MB, fps, this.START_TIME, this.END_TIME, saveName);
            case h264_amf -> buildAMFX264Peg(videoURI, audioURI, dimensions, allow100MB, fps, this.START_TIME, this.END_TIME, saveName);
            case h264_qsv -> throw new NotImplementedException("QSV is not currently supported");
            case libvpx_vp9 -> buildVP9Peg(videoURI, audioURI, dimensions, allow100MB, fps, this.START_TIME, this.END_TIME, saveName);
        };
    }

    private String buildAMFX264Peg(String videoURI, String audioURI, VideoChecks.Sizes size, boolean allow100MB, double fps, double startTime, double endTime, String saveName) {
        float bitrate = (float) (allow100MB ? 8e8 / (endTime - startTime) : 6.4e7 / (endTime - startTime));
        String baseCommand = "ffmpeg -ss %.2f -i \"%s\" -ss %.2f -i \"%s\" -map 0:0 -map 1:0 -to %.2f -c:v h264_amf -c:a aac -quality:v 0 -rc:v vbr_peak -b:v %.3f -maxrate:v %.3f -b:a 128k -y \"%s.mp4\"";
        return String.format(baseCommand, startTime, videoURI, startTime, audioURI, endTime - startTime, bitrate / 1.4,
                bitrate, saveName);
    }

    private String buildNVENCX264Peg(String videoURI, String audioURI, VideoChecks.Sizes size, boolean allow100MB, double fps, double startTime, double endTime, String saveName) {
        int crf = switch (size != VideoChecks.Sizes.Source ? size : VideoChecks.getSize()) {
            case x2160p -> 12;
            case x1440p -> 21;
            case x1080p -> 27;
            case x720p  -> 29;
            case x480p  -> 31;
            case x360p  -> 33;
            case x240p  -> 34;
            case x144p  -> 35;
            default -> throw new IllegalStateException("Unexpected value: " + (size == VideoChecks.Sizes.Source ? size : VideoChecks.getSize()));
        };
        float bitrate = (float) (allow100MB ? 8e8 / (endTime - startTime) : 6.4e7 / (endTime - startTime));
        String baseCommand = "ffmpeg -ss %.2f -i \"%s\" -ss %.2f -i \"%s\" -map 0:0 -map 1:0 -to %.2f -c:v h264_nvenc -c:a aac %s -preset:v p6 -rc-lookahead 4 -cq %s -b:v %.3f -maxrate:v %.3f -r %.3f -b:a 128k -y \"%s.mp4\"";
        return String.format(baseCommand, startTime, videoURI, startTime, audioURI, endTime - startTime, VideoChecks.sizeFormatter(size), crf, bitrate / 1.4, bitrate, fps, saveName);
    }

    private String buildCPUX264Peg(String videoURI, String audioURI, VideoChecks.Sizes dimensions, boolean allow100MB, double fps, double startTime, double endTime, String saveName) {
        float bitrate = (float) (allow100MB ? 8e8 / (endTime - startTime) : 6.4e7 / (endTime - startTime));
        int crf = switch (dimensions != VideoChecks.Sizes.Source ? dimensions : VideoChecks.getSize()) {
            case x2160p -> 15;
            case x1440p -> 24;
            case x1080p -> 31;
            case x720p  -> 32;
            case x480p  -> 34;
            case x360p  -> 36;
            case x240p  -> 37;
            case x144p  -> 38;
            default -> throw new IllegalStateException("Unexpected value: " + (dimensions == VideoChecks.Sizes.Source ? dimensions : VideoChecks.getSize()));
        };
        String baseCommand = "ffmpeg -ss %.2f -i \"%s\" -ss %.2f -i \"%s\" -map 0:0 -map 1:0 -to %.2f -c:v libx264 -c:a libopus -crf:v %s -b:v %.3f -maxrate:v %.3f -b:a 96k -r %.3f %s -y \"%s.mp4\"";
        return String.format(baseCommand, startTime, videoURI, startTime, audioURI, endTime - startTime, crf, bitrate / 1.4, bitrate, fps,
                VideoChecks.sizeFormatter(dimensions), saveName);
    }

    private String buildVP9Peg(String videoURI, String audioURI, VideoChecks.Sizes dimensions, boolean allow100MB, double fps, double startTime,
                               double endTime, String saveName) {
        int crf = switch (dimensions != VideoChecks.Sizes.Source ? dimensions : VideoChecks.getSize()) {
            case x2160p -> 15;
            case x1440p -> 24;
            case x1080p -> 31;
            case x720p  -> 32;
            case x480p  -> 34;
            case x360p  -> 36;
            case x240p  -> 37;
            case x144p  -> 38;
            default -> throw new IllegalStateException("Unexpected value: " + (dimensions == VideoChecks.Sizes.Source ? dimensions : VideoChecks.getSize()));
        };
        float bitrate = (float) (allow100MB ? 8e8 / (endTime - startTime) : 6.4e7 / (endTime - startTime));
        String baseCommand = "ffmpeg -ss %.2f -i \"%s\" -ss %.2f -i \"%s\" -map 0:0 -map 1:0 -to %.2f -c:v libvpx-vp9 -c:a libopus -crf:v %s -b:v %.3f -maxrate:v %.3f -b:a 96k -r %.3f %s -y \"%s.webm\"";
        return String.format(baseCommand, startTime, videoURI, startTime, audioURI, endTime - startTime, crf, bitrate / 1.4, bitrate, fps,
                VideoChecks.sizeFormatter(dimensions), saveName);
    }
}
