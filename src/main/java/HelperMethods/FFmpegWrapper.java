package HelperMethods;

import javafx.scene.control.ProgressBar;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.OutputStream;

public class FFmpegWrapper {

    public static void killProcess(Process process) throws IOException, InterruptedException {
        OutputStream ostream = process.getOutputStream();
        ostream.write("q\n".getBytes());
        ostream.flush();
        process.waitFor();
    }

    public static String getFFMPEGProgress(String line, double duration, double fps, double totalFrames, ProgressBar progressBar) {
        if (line.contains("frame=")) {
            int currentFrame = Integer.parseInt(StringUtils.substringBetween(line, "frame=", "fps=")
                    .replaceAll("\\s", ""));
            float currentfps = Float.parseFloat(StringUtils.substringBetween(line, "fps=", "q=").replaceAll("\\s", ""));
            String currentSize = StringUtils.substringBetween(line, "size=", "time=").replaceAll("\\s", "");
            String currentBitrate = StringUtils.substringBetween(line, "bitrate=", "speed=").replaceAll("\\s", "");
            float currentSpeed = Float.parseFloat(StringUtils.substringBefore(StringUtils.substringAfter(line, "speed=")
                    .replaceAll("\\s", ""), "x"));
            progressBar.setProgress((float) currentFrame / totalFrames);
            int eta_seconds = (int) ((duration - (currentFrame / fps)) / currentSpeed);
            int hours = eta_seconds / 3600;
            int minutes = (eta_seconds % 3600) / 60;
            int seconds = eta_seconds % 60;
            line = String.format("Frame %s of %s | %s, %sx speed, ETA: ~%s", currentFrame, totalFrames,
                    currentBitrate, currentSpeed, String.format("%02d:%02d:%02d", hours, minutes, seconds));
            return line;
        }
        return "No progress available.";
    }



}
