package HelperMethods;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

public class EncoderCheck {

    public enum Encoders {
        libx264,
        h264_nvenc,
        h264_amf,
        h264_qsv
    }

    public static ArrayList<Encoders> getEncoders() {
        ArrayList<Encoders> available = new ArrayList<>(3);
        available.add(Encoders.libx264);
        System.out.println("Detecting encoders...");
        try {
            System.out.println("Checking for h264_nvenc...");
            Process hwaccel = StreamedCommand.runCommand("ffmpeg -i src/main/resources/videoResources/got_this.webm -c:v h264_nvenc -frames 1 -f null NUL");
            int resultCode = StreamedCommand.waitForExit(hwaccel, 1);
            if (resultCode == 0) {
                available.add(Encoders.h264_nvenc);
                System.out.println("Found h264_nvenc");
            }
            System.out.println("Checking for h264_amf...");
            hwaccel = StreamedCommand.runCommand("ffmpeg -i src/main/resources/videoResources/got_this.webm -c:v h264_amf -frames 1 -f null NUL");
            resultCode = StreamedCommand.waitForExit(hwaccel, 1);
            if (resultCode == 0) {
                available.add(Encoders.h264_amf);
                System.out.println("Found h264_amf");
            }
            System.out.println("Checking for h264_qsv...");
            hwaccel = StreamedCommand.runCommand("ffmpeg -i src/main/resources/videoResources/got_this.webm -c:v h264_qsv -frames 1 -f null NUL");
            resultCode = StreamedCommand.waitForExit(hwaccel, 1);
            if (resultCode == 0) {
                available.add(Encoders.h264_qsv);
            }
        }catch (IOException | TimeoutException e){
            System.out.println("Failed to all encoders.\n Reason: " + e.getMessage());
        }
        return available;
    }

    public static String enumToString(Encoders encoder) {
        return switch (encoder) {
            case libx264 -> "libx264";
            case h264_nvenc -> "h264_nvenc";
            case h264_amf -> "h264_amf";
            case h264_qsv -> "h264_qsv";
        };
    }
}
