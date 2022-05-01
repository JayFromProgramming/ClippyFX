package HelperMethods;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class VideoChecks {

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
    public static final ArrayList<Sizes> AVAILABLE_SIZES = new ArrayList<>(Sizes.values().length);
    private static int HEIGHT = 0;

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
        for (Sizes size : AVAILABLE_SIZES) {
            if (size != Sizes.Source) {
                sizes.add(size.toString().substring(1));
            }else sizes.add(size.toString());
        }
        return sizes;
    }

    public static ArrayList<String> getSizesString(){
        ArrayList<String> sizes = new ArrayList<>(9);
        for (Sizes size : Sizes.values()) {
            sizes.add(size.toString());
        }
        return sizes;
    }

    public static ArrayList<Sizes> getAllowedSizes(){
        return AVAILABLE_SIZES;
    };

    public static Sizes getSize(){
        if (AVAILABLE_SIZES.size() <= 1) return Sizes.x144p;
        return AVAILABLE_SIZES.get(1);
    }

    public static String sizeFormatter(Sizes size){
        return switch (size) {
            case Source -> "";
            case x2160p -> "scale=-1:2160";
            case x1440p -> "scale=-1:1440";
            case x1080p -> "scale=-1:1080";
            case x720p ->  "scale=-1:720";
            case x480p ->  "scale=-1:480";
            case x360p ->  "scale=-1:360";
            case x240p ->  "scale=-1:240";
            case x144p ->  "scale=-1:144";
        };
    }

    public static void checkAllowedSizes(File file) throws IOException {
        System.out.println("Detecting allowed sizes...");
        checkAllowedSizes(file.getAbsolutePath());
    }

    public static void checkAllowedSizes(String path) throws IOException {
        String hSize = StreamedCommand.getCommandOutput("ffprobe -v error -select_streams v:0 -show_entries stream=height -of csv=s=x:p=0 -i \"" + path + "\"");
        calcAllowedSizes(hSize);
    }

    public static void calcAllowedSizes(String hSize) {
        HEIGHT = Integer.parseInt(hSize);
        AVAILABLE_SIZES.clear();
        AVAILABLE_SIZES.add(Sizes.Source);
        if (hSize.equals("")) {
            System.out.println("Failed to detect allowed sizes.\n Reason: Failed to get video height.");
        } else {
            int height = Integer.parseInt(hSize);
            if (height >= 2160) AVAILABLE_SIZES.add(Sizes.x2160p);
            if (height >= 1440) AVAILABLE_SIZES.add(Sizes.x1440p);
            if (height >= 1080) AVAILABLE_SIZES.add(Sizes.x1080p);
            if (height >= 720)  AVAILABLE_SIZES.add(Sizes.x720p);
            if (height >= 480)  AVAILABLE_SIZES.add(Sizes.x480p);
            if (height >= 360)  AVAILABLE_SIZES.add(Sizes.x360p);
            if (height >= 240)  AVAILABLE_SIZES.add(Sizes.x240p);
            if (height >= 144)  AVAILABLE_SIZES.add(Sizes.x144p);
        }
    }

    private static String printEncoderError(String result) {
        String[] errorSplit = result.split("Error");
        if (errorSplit.length > 1) {
            return " Error" + errorSplit[1];
        }else {
            return " " + result;
        }
    }

    private static boolean actualError(String result) {
        return result.contains("Error") || result.contains("error") || result.contains("unsupported");
    }

    public static void checkEncoders() {
        AVAILABLE_ENCODERS.clear();
        AVAILABLE_ENCODERS.add(Encoders.libx264);
        AVAILABLE_ENCODERS.add(Encoders.libvpx_vp9);
        System.out.println("Detecting encoders...");
        System.out.println("Expecting libx264");
        System.out.println("Expecting libvpx_vp9");
        try {
            Process hwaccel = StreamedCommand.runCommand("ffmpeg -i resources/videoResources/enCheck.webm -c:v h264_nvenc -frames 1 -f null NUL");
            String result = StreamedCommand.returnErrorString(hwaccel, 2);
            if (result.equals("")) {
                AVAILABLE_ENCODERS.add(Encoders.h264_nvenc);
                System.out.println("Found h264_nvenc");
            }else if(!actualError(result)){
                System.out.println("Unable to determine if h264_nvenc is available.\nAssuming h264_nvenc is available: "
                        + printEncoderError(result));
                AVAILABLE_ENCODERS.add(Encoders.h264_nvenc);
            } else {
                System.out.println("Failed to find h264_nvenc:" + printEncoderError(result));
            }
            hwaccel = StreamedCommand.runCommand("ffmpeg -i resources/videoResources/enCheck.webm -c:v h264_amf -frames 1 -f null NUL");
            result = StreamedCommand.returnErrorString(hwaccel, 2);
            if (result.equals("")) {
                AVAILABLE_ENCODERS.add(Encoders.h264_amf);
                System.out.println("Found h264_amf");
            } else if(!actualError(result)){
                System.out.println("Unable to determine if h264_amf is available.\nAssuming h264_amf is available: "
                        + printEncoderError(result));
                AVAILABLE_ENCODERS.add(Encoders.h264_amf);
            } else {
                System.out.println("Failed to find h264_amf:" + printEncoderError(result));
            }
            hwaccel = StreamedCommand.runCommand("ffmpeg -i resources/videoResources/enCheck.webm -c:v h264_qsv -frames 1 -f null NUL");
            result = StreamedCommand.returnErrorString(hwaccel, 2);
            if (result.equals("")) {
                AVAILABLE_ENCODERS.add(Encoders.h264_qsv);
                System.out.println("Found h264_qsv");
            } else if(!actualError(result)){
                System.out.println("Unable to determine if h264_qsv is available.\nAssuming h264_qsv is available\n"
                        + printEncoderError(result));
                AVAILABLE_ENCODERS.add(Encoders.h264_qsv);
            } else {
                System.out.println("Failed to find h264_qsv:" + printEncoderError(result));
            }
        }catch (IOException e){
            System.out.println("Failed to all encoders.\n Reason: " + e.getMessage());
        }
    }

}
