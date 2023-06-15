package EncodingMagic;

import HelperMethods.PegArgument;
import HelperMethods.SettingsWrapper;
import HelperMethods.VideoChecks;
import Interfaces.PegGenerator;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;


public class URLPegGenerator implements PegGenerator {

    private String fileLink;
    private double CLIP_SPEED;
    private double CLIP_VOLUME;
    private double sourceFps;
    private int sourceTotalFrames;
    private double START_TIME;
    private double END_TIME;
    public int cropX1;
    public int cropX2;
    public int cropY1;
    public int cropY2;

    public URLPegGenerator(){}

    public URLPegGenerator(String url) throws URISyntaxException {
        this.fileLink = url;
    }

    @Override
    public void setFile(File file) {
        throw new UnsupportedOperationException("This method is not supported by the URLPegGenerator");
    }

    @Override
    public PegType getType() {
        return PegType.URI;
    }

    @Override
    public double getStartTime() {return START_TIME;}

    @Override
    public double getEndTime() {return END_TIME;}

    @Override
    public double getFPS() {return sourceFps;}

    @Override
    public String getLocation() {
        return fileLink;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getURI() {
        return null;
    }

    @Override
    public File getFile() {
        throw new UnsupportedOperationException("This method is not supported by the URIPegGenerator");
    }

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
        try {
            URI uri_object = new URI(uri);
            this.fileLink = String.valueOf(Paths.get(uri_object));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setVideoYT(JSONObject youtubeData) {
        throw new UnsupportedOperationException("This method is not supported by the FilePegGenerator");
    }

    @Override
    public void passMetaData(float source_fps, float source_duration) {
        this.sourceFps = source_fps;
        this.sourceTotalFrames = (int) (source_duration * sourceFps);
    }

    @Override
    public void loadClipBounds(double start, double end, double speed, double volume) {
        START_TIME = start;
        END_TIME = end;
        CLIP_SPEED = speed;
        CLIP_VOLUME = volume;
    }

    @Override
    public void setClipCrop(int x1, int x2, int y1, int y2) {
        cropX1 = x1;
        cropX2 = x2;
        cropY1 = y1;
        cropY2 = y2;
    }

    @Override
    public double getTotalFrames() {
        return sourceTotalFrames;
    }

    @Override
    public String getPreferredSaveLocation() {
        return SettingsWrapper.getSetting("defaultAdvancedSavePath").value;
    }

    @Override
    public String buildPeg(String savePath, PegArgument args) throws IOException {
        System.out.println("Building Peg");
        StringBuilder command = new StringBuilder();
        command.append(String.format("ffmpeg -y -ss %.2f -i \"%s\" -to %.2f ", START_TIME, fileLink,
                (END_TIME - START_TIME) * (1 / CLIP_SPEED)));
        command.append(switch (args.encoder) {
            case libx264 -> buildCPUX264Peg(args);
            case h264_nvenc -> buildNVENCX264Peg(args);
            case h264_amf -> buildAMFX264Peg(args);
            case h264_qsv -> throw new NotImplementedException("QSV is not currently supported (and never will be)");
            case libvpx_vp9 -> buildVP9Peg(args);
        });
        double bitrate = args.allow100MB ? 8e8 / (END_TIME - START_TIME) : 6.4e7 / (END_TIME - START_TIME);
        command.append(String.format(" -b:v %.2f -maxrate:v %.2f", bitrate / 1.392, bitrate));
        ArrayList<String> vf = new ArrayList<>();
        ArrayList<String> af = new ArrayList<>();
        if (args.dimensions != VideoChecks.Sizes.Source) vf.add(VideoChecks.sizeFormatter(args.dimensions));
        if (CLIP_SPEED != 1.0) {
            vf.add("setpts=" + 1 / CLIP_SPEED + "*PTS");
            af.add("atempo=" + CLIP_SPEED);
            args.fps *= CLIP_SPEED;
        }
        if (CLIP_VOLUME != 1.0) af.add("volume=" + CLIP_VOLUME);
        if (args.fps != this.sourceFps) command.append(String.format(" -r %.3f", args.fps));
        if (vf.size() != 0) command.append(" -vf \"").append(StringUtils.joinWith(",", vf.toArray())).append("\"");
        if (af.size() != 0) command.append(" -af \"").append(StringUtils.joinWith(",", af.toArray())).append("\"");
        String extension = switch (args.encoder){
            case libx264, h264_nvenc, h264_amf -> ".mp4";
            case h264_qsv -> throw new NotImplementedException("QSV is not currently supported (and never will be)");
            case libvpx_vp9 -> ".webm";
        };
        command.append(" \"").append(savePath).append(extension).append("\"");
        return command.toString();
    }

    private String buildAMFX264Peg(PegArgument args) {
        return "-c:v h264_amf -quality:v 0 -rc:v vbr_peak -c:a aac -b:a 128k";
    }

    private String buildNVENCX264Peg(PegArgument args) {
        int crf = switch (args.dimensions != VideoChecks.Sizes.Source ? args.dimensions : VideoChecks.getSize()) {
            case x2160p -> 12;
            case x1440p -> 21;
            case x1080p -> 27;
            case x720p  -> 29;
            case x480p  -> 31;
            case x360p  -> 33;
            case x240p  -> 34;
            case x144p  -> 35;
            default -> throw new IllegalStateException("Unexpected value: " + (args.dimensions == VideoChecks.Sizes.Source ? args.dimensions : VideoChecks.getSize()));
        };
        return String.format("-c:v h264_nvenc -preset:v p6 -rc-lookahead 4 -cq %s -c:a aac -b:a 128k", crf);
    }

    private String buildCPUX264Peg(PegArgument args) {
        int crf = switch (args.dimensions != VideoChecks.Sizes.Source ? args.dimensions : VideoChecks.getSize()) {
            case x2160p -> 12;
            case x1440p -> 21;
            case x1080p -> 27;
            case x720p  -> 29;
            case x480p  -> 31;
            case x360p  -> 33;
            case x240p  -> 34;
            case x144p  -> 35;
            default -> throw new IllegalStateException("Unexpected value: " + (args.dimensions == VideoChecks.Sizes.Source
                    ? args.dimensions : VideoChecks.getSize()));
        };
        return String.format("-c:v libx264 -crf:v %s -bufsize:v 64k -c:a aac -b:a 128k", crf);
    }

    private String buildVP9Peg(PegArgument args) {
        int crf = switch (args.dimensions != VideoChecks.Sizes.Source ? args.dimensions : VideoChecks.getSize()) {
            case x2160p -> 15;
            case x1440p -> 24;
            case x1080p -> 31;
            case x720p  -> 32;
            case x480p  -> 34;
            case x360p  -> 36;
            case x240p  -> 37;
            case x144p  -> 38;
            default -> throw new IllegalStateException("Unexpected value: " + (args.dimensions == VideoChecks.Sizes.Source ?
                    args.dimensions : VideoChecks.getSize()));
        };
        return String.format("-c:v libvpx-vp9 -crf:v %s -cpu-used 1 -row-mt 1 -c:a libopus -b:a 96k", crf);
    }
}
