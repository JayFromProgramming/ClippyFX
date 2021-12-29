package EncodingMagic;

import HelperMethods.EncoderCheck;
import Interfaces.PegGenerator;
import org.apache.commons.lang3.NotImplementedException;
import org.json.JSONObject;


public class FilePegGenerator implements PegGenerator {

    private String filePath;
    private double sourceFps;
    private int sourceTotalFrames;
    private double START_TIME;
    private double END_TIME;


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
    public void setVideoFile(String uri) {
        filePath = uri.substring(6); // remove "file://"
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

    @Override
    public String buildPeg(EncoderCheck.Encoders encoder, EncoderCheck.Sizes dimensions,
                           boolean allow100MB, double fps, String saveName){
        return switch (encoder) {
            case libx264 -> buildCPUX264Peg(dimensions, allow100MB, fps, START_TIME, END_TIME, filePath, saveName);
            case h264_nvenc -> buildNVENCX264Peg(dimensions, allow100MB, fps, START_TIME, END_TIME, filePath, saveName);
            case h264_amf -> buildAMFX264Peg(dimensions, allow100MB, fps, START_TIME, END_TIME, filePath, saveName);
            case h264_qsv -> throw new NotImplementedException("QSV is not currently supported");
            case libvpx_vp9 -> buildVP9Peg(dimensions, allow100MB, fps, START_TIME, END_TIME, filePath, saveName);
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

    private String buildCPUX264Peg(EncoderCheck.Sizes dimensions, boolean allow100MB, double fps, double startTime, double endTime,
                                String fileName, String saveName) {
        String baseCommand = "ffmpeg -ss %.2f -i \"%s\" -to %.2f -c:v libx264 -c:a libopus -b:v 1500k -b:a 128k -y \"%s.mp4\"";
        return String.format(baseCommand, startTime, fileName, endTime, saveName);
    }

    private String buildVP9Peg(EncoderCheck.Sizes dimensions, boolean allow100MB, double fps, double startTime, double endTime,
                               String fileName, String saveName) {
        String baseCommand = "ffmpeg -ss %.2f -i \"%s\" -to %.2f -c:v libvpx-vp9 -c:a libopus -b:v 1500k -b:a 128k -r %.3f %s -y \"%s.webm\"";
        return String.format(baseCommand, startTime, fileName, endTime, fps,
                EncoderCheck.sizeFormatter(dimensions), saveName);
    }
}
