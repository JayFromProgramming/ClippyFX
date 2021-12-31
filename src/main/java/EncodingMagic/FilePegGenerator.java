package EncodingMagic;

import HelperMethods.VideoChecks;
import Interfaces.PegGenerator;
import org.apache.commons.lang3.NotImplementedException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;


public class FilePegGenerator implements PegGenerator {

    private String filePath;
    private String tempPath;
    private double sourceFps;
    private int sourceTotalFrames;
    private double START_TIME;
    private double END_TIME;

    public FilePegGenerator(){}

    public FilePegGenerator(String uri){
        if (uri.contains("file:/")) {
            this.filePath = uri.replace("file:/", "").replace("/", "\\");
        }else this.filePath = uri;
    }

    @Override
    public PegType getType() {
        return PegType.File;
    }

    @Override
    public double getStartTime() {return START_TIME;}

    @Override
    public double getEndTime() {return END_TIME;}

    @Override
    public double getFPS() {return sourceFps;}

    @Override
    public ArrayList<String> getEncoders() {
        return VideoChecks.getEncodersString();
    }

    @Override
    public ArrayList<String> getSizes() {
        return VideoChecks.getAllowedSizesString();
    }

    @Override
    public void setVideoFile(String uri) {
        if (uri.contains("file:/")) {
            this.filePath = uri.replace("file:/", "").replace("/", "\\");
        }else this.filePath = uri;
    }

    public void setTempFile(String path) {
        tempPath = path;
    }

    @Override
    public void setVideoYT(JSONObject youtubeData) {
        throw new UnsupportedOperationException("This method is not supported by the FilePegGenerator");
    }

    @Override
    public void passMetaData(double source_fps, double source_duration) {
        this.sourceFps = source_fps;
        this.sourceTotalFrames = (int) (source_duration * sourceFps);
    }

    @Override
    public void loadClipBounds(double start, double end) {
        START_TIME = start;
        END_TIME = end;
    }

    @Override
    public double getTotalClipFrames() {
        return sourceTotalFrames;
    }

    private boolean checkDuplicates(String savePath){
        if (Objects.equals(filePath, savePath)){
            if (tempPath == null) throw new IllegalStateException("Both input path and output path are the same, " +
                    "no available temp file to fall back to");
            return true;
        }
        return false;
    }

    @Override
    public String buildPeg(VideoChecks.Encoders encoder, VideoChecks.Sizes dimensions,
                           boolean allow100MB, double fps, String savePath){
        System.out.println("Building Peg");
        System.out.println("File Path: " + filePath + "\nTemp Path: " + tempPath
                + "\nSave Path: " + savePath + "\nEncoder: " + encoder.toString() +
                "\nDimensions: " + dimensions.toString() + "\nAllow 100MB: " + allow100MB + "\nFPS: " + fps);

        return switch (encoder) {
            case libx264 -> buildCPUX264Peg(dimensions, allow100MB, fps, START_TIME, END_TIME, savePath);
            case h264_nvenc -> buildNVENCX264Peg(dimensions, allow100MB, fps, START_TIME, END_TIME, savePath);
            case h264_amf -> buildAMFX264Peg(dimensions, allow100MB, fps, START_TIME, END_TIME, savePath);
            case h264_qsv -> throw new NotImplementedException("QSV is not currently supported");
            case libvpx_vp9 -> buildVP9Peg(dimensions, allow100MB, fps, START_TIME, END_TIME, savePath);
        };
    }

    private String buildAMFX264Peg(EncoderCheck.Sizes size, boolean allow100MB, double fps, double startTime, double endTime,
                                   String fileName, String saveName) {
        String baseCommand = "ffmpeg -ss %.2f -i \"%s\" -to %.2f -c:v h264_amf -c:a aac -preset:v p2 -cq:v 23 -b:a 128k -y \"%s.mp4\"";
        return String.format(baseCommand, startTime, fileName, endTime, saveName);
    }

    private String buildNVENCX264Peg(EncoderCheck.Sizes size, boolean allow100MB, double fps, double startTime, double endTime,
                                     String fileName, String saveName) {
        String baseCommand = "ffmpeg -ss %.2f -i \"%s\" -to %.2f -c:v h264_nvenc -c:a libopus -b:v 1500k -b:a 128k -y \"%s.mp4\"";
        return String.format(baseCommand, startTime, fileName, endTime, saveName);
    }

    private String buildCPUX264Peg(VideoChecks.Sizes dimensions, boolean allow100MB, double fps, double startTime, double endTime, String saveName) {
        if (checkDuplicates(saveName + ".mp4")) filePath = tempPath;
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
        String baseCommand = "ffmpeg -ss %.2f -i \"%s\" -to %.2f -c:v libx264 -c:a libopus -crf:v %s -b:v %.3f -maxrate:v %.3f -b:a 96k -r %.3f %s -y \"%s.mp4\"";
        return String.format(baseCommand, startTime, filePath, endTime - startTime, crf, bitrate / 1.4, bitrate, fps,
                VideoChecks.sizeFormatter(dimensions), saveName);
    }

    private String buildVP9Peg(EncoderCheck.Sizes dimensions, boolean allow100MB, double fps, double startTime, double endTime,
                               String fileName, String saveName) {
        String baseCommand = "ffmpeg -ss %.2f -i \"%s\" -to %.2f -c:v libvpx-vp9 -c:a libopus -b:v 1500k -b:a 128k -r %.3f %s -y \"%s.webm\"";
        return String.format(baseCommand, startTime, fileName, endTime, fps,
                EncoderCheck.sizeFormatter(dimensions), saveName);
    }
}
