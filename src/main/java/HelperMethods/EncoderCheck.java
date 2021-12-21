package HelperMethods;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class EncoderCheck {

    public enum Encoders {
        libx264,
        h264_nvenc,
        h264_amf
    }

    public static Encoders[] getEncoders() {
        Encoders[] available = new Encoders[3];
        available[2] = Encoders.libx264;
        System.out.println("Detecting encoders...");
        try {
            System.out.println("Checking for h264_nvenc...");
            Process hwaccel = StreamedCommand.runCommand("ffmpeg -i src/main/resources/videoResources/got_this.webm -c:v h264_nvenc -frames 1 -f null NUL");
            int resultCode = StreamedCommand.waitForExit(hwaccel, 2);
            if (resultCode == 0) {
                available[0] = Encoders.h264_nvenc;
                System.out.println("Found h264_nvenc");
            }
            System.out.println("Checking for h264_amf...");
            hwaccel = StreamedCommand.runCommand("ffmpeg -i src/main/resources/videoResources/got_this.webm -c:v h264_amf -frames 1 -f null NUL");
            resultCode = StreamedCommand.waitForExit(hwaccel, 2);
            if (resultCode == 0) {
                available[1] = Encoders.h264_amf;
                System.out.println("Found h264_amf");
            }
        }catch (IOException | TimeoutException e){
            System.out.println("Failed to all encoders.\n Reason: " + e.getMessage());
        }
        return available;
    }
}
