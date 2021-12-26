package HelperMethods;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class EncoderCheck {

    public enum Encoders {
        libx264,
        h264_nvenc,
        h264_amf,
        h264_qsv,
        libvpx_vp9,
    }

    public enum Sizes {
        Source,
        x2160p,
        x1440p,
        x1080p,
        x720p,
        x480p,
        x360p,
        x240p,
        x144p
    }

    private static final ArrayList<Encoders> AVAILABLE_ENCODERS = new ArrayList<>(Encoders.values().length);
    private static final ArrayList<Sizes> AVAILABLE_SIZES = new ArrayList<>(Sizes.values().length);

    public static ArrayList<Encoders> getEncoders(){
        return AVAILABLE_ENCODERS;
    }

    public static ArrayList<String> getEncodersString(){
        ArrayList<String> encoders = new ArrayList<>(3);
        for (Encoders encoder : AVAILABLE_ENCODERS) {
            encoders.add(encoder.toString());
        }
        return encoders;
    }

    public static ArrayList<String> getAllowedSizesString(){
        ArrayList<String> sizes = new ArrayList<>(9);
        for (Sizes size : Sizes.values()) {
            sizes.add(size.toString());
        }
        return sizes;
    }

//    public static ArrayList<Sizes> getAllowedSizes(File file) throws IOException {
//
//    }

    public static void checkEncoders() {
        AVAILABLE_ENCODERS.clear();
        AVAILABLE_ENCODERS.add(Encoders.libx264);
        AVAILABLE_ENCODERS.add(Encoders.libvpx_vp9);
        System.out.println("Detecting encoders...");
        try {
            System.out.println("Checking for h264_nvenc...");
            Process hwaccel = StreamedCommand.runCommand("ffmpeg -i src/main/resources/videoResources/got_this.webm -c:v h264_nvenc -frames 1 -f null NUL");
            int resultCode = StreamedCommand.waitForExit(hwaccel, 1);
            if (resultCode == 0) {
                AVAILABLE_ENCODERS.add(Encoders.h264_nvenc);
                System.out.println("Found h264_nvenc");
            }
            System.out.println("Checking for h264_amf...");
            hwaccel = StreamedCommand.runCommand("ffmpeg -i src/main/resources/videoResources/got_this.webm -c:v h264_amf -frames 1 -f null NUL");
            resultCode = StreamedCommand.waitForExit(hwaccel, 1);
            if (resultCode == 0) {
                AVAILABLE_ENCODERS.add(Encoders.h264_amf);
                System.out.println("Found h264_amf");
            }
            System.out.println("Checking for h264_qsv...");
            hwaccel = StreamedCommand.runCommand("ffmpeg -i src/main/resources/videoResources/got_this.webm -c:v h264_qsv -frames 1 -f null NUL");
            resultCode = StreamedCommand.waitForExit(hwaccel, 1);
            if (resultCode == 0) {
                AVAILABLE_ENCODERS.add(Encoders.h264_qsv);
            }
        }catch (IOException e){
            System.out.println("Failed to all encoders.\n Reason: " + e.getMessage());
        }
    }

}
