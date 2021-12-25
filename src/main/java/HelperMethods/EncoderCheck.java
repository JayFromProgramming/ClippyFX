package HelperMethods;

import java.io.IOException;
import java.util.ArrayList;

public class EncoderCheck {

    public enum Encoder {
        libx264,
        h264_nvenc,
        h264_amf,
        h264_qsv
    }

    private static final ArrayList<Encoder> AVAILABLE_ENCODERS = new ArrayList<>(3);

    public static ArrayList<Encoder> getEncoders(){
        return AVAILABLE_ENCODERS;
    }

    public static ArrayList<String> getEncodersString(){
        ArrayList<String> encoders = new ArrayList<>(3);
        for (Encoder encoder : AVAILABLE_ENCODERS) {
            encoders.add(enumToString(encoder));
        }
        return encoders;
    }

    public static void checkEncoders() {
        AVAILABLE_ENCODERS.clear();
        AVAILABLE_ENCODERS.add(Encoder.libx264);
        System.out.println("Detecting encoders...");
        try {
            System.out.println("Checking for h264_nvenc...");
            Process hwaccel = StreamedCommand.runCommand("ffmpeg -i src/main/resources/videoResources/got_this.webm -c:v h264_nvenc -frames 1 -f null NUL");
            int resultCode = StreamedCommand.waitForExit(hwaccel, 1);
            if (resultCode == 0) {
                AVAILABLE_ENCODERS.add(Encoder.h264_nvenc);
                System.out.println("Found h264_nvenc");
            }
            System.out.println("Checking for h264_amf...");
            hwaccel = StreamedCommand.runCommand("ffmpeg -i src/main/resources/videoResources/got_this.webm -c:v h264_amf -frames 1 -f null NUL");
            resultCode = StreamedCommand.waitForExit(hwaccel, 1);
            if (resultCode == 0) {
                AVAILABLE_ENCODERS.add(Encoder.h264_amf);
                System.out.println("Found h264_amf");
            }
            System.out.println("Checking for h264_qsv...");
            hwaccel = StreamedCommand.runCommand("ffmpeg -i src/main/resources/videoResources/got_this.webm -c:v h264_qsv -frames 1 -f null NUL");
            resultCode = StreamedCommand.waitForExit(hwaccel, 1);
            if (resultCode == 0) {
                AVAILABLE_ENCODERS.add(Encoder.h264_qsv);
            }
        }catch (IOException e){
            System.out.println("Failed to all encoders.\n Reason: " + e.getMessage());
        }
    }

    public static String enumToString(Encoder encoder) {
        return switch (encoder) {
            case libx264 -> "libx264";
            case h264_nvenc -> "h264_nvenc";
            case h264_amf -> "h264_amf";
            case h264_qsv -> "h264_qsv";
        };
    }
}
