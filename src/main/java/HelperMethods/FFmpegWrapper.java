package HelperMethods;

import javafx.scene.control.ProgressBar;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;

public class FFmpegWrapper {

    public static int currentFrame = 0;
    public static double totalFrames = 0;
    public static int clipFrames = 0;
    public static double fps = 0;

    public static void killProcess(Process process) throws IOException, InterruptedException {
        OutputStream ostream = process.getOutputStream();
        ostream.write("q\n".getBytes());
        ostream.flush();
        process.waitFor();
    }

    public static double getPlaybackPercent(int offset, int actualTotalFrames) {
        return (currentFrame + offset) / (double) actualTotalFrames;
    }

    public static String getFFMPEGProgress(String line, double duration, double fps, double totalFrames, ProgressBar progressBar) {
        FFmpegWrapper.totalFrames = totalFrames;
        FFmpegWrapper.clipFrames = (int) (duration * fps);
        FFmpegWrapper.fps = fps;
        if (line.contains("frame=")) {
            currentFrame = Integer.parseInt(StringUtils.substringBetween(line, "frame=", "fps=")
                    .replaceAll("\\s", ""));
            float currentfps = Float.parseFloat(StringUtils.substringBetween(line, "fps=", "q=").replaceAll("\\s", ""));
            String currentSize = StringUtils.substringBetween(line, "size=", "time=").replaceAll("\\s", "");
            String currentBitrate = StringUtils.substringBetween(line, "bitrate=", "speed=").replaceAll("\\s", "");
            float currentSpeed = Float.parseFloat(StringUtils.substringBefore(StringUtils.substringAfter(line, "speed=")
                    .replaceAll("\\s", ""), "x"));
            progressBar.setProgress((float) currentFrame / FFmpegWrapper.clipFrames);
            int eta_seconds = (int) ((duration - (currentFrame / fps)) / currentSpeed);
            int hours = eta_seconds / 3600;
            int minutes = (eta_seconds % 3600) / 60;
            int seconds = eta_seconds % 60;
            line = String.format("Frame %s of %s | %s, %sx speed, ETA: ~%s, Current Size %s", currentFrame,
                    FFmpegWrapper.clipFrames, currentBitrate, currentSpeed,
                    String.format("%02d:%02d:%02d", hours, minutes, seconds), currentSize);
            return line;
        }
        return "No progress available.";
    }



}
